package domain

type FileCategory string

const (
	CategoryDocument FileCategory = "DOCUMENT"
	CategoryPhoto    FileCategory = "PHOTO"
	CategoryOther    FileCategory = "OTHER"
)

func (fc FileCategory) Validate() bool {
	switch fc {
	case CategoryDocument, CategoryPhoto, CategoryOther:
		return true
	default:
		return false
	}
}
