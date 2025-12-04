package service

import (
	"context"
	"files-service/internal/domain"
	"fmt"
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

	_, err := s.storage.Upload(ctx, input.Data, objectKey, input.ContentType)
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

func (s *fileService) Delete(ctx context.Context, fileID string) error {

	f, err := s.repo.FindById(ctx, fileID)
	if err != nil {
		return err
	}
	if f == nil {
		return domain.ErrNotFound
	}

	if err := s.storage.Delete(ctx, f.ObjectKey); err != nil {
		return err
	}

	return s.repo.Delete(ctx, fileID)
}
