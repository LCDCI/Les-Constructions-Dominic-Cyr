package domain

import "time"

type FileMetadata struct {
	ID          string
	FileName    string
	ContentType string
	Category    FileCategory
	ProjectID   string
	UploadedBy  string
	Url         string
	CreatedAt   time.Time
}
