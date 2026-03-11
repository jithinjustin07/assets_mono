package com.aumReport.aum.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataResponse {
    public String accountNumber;
    public String accountName;
    private String dataProvider;
    private Boolean isSupervised;
    private double marketValue;
    private Boolean aum;
    private String relationshipManager;
    private String advisor;
    private String startDate;
    private String asOfDate;
    private String closedDate;
    private String relationshipName;
    private Boolean aua;
    private String platform;

}
