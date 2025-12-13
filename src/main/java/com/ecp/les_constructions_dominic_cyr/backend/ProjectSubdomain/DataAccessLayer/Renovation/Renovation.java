package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@Table(name = "renovations")
@NoArgsConstructor
public class Renovation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private RenovationIdentifier renovationIdentifier;

    private String beforeImageIdentifier;

    private String afterImageIdentifier;

    private String description;

    public Renovation(@NonNull RenovationIdentifier renovationIdentifier, @NonNull String beforeImageIdentifier, @NonNull String afterImageIdentifier, @NonNull String description) {
        this.renovationIdentifier = renovationIdentifier;
        this.beforeImageIdentifier = beforeImageIdentifier;
        this.afterImageIdentifier = afterImageIdentifier;
        this.description = description;
    }
}
