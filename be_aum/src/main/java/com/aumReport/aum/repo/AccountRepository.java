package com.aumReport.aum.repo;

import com.aumReport.aum.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    
    List<Account> findByVendorId(Long vendorId);
    
    Optional<Account> findByNumber(String number);
    
    List<Account> findAllByNumber(String number);
    
    List<Account> findByName(String name);
    
    void deleteByNumber(String number);
    
    @Query(value = """
            SELECT a.id, adv.name as advisor_name, c.name as custodian_name
            FROM account a
            LEFT JOIN household_entity he ON a.entity_id = he.id
            LEFT JOIN relationship r ON he.relationship_id = r.id
            LEFT JOIN advisor adv ON r.advisor_id = adv.id
            LEFT JOIN custodian c ON a.custodian_id = c.id
            WHERE a.id IN :accountIds
            """, nativeQuery = true)
    List<Object[]> findAdvisorAndCustodianNamesByAccountIds(@Param("accountIds") List<Long> accountIds);
    
    @Query(value = """
            SELECT c.name
            FROM account a
            LEFT JOIN custodian c ON a.custodian_id = c.id
            WHERE a.id = :accountId
            """, nativeQuery = true)
    String findCustodianNameByAccountId(@Param("accountId") Long accountId);
}
