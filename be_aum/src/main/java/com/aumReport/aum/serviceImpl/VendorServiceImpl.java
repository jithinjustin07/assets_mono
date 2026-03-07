package com.aumReport.aum.serviceImpl;

import com.aumReport.aum.entity.Vendor;
import com.aumReport.aum.repo.VendorRepository;
import com.aumReport.aum.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VendorServiceImpl implements VendorService {
    
    @Autowired
    private VendorRepository vendorRepository;

    @Override
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    @Override
    public Optional<Vendor> getVendorById(Long id) {
        return vendorRepository.findById(id);
    }

    @Override
    public Vendor saveVendor(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    @Override
    public Vendor updateVendor(Long id, Vendor vendor) {
        if (vendorRepository.existsById(id)) {
            vendor.setId(id.intValue());
            return vendorRepository.save(vendor);
        }
        return null;
    }

    @Override
    public void deleteVendor(Long id) {
        vendorRepository.deleteById(id);
    }
    
    @Override
    public Optional<Vendor> getVendorByName(String name) {
        return vendorRepository.findByNameIgnoreCase(name);
    }
    
    @Override
    public List<Vendor> getVendorsByNames(List<String> names) {
        return vendorRepository.findByNameIgnoreCaseIn(names);
    }
}
