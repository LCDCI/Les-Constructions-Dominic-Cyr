package domain

type FileCategory string

const (
	CategoryDocument FileCategory = "DOCUMENT"
	CategoryPhoto    FileCategory = "PHOTO"
	CategoryOther    FileCategory = "OTHER"
)

// Validate checks if the category is one of the defined constants.
func (fc FileCategory) Validate() bool {
	switch fc {
	case CategoryDocument, CategoryPhoto, CategoryOther:
		return true
	default:
		return false
	}
}
