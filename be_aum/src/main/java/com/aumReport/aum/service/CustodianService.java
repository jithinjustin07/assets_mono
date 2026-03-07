package com.aumReport.aum.service;

import com.aumReport.aum.entity.Custodian;

import java.util.List;
import java.util.Optional;

public interface CustodianService {
    
    List<Custodian> getAllCustodians();
    
    Optional<Custodian> getCustodianById(Integer id);
    
    Custodian saveCustodian(Custodian custodian);
    
    Custodian updateCustodian(Integer id, Custodian custodian);
    
    void deleteCustodian(Integer id);
    
    Optional<Custodian> getCustodianByName(String name);
    
    List<Custodian> getCustodiansByNameContaining(String name);
    
    List<Custodian> getCustodiansByIds(List<Integer> ids);
}
