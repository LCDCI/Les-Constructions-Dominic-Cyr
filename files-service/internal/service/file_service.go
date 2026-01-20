package service

import (
	"archive/zip"
	"bytes"
	"context"
	"files-service/internal/domain"
	"fmt"
	"log"
	"strings"
	"time"

	"github.com/google/uuid"
)

type fileService struct {
	repo    domain.FileRepository
	storage StorageClient
}

func NewFileService(repo domain.FileRepository, storage StorageClient) domain.FileService {
	return &fileService{
		repo:    repo,
		storage: storage,
	}
}

func (s *fileService) Upload(ctx context.Context, input domain.FileUploadInput) (domain.FileMetadata, error) {

	if input.FileName == "" || input.ContentType == "" || len(input.Data) == 0 {
		return domain.FileMetadata{}, domain.ErrValidation
	}

	if !input.Category.Validate() {
		return domain.FileMetadata{}, domain.ErrValidation
	}

	id := uuid.New().String()
	dateFolder := time.Now().Format("2006-01-02")

	var objectKey string

	if input.Category == domain.CategoryDocument {
		if strings.TrimSpace(input.ProjectID) == "" {
			return domain.FileMetadata{}, domain.ErrValidation
		}

		objectKey = fmt.Sprintf(
			"documents/%s/%s",
			input.ProjectID,
			id,
		)
	}

	if input.Category == domain.CategoryPhoto {

		// PHOTO FOR SPECIFIC PROJECT
		if strings.TrimSpace(input.ProjectID) != "" {
			objectKey = fmt.Sprintf(
				"photos/projects/%s/%s/%s",
				input.ProjectID,
				dateFolder,
				id,
			)

		} else {
			// GLOBAL PHOTO (Homepage, marketing images, etc.)
			objectKey = fmt.Sprintf(
				"photos/global/%s/%s",
				dateFolder,
				id,
			)
		}
	}

	// Store metadata in Spaces for future reconciliation
	metadata := map[string]string{
		"uploadedby": input.UploadedBy,
		"filename":   input.FileName,
	}

	_, err := s.storage.Upload(ctx, input.Data, objectKey, input.ContentType, metadata)
	if err != nil {
		return domain.FileMetadata{}, domain.ErrStorageFailed
	}

	file := domain.File{
		ID:           id,
		ProjectID:   input.ProjectID,
		FileName:     input.FileName,
		UploadedBy:   input.UploadedBy,
		UploaderRole: input.UploaderRole,
		ContentType:  input.ContentType,
		Category:     input.Category,
		Size:         int64(len(input.Data)),
		ObjectKey:    objectKey,
		CreatedAt:    time.Now(),
	}

	if err := s.repo.Save(ctx, &file); err != nil {
		_ = s.storage.Delete(ctx, objectKey)
		return domain.FileMetadata{}, err
	}

	return domain.FileMetadata{
		ID:          id,
		FileName:    file.FileName,
		ContentType: file.ContentType,
		Category:    file.Category,
		ProjectID:   file.ProjectID,
		UploadedBy:  file.UploadedBy,
		Url:         "/files/" + id,
	}, nil
}

func (s *fileService) Get(ctx context.Context, fileID string) ([]byte, string, error) {

	f, err := s.repo.FindById(ctx, fileID)
	if err != nil {
		return nil, "", err
	}
	if f == nil {
		return nil, "", domain.ErrNotFound
	}

	data, err := s.storage.Download(ctx, f.ObjectKey)
	if err != nil {
		return nil, "", err
	}

	return data, f.ContentType, nil
}

// GetWithRoleCheck checks if user has permission to download file based on role
func (s *fileService) GetWithRoleCheck(ctx context.Context, fileID, role, userId string) ([]byte, string, error) {
	f, err := s.repo.FindById(ctx, fileID)
	if err != nil {
		return nil, "", err
	}
	if f == nil {
		return nil, "", domain.ErrNotFound
	}

	// Check permission based on role
	hasPermission := false
	switch role {
	case "OWNER":
		hasPermission = true // Owner can access all files (including NULL role files)
	case "CONTRACTOR":
		// CONTRACTOR: only their files with explicit CONTRACTOR role (no NULL role files)
		hasPermission = f.UploadedBy == userId && f.UploaderRole != nil && *f.UploaderRole == "CONTRACTOR"
	case "SALESPERSON":
		// SALESPERSON: only their files with explicit SALESPERSON role (no NULL role files)
		hasPermission = f.UploadedBy == userId && f.UploaderRole != nil && *f.UploaderRole == "SALESPERSON"
	case "CUSTOMER":
		// CUSTOMER: files from CONTRACTOR or SALESPERSON (no NULL role files)
		hasPermission = f.UploaderRole != nil && (*f.UploaderRole == "CONTRACTOR" || *f.UploaderRole == "SALESPERSON")
	default:
		hasPermission = false
	}

	if !hasPermission {
		return nil, "", domain.ErrNotFound // Return not found to hide existence
	}

	data, err := s.storage.Download(ctx, f.ObjectKey)
	if err != nil {
		return nil, "", err
	}

	return data, f.ContentType, nil
}

func (s *fileService) ListByProjectID(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
	if projectID == "" {
		return nil, domain.ErrValidation
	}

	files, err := s.repo.FindByProjectID(ctx, projectID)
	if err != nil {
		return nil, err
	}

	metadataList := make([]domain.FileMetadata, len(files))
	for i, f := range files {
		metadataList[i] = domain.FileMetadata{
			ID:          f.ID,
			FileName:    f.FileName,
			ContentType: f.ContentType,
			Category:    f.Category,
			ProjectID:   f.ProjectID,
			UploadedBy:  f.UploadedBy,
			Url:         "/files/" + f.ID,
			CreatedAt:   f.CreatedAt.Format(time.RFC3339),
		}
	}
	return metadataList, nil
}

// ListDocumentsByProjectIDAndRole returns documents filtered by role
func (s *fileService) ListDocumentsByProjectIDAndRole(ctx context.Context, projectID, role, userId string) ([]domain.FileMetadata, error) {
	if projectID == "" {
		return nil, domain.ErrValidation
	}

	files, err := s.repo.FindByProjectIDAndRole(ctx, projectID, role, userId)
	if err != nil {
		return nil, err
	}

	// Filter to only DOCUMENT category
	metadataList := make([]domain.FileMetadata, 0)
	for _, f := range files {
		if f.Category == domain.CategoryDocument {
			metadataList = append(metadataList, domain.FileMetadata{
				ID:          f.ID,
				FileName:    f.FileName,
				ContentType: f.ContentType,
				Category:    f.Category,
				ProjectID:   f.ProjectID,
				UploadedBy:  f.UploadedBy,
				Url:         "/files/" + f.ID,
				CreatedAt:   f.CreatedAt.Format(time.RFC3339),
			})
		}
	}
	return metadataList, nil
}

// DownloadProjectDocumentsZip creates a ZIP archive of all documents for a project filtered by role
func (s *fileService) DownloadProjectDocumentsZip(ctx context.Context, projectID, role, userId, projectName string) ([]byte, error) {
	if projectID == "" {
		return nil, domain.ErrValidation
	}

	// Get filtered documents
	documents, err := s.ListDocumentsByProjectIDAndRole(ctx, projectID, role, userId)
	if err != nil {
		return nil, err
	}

	if len(documents) == 0 {
		return nil, domain.ErrNotFound
	}

	return s.createZipArchive(ctx, documents)
}

// createZipArchive creates a ZIP archive from documents
func (s *fileService) createZipArchive(ctx context.Context, documents []domain.FileMetadata) ([]byte, error) {
	// Import archive/zip and bytes
	var buf bytes.Buffer
	zipWriter := zip.NewWriter(&buf)

	// Add each file to ZIP
	for _, doc := range documents {
		// Get file from repository to get object key
		f, err := s.repo.FindById(ctx, doc.ID)
		if err != nil || f == nil {
			log.Printf("[WARN] File %s not found for ZIP", doc.ID)
			continue
		}

		// Download file from storage
		fileData, err := s.storage.Download(ctx, f.ObjectKey)
		if err != nil {
			log.Printf("[WARN] Failed to download file %s for ZIP: %v", doc.ID, err)
			continue
		}

		// Create file entry in ZIP
		fileWriter, err := zipWriter.Create(doc.FileName)
		if err != nil {
			log.Printf("[WARN] Failed to create ZIP entry for %s: %v", doc.FileName, err)
			continue
		}

		// Write file data to ZIP
		if _, err := fileWriter.Write(fileData); err != nil {
			log.Printf("[WARN] Failed to write file %s to ZIP: %v", doc.FileName, err)
			continue
		}
	}

	if err := zipWriter.Close(); err != nil {
		return nil, fmt.Errorf("failed to close ZIP: %w", err)
	}

	return buf.Bytes(), nil
}

func (s *fileService) ListArchivedByProjectID(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
	if projectID == "" {
		return nil, domain.ErrValidation
	}

	files, err := s.repo.FindArchivedByProjectID(ctx, projectID)
	if err != nil {
		return nil, err
	}

	metadataList := make([]domain.FileMetadata, len(files))
	for i, f := range files {
		metadataList[i] = domain.FileMetadata{
			ID:          f.ID,
			FileName:    f.FileName,
			ContentType: f.ContentType,
			Category:    f.Category,
			ProjectID:   f.ProjectID,
			UploadedBy:  f.UploadedBy,
			Url:         "/files/" + f.ID,
			CreatedAt:   f.CreatedAt.Format(time.RFC3339),
			IsArchived:  f.IsArchived,
		}
	}
	return metadataList, nil
}

func (s *fileService) Delete(ctx context.Context, fileID string, deletedBy string) error {
	// Validate deletedBy field
	deletedBy = strings.TrimSpace(deletedBy)
	if deletedBy == "" {
		return domain.ErrValidation
	}
	// Ensure deletedBy is a valid UUID
	if _, err := uuid.Parse(deletedBy); err != nil {
		return domain.ErrValidation
	}

	// Find the file to ensure it exists and is active
	f, err := s.repo.FindById(ctx, fileID)
	if err != nil {
		log.Printf("[ERROR] Failed to find file %s for deletion: %v", fileID, err)
		return err
	}
	if f == nil {
		log.Printf("[WARN] File %s not found for deletion", fileID)
		return domain.ErrNotFound
	}

	// Perform soft delete
	if err := s.repo.Delete(ctx, fileID, deletedBy); err != nil {
		log.Printf("[ERROR] Failed to delete file %s: %v", fileID, err)
		return err
	}

	log.Printf("[AUDIT] File deleted: FileID=%s, DeletedBy=%s", fileID, deletedBy)
	return nil
}

// Archive marks a file as archived instead of deleted.
// Archived files are hidden from the UI but remain in the database for legal/compliance reasons.
// This is used for correcting wrong uploads while maintaining a complete audit trail.
// Owner-only permission required (enforced at the HTTP handler level).
func (s *fileService) Archive(ctx context.Context, fileID string, archivedBy string) error {
	// Validate archivedBy field
	archivedBy = strings.TrimSpace(archivedBy)
	if archivedBy == "" {
		return domain.ErrValidation
	}
	// Ensure archivedBy is a valid UUID
	if _, err := uuid.Parse(archivedBy); err != nil {
		return domain.ErrValidation
	}

	// Find the file to ensure it exists and is not already archived
	f, err := s.repo.FindById(ctx, fileID)
	if err != nil {
		log.Printf("[ERROR] Failed to find file %s for archiving: %v", fileID, err)
		return err
	}
	if f == nil {
		log.Printf("[WARN] File %s not found for archiving", fileID)
		return domain.ErrNotFound
	}

	// Perform archive
	if err := s.repo.Archive(ctx, fileID, archivedBy); err != nil {
		log.Printf("[ERROR] Failed to archive file %s: %v", fileID, err)
		return err
	}

	log.Printf("[AUDIT] File archived: FileID=%s, FileName=%s, ProjectID=%s, ArchivedBy=%s", fileID, f.FileName, f.ProjectID, archivedBy)
	return nil
}

// Unarchive restores a previously archived file, making it visible in the UI again.
// This allows owners to restore mistakenly archived files or correct their decision.
func (s *fileService) Unarchive(ctx context.Context, fileID string) error {
	// Check if file exists and is archived
	f, err := s.repo.FindById(ctx, fileID)
	if err != nil {
		log.Printf("[ERROR] Failed to find file %s for unarchiving: %v", fileID, err)
		return err
	}
	if f == nil {
		log.Printf("[WARN] File %s not found or not archived", fileID)
		return domain.ErrNotFound
	}

	// Perform unarchive
	if err := s.repo.Unarchive(ctx, fileID); err != nil {
		log.Printf("[ERROR] Failed to unarchive file %s: %v", fileID, err)
		return err
	}

	log.Printf("[AUDIT] File unarchived: FileID=%s, FileName=%s, ProjectID=%s", fileID, f.FileName, f.ProjectID)
	return nil
}

// ReconcileStorageWithDatabase scans Spaces for files and creates DB records for any that are missing.
// This is useful for syncing files that were uploaded directly to Spaces without going through the API.
func (s *fileService) ReconcileStorageWithDatabase(ctx context.Context, projectID string) (int, error) {
	if projectID == "" {
		return 0, domain.ErrValidation
	}

	// List all files in Spaces under documents and photos for this project
	objects, err := s.storage.ListObjects(ctx, "documents/"+projectID)
	if err != nil {
		return 0, err
	}

	photoObjects, err := s.storage.ListObjects(ctx, "photos/projects/"+projectID)
	if err != nil {
		return 0, err
	}
	objects = append(objects, photoObjects...)

	var synced int
	now := time.Now()

	for _, obj := range objects {
		// Check if file already exists in DB
		existing, err := s.repo.FindByObjectKey(ctx, obj.Key)
		if err != nil {
			log.Printf("[WARN] Error checking file %s: %v", obj.Key, err)
			continue
		}
		if existing != nil {
			continue // Already in DB
		}

		// Extract category from path
		var category domain.FileCategory
		if strings.Contains(obj.Key, "documents/") {
			category = domain.CategoryDocument
		} else if strings.Contains(obj.Key, "photos/") {
			category = domain.CategoryPhoto
		} else {
			category = domain.CategoryOther
		}

		// Extract filename from metadata or object key
		fileName := obj.Metadata["Filename"]
		if fileName == "" {
			// Fallback to extracting from object key (last part after /)
			fileName = obj.Key
			if idx := strings.LastIndex(obj.Key, "/"); idx >= 0 {
				fileName = obj.Key[idx+1:]
			}
		}

		// Get uploader from metadata
		uploadedBy := obj.Metadata["Uploadedby"]
		if uploadedBy == "" {
			uploadedBy = "Unknown uploader" // Fallback for old files without metadata
		}

		// Get content type from object info or use default
		contentType := obj.ContentType
		if contentType == "" {
			contentType = "application/octet-stream"
		}

		// Create a new File record with metadata from Spaces
		f := &domain.File{
			ID:          uuid.New().String(),
			ProjectID:   projectID,
			FileName:    fileName,
			ObjectKey:   obj.Key,
			Category:    category,
			Size:        obj.Size,
			ContentType: contentType,
			CreatedAt:   now,
			UploadedBy:  uploadedBy,
			IsActive:    true,
		}

		if err := s.repo.Save(ctx, f); err != nil {
			log.Printf("[ERROR] Failed to save reconciled file %s: %v", obj.Key, err)
			continue
		}

		synced++
		log.Printf("[AUDIT] Reconciled file: ID=%s, ObjectKey=%s, ProjectID=%s", f.ID, f.ObjectKey, f.ProjectID)
	}

	log.Printf("[INFO] Reconciliation complete for project %s: %d files synced", projectID, synced)
	return synced, nil
}

// AutoReconcileAllProjects scans Spaces for all projects and syncs any missing files to the database.
// This is called automatically on startup to handle files uploaded directly to Spaces without DB records.
func (s *fileService) AutoReconcileAllProjects(ctx context.Context) error {
	// List all documents and photos prefixes to find unique projects
	docObjects, err := s.storage.ListObjects(ctx, "documents/")
	if err != nil {
		log.Printf("[WARN] Failed to list documents: %v", err)
		// Don't fail startup if listing fails
	}

	photoObjects, err := s.storage.ListObjects(ctx, "photos/projects/")
	if err != nil {
		log.Printf("[WARN] Failed to list photos: %v", err)
		// Don't fail startup if listing fails
	}

	// Extract unique project IDs from object keys
	projectIDs := make(map[string]bool)

	// Parse documents/projectID/* paths
	for _, obj := range docObjects {
		parts := strings.Split(obj.Key, "/")
		if len(parts) >= 2 {
			projectID := parts[1]
			if projectID != "" {
				projectIDs[projectID] = true
			}
		}
	}

	// Parse photos/projects/projectID/* paths
	for _, obj := range photoObjects {
		parts := strings.Split(obj.Key, "/")
		if len(parts) >= 3 {
			projectID := parts[2]
			if projectID != "" {
				projectIDs[projectID] = true
			}
		}
	}

	// Reconcile each project
	totalSynced := 0
	for projectID := range projectIDs {
		synced, err := s.ReconcileStorageWithDatabase(ctx, projectID)
		if err != nil {
			log.Printf("[WARN] Reconciliation failed for project %s: %v", projectID, err)
			continue
		}
		totalSynced += synced
		if synced > 0 {
			log.Printf("[INFO] Auto-reconciled %d files for project %s", synced, projectID)
		}
	}

	if totalSynced > 0 {
		log.Printf("[INFO] Auto-reconciliation complete: %d files synced across all projects", totalSynced)
	} else {
		log.Println("[INFO] Auto-reconciliation: no missing files found")
	}

	return nil
}
