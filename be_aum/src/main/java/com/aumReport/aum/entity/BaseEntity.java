package com.aumReport.aum.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * JPA entity base class"
 */
@Getter
@Setter
@MappedSuperclass
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wf_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "created_timestamp")
    private ZonedDateTime createdTimestamp = ZonedDateTime.now();

    @Column(name = "created_by")
    private Long createdBy;

    public BaseEntity(){
        this.createdTimestamp = ZonedDateTime.now();
    }
    public void setCreatedMetaData(Long createdBy){
        setCreatedMetaData(createdBy,ZonedDateTime.now());
    }
    public void setCreatedMetaData(Long createdBy,ZonedDateTime createdTimestamp){
        this.createdTimestamp=createdTimestamp != null ? createdTimestamp : this.createdTimestamp;
        this.createdBy = createdBy != null ? createdBy : this.createdBy;
    }

}