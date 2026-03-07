package com.aumReport.aum.repo;

import com.aumReport.aum.entity.Custodian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustodianRepository extends JpaRepository<Custodian, Integer> {
    
    Optional<Custodian> findByNameIgnoreCase(String name);
    
    List<Custodian> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Custodian c WHERE c.id IN :ids")
    List<Custodian> findByIds(@Param("ids") List<Integer> ids);
}
