package com.aumReport.aum.service;

import com.aumReport.aum.entity.Holding;

import java.util.List;
import java.util.Map;

public interface HoldingService {
    
    List<Holding> getAllHoldings();
    
    Holding getHoldingById(Integer id);
    
    Holding saveHolding(Holding holding);
    
    Holding updateHolding(Integer id, Holding holding);
    
    void deleteHolding(Integer id);
    
    List<Holding> getHoldingsByAccountId(int accountId);
    
    List<Holding> getHoldingsByAssetId(int assetId);
    
    List<Holding> getHoldingsByProductId(Integer productId);
    
    List<Holding> getHoldingsByAccountIds(List<Integer> accountIds);
    
    Double getTotalValueByAccountId(int accountId);
    
    Map<Integer, Double> getTotalValuesByAccountIds(List<Integer> accountIds);
}
