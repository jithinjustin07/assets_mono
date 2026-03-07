package com.aumReport.aum.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

/**
 * JPA entity for "BaseUpdatableEntity"
 */
@Getter
@Setter
@MappedSuperclass
public class BaseUpdatableEntity extends BaseEntity {

    @Column(name = "updated_timestamp")
    private ZonedDateTime updatedTimestamp;

    @Column(name = "updated_by")
    private Long updatedBy;

    public BaseUpdatableEntity() {
        super();
        this.updatedTimestamp = ZonedDateTime.now();
    }
    public void setUpdatedMetaData(Long updatedBy){
        setUpdatedMetaData(updatedBy,ZonedDateTime.now());
    }
    public void setUpdatedMetaData(Long updatedBy,ZonedDateTime updatedTimestamp){
        this.updatedTimestamp = updatedTimestamp != null ? updatedTimestamp : this.updatedTimestamp;
        this.updatedBy=updatedBy != null ? updatedBy : this.updatedBy;
    }
}
