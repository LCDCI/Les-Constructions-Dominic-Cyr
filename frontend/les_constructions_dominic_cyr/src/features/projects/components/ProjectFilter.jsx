import React from 'react';

const ProjectFilter = ({ filters, onFilterChange }) => {
    return (
        <div style={{
            backgroundColor: 'white',
            padding: '20px',
            borderRadius: '8px',
            marginBottom: '24px',
            boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
        }}>
            <h3 style={{
                fontSize: '16px',
                fontWeight: '600',
                marginBottom: '16px',
                color: '#1F2937'
            }}>
                Filters
            </h3>

            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                gap: '16px'
            }}>
                <div>
                    <label style={{
                        display: 'block',
                        fontSize: '14px',
                        fontWeight: '500',
                        marginBottom: '6px',
                        color: '#374151'
                    }}>
                        Status
                    </label>
                    <select
                        value={filters.status || ''}
                        onChange={(e) => onFilterChange({ ... filters, status: e.target. value || null })}
                        style={{
                            width: '100%',
                            padding: '8px 12px',
                            borderRadius: '6px',
                            border: '1px solid #D1D5DB',
                            fontSize: '14px'
                        }}
                    >
                        <option value="">All Statuses</option>
                        <option value="PLANNED">Planned</option>
                        <option value="IN_PROGRESS">In Progress</option>
                        <option value="DELAYED">Delayed</option>
                        <option value="COMPLETED">Completed</option>
                        <option value="CANCELLED">Cancelled</option>
                    </select>
                </div>

                <div>
                    <label style={{
                        display: 'block',
                        fontSize: '14px',
                        fontWeight: '500',
                        marginBottom: '6px',
                        color: '#374151'
                    }}>
                        Start Date From
                    </label>
                    <input
                        type="date"
                        value={filters.startDate || ''}
                        onChange={(e) => onFilterChange({ ...filters, startDate: e.target.value || null })}
                        style={{
                            width: '100%',
                            padding: '8px 12px',
                            borderRadius: '6px',
                            border: '1px solid #D1D5DB',
                            fontSize: '14px'
                        }}
                    />
                </div>

                <div>
                    <label style={{
                        display: 'block',
                        fontSize: '14px',
                        fontWeight: '500',
                        marginBottom: '6px',
                        color: '#374151'
                    }}>
                        End Date To
                    </label>
                    <input
                        type="date"
                        value={filters.endDate || ''}
                        onChange={(e) => onFilterChange({ ...filters, endDate: e.target.value || null })}
                        style={{
                            width: '100%',
                            padding: '8px 12px',
                            borderRadius: '6px',
                            border: '1px solid #D1D5DB',
                            fontSize: '14px'
                        }}
                    />
                </div>

                <div>
                    <label style={{
                        display: 'block',
                        fontSize: '14px',
                        fontWeight: '500',
                        marginBottom: '6px',
                        color: '#374151'
                    }}>
                        Customer ID
                    </label>
                    <input
                        type="text"
                        value={filters.customerId || ''}
                        onChange={(e) => onFilterChange({ ...filters, customerId: e.target. value || null })}
                        placeholder="Enter customer ID"
                        style={{
                            width: '100%',
                            padding: '8px 12px',
                            borderRadius: '6px',
                            border: '1px solid #D1D5DB',
                            fontSize: '14px'
                        }}
                    />
                </div>
            </div>

            <button
                onClick={() => onFilterChange({})}
                style={{
                    marginTop: '16px',
                    padding: '8px 16px',
                    backgroundColor: '#F3F4F6',
                    color: '#374151',
                    border: 'none',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    fontSize: '14px',
                    fontWeight: '500'
                }}
            >
                Clear Filters
            </button>
        </div>
    );
};

export default ProjectFilter;