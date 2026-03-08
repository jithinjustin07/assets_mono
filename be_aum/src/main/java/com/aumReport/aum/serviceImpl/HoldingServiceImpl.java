package com.aumReport.aum.serviceImpl;

import com.aumReport.aum.entity.Holding;
import com.aumReport.aum.repo.HoldingRepository;
import com.aumReport.aum.service.HoldingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HoldingServiceImpl implements HoldingService {
    
    @Autowired
    private HoldingRepository holdingRepository;
    
    @Override
    public List<Holding> getAllHoldings() {
        return holdingRepository.findAll();
    }
    
    @Override
    public Holding getHoldingById(Integer id) {
        Optional<Holding> holding = holdingRepository.findById(id);
        return holding.orElse(null);
    }
    
    @Override
    public Holding saveHolding(Holding holding) {
        return holdingRepository.save(holding);
    }
    
    @Override
    public Holding updateHolding(Integer id, Holding holding) {
        holding.setId(id);
        return holdingRepository.save(holding);
    }
    
    @Override
    public void deleteHolding(Integer id) {
        holdingRepository.deleteById(id);
    }
    
    @Override
    public List<Holding> getHoldingsByAccountId(int accountId) {
        return holdingRepository.findByAccountId(accountId);
    }
    
    @Override
    public List<Holding> getHoldingsByAssetId(int assetId) {
        return holdingRepository.findByAssetId(assetId);
    }
    
    @Override
    public List<Holding> getHoldingsByProductId(Integer productId) {
        return holdingRepository.findByProductId(productId);
    }
    
    @Override
    public List<Holding> getHoldingsByAccountIds(List<Integer> accountIds) {
        return holdingRepository.findByAccountIds(accountIds);
    }
    
    @Override
    public Double getTotalValueByAccountId(int accountId) {
        return holdingRepository.getTotalValueByAccountId(accountId);
    }
}
