package com.aumReport.aum.repo;

import com.aumReport.aum.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Integer> {
    
    List<Holding> findByAccountId(int accountId);
    
    List<Holding> findByAssetId(int assetId);
    
    List<Holding> findByProductId(Integer productId);
    
    @Query("SELECT h FROM Holding h WHERE h.accountId IN :accountIds")
    List<Holding> findByAccountIds(@Param("accountIds") List<Integer> accountIds);
    
    @Query("SELECT h.accountId, SUM(h.value) FROM Holding h WHERE h.accountId IN :accountIds GROUP BY h.accountId")
    List<Object[]> getTotalValuesByAccountIds(@Param("accountIds") List<Integer> accountIds);
    
    @Query("SELECT SUM(h.value) FROM Holding h WHERE h.accountId = :accountId")
    Double getTotalValueByAccountId(@Param("accountId") int accountId);
}
