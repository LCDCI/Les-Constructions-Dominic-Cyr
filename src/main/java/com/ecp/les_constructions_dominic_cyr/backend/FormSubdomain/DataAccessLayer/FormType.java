package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer;

/**
 * Enum representing the different types of customer forms that can be created and assigned.
 */
public enum FormType {
    /**
     * Exterior Doors Form - Customer creates design on Novatech website and uploads PDF
     * Website: https://novat echcanadian.ca/createur-entree
     */
    EXTERIOR_DOORS,
    
    /**
     * Garage Doors Form - Customer creates design on Universal Garage Doors website and saves PDF
     * Website: https://www.universalgaragedoors.ca/centre-de-design
     */
    GARAGE_DOORS,
    
    /**
     * Windows Form - Customer fills out window color preferences
     * Fields: exterior/interior colors for facade, sides and back
     */
    WINDOWS,
    
    /**
     * Asphalt Shingles Form - Customer selects roofing materials
     * Fields: company, collection, color, steel color (if applicable)
     */
    ASPHALT_SHINGLES,
    
    /**
     * Woodwork Form - Customer selects interior doors, handles, baseboards
     * Fields: interior door model, handle model with finishes, baseboard model with height
     */
    WOODWORK,
    
    /**
     * Paint Form - Customer provides paint details for various surfaces
     * Fields: ceiling, doors, woodwork, general walls, accent walls (multiple)
     */
    PAINT
}
