package com.aumReport.aum.controller;

import com.aumReport.aum.entity.Custodian;
import com.aumReport.aum.service.CustodianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/custodians")
public class CustodianController {

    @Autowired
    private CustodianService custodianService;

    @GetMapping
    public List<Custodian> getAllCustodians() {
        return custodianService.getAllCustodians();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Custodian> getCustodianById(@PathVariable Integer id) {
        Optional<Custodian> custodian = custodianService.getCustodianById(id);
        return custodian.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Custodian> getCustodianByName(@PathVariable String name) {
        Optional<Custodian> custodian = custodianService.getCustodianByName(name);
        return custodian.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/{name}")
    public List<Custodian> getCustodiansByNameContaining(@PathVariable String name) {
        return custodianService.getCustodiansByNameContaining(name);
    }

    @PostMapping
    public Custodian createCustodian(@RequestBody Custodian custodian) {
        return custodianService.saveCustodian(custodian);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Custodian> updateCustodian(@PathVariable Integer id, @RequestBody Custodian custodian) {
        try {
            return ResponseEntity.ok(custodianService.updateCustodian(id, custodian));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustodian(@PathVariable Integer id) {
        try {
            custodianService.deleteCustodian(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/batch")
    public List<Custodian> getCustodiansByIds(@RequestBody List<Integer> ids) {
        return custodianService.getCustodiansByIds(ids);
    }
}
