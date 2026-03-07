package com.aumReport.aum.entity;


import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA entity class for "Advisor"
 *
 * @author Telosys
 *
 */
@Entity
@Table(name="advisor", schema="public" )
public class Advisor implements Serializable {
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

    @Column(name="client_id")
    private Integer         clientId ;

    /**
     * Constructor
     */
    public Advisor() {
        super();
    }

    public void setId( int id ) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setCreatedTimestamp( LocalDateTime createdTimestamp ) {
        this.createdTimestamp = createdTimestamp;
    }

    public LocalDateTime getCreatedTimestamp() {
        return this.createdTimestamp;
    }

    public void setUpdatedTimestamp( LocalDateTime updatedTimestamp ) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return this.updatedTimestamp;
    }

    public void setCreatedBy( Integer createdBy ) {
        this.createdBy = createdBy;
    }

    public Integer getCreatedBy() {
        return this.createdBy;
    }

    public void setUpdatedBy( Integer updatedBy ) {
        this.updatedBy = updatedBy;
    }

    public Integer getUpdatedBy() {
        return this.updatedBy;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setClientId( Integer clientId ) {
        this.clientId = clientId;
    }

    public Integer getClientId() {
        return this.clientId;
    }


    public Advisor clone(boolean includeKeys) {
        Advisor copy = new Advisor();
        if (includeKeys) {
            copy.id = this.id;
        }
        copy.createdTimestamp = this.createdTimestamp;
        copy.updatedTimestamp = this.updatedTimestamp;
        copy.createdBy = this.createdBy;
        copy.updatedBy = this.updatedBy;
        copy.name = this.name;
        copy.clientId = this.clientId;
        return copy;
    }

    @Override
    public String toString() {
        String separator = "|";
        StringBuilder sb = new StringBuilder();
        sb.append("Advisor[");
        sb.append("id=").append(id);
        sb.append(separator).append("createdTimestamp=").append(createdTimestamp);
        sb.append(separator).append("updatedTimestamp=").append(updatedTimestamp);
        sb.append(separator).append("createdBy=").append(createdBy);
        sb.append(separator).append("updatedBy=").append(updatedBy);
        sb.append(separator).append("name=").append(name);
        sb.append(separator).append("clientId=").append(clientId);
        sb.append("]");
        return sb.toString();
    }
}
