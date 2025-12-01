package model

type FileUploadResponse struct {
	FileId     string `json:"fileId"`
	ProjectId  string `json:"projectId"`
	FileName   string `json:"fileName"`
	UploadedBy string `json:"uploadedBy"`
	URL        string `json:"url"`
}
