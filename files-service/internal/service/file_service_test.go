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

func (m *mockStorage) Upload(ctx context.Context, data []byte, key string, contentType string) (string, error) {
	return m.UploadFn(ctx, data, key, contentType)
}
func (m *mockStorage) Download(ctx context.Context, key string) ([]byte, error) {
	return m.DownloadFn(ctx, key)
}
func (m *mockStorage) Delete(ctx context.Context, key string) error {
	return m.DeleteFn(ctx, key)
}

// ---------------------------------------------------
// ---------- UPLOAD TESTS ---------------------------
// ---------------------------------------------------

// ---------- UPLOAD SUCCESS ----------
func TestUpload_Success(t *testing.T) {

	repo := &mockRepo{
		SaveFn: func(ctx context.Context, f *domain.File) error { return nil },
	}

	storage := &mockStorage{
		UploadFn: func(ctx context.Context, data []byte, key string, c string) (string, error) {
			return key, nil
		},
		DeleteFn: func(ctx context.Context, key string) error { return nil },
	}

	s := service.NewFileService(repo, storage)

	input := domain.FileUploadInput{
		FileName:    "test.png",
		ContentType: "image/png",
		Category:    domain.CategoryPhoto, // Changed to a valid category
		Data:        []byte("ABC"),
	}

	metadata, err := s.Upload(context.Background(), input)
	if err != nil {
		t.Fatalf("unexpected: %v", err)
	}

	if metadata.FileName != "test.png" {
		t.Fatalf("wrong output filename")
	}
}

// ---------- UPLOAD FAIL (validation) ----------
func TestUpload_ValidationError(t *testing.T) {
	repo := &mockRepo{}
	storage := &mockStorage{}

	s := service.NewFileService(repo, storage)

	_, err := s.Upload(context.Background(), domain.FileUploadInput{
		FileName:    "x",
		ContentType: "y",
		Category:    domain.FileCategory("INVALID"), // Invalid Category
		Data:        []byte("123"),
	})

	if err != domain.ErrValidation {
		t.Fatalf("expected ErrValidation, got %v", err)
	}
}

// ---------- UPLOAD FAIL (storage) ----------
func TestUpload_StorageError(t *testing.T) {
	repo := &mockRepo{}
	expectedErr := errors.New("S3 DOWN")
	storage := &mockStorage{
		UploadFn: func(ctx context.Context, data []byte, key string, c string) (string, error) {
			return "", expectedErr
		},
	}

	s := service.NewFileService(repo, storage)

	_, err := s.Upload(context.Background(), domain.FileUploadInput{
		FileName:    "x",
		ContentType: "y",
		Category:    domain.CategoryOther,
		Data:        []byte("123"),
	})

	if !errors.Is(err, expectedErr) {
		t.Fatalf("expected storage error, got %v", err)
	}
}

// ---------- UPLOAD FAIL (repo save) and cleanup SUCCESS ----------
func TestUpload_SaveError_CleanupSuccess(t *testing.T) {
	expectedErr := errors.New("db down")
	repo := &mockRepo{
		SaveFn: func(ctx context.Context, f *domain.File) error { return expectedErr },
	}

	deleteCalled := false
	storage := &mockStorage{
		UploadFn: func(ctx context.Context, data []byte, key string, c string) (string, error) {
			return key, nil
		},
		DeleteFn: func(ctx context.Context, key string) error {
			deleteCalled = true
			return nil
		},
	}

	s := service.NewFileService(repo, storage)

	_, err := s.Upload(context.Background(), domain.FileUploadInput{
		FileName:    "x",
		ContentType: "y",
		Category:    domain.CategoryOther,
		Data:        []byte("123"),
	})

	if !errors.Is(err, expectedErr) {
		t.Fatalf("expected repo error, got %v", err)
	}
	if !deleteCalled {
		t.Fatalf("expected storage delete to be called for cleanup")
	}
}

// ---------- UPLOAD FAIL (repo save) and cleanup FAIL (error should still be the Save error) ----------
func TestUpload_SaveError_CleanupFails(t *testing.T) {
	expectedSaveErr := errors.New("db down")
	cleanupErr := errors.New("cleanup failed")

	repo := &mockRepo{
		SaveFn: func(ctx context.Context, f *domain.File) error { return expectedSaveErr },
	}

	storage := &mockStorage{
		UploadFn: func(ctx context.Context, data []byte, key string, c string) (string, error) {
			return key, nil
		},
		// Cleanup fails, but the service should still return the original Save error
		DeleteFn: func(ctx context.Context, key string) error { return cleanupErr },
	}

	s := service.NewFileService(repo, storage)

	_, err := s.Upload(context.Background(), domain.FileUploadInput{
		FileName:    "x",
		ContentType: "y",
		Category:    domain.CategoryOther,
		Data:        []byte("123"),
	})

	if !errors.Is(err, expectedSaveErr) {
		t.Fatalf("expected original Save error, got %v", err)
	}
}

// ---------------------------------------------------
// ---------- GET TESTS ------------------------------
// ---------------------------------------------------

// ---------- GET SUCCESS ----------
func TestGet_Success(t *testing.T) {

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ObjectKey: "OK", ContentType: "text/plain"}, nil
		},
	}

	storage := &mockStorage{
		DownloadFn: func(ctx context.Context, key string) ([]byte, error) {
			return []byte("DATA"), nil
		},
	}

	s := service.NewFileService(repo, storage)

	data, ct, err := s.Get(context.Background(), "123")
	if err != nil {
		t.Fatalf("unexpected: %v", err)
	}

	if string(data) != "DATA" || ct != "text/plain" {
		t.Fatalf("wrong output. Got data: %s, ct: %s", string(data), ct)
	}
}

// ---------- GET NOT FOUND ----------
func TestGet_NotFound(t *testing.T) {

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return nil, nil // Repo returns (nil, nil) for not found
		},
	}

	storage := &mockStorage{}

	s := service.NewFileService(repo, storage)

	_, _, err := s.Get(context.Background(), "xxx")
	if err != domain.ErrNotFound {
		t.Fatalf("expected ErrNotFound, got %v", err)
	}
}

// ---------- GET FAIL (repo find) ----------
func TestGet_RepoError(t *testing.T) {
	expectedErr := errors.New("db find error")
	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return nil, expectedErr
		},
	}
	storage := &mockStorage{}
	s := service.NewFileService(repo, storage)

	_, _, err := s.Get(context.Background(), "123")
	if !errors.Is(err, expectedErr) {
		t.Fatalf("expected repo error, got %v", err)
	}
}

// ---------- GET FAIL (storage download) ----------
func TestGet_DownloadError(t *testing.T) {
	expectedErr := errors.New("S3 download error")
	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ObjectKey: "OK"}, nil
		},
	}
	storage := &mockStorage{
		DownloadFn: func(ctx context.Context, key string) ([]byte, error) {
			return nil, expectedErr
		},
	}
	s := service.NewFileService(repo, storage)

	_, _, err := s.Get(context.Background(), "123")
	if !errors.Is(err, expectedErr) {
		t.Fatalf("expected download error, got %v", err)
	}
}

// ---------------------------------------------------
// ---------- DELETE TESTS ---------------------------
// ---------------------------------------------------

// ---------- DELETE SUCCESS ----------
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
		t.Fatalf("unexpected: %v", err)
	}
}

// ---------- DELETE NOT FOUND ----------
func TestDelete_NotFound(t *testing.T) {

	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) { return nil, nil },
	}

	storage := &mockStorage{}

	s := service.NewFileService(repo, storage)

	err := s.Delete(context.Background(), "123")
	if err != domain.ErrNotFound {
		t.Fatalf("expected ErrNotFound, got %v", err)
	}
}

// ---------- DELETE FAIL (repo find) ----------
func TestDelete_FindError(t *testing.T) {
	expectedErr := errors.New("db find error")
	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return nil, expectedErr
		},
	}
	storage := &mockStorage{}
	s := service.NewFileService(repo, storage)

	err := s.Delete(context.Background(), "123")
	if !errors.Is(err, expectedErr) {
		t.Fatalf("expected find error, got %v", err)
	}
}

// ---------- DELETE FAIL (storage delete) ----------
func TestDelete_StorageError(t *testing.T) {
	expectedErr := errors.New("S3 delete error")
	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ObjectKey: "key"}, nil
		},
	}
	storage := &mockStorage{
		DeleteFn: func(ctx context.Context, key string) error { return expectedErr },
	}
	s := service.NewFileService(repo, storage)

	err := s.Delete(context.Background(), "123")
	if !errors.Is(err, expectedErr) {
		t.Fatalf("expected S3 delete error, got %v", err)
	}
}

// ---------- DELETE FAIL (repo delete) ----------
func TestDelete_RepoDeleteError(t *testing.T) {
	expectedErr := errors.New("db delete error")
	repo := &mockRepo{
		FindByIdFn: func(ctx context.Context, id string) (*domain.File, error) {
			return &domain.File{ObjectKey: "key"}, nil
		},
		DeleteFn: func(ctx context.Context, id string) error { return expectedErr },
	}
	storage := &mockStorage{
		DeleteFn: func(ctx context.Context, key string) error { return nil },
	}
	s := service.NewFileService(repo, storage)

	err := s.Delete(context.Background(), "123")
	if !errors.Is(err, expectedErr) {
		t.Fatalf("expected repo delete error, got %v", err)
	}
}
