package service_test

import (
	"context"
	"errors"
	"files-service/internal/domain"
	"files-service/internal/service"
	"testing"
)

type mockRepo struct {
	SaveFn     func(ctx context.Context, f *domain.File) error
	FindByIdFn func(ctx context.Context, id string) (*domain.File, error)
	DeleteFn   func(ctx context.Context, id string) error
}

func (m *mockRepo) Save(ctx context.Context, f *domain.File) error { return m.SaveFn(ctx, f) }
func (m *mockRepo) FindById(ctx context.Context, id string) (*domain.File, error) {
	return m.FindByIdFn(ctx, id)
}
func (m *mockRepo) Delete(ctx context.Context, id string) error { return m.DeleteFn(ctx, id) }

type mockStorage struct {
	UploadFn   func(ctx context.Context, data []byte, key string, contentType string) (string, error)
	DownloadFn func(ctx context.Context, key string) ([]byte, error)
	DeleteFn   func(ctx context.Context, key string) error
}

func (m *mockStorage) Upload(ctx context.Context, data []byte, key string, ct string) (string, error) {
	return m.UploadFn(ctx, data, key, ct)
}
func (m *mockStorage) Download(ctx context.Context, key string) ([]byte, error) {
	return m.DownloadFn(ctx, key)
}
func (m *mockStorage) Delete(ctx context.Context, key string) error { return m.DeleteFn(ctx, key) }

//
// ------------------ UPLOAD TESTS ------------------
//

func TestUpload_Success(t *testing.T) {

	repo := &mockRepo{
		SaveFn: func(ctx context.Context, f *domain.File) error { return nil },
	}

	storage := &mockStorage{
		UploadFn: func(ctx context.Context, data []byte, key string, c string) (string, error) {
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
		UploadFn: func(ctx context.Context, data []byte, key, c string) (string, error) {
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
		UploadFn: func(ctx context.Context, d []byte, k, c string) (string, error) { return k, nil },
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
		UploadFn: func(ctx context.Context, d []byte, k, c string) (string, error) { return k, nil },
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

//
// ------------------ GET TESTS ------------------
//

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
		DownloadFn: func(ctx context.Context, key string) ([]byte, error) {
			return nil, downloadErr
		},
	}

	s := service.NewFileService(repo, storage)

	_, _, err := s.Get(context.Background(), "123")
	if !errors.Is(err, downloadErr) {
		t.Fatalf("expected download error")
	}
}

//
// ------------------ DELETE TESTS ------------------
//

func TestDelete_Success(t *testing.T) {

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ObjectKey: "key"}, nil
		},
		DeleteFn: func(ctx context.Context, id string) error { return nil },
	}

	storage := &mockStorage{
		DeleteFn: func(ctx context.Context, key string) error { return nil },
	}

	s := service.NewFileService(repo, storage)

	err := s.Delete(context.Background(), "123")
	if err != nil {
		t.Fatalf("unexpected error")
	}
}

func TestDelete_NotFound(t *testing.T) {

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) { return nil, nil },
	}

	s := service.NewFileService(repo, &mockStorage{})

	err := s.Delete(context.Background(), "123")
	if err != domain.ErrNotFound {
		t.Fatalf("expected ErrNotFound")
	}
}

func TestDelete_FindError(t *testing.T) {

	findErr := errors.New("db error")

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) { return nil, findErr },
	}

	s := service.NewFileService(repo, &mockStorage{})

	err := s.Delete(context.Background(), "123")
	if !errors.Is(err, findErr) {
		t.Fatalf("expected find error")
	}
}

func TestDelete_StorageError(t *testing.T) {

	storageErr := errors.New("S3 delete error")

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ObjectKey: "key"}, nil
		},
	}

	storage := &mockStorage{
		DeleteFn: func(ctx context.Context, key string) error { return storageErr },
	}

	s := service.NewFileService(repo, storage)

	err := s.Delete(context.Background(), "123")
	if !errors.Is(err, storageErr) {
		t.Fatalf("expected storage delete error")
	}
}

func TestDelete_RepoDeleteError(t *testing.T) {

	repoErr := errors.New("repo delete error")

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ObjectKey: "key"}, nil
		},
		DeleteFn: func(ctx context.Context, id string) error { return repoErr },
	}

	storage := &mockStorage{
		DeleteFn: func(ctx context.Context, key string) error { return nil },
	}

	s := service.NewFileService(repo, storage)

	err := s.Delete(context.Background(), "123")
	if !errors.Is(err, repoErr) {
		t.Fatalf("expected repo delete error")
	}
}
