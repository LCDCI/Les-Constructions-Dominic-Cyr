package handlers_test

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"mime/multipart"
	"net/http"
	"net/http/httptest"
	"net/textproto"
	"testing"

	"files-service/internal/domain"
	"files-service/internal/http/handlers"
	"files-service/internal/http/middleware"

	"github.com/gin-gonic/gin"
)

type mockFileService struct {
	UploadFn         func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error)
	GetFn            func(ctx context.Context, id string) ([]byte, string, error)
	DeleteFn         func(ctx context.Context, id string) error
	ListByProjectIDFn func(ctx context.Context, projectID string) ([]domain.FileMetadata, error)
}

func (m *mockFileService) Upload(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
	return m.UploadFn(ctx, in)
}
func (m *mockFileService) Get(ctx context.Context, id string) ([]byte, string, error) {
	return m.GetFn(ctx, id)
}
func (m *mockFileService) Delete(ctx context.Context, id string) error {
	return m.DeleteFn(ctx, id)
}
func (m *mockFileService) ListByProjectID(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
	if m.ListByProjectIDFn != nil {
		return m.ListByProjectIDFn(ctx, projectID)
	}
	return nil, nil
}

func setupRouter(s domain.FileService) *gin.Engine {
	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.Use(middleware.ErrorHandler)
	ctrl := handlers.NewFileController(s)
	ctrl.RegisterRoutes(r)
	return r
}

func multipartRequest(t *testing.T, filename, mimeType string, fileData []byte, category, projectID string) *http.Request {
	body := &bytes.Buffer{}
	writer := multipart.NewWriter(body)

	if filename != "" {
		h := make(textproto.MIMEHeader)
		h.Set("Content-Disposition", fmt.Sprintf(`form-data; name="file"; filename="%s"`, filename))
		h.Set("Content-Type", mimeType)
		part, _ := writer.CreatePart(h)
		part.Write(fileData)
	}

	writer.WriteField("category", category)
	writer.WriteField("projectId", projectID)
	writer.WriteField("uploadedBy", "user-123")

	writer.Close()

	req := httptest.NewRequest("POST", "/files", body)
	req.Header.Set("Content-Type", writer.FormDataContentType())
	return req
}

func TestUpload_Success(t *testing.T) {
	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
			return domain.FileMetadata{
				ID:         "123",
				FileName:   in.FileName,
				UploadedBy: in.UploadedBy,
				ProjectID:  in.ProjectID,
				Url:        "/files/123",
			}, nil
		},
	}

	router := setupRouter(mock)
	req := multipartRequest(t, "example.txt", "text/plain", []byte("hello"), string(domain.CategoryOther), "proj-1")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d", w.Code)
	}
}

func TestUpload_Document_Success(t *testing.T) {
	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
			return domain.FileMetadata{ID: "999"}, nil
		},
	}

	router := setupRouter(mock)
	req := multipartRequest(t, "doc.pdf", "application/pdf", []byte("pdf"), string(domain.CategoryDocument), "12345")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d", w.Code)
	}
}

func TestUpload_Photo_Success(t *testing.T) {
	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
			return domain.FileMetadata{ID: "photo"}, nil
		},
	}

	router := setupRouter(mock)
	req := multipartRequest(t, "img.jpg", "image/jpeg", []byte("jpegdata"), string(domain.CategoryPhoto), "proj-44")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d", w.Code)
	}
}

func TestUpload_GlobalPhoto_Success(t *testing.T) {
	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
			return domain.FileMetadata{ID: "gphoto"}, nil
		},
	}

	router := setupRouter(mock)
	req := multipartRequest(t, "hero.png", "image/png", []byte("data"), string(domain.CategoryPhoto), "")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d", w.Code)
	}
}

func TestUpload_InvalidCategory(t *testing.T) {
	router := setupRouter(&mockFileService{})

	req := multipartRequest(t, "x.png", "image/png", []byte("data"), "INVALID", "proj-123")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", w.Code)
	}
}

func TestUpload_NoFile_BadRequest(t *testing.T) {
	router := setupRouter(&mockFileService{})

	req := httptest.NewRequest("POST", "/files", bytes.NewBufferString("x=1"))
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", w.Code)
	}
}

func TestUpload_ValidationError(t *testing.T) {
	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
			return domain.FileMetadata{}, domain.ErrValidation
		},
	}

	router := setupRouter(mock)
	req := multipartRequest(t, "bad.txt", "text/plain", []byte("data"), string(domain.CategoryOther), "xxx")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", w.Code)
	}
}

func TestUpload_StorageError(t *testing.T) {
	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
			return domain.FileMetadata{}, domain.ErrStorageFailed
		},
	}

	router := setupRouter(mock)
	req := multipartRequest(t, "x.txt", "text/plain", []byte("data"), string(domain.CategoryOther), "yy")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusServiceUnavailable {
		t.Fatalf("expected 503, got %d", w.Code)
	}
}

func TestUpload_InternalError(t *testing.T) {
	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
			return domain.FileMetadata{}, errors.New("db failure")
		},
	}

	router := setupRouter(mock)
	req := multipartRequest(t, "x.txt", "text/plain", []byte("data"), string(domain.CategoryOther), "p")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d", w.Code)
	}
}

func TestDownload_Success(t *testing.T) {
	mock := &mockFileService{
		GetFn: func(ctx context.Context, id string) ([]byte, string, error) {
			return []byte("abc"), "text/plain", nil
		},
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("GET", "/files/123", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", w.Code)
	}
}

func TestDownload_NotFound(t *testing.T) {
	mock := &mockFileService{
		GetFn: func(ctx context.Context, id string) ([]byte, string, error) {
			return nil, "", domain.ErrNotFound
		},
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("GET", "/files/xxx", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusNotFound {
		t.Fatalf("expected 404, got %d", w.Code)
	}
}

func TestDownload_InternalError(t *testing.T) {
	mock := &mockFileService{
		GetFn: func(ctx context.Context, id string) ([]byte, string, error) {
			return nil, "", errors.New("db fail")
		},
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("GET", "/files/xxx", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d", w.Code)
	}
}

func TestDelete_Success(t *testing.T) {
	mock := &mockFileService{
		DeleteFn: func(ctx context.Context, id string) error { return nil },
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("DELETE", "/files/123", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusNoContent {
		t.Fatalf("expected 204, got %d", w.Code)
	}
}

func TestDelete_NotFound(t *testing.T) {
	mock := &mockFileService{
		DeleteFn: func(ctx context.Context, id string) error { return domain.ErrNotFound },
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("DELETE", "/files/xxx", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusNotFound {
		t.Fatalf("expected 404, got %d", w.Code)
	}
}

func TestDelete_InternalError(t *testing.T) {
	mock := &mockFileService{
		DeleteFn: func(ctx context.Context, id string) error { return errors.New("fail") },
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("DELETE", "/files/xxx", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d", w.Code)
	}
}

func TestListProjectFiles_AllFiles(t *testing.T) {
	mock := &mockFileService{
		ListByProjectIDFn: func(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
			return []domain.FileMetadata{
				{ID: "1", Category: domain.CategoryDocument, FileName: "doc.pdf"},
				{ID: "2", Category: domain.CategoryPhoto, FileName: "photo.jpg"},
			}, nil
		},
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("GET", "/projects/proj-123/files", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", w.Code)
	}
}

func TestListProjectFiles_FilterByDocument(t *testing.T) {
	mock := &mockFileService{
		ListByProjectIDFn: func(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
			return []domain.FileMetadata{
				{ID: "1", Category: domain.CategoryDocument, FileName: "doc.pdf"},
				{ID: "2", Category: domain.CategoryPhoto, FileName: "photo.jpg"},
			}, nil
		},
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("GET", "/projects/proj-123/files?category=document", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", w.Code)
	}
}

func TestListProjectFiles_FilterByPhoto(t *testing.T) {
	mock := &mockFileService{
		ListByProjectIDFn: func(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
			return []domain.FileMetadata{
				{ID: "1", Category: domain.CategoryDocument, FileName: "doc.pdf"},
				{ID: "2", Category: domain.CategoryPhoto, FileName: "photo.jpg"},
			}, nil
		},
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("GET", "/projects/proj-123/files?category=photo", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", w.Code)
	}
}

func TestListProjectFiles_Error(t *testing.T) {
	mock := &mockFileService{
		ListByProjectIDFn: func(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
			return nil, errors.New("db error")
		},
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("GET", "/projects/proj-123/files", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d", w.Code)
	}
}
