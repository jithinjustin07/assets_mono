package com.aumReport.aum.service;

import com.aumReport.aum.entity.Vendor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface VendorService {
    
    List<Vendor> getAllVendors();
    
    Optional<Vendor> getVendorById(Long id);
    
    Vendor saveVendor(Vendor vendor);
    
    Vendor updateVendor(Long id, Vendor vendor);
    
    void deleteVendor(Long id);
    
    Optional<Vendor> getVendorByName(String name);
    
    List<Vendor> getVendorsByNames(List<String> names);
}
