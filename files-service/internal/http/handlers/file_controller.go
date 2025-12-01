package handlers

import (
	"files-service/internal/domain"
	"files-service/internal/model"
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
	// Use multipart/form-data for uploads
	r.POST("/files", fc.upload)
	r.GET("/files/:id", fc.download)
	r.DELETE("/files/:id", fc.delete)
}

func (fc *FileController) upload(c *gin.Context) {

	file, err := c.FormFile("file")
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "file not provided or invalid form-data"})
		return
	}

	categoryStr := c.PostForm("category")
	if categoryStr == "" {
		categoryStr = string(domain.CategoryOther) // Default to OTHER if not provided
	}

	src, err := file.Open()
	if err != nil {
		c.Error(domain.NewDomainError("failed to open file")) // Use domain error
		return
	}
	defer src.Close()

	fileData := make([]byte, file.Size)
	_, err = src.Read(fileData)
	if err != nil {
		c.Error(domain.NewDomainError("failed to read file data"))
		return
	}

	input := domain.FileUploadInput{
		FileName:    file.Filename,
		ContentType: file.Header.Get("Content-Type"),
		Category:    domain.FileCategory(categoryStr),
		Data:        fileData,
	}

	metadata, err := fc.s.Upload(c.Request.Context(), input)
	if err != nil {
		c.Error(err)
		return
	}

	resp := model.FileUploadResponse{
		FileId:   metadata.ID,
		FileName: metadata.FileName,
		URL:      metadata.Url,
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

	if err := fc.s.Delete(c.Request.Context(), id); err != nil {
		c.Error(err)
		return
	}

	c.Status(http.StatusNoContent)
}
