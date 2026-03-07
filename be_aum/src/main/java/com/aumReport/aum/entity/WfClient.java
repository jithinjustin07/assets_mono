package com.aumReport.aum.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name="client")
@Getter
@Setter
@Cache(region = "client", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class WfClient  extends BaseUpdatableEntity {

    @Column(name="name", nullable=false)
    private String name;

}