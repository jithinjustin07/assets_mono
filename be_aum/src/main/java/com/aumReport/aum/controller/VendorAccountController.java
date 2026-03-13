package com.aumReport.aum.controller;

import com.aumReport.aum.entity.Account;
import com.aumReport.aum.service.AumService;
import com.aumReport.aum.service.VendorService;
import com.aumReport.aum.service.AccountService;
import com.aumReport.aum.enums.VendorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vendor-accounts")
public class VendorAccountController {

    @Autowired
    private VendorService vendorService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/black-diamond")
    public ResponseEntity<List<Account>> getBlackDiamondAccounts() {
        Optional<com.aumReport.aum.entity.Vendor> blackDiamondVendors = vendorService.getVendorByName(VendorType.Black_Diamond.getValue());
        if (blackDiamondVendors.isPresent()) {
            Long blackDiamondVendorId = (long) blackDiamondVendors.get().getId();
            List<Account> filteredAccounts = new ArrayList<>();
            
            filteredAccounts.addAll(accountService.getDistinctAccountsByVendorId(blackDiamondVendorId));
            return new ResponseEntity<>(filteredAccounts, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/addepar")
    public ResponseEntity<List<Account>> getAddeparAccounts() {
        Optional<com.aumReport.aum.entity.Vendor> addeparVendors = vendorService.getVendorByName(VendorType.Addepar.getValue());
        if (addeparVendors.isPresent()) {
            Long addeparVendorId = (long) addeparVendors.get().getId();
            List<Account> filteredAccounts = new ArrayList<>();
            
            filteredAccounts.addAll(accountService.getDistinctAccountsByVendorId(addeparVendorId));
            return new ResponseEntity<>(filteredAccounts, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
