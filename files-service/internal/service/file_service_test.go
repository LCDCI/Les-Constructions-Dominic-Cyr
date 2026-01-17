package service_test

import (
	"context"
	"errors"
	"files-service/internal/domain"
	"files-service/internal/service"
	"testing"
)

type mockRepo struct {
	SaveFn      func(ctx context.Context, f *domain.File) error
	FindByIdFn  func(ctx context.Context, id string) (*domain.File, error)
	DeleteFn    func(ctx context.Context, id string, deletedBy string) error
	ArchiveFn   func(ctx context.Context, id string, archivedBy string) error
	UnarchiveFn func(ctx context.Context, id string) error

	FindByProjectIDFn         func(ctx context.Context, projectID string) ([]domain.File, error)
	FindByObjectKeyFn         func(ctx context.Context, objectKey string) (*domain.File, error)
	FindArchivedByProjectIDFn func(ctx context.Context, projectID string) ([]domain.File, error)
}

func (m *mockRepo) Save(ctx context.Context, f *domain.File) error { return m.SaveFn(ctx, f) }
func (m *mockRepo) FindById(ctx context.Context, id string) (*domain.File, error) {
	return m.FindByIdFn(ctx, id)
}
func (m *mockRepo) Delete(ctx context.Context, id string, deletedBy string) error {
	return m.DeleteFn(ctx, id, deletedBy)
}
func (m *mockRepo) Archive(ctx context.Context, id string, archivedBy string) error {
	if m.ArchiveFn != nil {
		return m.ArchiveFn(ctx, id, archivedBy)
	}
	return nil
}
func (m *mockRepo) Unarchive(ctx context.Context, id string) error {
	if m.UnarchiveFn != nil {
		return m.UnarchiveFn(ctx, id)
	}
	return nil
}
func (m *mockRepo) FindByProjectID(ctx context.Context, projectID string) ([]domain.File, error) {
	if m.FindByProjectIDFn != nil {
		return m.FindByProjectIDFn(ctx, projectID)
	}
	return nil, nil
}

func (m *mockRepo) FindByObjectKey(ctx context.Context, objectKey string) (*domain.File, error) {
	if m.FindByObjectKeyFn != nil {
		return m.FindByObjectKeyFn(ctx, objectKey)
	}
	return nil, nil
}

func (m *mockRepo) FindArchivedByProjectID(ctx context.Context, projectID string) ([]domain.File, error) {
	if m.FindArchivedByProjectIDFn != nil {
		return m.FindArchivedByProjectIDFn(ctx, projectID)
	}
	return nil, nil
}

type mockStorage struct {
	UploadFn      func(ctx context.Context, data []byte, key string, contentType string, metadata map[string]string) (string, error)
	DownloadFn    func(ctx context.Context, key string) ([]byte, error)
	DeleteFn      func(ctx context.Context, key string) error
	ListObjectsFn func(ctx context.Context, prefix string) ([]service.ObjectInfo, error)
}

func (m *mockStorage) Upload(ctx context.Context, data []byte, key string, ct string, metadata map[string]string) (string, error) {
	return m.UploadFn(ctx, data, key, ct, metadata)
}
func (m *mockStorage) Download(ctx context.Context, key string) ([]byte, error) {
	return m.DownloadFn(ctx, key)
}
func (m *mockStorage) Delete(ctx context.Context, key string) error { return m.DeleteFn(ctx, key) }

func (m *mockStorage) ListObjects(ctx context.Context, prefix string) ([]service.ObjectInfo, error) {
	if m.ListObjectsFn != nil {
		return m.ListObjectsFn(ctx, prefix)
	}
	return nil, nil
}

func TestUpload_Success(t *testing.T) {
	repo := &mockRepo{
		SaveFn: func(ctx context.Context, f *domain.File) error { return nil },
	}

	storage := &mockStorage{
		UploadFn: func(ctx context.Context, data []byte, key string, c string, metadata map[string]string) (string, error) {
			return key, nil
		},
	}

	s := service.NewFileService(repo, storage)

	input := domain.FileUploadInput{
		FileName:    "test.png",
		ContentType: "image/png",
		Category:    domain.CategoryPhoto,
		ProjectID:   "project-1",
		UploadedBy:  "user-1",
		Data:        []byte("ABC"),
	}

	metadata, err := s.Upload(context.Background(), input)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	if metadata.FileName != "test.png" {
		t.Fatalf("wrong filename")
	}
}

func TestUpload_ValidationError(t *testing.T) {
	s := service.NewFileService(&mockRepo{}, &mockStorage{})

	_, err := s.Upload(context.Background(), domain.FileUploadInput{
		FileName:    "x",
		ContentType: "y",
		Category:    domain.FileCategory("INVALID"),
		ProjectID:   "p",
		UploadedBy:  "u",
		Data:        []byte("123"),
	})

	if err != domain.ErrValidation {
		t.Fatalf("expected ErrValidation, got %v", err)
	}
}

func TestUpload_StorageError(t *testing.T) {
	storageErr := errors.New("storage down")

	storage := &mockStorage{
		UploadFn: func(ctx context.Context, data []byte, key, c string, metadata map[string]string) (string, error) {
			return "", storageErr
		},
	}

	s := service.NewFileService(&mockRepo{}, storage)

	_, err := s.Upload(context.Background(), domain.FileUploadInput{
		FileName:    "x",
		ContentType: "y",
		Category:    domain.CategoryOther,
		ProjectID:   "p",
		UploadedBy:  "u",
		Data:        []byte("123"),
	})

	if !errors.Is(err, domain.ErrStorageFailed) {
		t.Fatalf("expected ErrStorageFailed, got %v", err)
	}
}

func TestUpload_SaveError_CleanupSuccess(t *testing.T) {
	expected := errors.New("db error")

	repo := &mockRepo{
		SaveFn: func(ctx context.Context, f *domain.File) error { return expected },
	}

	cleanupCalled := false

	storage := &mockStorage{
		UploadFn: func(ctx context.Context, d []byte, k, c string, metadata map[string]string) (string, error) {
			return k, nil
		},
		DeleteFn: func(ctx context.Context, k string) error {
			cleanupCalled = true
			return nil
		},
	}

	s := service.NewFileService(repo, storage)

	_, err := s.Upload(context.Background(), domain.FileUploadInput{
		FileName:    "x",
		ContentType: "y",
		Category:    domain.CategoryOther,
		ProjectID:   "p",
		UploadedBy:  "u",
		Data:        []byte("123"),
	})

	if !errors.Is(err, expected) {
		t.Fatalf("expected save error")
	}

	if !cleanupCalled {
		t.Fatalf("expected cleanup delete")
	}
}

func TestUpload_SaveError_CleanupFails(t *testing.T) {
	saveErr := errors.New("db fail")

	repo := &mockRepo{
		SaveFn: func(ctx context.Context, f *domain.File) error { return saveErr },
	}

	storage := &mockStorage{
		UploadFn: func(ctx context.Context, d []byte, k, c string, metadata map[string]string) (string, error) {
			return k, nil
		},
		DeleteFn: func(ctx context.Context, k string) error { return errors.New("cleanup failed") },
	}

	s := service.NewFileService(repo, storage)

	_, err := s.Upload(context.Background(), domain.FileUploadInput{
		FileName:    "x",
		ContentType: "y",
		Category:    domain.CategoryOther,
		ProjectID:   "p",
		UploadedBy:  "u",
		Data:        []byte("123"),
	})

	if !errors.Is(err, saveErr) {
		t.Fatalf("expected saveErr, got %v", err)
	}
}

func TestListByProjectID_Success(t *testing.T) {
	mockFiles := []domain.File{
		{ID: "photo-1", FileName: "pic1.jpg", ProjectID: "proj-A"},
		{ID: "photo-2", FileName: "pic2.jpg", ProjectID: "proj-A"},
	}

	repo := &mockRepo{
		FindByProjectIDFn: func(ctx context.Context, projectID string) ([]domain.File, error) {
			if projectID == "proj-A" {
				return mockFiles, nil
			}
			return nil, nil
		},
	}
	s := service.NewFileService(repo, &mockStorage{})

	metadataList, err := s.ListByProjectID(context.Background(), "proj-A")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	if len(metadataList) != 2 {
		t.Fatalf("expected 2 files, got %d", len(metadataList))
	}
	if metadataList[0].ID != "photo-1" || metadataList[1].FileName != "pic2.jpg" {
		t.Fatalf("mismatched file data in list")
	}
}

func TestListByProjectID_RepoError(t *testing.T) {
	repoErr := errors.New("database connection failed")

	repo := &mockRepo{
		FindByProjectIDFn: func(ctx context.Context, projectID string) ([]domain.File, error) {
			return nil, repoErr
		},
	}
	s := service.NewFileService(repo, &mockStorage{})

	_, err := s.ListByProjectID(context.Background(), "proj-B")
	if !errors.Is(err, repoErr) {
		t.Fatalf("expected repository error, got %v", err)
	}
}

func TestGet_Success(t *testing.T) {
	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ObjectKey: "key", ContentType: "text/plain"}, nil
		},
	}

	storage := &mockStorage{
		DownloadFn: func(ctx context.Context, key string) ([]byte, error) { return []byte("DATA"), nil },
	}

	s := service.NewFileService(repo, storage)

	data, ct, err := s.Get(context.Background(), "123")
	if err != nil {
		t.Fatalf("unexpected: %v", err)
	}

	if string(data) != "DATA" || ct != "text/plain" {
		t.Fatalf("wrong content")
	}
}

func TestGet_NotFound(t *testing.T) {
	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) { return nil, nil },
	}

	s := service.NewFileService(repo, &mockStorage{})

	_, _, err := s.Get(context.Background(), "xxx")
	if err != domain.ErrNotFound {
		t.Fatalf("expected ErrNotFound")
	}
}

func TestGet_FindError(t *testing.T) {
	findErr := errors.New("db fail")

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) { return nil, findErr },
	}

	s := service.NewFileService(repo, &mockStorage{})

	_, _, err := s.Get(context.Background(), "123")
	if !errors.Is(err, findErr) {
		t.Fatalf("expected find error")
	}
}

func TestGet_DownloadError(t *testing.T) {
	downloadErr := errors.New("download fail")

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ObjectKey: "key"}, nil
		},
	}

	storage := &mockStorage{
		DownloadFn: func(ctx context.Context, key string) ([]byte, error) { return nil, downloadErr },
	}

	s := service.NewFileService(repo, storage)

	_, _, err := s.Get(context.Background(), "123")
	if !errors.Is(err, downloadErr) {
		t.Fatalf("expected download error")
	}
}

func TestDelete_Success(t *testing.T) {
	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ID: id, FileName: "test.pdf", ProjectID: "proj-1", Category: domain.CategoryDocument}, nil
		},
		DeleteFn: func(ctx context.Context, id string, deletedBy string) error { return nil },
	}

	s := service.NewFileService(repo, &mockStorage{})

	err := s.Delete(context.Background(), "123", "11111111-1111-1111-1111-111111111111")
	if err != nil {
		t.Fatalf("unexpected error")
	}
}

func TestDelete_NotFound(t *testing.T) {
	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) { return nil, nil },
	}

	s := service.NewFileService(repo, &mockStorage{})

	err := s.Delete(context.Background(), "nonexistent", "11111111-1111-1111-1111-111111111111")
	if err != domain.ErrNotFound {
		t.Fatalf("expected ErrNotFound")
	}
}

func TestDelete_FindError(t *testing.T) {
	findErr := errors.New("database connection error")

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) { return nil, findErr },
	}

	s := service.NewFileService(repo, &mockStorage{})

	err := s.Delete(context.Background(), "file-123", "11111111-1111-1111-1111-111111111111")
	if !errors.Is(err, findErr) {
		t.Fatalf("expected find error")
	}
}

func TestDelete_StorageError(t *testing.T) {
	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ObjectKey: "key"}, nil
		},
		DeleteFn: func(ctx context.Context, id string, deletedBy string) error { return nil },
	}

	s := service.NewFileService(repo, &mockStorage{})

	err := s.Delete(context.Background(), "123", "11111111-1111-1111-1111-111111111111")
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
}

func TestDelete_RepoDeleteError(t *testing.T) {
	repoErr := errors.New("repo delete error")

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ObjectKey: "key"}, nil
		},
		DeleteFn: func(ctx context.Context, id string, deletedBy string) error { return repoErr },
	}

	storage := &mockStorage{
		DeleteFn: func(ctx context.Context, key string) error { return nil },
	}

	s := service.NewFileService(repo, storage)

	err := s.Delete(context.Background(), "123", "11111111-1111-1111-1111-111111111111")
	if !errors.Is(err, repoErr) {
		t.Fatalf("expected repo delete error")
	}
}

func TestDelete_WithAuditTrail(t *testing.T) {
	deleteCalledWithParams := false

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{
				ID:        id,
				FileName:  "test.pdf",
				ProjectID: "proj-1",
				Category:  domain.CategoryDocument,
			}, nil
		},
		DeleteFn: func(ctx context.Context, id string, deletedBy string) error {
			deleteCalledWithParams = true
			if id != "file-123" {
				t.Errorf("expected id 'file-123', got '%s'", id)
			}
			if deletedBy != "11111111-1111-1111-1111-111111111111" {
				t.Errorf("expected deletedBy '11111111-1111-1111-1111-111111111111', got '%s'", deletedBy)
			}
			return nil
		},
	}

	s := service.NewFileService(repo, &mockStorage{})

	err := s.Delete(context.Background(), "file-123", "11111111-1111-1111-1111-111111111111")
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}

	if !deleteCalledWithParams {
		t.Fatal("Delete was not called on repository with correct parameters")
	}
}

func TestDelete_EmptyDeletedBy(t *testing.T) {
	s := service.NewFileService(&mockRepo{}, &mockStorage{})

	err := s.Delete(context.Background(), "file-123", "")
	if err != domain.ErrValidation {
		t.Fatalf("expected validation error for empty deletedBy, got %v", err)
	}

	err = s.Delete(context.Background(), "file-123", "   ")
	if err != domain.ErrValidation {
		t.Fatalf("expected validation error for whitespace deletedBy, got %v", err)
	}
}

func TestDelete_FileNotFound(t *testing.T) {
	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return nil, nil
		},
	}

	s := service.NewFileService(repo, &mockStorage{})

	err := s.Delete(context.Background(), "nonexistent", "11111111-1111-1111-1111-111111111111")
	if err != domain.ErrNotFound {
		t.Fatalf("expected not found error, got %v", err)
	}
}

func TestDelete_RepositoryDeleteError(t *testing.T) {
	softDeleteErr := errors.New("soft delete failed")

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ID: id, FileName: "test.pdf"}, nil
		},
		DeleteFn: func(ctx context.Context, id string, deletedBy string) error {
			return softDeleteErr
		},
	}

	s := service.NewFileService(repo, &mockStorage{})

	err := s.Delete(context.Background(), "file-123", "11111111-1111-1111-1111-111111111111")
	if !errors.Is(err, softDeleteErr) {
		t.Fatalf("expected soft delete error, got %v", err)
	}
}

// Archive Tests
func TestArchive_Success(t *testing.T) {
	archiveCalled := false
	archiveUserID := ""

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ID: id, FileName: "photo.jpg", ProjectID: "proj-1"}, nil
		},
		ArchiveFn: func(ctx context.Context, id string, archivedBy string) error {
			archiveCalled = true
			archiveUserID = archivedBy
			return nil
		},
	}

	s := service.NewFileService(repo, &mockStorage{})

	validUUID := "11111111-1111-1111-1111-111111111111"
	err := s.Archive(context.Background(), "file-123", validUUID)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	if !archiveCalled {
		t.Fatalf("expected archive to be called")
	}

	if archiveUserID != validUUID {
		t.Fatalf("expected user ID to be passed, got %s", archiveUserID)
	}
}

func TestArchive_MissingArchivedBy(t *testing.T) {
	s := service.NewFileService(&mockRepo{}, &mockStorage{})

	err := s.Archive(context.Background(), "file-123", "")
	if err != domain.ErrValidation {
		t.Fatalf("expected validation error, got %v", err)
	}
}

func TestArchive_InvalidUUID(t *testing.T) {
	s := service.NewFileService(&mockRepo{}, &mockStorage{})

	err := s.Archive(context.Background(), "file-123", "not-a-uuid")
	if err != domain.ErrValidation {
		t.Fatalf("expected validation error for invalid UUID, got %v", err)
	}
}

func TestArchive_FileNotFound(t *testing.T) {
	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return nil, nil
		},
	}

	s := service.NewFileService(repo, &mockStorage{})

	validUUID := "11111111-1111-1111-1111-111111111111"
	err := s.Archive(context.Background(), "nonexistent", validUUID)
	if err != domain.ErrNotFound {
		t.Fatalf("expected not found error, got %v", err)
	}
}

func TestArchive_RepositoryArchiveError(t *testing.T) {
	archiveErr := errors.New("archive failed")

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ID: id, FileName: "test.jpg"}, nil
		},
		ArchiveFn: func(ctx context.Context, id string, archivedBy string) error {
			return archiveErr
		},
	}

	s := service.NewFileService(repo, &mockStorage{})

	validUUID := "11111111-1111-1111-1111-111111111111"
	err := s.Archive(context.Background(), "file-123", validUUID)
	if !errors.Is(err, archiveErr) {
		t.Fatalf("expected archive error, got %v", err)
	}
}

// Unarchive Tests
func TestUnarchive_Success(t *testing.T) {
	unarchiveCalled := false

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ID: id, FileName: "photo.jpg", ProjectID: "proj-1"}, nil
		},
		UnarchiveFn: func(ctx context.Context, id string) error {
			unarchiveCalled = true
			return nil
		},
	}

	s := service.NewFileService(repo, &mockStorage{})

	err := s.Unarchive(context.Background(), "file-123")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	if !unarchiveCalled {
		t.Fatalf("expected unarchive to be called")
	}
}

func TestUnarchive_FileNotFound(t *testing.T) {
	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return nil, nil
		},
	}

	s := service.NewFileService(repo, &mockStorage{})

	err := s.Unarchive(context.Background(), "nonexistent")
	if err != domain.ErrNotFound {
		t.Fatalf("expected not found error, got %v", err)
	}
}

func TestUnarchive_RepositoryUnarchiveError(t *testing.T) {
	unarchiveErr := errors.New("unarchive failed")

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ID: id, FileName: "test.jpg"}, nil
		},
		UnarchiveFn: func(ctx context.Context, id string) error {
			return unarchiveErr
		},
	}

	s := service.NewFileService(repo, &mockStorage{})

	err := s.Unarchive(context.Background(), "file-123")
	if !errors.Is(err, unarchiveErr) {
		t.Fatalf("expected unarchive error, got %v", err)
	}
}
