package com.aumReport.aum.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA entity class for "Relationship"
 *
 * @author Telosys
 *
 */
@Entity
@Getter
@Setter
@Table(name="relationship", schema="public" )
public class Relationship implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    //--- PRIMARY KEY
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private int        id ;

    //--- OTHER DATA FIELDS
    @Column(name="created_timestamp", nullable=false)
    private LocalDateTime   createdTimestamp ;

    @Column(name="updated_timestamp", nullable=false)
    private LocalDateTime   updatedTimestamp ;

    @Column(name="created_by")
    private Integer         createdBy ;

    @Column(name="updated_by")
    private Integer         updatedBy ;

    @Column(name="name", nullable=false, length=2147483647)
    private String          name ;

    @Column(name="type", nullable=false, length=2147483647)
    private String          type ;

    @Column(name="active", nullable=false)
    private boolean         active ;

    @Column(name="location", nullable=false, length=2147483647)
    private String          location ;

    @Column(name="household_id", nullable=false)
    private int             householdId ;

    @Column(name="advisor_id")
    private Integer         advisorId ;

    @Column(name="client_id")
    private Integer         clientId ;

    @Column(name="version_name", nullable=false, length=2147483647)
    private String          versionName ;

    @Column(name="reporting_frequency", length=2147483647)
    private String          reportingFrequency ;

    @Column(name="relationship_manager_id")
    private Integer         relationshipManagerId ;

    @Column(name="implementation_flag", nullable=false)
    private boolean         implementationFlag ;

    @Column(name="reporting_last_date")
    private LocalDateTime   reportingLastDate ;

    @Column(name="managed_portfolio_id", nullable=false)
    private int             managedPortfolioId ;

    @Column(name="parent_id")
    private Integer         parentId ;

    @Column(name="vendor_id", nullable=false)
    private int             vendorId ;

    @Column(name="target_id", nullable=false)
    private int             targetId ;

    /**
     * Constructor
     */
    public Relationship() {
        super();
    }
}
