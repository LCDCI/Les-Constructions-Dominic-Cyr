# Form Subdomain Documentation

## Overview

The Form Subdomain is a comprehensive system for managing customer-related forms in the construction project management application. It allows salespersons/owners to create and assign various types of forms to customers, track submissions, reopen forms for revisions, and maintain a complete history of all submissions.

## Features

### Core Functionality

1. **Form Creation & Assignment**
   - Salespersons/owners can create forms and assign them to customers
   - Each form is associated with a specific project and customer
   - Prevents duplicate form types per customer per project
   - Automatic email and system notifications sent to customers

2. **Form Types**
   - **Exterior Doors (EXTERIOR_DOORS)**: Customer creates design on external website, downloads PDF, uploads to portal
   - **Garage Doors (GARAGE_DOORS)**: Customer creates design on external website, saves PDF, uploads to portal
   - **Windows (WINDOWS)**: Interactive form for selecting window colors (facade, sides, back - interior/exterior)
   - **Asphalt Shingles (ASPHALT_SHINGLES)**: Selection form for roofing company, collection, color, steel color
   - **Woodwork (WOODWORK)**: Selection form for interior door models, handle models/finishes, baseboard models with height
   - **Paint (PAINT)**: Form for specifying paint details for ceilings, doors, woodwork, walls, and accent walls

3. **Form Statuses**
   - **DRAFT**: Form created but not yet assigned
   - **ASSIGNED**: Form assigned to customer
   - **IN_PROGRESS**: Customer is actively working on the form
   - **SUBMITTED**: Customer has submitted the form
   - **REOPENED**: Form reopened by salesperson/owner for revision
   - **COMPLETED**: Form has been approved and completed

4. **Submission History**
   - Every form submission creates a historical snapshot
   - Tracks submission number, timestamp, and form data state
   - Customer can add notes with each submission
   - History is preserved even when forms are reopened

5. **Reopen Functionality**
   - Salespersons/owners can reopen submitted forms
   - Customer receives email and system notification
   - Tracks reopen count and reason
   - Previous submissions preserved in history
   - Optional new instructions can be added

6. **Notifications**
   - Email notifications sent via MailerServiceClient
   - System notifications created via NotificationService
   - Triggers:
     - Form assigned to customer
     - Form submitted by customer (to salesperson/owner)
     - Form reopened (to customer)

## Architecture

### Package Structure

```
FormSubdomain/
├── DataAccessLayer/
│   ├── Form.java                          # Main form entity
│   ├── FormIdentifier.java                # Embedded identifier
│   ├── FormSubmissionHistory.java         # Submission history entity
│   ├── FormType.java                      # Enum for form types
│   ├── FormStatus.java                    # Enum for form statuses
│   ├── FormRepository.java                # Form data repository
│   └── FormSubmissionHistoryRepository.java # History repository
├── BusinessLayer/
│   ├── FormService.java                   # Service interface
│   └── FormServiceImpl.java               # Service implementation
├── PresentationLayer/
│   ├── FormController.java                # REST controller
│   ├── FormRequestModel.java              # Create/update request DTO
│   ├── FormResponseModel.java             # Response DTO
│   ├── FormDataUpdateRequestModel.java    # Customer form data update
│   ├── FormReopenRequestModel.java        # Reopen request DTO
│   └── FormSubmissionHistoryResponseModel.java  # History DTO
└── MapperLayer/
    ├── FormMapper.java                    # Entity<->DTO mapper
    └── FormSubmissionHistoryMapper.java   # History mapper
```

### Database Schema

#### forms table
- Stores main form data including status, customer info, form data (JSONB)
- Tracks assignment, submission, completion, and reopen dates
- References projects and users tables
- Indexes on identifier, project, customer, status, type

#### form_submission_history table
- Stores historical snapshots of each submission
- Includes submission number, status at time of submission, notes
- JSONB snapshot of form data at time of submission
- Indexed by form identifier and submission date

## API Endpoints

### Form Management Endpoints

#### Create Form (POST /api/v1/forms)
**Authorization**: ROLE_OWNER, ROLE_SALESPERSON

Creates and assigns a new form to a customer.

**Request Body**:
```json
{
  "formType": "WINDOWS",
  "projectIdentifier": "project-123",
  "customerId": "customer-uuid",
  "formTitle": "Window Color Selection",
  "instructions": "Please select colors for all windows",
  "formData": {}
}
```

**Response**: FormResponseModel (201 Created)

#### Get Form (GET /api/v1/forms/{formId})
**Authorization**: Owner, Salesperson, or Customer who owns the form

Retrieves a specific form by ID.

**Response**: FormResponseModel (200 OK)

#### Get All Forms (GET /api/v1/forms)
**Query Parameters**:
- `projectId`: Filter by project
- `customerId`: Filter by customer
- `status`: Filter by form status
- `formType`: Filter by form type

**Authorization**: 
- Owners/Salespersons: See all forms (with filters)
- Customers: Only see their own forms

**Response**: List of FormResponseModel (200 OK)

#### Get Forms by Project (GET /api/v1/forms/project/{projectId})
**Authorization**: ROLE_OWNER, ROLE_SALESPERSON

**Response**: List of FormResponseModel (200 OK)

#### Get Forms by Customer (GET /api/v1/forms/customer/{customerId})
**Authorization**: Customers can only view their own, owners/salespersons can view all

**Response**: List of FormResponseModel (200 OK)

#### Update Form Data (PUT /api/v1/forms/{formId}/data)
**Authorization**: Customer who owns the form, or owner/salesperson

Used by customers to save progress while filling out forms.

**Request Body**:
```json
{
  "formData": {
    "exteriorColorFacade": "White",
    "interiorColorFacade": "Oak"
  },
  "submissionNotes": "Optional notes",
  "isSubmitting": false
}
```

**Response**: FormResponseModel (200 OK)

#### Submit Form (POST /api/v1/forms/{formId}/submit)
**Authorization**: Customer who owns the form

Marks form as submitted and creates submission history entry.

**Request Body**:
```json
{
  "formData": {
    "exteriorColorFacade": "White",
    "interiorColorFacade": "Oak",
    "exteriorColorSides": "Beige",
    "interiorColorSides": "Walnut"
  },
  "submissionNotes": "All colors selected per architect's recommendations",
  "isSubmitting": true
}
```

**Response**: FormResponseModel (200 OK)

#### Reopen Form (POST /api/v1/forms/{formId}/reopen)
**Authorization**: ROLE_OWNER, ROLE_SALESPERSON

Reopens a submitted form for customer to revise.

**Request Body**:
```json
{
  "reopenReason": "Need to adjust northern window colors",
  "newInstructions": "Please review and update colors for north-facing windows"
}
```

**Response**: FormResponseModel (200 OK)

#### Complete Form (POST /api/v1/forms/{formId}/complete)
**Authorization**: ROLE_OWNER, ROLE_SALESPERSON

Marks form as completed (final approval).

**Response**: FormResponseModel (200 OK)

#### Update Form Details (PUT /api/v1/forms/{formId})
**Authorization**: ROLE_OWNER, ROLE_SALESPERSON

Updates form title and instructions (not customer data).

**Request Body**:
```json
{
  "formTitle": "Updated Window Selection",
  "instructions": "Updated instructions..."
}
```

**Response**: FormResponseModel (200 OK)

#### Delete Form (DELETE /api/v1/forms/{formId})
**Authorization**: ROLE_OWNER, ROLE_SALESPERSON

Deletes form and all its submission history.

**Response**: 204 No Content

#### Get Submission History (GET /api/v1/forms/{formId}/history)
**Authorization**: Owner, Salesperson, or Customer who owns the form

Retrieves all submission history for a form.

**Response**: List of FormSubmissionHistoryResponseModel (200 OK)

## Form Data Structure Examples

### Windows Form Data
```json
{
  "exteriorColorFacade": "White",
  "interiorColorFacade": "Oak",
  "exteriorColorSides": "Beige",
  "interiorColorSides": "Walnut",
  "otherDetails": "Custom trim color requested"
}
```

### Asphalt Shingles Form Data
```json
{
  "company": "IKO",
  "collection": "Cambridge",
  "color": "Weathered Wood",
  "hasSteelRoof": true,
  "steelColor": "Charcoal Grey"
}
```

### Paint Form Data
```json
{
  "paintCompany": "Benjamin Moore",
  "ceilingColor": {
    "number": "OC-45",
    "name": "Swiss Coffee"
  },
  "doorColor": {
    "number": "2127-40",
    "name": "Midnight Navy"
  },
  "woodworkColor": {
    "number": "OC-17",
    "name": "White Dove"
  },
  "generalWallColor": {
    "number": "AC-28",
    "name": "Gray Owl"
  },
  "accentWalls": [
    {
      "location": "Living Room North Wall",
      "number": "HC-172",
      "name": "Revere Pewter"
    },
    {
      "location": "Master Bedroom",
      "number": "2130-40",
      "name": "Hale Navy"
    }
  ]
}
```

### Woodwork Form Data
```json
{
  "interiorDoorModel": "Masonite Carrara",
  "handleModel": "Linea Rosette Carré",
  "handleFinish": "Matte Black",
  "baseboardModel": "1500 Series",
  "baseboardHeight": "5.25 inches"
}
```

## Notification System

### Email Templates

The service includes HTML email templates for:

1. **Form Assigned**: Sent to customer when form is assigned
   - Includes form type, project info, instructions
   - Link to customer portal

2. **Form Submitted**: Sent to salesperson/owner who assigned the form
   - Includes customer name, form type, submission timestamp
   - Link to review in admin portal

3. **Form Reopened**: Sent to customer
   - Includes reopen reason, updated instructions
   - Link to edit form in customer portal

### System Notifications

All email notifications are accompanied by in-app notifications via the existing NotificationService:

- Category: `FORM_ASSIGNED`, `FORM_SUBMITTED`, `FORM_REOPENED`
- Includes deep links to relevant form pages
- Visible in user's notification center

## Security & Authorization

### Role-Based Access Control

- **Owners & Salespersons**:
  - Create, assign, update, delete forms
  - View all forms
  - Reopen and complete forms
  - View submission history

- **Customers**:
  - View only their assigned forms
  - Update form data for their forms
  - Submit their forms
  - View their submission history
  - Cannot create, delete, or complete forms

### Data Validation

- Form type uniqueness per customer per project
- Customer ownership verification on updates
- Status validation for operations (can't update completed forms, can only reopen submitted forms, etc.)
- Required field validation via Jakarta Bean Validation

## Testing

### Unit Tests

Comprehensive unit tests in `FormServiceImplUnitTest.java` cover:

- Create and assign form (with various scenarios)
- Get form operations
- Update form data (authorized/unauthorized)
- Submit form (successful and error cases)
- Reopen form (valid and invalid statuses)
- Complete form (permission and status checks)
- Delete form operations
- Submission history tracking
- Form type existence checks

### Test Coverage

Key scenarios tested:
- Happy paths for all operations
- Authorization failures
- Validation errors
- Status transition rules
- Notification sending
- History creation

## Integration Points

### Dependencies

The Form Subdomain integrates with:

1. **Users Subdomain**: User lookup and validation
2. **Project Subdomain**: Project association (via foreign key)
3. **Communication Subdomain**: 
   - NotificationService for in-app notifications
   - MailerServiceClient for email notifications

### External Services

- **Mailer Service**: Async email sending via WebClient
- **Auth0**: User authentication and role management

## Future Enhancements

Potential improvements:

1. **File Upload Support**: Direct PDF upload for exterior/garage door forms
2. **Form Templates**: Pre-defined form field templates for each form type
3. **Approval Workflow**: Multi-step approval process
4. **Form Analytics**: Completion rates, average time to complete
5. **Bulk Operations**: Assign same form to multiple customers
6. **Form Versioning**: Track changes to form structure over time
7. **Comments/Discussion**: Allow threaded discussions on forms
8. **Mobile Optimisation**: Dedicated mobile form filling experience
9. **Auto-save**: Periodic auto-save of customer progress
10. **Reminders**: Automated reminders for incomplete forms

## Database Migration

When deploying, ensure the database schema is updated:

```sql
-- Run the schema updates from schema.sql
-- The forms and form_submission_history tables will be created
```

For existing deployments, the schema changes are backward compatible and can be applied without downtime.

## Monitoring & Logging

The service includes comprehensive logging:

- **Info Level**: Form creation, submission, reopening, completion
- **Error Level**: Notification/email failures, validation errors
- **Debug Level**: Data transformations and detailed flow

Recommended monitoring:
- Form submission rates
- Reopen frequency  - Notification delivery success rates
- Average time from assignment to submission

## Support for 6 Form Types

The system is designed to handle all 6 form types specified in the requirements:

1. ✅ **Exterior Doors**: Upload PDF from Novatech website
2. ✅ **Garage Doors**: Upload PDF from Universal Garage Doors website
3. ✅ **Windows**: Structured form for color selections
4. ✅ **Asphalt Shingles**: Structured form for material selections
5. ✅ **Woodwork**: Structured form for interior selections from Intermat
6. ✅ **Paint**: Structured form for paint specifications with multiple accent walls

The flexible JSONB storage allows each form type to have its own custom data structure while sharing the same underlying infrastructure.

---

## Quick Start for Developers

### Creating a New Form via Postman/API

1. Authenticate as salesperson/owner
2. POST to `/api/v1/forms`:
   ```json
   {
     "formType": "WINDOWS",
     "projectIdentifier": "your-project-id",
     "customerId": "customer-uuid",
     "formTitle": "Window Selection Form",
     "instructions": "Please select all window colors"
   }
   ```

3. Customer receives notification
4. Customer fills out form via PUT `/api/v1/forms/{formId}/data`
5. Customer submits via POST `/api/v1/forms/{formId}/submit`
6. Salesperson reviews and either completes or reopens

### Running Tests

```bash
./gradlew test --tests "FormServiceImplUnitTest"
```

### Accessing in Frontend

The frontend should integrate with these endpoints to provide:
- Form assignment interface for salespersons
- Form filling interface for customers
- Form review interface for salespersons/owners
- Submission history viewer

---

## Conclusion

The Form Subdomain provides a complete, flexible, and robust solution for managing customer forms throughout the project lifecycle. With comprehensive validation, notification support, submission history tracking, and a flexible data model, it supports all 6 required form types while maintaining extensibility for future needs.
