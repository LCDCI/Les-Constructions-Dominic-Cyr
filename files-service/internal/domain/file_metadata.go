package domain

type FileMetadata struct {
	ID          string       `json:"id"`
	FileName    string       `json:"fileName"`
	ContentType string       `json:"contentType"`
	Category    FileCategory `json:"category"`
	ProjectID   string       `json:"projectId"`
	UploadedBy  string       `json:"uploadedBy"`
	Url         string       `json:"url"`
}
