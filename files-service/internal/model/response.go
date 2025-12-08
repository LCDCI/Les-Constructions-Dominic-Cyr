package model

type FileUploadResponse struct {
	FileId      string `json:"fileId"`
	ProjectId   string `json:"projectId"`
	FileName    string `json:"fileName"`
	ContentType string `json:"contentType"`
	Category    string `json:"category"`
	UploadedBy  string `json:"uploadedBy"`
	URL         string `json:"url"`
}
