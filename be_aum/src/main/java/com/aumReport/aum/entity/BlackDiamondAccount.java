package com.aumReport.aum.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "black_diamond_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlackDiamondAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean accountIsOpen;

    private String relationshipId;
    private String relationshipName;

    private String portfolioId;
    private String portfolioName;

    private String accountId;
    private String accountNumber;
    private String accountName;

    private String custodian;
    private String goal;

    private String aum;

    private String taxStatus;
    private String accountReportingTarget;
    private String accountBenchmark;

    private Long assetId;
    private String assetName;

    @Column(name = "class")
    private String className;

    private BigDecimal marketValue;

    private LocalDate asOfDate;

    private String advisor;

    @Column(name = "issupervised")
    private Boolean isSupervised;
}