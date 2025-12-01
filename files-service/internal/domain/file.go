package domain

import (
	"time"
)

type File struct {
	ID          string
	FileName    string
	ContentType string
	Category    FileCategory
	Size        int64
	ObjectKey   string
	CreatedAt   time.Time
}

type FileMetadata struct {
	ID          string
	FileName    string
	ContentType string
	Category    FileCategory // Use domain type
	Url         string
}

type FileUploadInput struct {
	FileName    string
	ContentType string
	Category    FileCategory // Use domain type
	Data        []byte
}

var (
	ErrNotFound      = NewDomainError("file not found")
	ErrValidation    = NewDomainError("invalid input provided")
	ErrStorageFailed = NewDomainError("failed to process file storage")
)

type DomainError struct {
	Msg string
}

func (e *DomainError) Error() string {
	return e.Msg
}

func NewDomainError(msg string) *DomainError {
	return &DomainError{Msg: msg}
}
