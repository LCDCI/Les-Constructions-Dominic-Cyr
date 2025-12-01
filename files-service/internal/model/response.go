package model

type FileUploadResponse struct {
	FileId   string `json:"fileId"`
	FileName string `json:"fileName"`
	URL      string `json:"url"`
}
