package service

import (
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
		ID:          id,
		ProjectID:   input.ProjectID,
		FileName:    input.FileName,
		UploadedBy:  input.UploadedBy,
		ContentType: input.ContentType,
		Category:    input.Category,
		Size:        int64(len(input.Data)),
		ObjectKey:   objectKey,
		CreatedAt:   time.Now(),
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

	// Perform soft delete - deactivate the file, don't remove it
	if err := s.repo.Delete(ctx, fileID, deletedBy); err != nil {
		log.Printf("[ERROR] Failed to delete file %s: %v", fileID, err)
		return err
	}

	// Also delete the file from storage to prevent storage bloat
	if err := s.storage.Delete(ctx, f.ObjectKey); err != nil {
		log.Printf("[ERROR] Failed to delete file %s from storage: %v", f.ObjectKey, err)
		// Continue; file is soft-deleted in DB, but storage cleanup failed
	}
	// Log deletion for audit trail
	log.Printf("[AUDIT] File deleted (archived): ID=%s, FileName=%s, ProjectID=%s, Category=%s, DeletedBy=%s",
		fileID, f.FileName, f.ProjectID, f.Category, deletedBy)

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
