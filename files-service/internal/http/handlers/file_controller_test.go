package handlers_test

import (
	"bytes"
	"context"
	"errors"
	"files-service/internal/domain"
	"files-service/internal/http/handlers"
	"files-service/internal/http/middleware" // Import middleware
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
	// Register the error handler middleware for proper error testing
	r.Use(middleware.ErrorHandler)
	ctrl := handlers.NewFileController(s)
	ctrl.RegisterRoutes(r)
	return r
}

// Helper to create a multipart request
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

	writer.Close()

	req := httptest.NewRequest("POST", "/files", body)
	req.Header.Set("Content-Type", writer.FormDataContentType())
	return req, writer.FormDataContentType()
}

//
// ----------- UPLOAD TESTS -----------
//

// SUCCESS
func TestUpload_Success(t *testing.T) {

	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
			if in.FileName != "example.txt" || in.Category != domain.CategoryOther {
				t.Fatalf("wrong input to service")
			}
			return domain.FileMetadata{ID: "123", FileName: in.FileName, Url: "/files/123"}, nil
		},
	}
	router := setupRouter(mock)

	req, _ := createMultipartRequest(t, "example.txt", []byte("hello world"), "") // Category defaults to OTHER
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d", w.Code)
	}
}

// SUCCESS - With explicit category
func TestUpload_Success_WithCategory(t *testing.T) {
	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
			if in.Category != domain.CategoryDocument {
				t.Fatalf("wrong category: expected DOCUMENT, got %s", in.Category)
			}
			return domain.FileMetadata{ID: "123", FileName: in.FileName, Url: "/files/123"}, nil
		},
	}
	router := setupRouter(mock)

	req, _ := createMultipartRequest(t, "doc.pdf", []byte("pdf data"), string(domain.CategoryDocument))
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d", w.Code)
	}
}

// NEGATIVE – missing file field
func TestUpload_NoFile_BadRequest(t *testing.T) {
	mock := &mockFileService{}
	router := setupRouter(mock)

	// Use a regular form, not a multipart form with "file"
	req := httptest.NewRequest("POST", "/files", bytes.NewBufferString("key=value"))
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d. Body: %s", w.Code, w.Body.String())
	}
}

// NEGATIVE – Service returns ErrValidation -> 400 Bad Request
func TestUpload_ValidationError_BadRequest(t *testing.T) {

	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
			return domain.FileMetadata{}, domain.ErrValidation
		},
	}

	router := setupRouter(mock)
	req, _ := createMultipartRequest(t, "x.txt", []byte("data"), "")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d. Body: %s", w.Code, w.Body.String())
	}
}

// NEGATIVE – Service returns ErrStorageFailed -> 503 Service Unavailable
func TestUpload_StorageError_ServiceUnavailable(t *testing.T) {

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
		t.Fatalf("expected 503, got %d. Body: %s", w.Code, w.Body.String())
	}
}

// NEGATIVE – Service returns generic internal error -> 500 Internal Server Error
func TestUpload_GenericInternalError(t *testing.T) {

	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
			return domain.FileMetadata{}, errors.New("a low-level DB driver error")
		},
	}

	router := setupRouter(mock)
	req, _ := createMultipartRequest(t, "x.txt", []byte("data"), "")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	// Should be mapped to 500 by the ErrorHandler middleware for non-domain errors
	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d. Body: %s", w.Code, w.Body.String())
	}
}

//
// ----------- DOWNLOAD TESTS -----------
//

// SUCCESS
func TestDownload_Success(t *testing.T) {

	mock := &mockFileService{
		GetFn: func(ctx context.Context, id string) ([]byte, string, error) {
			return []byte("content"), "text/plain", nil
		},
	}

	router := setupRouter(mock)

	req := httptest.NewRequest("GET", "/files/123", nil)
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", w.Code)
	}
	if w.Header().Get("Content-Type") != "text/plain" {
		t.Fatalf("wrong Content-Type header: %s", w.Header().Get("Content-Type"))
	}
	if w.Body.String() != "content" {
		t.Fatalf("wrong response body")
	}
}

// NEGATIVE – not found -> 404 Not Found
func TestDownload_NotFound(t *testing.T) {

	mock := &mockFileService{
		GetFn: func(ctx context.Context, id string) ([]byte, string, error) {
			return nil, "", domain.ErrNotFound
		},
	}

	router := setupRouter(mock)

	req := httptest.NewRequest("GET", "/files/zzz", nil)
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusNotFound {
		t.Fatalf("expected 404, got %d. Body: %s", w.Code, w.Body.String())
	}
}

// NEGATIVE – internal service error -> 500 Internal Server Error
func TestDownload_InternalError(t *testing.T) {

	mock := &mockFileService{
		GetFn: func(ctx context.Context, id string) ([]byte, string, error) {
			return nil, "", errors.New("repository connection failed")
		},
	}

	router := setupRouter(mock)

	req := httptest.NewRequest("GET", "/files/zzz", nil)
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d. Body: %s", w.Code, w.Body.String())
	}
}

//
// ----------- DELETE TESTS -----------
//

// SUCCESS
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
	if w.Body.String() != "" {
		t.Fatalf("expected empty body, got %s", w.Body.String())
	}
}

// NEGATIVE – not found -> 404 Not Found
func TestDelete_NotFound(t *testing.T) {

	mock := &mockFileService{
		DeleteFn: func(ctx context.Context, id string) error { return domain.ErrNotFound },
	}

	router := setupRouter(mock)

	req := httptest.NewRequest("DELETE", "/files/xxx", nil)
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusNotFound {
		t.Fatalf("expected 404, got %d. Body: %s", w.Code, w.Body.String())
	}
}

// NEGATIVE – internal service error -> 500 Internal Server Error
func TestDelete_InternalError(t *testing.T) {

	mock := &mockFileService{
		DeleteFn: func(ctx context.Context, id string) error { return errors.New("generic deletion error") },
	}

	router := setupRouter(mock)

	req := httptest.NewRequest("DELETE", "/files/xxx", nil)
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d. Body: %s", w.Code, w.Body.String())
	}
}
