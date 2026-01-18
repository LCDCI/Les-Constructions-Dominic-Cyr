package domain

import "time"

type File struct {
	ID          string
	FileName    string
	ContentType string
	Category    FileCategory
	ProjectID   string
	UploadedBy  string
	Size        int64
	ObjectKey   string
	CreatedAt   time.Time
	IsActive    bool
	DeletedAt   *time.Time
	DeletedBy   string
	IsArchived  bool
	ArchivedAt  *time.Time
	ArchivedBy  string
}
