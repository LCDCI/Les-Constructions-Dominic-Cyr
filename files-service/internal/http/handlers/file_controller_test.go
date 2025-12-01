package handlers_test

import (
	"bytes"
	"context"
	"errors"
	"files-service/internal/domain"
	"files-service/internal/http/handlers"
	"files-service/internal/http/middleware"
	"mime/multipart"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
)

type mockFileService struct {
	UploadFn func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error)
	GetFn    func(ctx context.Context, id string) ([]byte, string, error)
	DeleteFn func(ctx context.Context, id string) error
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

func setupRouter(s domain.FileService) *gin.Engine {
	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.Use(middleware.ErrorHandler)
	ctrl := handlers.NewFileController(s)
	ctrl.RegisterRoutes(r)
	return r
}

// Multipart request helper
func createMultipartRequest(t *testing.T, filename string, fileData []byte, category string) (*http.Request, string) {
	body := &bytes.Buffer{}
	writer := multipart.NewWriter(body)

	if filename != "" {
		part, err := writer.CreateFormFile("file", filename)
		if err != nil {
			t.Fatalf("failed to create form file: %v", err)
		}
		part.Write(fileData)
	}

	if category != "" {
		writer.WriteField("category", category)
	}

	// REQUIRED fields for UploadInput validation
	writer.WriteField("projectId", "project-123")
	writer.WriteField("uploadedBy", "user-123")

	writer.Close()

	req := httptest.NewRequest("POST", "/files", body)
	req.Header.Set("Content-Type", writer.FormDataContentType())
	return req, writer.FormDataContentType()
}

//
// ------------------ UPLOAD TESTS ------------------
//

func TestUpload_Success(t *testing.T) {

	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {

			if in.FileName != "example.txt" {
				t.Fatalf("wrong FileName")
			}
			if in.Category != domain.CategoryOther {
				t.Fatalf("expected OTHER category")
			}

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

	req, _ := createMultipartRequest(t, "example.txt", []byte("hello"), "")
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d", w.Code)
	}
}

func TestUpload_Success_WithCategory(t *testing.T) {

	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {

			if in.Category != domain.CategoryDocument {
				t.Fatalf("expected DOCUMENT category")
			}

			return domain.FileMetadata{
				ID:       "123",
				FileName: in.FileName,
				Url:      "/files/123",
			}, nil
		},
	}

	router := setupRouter(mock)

	req, _ := createMultipartRequest(t, "doc.pdf", []byte("pdf"), string(domain.CategoryDocument))
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d", w.Code)
	}
}

func TestUpload_NoFile_BadRequest(t *testing.T) {

	mock := &mockFileService{}
	router := setupRouter(mock)

	req := httptest.NewRequest("POST", "/files", bytes.NewBufferString("a=1"))
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

	req, _ := createMultipartRequest(t, "bad.txt", []byte("data"), "")
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

	req, _ := createMultipartRequest(t, "x.txt", []byte("data"), "")
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

	req, _ := createMultipartRequest(t, "x.txt", []byte("data"), "")
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d", w.Code)
	}
}

//
// ------------------ DOWNLOAD TESTS ------------------
//

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

//
// ------------------ DELETE TESTS ------------------
//

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
