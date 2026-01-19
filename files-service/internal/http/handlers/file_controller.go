package handlers

import (
	"fmt"
	"files-service/internal/domain"
	"files-service/internal/model"
	"io"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

var allowedDocumentTypes = map[string]bool{
	"application/pdf": true,
	"application/vnd.openxmlformats-officedocument.wordprocessingml.document": true,
	"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":       true,
	"text/plain":               true,
	"application/json":         true,
	"application/octet-stream": true,
}

var allowedImageTypes = map[string]bool{
	"image/png":  true,
	"image/jpeg": true,
	"image/webp": true,
	"video/mp4":  true,
}

const maxUploadBytes = 25 * 1024 * 1024 // 25 MiB hard cap per upload

type FileController struct {
	s domain.FileService
}

func NewFileController(s domain.FileService) *FileController {
	return &FileController{s: s}
}

func (fc *FileController) RegisterRoutes(r *gin.Engine) {
	r.POST("/files", fc.upload)
	r.GET("/files/:id", fc.download)
	r.GET("/files/:id/download", fc.downloadWithRoleCheck)
	r.DELETE("/files/:id", fc.delete)
	r.POST("/files/:id/archive", fc.archive)
	r.POST("/files/:id/unarchive", fc.unarchive)
	r.GET("/projects/:projectId/files", fc.listProjectFiles)
	r.GET("/projects/:projectId/documents", fc.listProjectDocuments)
	r.GET("/projects/:projectId/documents/zip", fc.downloadProjectDocumentsZip)
	r.POST("/admin/reconcile/:projectId", fc.reconcile)
	r.POST("/upload", fc.UploadReport)
}

func (fc *FileController) upload(c *gin.Context) {
	file, err := c.FormFile("file")
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "file not provided or invalid form-data"})
		return
	}

	if file.Size > maxUploadBytes {
		c.JSON(http.StatusRequestEntityTooLarge, gin.H{"error": "file too large (max 25MB)"})
		return
	}

	contentType := file.Header.Get("Content-Type")

	categoryRaw := c.PostForm("category")
	if categoryRaw == "" {
		categoryRaw = string(domain.CategoryOther)
	}

	category := domain.FileCategory(strings.ToUpper(categoryRaw))
	if !category.Validate() {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid category. Use DOCUMENT, PHOTO, or OTHER"})
		return
	}

	switch category {
	case domain.CategoryDocument:
		if !allowedDocumentTypes[contentType] {
			c.JSON(http.StatusBadRequest, gin.H{
				"error": "Unsupported document type. Allowed: PDF, DOCX, XLSX, TXT, JSON",
			})
			return
		}
	case domain.CategoryPhoto:
		if !allowedImageTypes[contentType] {
			c.JSON(http.StatusBadRequest, gin.H{
				"error": "Unsupported photo type. Allowed: PNG, JPG, JPEG, WEBP",
			})
			return
		}
	}

	src, err := file.Open()
	if err != nil {
		c.Error(domain.ErrInternal)
		return
	}
	defer src.Close()

	fileBytes, err := io.ReadAll(src)
	if err != nil {
		c.Error(domain.ErrInternal)
		return
	}

	// Get uploadedBy - required field
	uploadedBy := strings.TrimSpace(c.PostForm("uploadedBy"))
	if uploadedBy == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "uploadedBy is required"})
		return
	}

	// Get uploaderRole from form data - required for role-based filtering
	uploaderRoleStr := strings.TrimSpace(c.PostForm("uploaderRole"))
	if uploaderRoleStr == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "uploaderRole is required"})
		return
	}
	
	// Validate role is one of the allowed values
	validRoles := map[string]bool{
		"OWNER":       true,
		"CONTRACTOR":  true,
		"SALESPERSON": true,
		"CUSTOMER":    true,
	}
	if !validRoles[strings.ToUpper(uploaderRoleStr)] {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid uploaderRole. Must be OWNER, CONTRACTOR, SALESPERSON, or CUSTOMER"})
		return
	}
	uploaderRole := strings.ToUpper(uploaderRoleStr)

	input := domain.FileUploadInput{
		FileName:     file.Filename,
		ContentType:  contentType,
		Category:     category,
		ProjectID:    strings.TrimSpace(c.PostForm("projectId")),
		UploadedBy:   uploadedBy,
		UploaderRole: &uploaderRole,
		Data:         fileBytes,
	}

	metadata, err := fc.s.Upload(c.Request.Context(), input)
	if err != nil {
		c.Error(err)
		return
	}

	resp := model.FileUploadResponse{
		FileId:     metadata.ID,
		ProjectId:  metadata.ProjectID,
		FileName:   metadata.FileName,
		UploadedBy: metadata.UploadedBy,
		URL:        metadata.Url,
	}

	c.JSON(http.StatusCreated, resp)
}

func (fc *FileController) download(c *gin.Context) {
	id := c.Param("id")

	data, contentType, err := fc.s.Get(c.Request.Context(), id)
	if err != nil {
		c.Error(err)
		return
	}

	// Support for video streaming with Range requests
	c.Header("Accept-Ranges", "bytes")
	c.Header("Content-Type", contentType)
	c.Header("Cache-Control", "public, max-age=31536000")

	// Handle Range request for video streaming
	rangeHeader := c.GetHeader("Range")
	if rangeHeader != "" && strings.HasPrefix(contentType, "video/") {
		// For now, serve full content with proper headers
		// Full Range implementation would require parsing and serving partial content
		c.Data(http.StatusOK, contentType, data)
		return
	}

	c.Data(http.StatusOK, contentType, data)
}

func (fc *FileController) listProjectFiles(c *gin.Context) {
	projectID := c.Param("projectId")

	archivedParam := c.Query("archived")

	var metadataList []domain.FileMetadata
	var err error
	if archivedParam == "true" {
		metadataList, err = fc.s.ListArchivedByProjectID(c.Request.Context(), projectID)
	} else {
		metadataList, err = fc.s.ListByProjectID(c.Request.Context(), projectID)
	}

	if err != nil {
		c.Error(err)
		return
	}

	photos := make([]domain.FileMetadata, 0)
	for _, metadata := range metadataList {
		if metadata.Category == domain.CategoryPhoto {
			photos = append(photos, metadata)
		}
	}

	c.JSON(http.StatusOK, photos)
}

func (fc *FileController) listProjectDocuments(c *gin.Context) {
	projectID := c.Param("projectId")
	role := c.Query("role")
	userId := c.Query("userId")

	// If role and userId are provided, use role-based filtering
	if role != "" && userId != "" {
		metadataList, err := fc.s.ListDocumentsByProjectIDAndRole(c.Request.Context(), projectID, role, userId)
		if err != nil {
			c.Error(err)
			return
		}
		c.JSON(http.StatusOK, metadataList)
		return
	}

	// Otherwise, return all documents (backward compatibility)
	metadataList, err := fc.s.ListByProjectID(c.Request.Context(), projectID)
	if err != nil {
		c.Error(err)
		return
	}

	documents := make([]domain.FileMetadata, 0)
	for _, metadata := range metadataList {
		if metadata.Category == domain.CategoryDocument {
			documents = append(documents, metadata)
		}
	}

	c.JSON(http.StatusOK, documents)
}

func (fc *FileController) downloadWithRoleCheck(c *gin.Context) {
	id := c.Param("id")
	role := c.Query("role")
	userId := c.Query("userId")

	if role == "" || userId == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "role and userId query parameters are required"})
		return
	}

	data, contentType, err := fc.s.GetWithRoleCheck(c.Request.Context(), id, role, userId)
	if err != nil {
		c.Error(err)
		return
	}

	// Set headers for download
	c.Header("Content-Type", contentType)
	c.Header("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s\"", "document"))
	c.Data(http.StatusOK, contentType, data)
}

func (fc *FileController) downloadProjectDocumentsZip(c *gin.Context) {
	projectID := c.Param("projectId")
	role := c.Query("role")
	userId := c.Query("userId")
	projectName := c.Query("projectName")

	if role == "" || userId == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "role and userId query parameters are required"})
		return
	}

	// Use projectName if provided, otherwise use projectId
	zipFileName := projectName
	if zipFileName == "" {
		zipFileName = fmt.Sprintf("project-%s", projectID)
	}
	zipFileName = strings.ReplaceAll(zipFileName, " ", "-") + "-documents.zip"

	zipData, err := fc.s.DownloadProjectDocumentsZip(c.Request.Context(), projectID, role, userId, projectName)
	if err != nil {
		c.Error(err)
		return
	}

	// Set headers for ZIP download
	c.Header("Content-Type", "application/zip")
	c.Header("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s\"", zipFileName))
	c.Data(http.StatusOK, "application/zip", zipData)
}

func (fc *FileController) delete(c *gin.Context) {
	id := c.Param("id")

	var request struct {
		DeletedBy string `json:"deletedBy"`
	}

	// Bind JSON from request body
	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request body or missing deletedBy field"})
		return
	}

	if request.DeletedBy == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "deletedBy field is required"})
		return
	}

	err := fc.s.Delete(c.Request.Context(), id, request.DeletedBy)
	if err != nil {
		c.Error(err)
		return
	}

	c.Status(http.StatusNoContent)
}

// archive handles archiving a file (owner-only permission required).
// Archives files instead of deleting them to maintain compliance and audit trails.
// The frontend should enforce owner-only permissions before calling this endpoint.
func (fc *FileController) archive(c *gin.Context) {
	id := c.Param("id")

	var request struct {
		ArchivedBy string `json:"archivedBy"`
	}

	// Bind JSON from request body
	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request body or missing archivedBy field"})
		return
	}

	if request.ArchivedBy == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "archivedBy field is required"})
		return
	}

	err := fc.s.Archive(c.Request.Context(), id, request.ArchivedBy)
	if err != nil {
		c.Error(err)
		return
	}

	c.Status(http.StatusNoContent)
}

// unarchive handles restoring a previously archived file.
// Restores archived files back to visible status so they can be used again.
func (fc *FileController) unarchive(c *gin.Context) {
	id := c.Param("id")

	err := fc.s.Unarchive(c.Request.Context(), id)
	if err != nil {
		c.Error(err)
		return
	}

	c.Status(http.StatusNoContent)
}

func (fc *FileController) reconcile(c *gin.Context) {
	projectID := c.Param("projectId")
	if projectID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "projectId is required"})
		return
	}

	synced, err := fc.s.ReconcileStorageWithDatabase(c.Request.Context(), projectID)
	if err != nil {
		c.Error(err)
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"message":         "reconciliation complete",
		"filesReconciled": synced,
		"projectId":       projectID,
	})
}

func (fc *FileController) UploadReport(c *gin.Context) {
    file, err := c.FormFile("file")
    if err != nil {
        c.JSON(http.StatusBadRequest, gin.H{"error": "File is required"})
        return
    }

    objectKey := c.PostForm("objectKey")
    contentType := c.PostForm("contentType")
    if contentType == "" {
        contentType = file.Header.Get("Content-Type")
    }

    src, err := file.Open()
    if err != nil {
        c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to open file"})
        return
    }
    defer src.Close()

    fileBytes, err := io.ReadAll(src)
    if err != nil {
        c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to read file content"})
        return
    }

    input := domain.FileUploadInput{
        FileName:    objectKey,
        ContentType: contentType,
        Category:    domain.CategoryDocument,
        ProjectID:   "SYSTEM_REPORTS", 
        UploadedBy:  "REPORT_SERVICE",
        Data:        fileBytes,
    }

    metadata, err := fc.s.Upload(c.Request.Context(), input)
    if err != nil {
        c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to save report to storage"})
        return
    }

    c.JSON(http.StatusOK, gin.H{
        "objectKey":    metadata.ID,
        "contentType":  contentType,
        "size":         len(fileBytes),
        "originalName": file.Filename,
    })
}
