package com.aumReport.aum.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="account", schema="public" )
@Getter
@Setter
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    //--- PRIMARY KEY
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private int        id ;

    //--- OTHER DATA FIELDS
    @Column(name="created_timestamp")
    private LocalDateTime   createdTimestamp ;

    @Column(name="updated_timestamp")
    private LocalDateTime   updatedTimestamp ;

    @Column(name="created_by")
    private Integer         createdBy ;

    @Column(name="updated_by")
    private Integer         updatedBy ;

    @Column(name="name", length=2147483647)
    private String          name ;

    @Column(name="number", length=2147483647)
    private String          number ;

    @Column(name="asset")
    private boolean         asset ;

    @Column(name="tax_status", length=2147483647)
    private String          taxStatus ;

    @Column(name="custodian_id")
    private Integer         custodianId ;

    @Column(name="relationship_id")
    private Integer         relationshipId ;

    @Column(name="aum")
    private boolean         aum ;

    @Column(name="managed")
    private boolean         managed ;

    @Column(name="account_benchmark_id")
    private Integer         accountBenchmarkId ;

    @Column(name="as_of_date")
    private LocalDateTime   asOfDate ;

    @Column(name="reporting_target", length=2147483647)
    private String          reportingTarget ;

    @Column(name="goal", length=2147483647)
    private String          goal ;

    @Column(name="custodial")
    private boolean         custodial ;

    @Column(name="entity_id")
    private Integer         entityId ;

    @Column(name="vendor_id")
    private Integer         vendorId ;

    @Column(name="type_id")
    private Integer         typeId ;

    @Transient
    private Boolean isAua;

    @Transient
    private String advisor;

    @Transient
    private String relationshipName;

    @Transient
    private String relationshipManager;

    /**
     * Constructor
     */
    public Account() {
        super();
    }

}
