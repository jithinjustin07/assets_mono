package com.aumReport.aum.controller;

import com.aumReport.aum.entity.Holding;
import com.aumReport.aum.service.HoldingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holdings")
public class HoldingController {
    
    @Autowired
    private HoldingService holdingService;
    
    @GetMapping
    public List<Holding> getAllHoldings() {
        return holdingService.getAllHoldings();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Holding> getHoldingById(@PathVariable Integer id) {
        Holding holding = holdingService.getHoldingById(id);
        return holding != null ? ResponseEntity.ok(holding) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/account/{accountId}")
    public List<Holding> getHoldingsByAccountId(@PathVariable int accountId) {
        return holdingService.getHoldingsByAccountId(accountId);
    }
    
    @GetMapping("/asset/{assetId}")
    public List<Holding> getHoldingsByAssetId(@PathVariable int assetId) {
        return holdingService.getHoldingsByAssetId(assetId);
    }
    
    @GetMapping("/product/{productId}")
    public List<Holding> getHoldingsByProductId(@PathVariable Integer productId) {
        return holdingService.getHoldingsByProductId(productId);
    }
    
    @GetMapping("/account/{accountId}/total-value")
    public ResponseEntity<Double> getTotalValueByAccountId(@PathVariable int accountId) {
        Double totalValue = holdingService.getTotalValueByAccountId(accountId);
        return totalValue != null ? ResponseEntity.ok(totalValue) : ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public Holding createHolding(@RequestBody Holding holding) {
        return holdingService.saveHolding(holding);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Holding> updateHolding(@PathVariable Integer id, @RequestBody Holding holding) {
        try {
            return ResponseEntity.ok(holdingService.updateHolding(id, holding));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHolding(@PathVariable Integer id) {
        try {
            holdingService.deleteHolding(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/batch")
    public List<Holding> getHoldingsByAccountIds(@RequestBody List<Integer> accountIds) {
        return holdingService.getHoldingsByAccountIds(accountIds);
    }
}
