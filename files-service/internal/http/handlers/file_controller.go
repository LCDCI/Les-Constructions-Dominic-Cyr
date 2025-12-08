package handlers

import (
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
}

type FileController struct {
	s domain.FileService
}

func NewFileController(s domain.FileService) *FileController {
	return &FileController{s: s}
}

func (fc *FileController) RegisterRoutes(r *gin.Engine) {
	r.POST("/files", fc.upload)
	r.GET("/files/:id", fc.download)
	r.DELETE("/files/:id", fc.delete)
	r.GET("/projects/:projectId/files", fc.listProjectFiles)
}

func (fc *FileController) upload(c *gin.Context) {
	file, err := c.FormFile("file")
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "file not provided or invalid form-data"})
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

	input := domain.FileUploadInput{
		FileName:    file.Filename,
		ContentType: contentType,
		Category:    category,
		ProjectID:   c.PostForm("projectId"),
		UploadedBy:  c.PostForm("uploadedBy"),
		Data:        fileBytes,
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

	c.Data(http.StatusOK, contentType, data)
}

func (fc *FileController) listProjectFiles(c *gin.Context) {
	projectID := c.Param("projectId")

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

func (fc *FileController) delete(c *gin.Context) {
	id := c.Param("id")

	err := fc.s.Delete(c.Request.Context(), id)
	if err != nil {
		c.Error(err)
		return
	}

	c.Status(http.StatusNoContent)
}
