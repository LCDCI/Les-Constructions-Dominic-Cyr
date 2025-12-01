package domain

type FileUploadInput struct {
	FileName    string
	ContentType string
	Category    FileCategory
	ProjectID   string
	UploadedBy  string
	Data        []byte
}
