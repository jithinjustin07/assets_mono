package com.aumReport.aum.serviceImpl;

import com.aumReport.aum.entity.Custodian;
import com.aumReport.aum.repo.CustodianRepository;
import com.aumReport.aum.service.CustodianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustodianServiceImpl implements CustodianService {
    
    @Autowired
    private CustodianRepository custodianRepository;
    
    @Override
    public List<Custodian> getAllCustodians() {
        return custodianRepository.findAll();
    }
    
    @Override
    public Optional<Custodian> getCustodianById(Integer id) {
        return custodianRepository.findById(id);
    }
    
    @Override
    public Custodian saveCustodian(Custodian custodian) {
        return custodianRepository.save(custodian);
    }
    
    @Override
    public Custodian updateCustodian(Integer id, Custodian custodian) {
        custodian.setId(id);
        return custodianRepository.save(custodian);
    }
    
    @Override
    public void deleteCustodian(Integer id) {
        custodianRepository.deleteById(id);
    }
    
    @Override
    public Optional<Custodian> getCustodianByName(String name) {
        return custodianRepository.findByNameIgnoreCase(name);
    }
    
    @Override
    public List<Custodian> getCustodiansByNameContaining(String name) {
        return custodianRepository.findByNameContainingIgnoreCase(name);
    }
    
    @Override
    public List<Custodian> getCustodiansByIds(List<Integer> ids) {
        return custodianRepository.findByIds(ids);
    }
}
