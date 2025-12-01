package service

import (
	"context"
	"files-service/internal/domain"
	"time"

	"github.com/google/uuid"
)

type StorageClient interface {
	Upload(ctx context.Context, data []byte, key string, contentType string) (string, error)
	Download(ctx context.Context, key string) ([]byte, error)
	Delete(ctx context.Context, key string) error
}

type fileService struct {
	repo    domain.FileRepository
	storage StorageClient
}

func NewFileService(repo domain.FileRepository, storage StorageClient) domain.FileService {
	return &fileService{repo: repo, storage: storage}
}

func (s *fileService) Upload(ctx context.Context, input domain.FileUploadInput) (domain.FileMetadata, error) {

	if !input.Category.Validate() {
		return domain.FileMetadata{}, domain.ErrValidation
	}

	id := uuid.New().String()
	objectKey := uuid.New().String()

	_, err := s.storage.Upload(ctx, input.Data, objectKey, input.ContentType)
	if err != nil {
		return domain.FileMetadata{}, err
	}

	file := domain.File{
		ID:          id,
		FileName:    input.FileName,
		ContentType: input.ContentType,
		Category:    input.Category,
		Size:        int64(len(input.Data)),
		ObjectKey:   objectKey,
		CreatedAt:   time.Now(),
	}

	if err := s.repo.Save(ctx, &file); err != nil {
		s.storage.Delete(ctx, objectKey)
		return domain.FileMetadata{}, err
	}

	return domain.FileMetadata{
		ID:          id,
		FileName:    file.FileName,
		ContentType: file.ContentType,
		Category:    file.Category,
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
