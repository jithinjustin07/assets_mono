package com.aumReport.aum.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * JPA entity class for "WfHouseholdEntity"
 */
@Entity
@Table(name="household_entity")
@Getter
@Setter
@Cache(region = "household_entity", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class WfHouseholdEntity extends BaseUpdatableEntity {

    @Column(name="name", nullable=false)
    private String name;


    @JsonManagedReference
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_id", referencedColumnName = "id")
    private WfRelationship parent;


}
