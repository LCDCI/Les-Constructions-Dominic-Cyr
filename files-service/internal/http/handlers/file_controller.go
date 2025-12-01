package handlers

import (
	"files-service/internal/domain"
	"files-service/internal/model"
	"io"
	"net/http"

	"github.com/gin-gonic/gin"
)

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
}

// Allowed document MIME types for Ticket CDC-121 (documents only)
var allowedDocumentTypes = map[string]bool{
	"application/pdf": true,
	"application/vnd.openxmlformats-officedocument.wordprocessingml.document": true, // DOCX
	"text/plain":               true,
	"application/octet-stream": true,
	"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": true, // XLSX
}

func (fc *FileController) upload(c *gin.Context) {
	file, err := c.FormFile("file")
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "file not provided or invalid form-data"})
		return
	}

	// Validate MIME type (documents only for CDC-121)
	contentType := file.Header.Get("Content-Type")
	if !allowedDocumentTypes[contentType] {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "Unsupported file type. Allowed: PDF, DOCX, TXT, XLSX.",
		})
		return
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

	category := c.PostForm("category")
	if category == "" {
		category = string(domain.CategoryOther)
	}

	input := domain.FileUploadInput{
		FileName:    file.Filename,
		ContentType: contentType,
		Category:    domain.FileCategory(category),
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

func (fc *FileController) delete(c *gin.Context) {
	id := c.Param("id")

	err := fc.s.Delete(c.Request.Context(), id)
	if err != nil {
		c.Error(err)
		return
	}

	c.Status(http.StatusNoContent)
}
