package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LivingEnvironment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "living_environment_amenities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivingEnvironmentAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "living_environment_content_id", nullable = false)
    @JsonIgnore
    private LivingEnvironmentContent livingEnvironmentContent;

    @Column(name = "amenity_key", nullable = false, length = 50)
    private String amenityKey;

    @Column(name = "amenity_label", nullable = false, length = 100)
    private String amenityLabel;

    @Column(name = "display_order")
    private Integer displayOrder;
}
