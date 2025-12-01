package model

type FileUploadRequest struct {
	FileName    string `json:"fileName"`
	ContentType string `json:"contentType"`
	Category    string `json:"category"`
}
