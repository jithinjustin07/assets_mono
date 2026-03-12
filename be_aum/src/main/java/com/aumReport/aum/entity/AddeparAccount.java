package com.aumReport.aum.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "addepar_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddeparAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String holdingAccount;

    private Long entityId;

    private String modelType;

    private Long directOwnerId;

    private Long topLevelOwnerId;

    private String topLevelOwner;

    private String acAccountNumber;

    private String acGoal;

    private String acCustodian;

    private Boolean acAvestar;

    private Boolean acAum;

    private String acReportingTarget;

    private String acAssetClass;

    private String acSubAssetClass;

    private BigDecimal adjustedValueUsd;

    private LocalDate acLastActivityDate;

    @Column(name = "issupervised")
    private Boolean isSupervised;
}