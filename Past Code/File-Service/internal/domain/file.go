package domain

import "time"

type File struct {
	ID          string
	FileName    string
	ContentType string
	Category    FileCategory
	ProjectID   string
	UploadedBy  string
	UploaderRole *string // NULL for existing files, populated for new files
	Size        int64
	ObjectKey   string
	CreatedAt   time.Time
	IsActive    bool
	DeletedAt   *time.Time
	DeletedBy   string
}
