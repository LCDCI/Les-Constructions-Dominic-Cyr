package domain

import "errors"

var (
	ErrNotFound      = NewDomainError("file not found")
	ErrValidation    = NewDomainError("invalid input provided")
	ErrStorageFailed = NewDomainError("failed to process file storage")
	ErrInternal      = errors.New("internal error")
)

type Error struct {
	Msg string
}

func (e *Error) Error() string { return e.Msg }

func NewDomainError(msg string) *Error {
	return &Error{Msg: msg}
}
