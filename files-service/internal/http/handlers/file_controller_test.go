package handlers_test

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"mime/multipart"
	"net/http"
	"net/http/httptest"
	"net/textproto"
	"strings"
	"testing"

	"files-service/internal/domain"
	"files-service/internal/http/handlers"
	"files-service/internal/http/middleware"

	"github.com/gin-gonic/gin"
)

type mockFileService struct {
	UploadFn    func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error)
	GetFn       func(ctx context.Context, id string) ([]byte, string, error)
	DeleteFn    func(ctx context.Context, id string, deletedBy string) error
	ArchiveFn   func(ctx context.Context, id string, archivedBy string) error
	UnarchiveFn func(ctx context.Context, id string) error

	ListByProjectIDFn              func(ctx context.Context, projectID string) ([]domain.FileMetadata, error)
	ListArchivedByProjectIDFn      func(ctx context.Context, projectID string) ([]domain.FileMetadata, error)
	ReconcileStorageWithDatabaseFn func(ctx context.Context, projectID string) (int, error)
}

func (m *mockFileService) Upload(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
	return m.UploadFn(ctx, in)
}
func (m *mockFileService) Get(ctx context.Context, id string) ([]byte, string, error) {
	return m.GetFn(ctx, id)
}
func (m *mockFileService) Delete(ctx context.Context, id string, deletedBy string) error {
	return m.DeleteFn(ctx, id, deletedBy)
}
func (m *mockFileService) Archive(ctx context.Context, id string, archivedBy string) error {
	if m.ArchiveFn != nil {
		return m.ArchiveFn(ctx, id, archivedBy)
	}
	return nil
}
func (m *mockFileService) Unarchive(ctx context.Context, id string) error {
	if m.UnarchiveFn != nil {
		return m.UnarchiveFn(ctx, id)
	}
	return nil
}
func (m *mockFileService) ListByProjectID(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
	if m.ListByProjectIDFn != nil {
		return m.ListByProjectIDFn(ctx, projectID)
	}
	return nil, nil
}
func (m *mockFileService) ListArchivedByProjectID(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
	if m.ListArchivedByProjectIDFn != nil {
		return m.ListArchivedByProjectIDFn(ctx, projectID)
	}
	return nil, nil
}
func (m *mockFileService) AutoReconcileAllProjects(ctx context.Context) error {
	return nil
}
func (m *mockFileService) ReconcileStorageWithDatabase(ctx context.Context, projectID string) (int, error) {
	if m.ReconcileStorageWithDatabaseFn != nil {
		return m.ReconcileStorageWithDatabaseFn(ctx, projectID)
	}
	return 0, nil
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

func TestUpload_Photo_WebP_Success(t *testing.T) {
	mock := &mockFileService{
		UploadFn: func(ctx context.Context, in domain.FileUploadInput) (domain.FileMetadata, error) {
			return domain.FileMetadata{ID: "webp-photo"}, nil
		},
	}

	router := setupRouter(mock)
	req := multipartRequest(t, "img.webp", "image/webp", []byte("webpdata"), string(domain.CategoryPhoto), "proj-44")
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

func TestUpload_UnsupportedPhotoType_Failure(t *testing.T) {
	router := setupRouter(&mockFileService{})

	req := multipartRequest(t, "data.json", "application/json", []byte("{}"), string(domain.CategoryPhoto), "proj-123")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d. Response: %s", w.Code, w.Body.String())
	}
}

func TestUpload_UnsupportedDocumentType_Failure(t *testing.T) {
	router := setupRouter(&mockFileService{})

	req := multipartRequest(t, "image.png", "image/png", []byte("data"), string(domain.CategoryDocument), "proj-123")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d. Response: %s", w.Code, w.Body.String())
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

// 🚀 NEW TEST: Successfully list photos by project ID
func TestListProjectFiles_Success(t *testing.T) {
	expectedMetadata := []domain.FileMetadata{
		{ID: "p1", FileName: "photo1.jpg", Category: domain.CategoryPhoto},
		{ID: "p2", FileName: "photo2.jpg", Category: domain.CategoryPhoto},
	}

	mock := &mockFileService{
		ListByProjectIDFn: func(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
			if projectID == "proj-A" {
				return expectedMetadata, nil
			}
			return nil, nil
		},
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("GET", "/projects/proj-A/files", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d. Body: %s", w.Code, w.Body.String())
	}

	var actualMetadata []domain.FileMetadata
	if err := json.Unmarshal(w.Body.Bytes(), &actualMetadata); err != nil {
		t.Fatalf("failed to unmarshal response: %v", err)
	}

	if len(actualMetadata) != 2 {
		t.Fatalf("expected 2 items, got %d", len(actualMetadata))
	}
	if actualMetadata[0].ID != "p1" {
		t.Fatalf("expected ID p1, got %s", actualMetadata[0].ID)
	}
}

// ❌ NEW TEST: Listing fails due to internal service error
func TestListProjectFiles_InternalError(t *testing.T) {
	internalErr := errors.New("service layer failure")

	mock := &mockFileService{
		ListByProjectIDFn: func(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
			return nil, internalErr
		},
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("GET", "/projects/proj-B/files", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d. Body: %s", w.Code, w.Body.String())
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
		DeleteFn: func(ctx context.Context, id string, deletedBy string) error { return nil },
	}

	router := setupRouter(mock)
	body := strings.NewReader(`{"deletedBy":"user-456"}`)
	req := httptest.NewRequest("DELETE", "/files/123", body)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusNoContent {
		t.Fatalf("expected 204, got %d", w.Code)
	}
}

func TestDelete_NotFound(t *testing.T) {
	mock := &mockFileService{
		DeleteFn: func(ctx context.Context, id string, deletedBy string) error { return domain.ErrNotFound },
	}

	router := setupRouter(mock)
	body := strings.NewReader(`{"deletedBy":"user-456"}`)
	req := httptest.NewRequest("DELETE", "/files/xxx", body)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusNotFound {
		t.Fatalf("expected 404, got %d", w.Code)
	}
}

func TestDelete_InternalError(t *testing.T) {
	mock := &mockFileService{
		DeleteFn: func(ctx context.Context, id string, deletedBy string) error { return errors.New("fail") },
	}

	router := setupRouter(mock)
	body := strings.NewReader(`{"deletedBy":"user-456"}`)
	req := httptest.NewRequest("DELETE", "/files/xxx", body)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d", w.Code)
	}
}

func TestListProjectDocuments_Success(t *testing.T) {
	mock := &mockFileService{
		ListByProjectIDFn: func(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
			return []domain.FileMetadata{
				{ID: "1", FileName: "doc1.pdf", Category: domain.CategoryDocument},
				{ID: "2", FileName: "photo1.jpg", Category: domain.CategoryPhoto},
				{ID: "3", FileName: "doc2.docx", Category: domain.CategoryDocument},
			}, nil
		},
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("GET", "/projects/proj-123/documents", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", w.Code)
	}

	// Verify response contains only documents
	body := w.Body.String()
	if !strings.Contains(body, "doc1.pdf") {
		t.Errorf("expected response to contain doc1.pdf")
	}
	if !strings.Contains(body, "doc2.docx") {
		t.Errorf("expected response to contain doc2.docx")
	}
	if strings.Contains(body, "photo1.jpg") {
		t.Errorf("expected response to NOT contain photo1.jpg")
	}
}

func TestListProjectDocuments_EmptyResult(t *testing.T) {
	mock := &mockFileService{
		ListByProjectIDFn: func(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
			return []domain.FileMetadata{}, nil
		},
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("GET", "/projects/proj-456/documents", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", w.Code)
	}

	body := w.Body.String()
	if body != "[]\n" && body != "[]" {
		t.Errorf("expected empty array, got %s", body)
	}
}

func TestListProjectDocuments_InternalError(t *testing.T) {
	mock := &mockFileService{
		ListByProjectIDFn: func(ctx context.Context, projectID string) ([]domain.FileMetadata, error) {
			return nil, errors.New("database error")
		},
	}

	router := setupRouter(mock)
	req := httptest.NewRequest("GET", "/projects/proj-789/documents", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d", w.Code)
	}
}

func TestSoftDelete_Success(t *testing.T) {
	deleteCalledWithParams := false

	mock := &mockFileService{
		DeleteFn: func(ctx context.Context, id string, deletedBy string) error {
			deleteCalledWithParams = true
			if id != "file-123" {
				t.Errorf("expected id 'file-123', got '%s'", id)
			}
			if deletedBy != "user-456" {
				t.Errorf("expected deletedBy 'user-456', got '%s'", deletedBy)
			}
			return nil
		},
	}

	router := setupRouter(mock)

	body := strings.NewReader(`{"deletedBy":"user-456"}`)
	req := httptest.NewRequest("DELETE", "/files/file-123", body)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusNoContent {
		t.Fatalf("expected 204, got %d", w.Code)
	}

	if !deleteCalledWithParams {
		t.Fatal("Delete was not called with correct parameters")
	}
}

func TestSoftDelete_MissingDeletedBy(t *testing.T) {
	router := setupRouter(&mockFileService{})

	req := httptest.NewRequest("DELETE", "/files/file-123", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", w.Code)
	}
}

func TestSoftDelete_EmptyDeletedBy(t *testing.T) {
	router := setupRouter(&mockFileService{})

	req := httptest.NewRequest("DELETE", "/files/file-123?deletedBy=", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", w.Code)
	}
}

func TestSoftDelete_FileNotFound(t *testing.T) {
	mock := &mockFileService{
		DeleteFn: func(ctx context.Context, id string, deletedBy string) error {
			return domain.ErrNotFound
		},
	}

	router := setupRouter(mock)

	body := strings.NewReader(`{"deletedBy":"user-456"}`)
	req := httptest.NewRequest("DELETE", "/files/nonexistent", body)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusNotFound {
		t.Fatalf("expected 404, got %d", w.Code)
	}
}

func TestSoftDelete_InternalError(t *testing.T) {
	mock := &mockFileService{
		DeleteFn: func(ctx context.Context, id string, deletedBy string) error {
			return errors.New("database error")
		},
	}

	router := setupRouter(mock)

	body := strings.NewReader(`{"deletedBy":"user-456"}`)
	req := httptest.NewRequest("DELETE", "/files/file-123", body)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d", w.Code)
	}
}

// Archive Endpoint Tests
func TestArchive_Success(t *testing.T) {
	archiveCalled := false
	archiveFileID := ""

	mock := &mockFileService{
		ArchiveFn: func(ctx context.Context, id string, archivedBy string) error {
			archiveCalled = true
			archiveFileID = id
			return nil
		},
	}

	router := setupRouter(mock)

	body := strings.NewReader(`{"archivedBy":"user-456-uuid"}`)
	req := httptest.NewRequest("POST", "/files/file-123/archive", body)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusNoContent {
		t.Fatalf("expected 204, got %d", w.Code)
	}

	if !archiveCalled {
		t.Fatalf("expected archive to be called")
	}

	if archiveFileID != "file-123" {
		t.Fatalf("expected file-123, got %s", archiveFileID)
	}
}

func TestArchive_MissingArchivedBy(t *testing.T) {
	mock := &mockFileService{
		ArchiveFn: func(ctx context.Context, id string, archivedBy string) error {
			return nil
		},
	}

	router := setupRouter(mock)

	body := strings.NewReader(`{"archivedBy":""}`)
	req := httptest.NewRequest("POST", "/files/file-123/archive", body)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", w.Code)
	}
}

func TestArchive_InvalidJSON(t *testing.T) {
	mock := &mockFileService{
		ArchiveFn: func(ctx context.Context, id string, archivedBy string) error {
			return nil
		},
	}

	router := setupRouter(mock)

	body := strings.NewReader(`invalid json`)
	req := httptest.NewRequest("POST", "/files/file-123/archive", body)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", w.Code)
	}
}

func TestArchive_ServiceError(t *testing.T) {
	mock := &mockFileService{
		ArchiveFn: func(ctx context.Context, id string, archivedBy string) error {
			return errors.New("service error")
		},
	}

	router := setupRouter(mock)

	body := strings.NewReader(`{"archivedBy":"user-456-uuid"}`)
	req := httptest.NewRequest("POST", "/files/file-123/archive", body)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d", w.Code)
	}
}

func TestArchive_FileNotFound(t *testing.T) {
	mock := &mockFileService{
		ArchiveFn: func(ctx context.Context, id string, archivedBy string) error {
			return domain.ErrNotFound
		},
	}

	router := setupRouter(mock)

	body := strings.NewReader(`{"archivedBy":"user-456-uuid"}`)
	req := httptest.NewRequest("POST", "/files/nonexistent/archive", body)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusNotFound {
		t.Fatalf("expected 404, got %d", w.Code)
	}
}

// Unarchive Endpoint Tests
func TestUnarchive_Success(t *testing.T) {
	unarchiveCalled := false
	unarchiveFileID := ""

	mock := &mockFileService{
		UnarchiveFn: func(ctx context.Context, id string) error {
			unarchiveCalled = true
			unarchiveFileID = id
			return nil
		},
	}

	router := setupRouter(mock)

	req := httptest.NewRequest("POST", "/files/file-123/unarchive", nil)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusNoContent {
		t.Fatalf("expected 204, got %d", w.Code)
	}

	if !unarchiveCalled {
		t.Fatalf("expected unarchive to be called")
	}

	if unarchiveFileID != "file-123" {
		t.Fatalf("expected file-123, got %s", unarchiveFileID)
	}
}

func TestUnarchive_FileNotFound(t *testing.T) {
	mock := &mockFileService{
		UnarchiveFn: func(ctx context.Context, id string) error {
			return domain.ErrNotFound
		},
	}

	router := setupRouter(mock)

	req := httptest.NewRequest("POST", "/files/nonexistent/unarchive", nil)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusNotFound {
		t.Fatalf("expected 404, got %d", w.Code)
	}
}

func TestUnarchive_ServiceError(t *testing.T) {
	mock := &mockFileService{
		UnarchiveFn: func(ctx context.Context, id string) error {
			return errors.New("database error")
		},
	}

	router := setupRouter(mock)

	req := httptest.NewRequest("POST", "/files/file-123/unarchive", nil)
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d", w.Code)
	}
}
