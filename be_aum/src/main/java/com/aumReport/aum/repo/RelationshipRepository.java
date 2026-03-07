package com.aumReport.aum.repo;

import com.aumReport.aum.entity.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Integer>, JpaSpecificationExecutor<Relationship> {
    
    List<Relationship> findByVendorId(int vendorId);
    
    List<Relationship> findByHouseholdId(int householdId);
    
    List<Relationship> findByAdvisorId(Integer advisorId);
    
    List<Relationship> findByClientId(Integer clientId);
    
    List<Relationship> findByActiveTrue();
    
    List<Relationship> findByActiveFalse();
    
    Optional<Relationship> findByName(String name);
    
    List<Relationship> findByType(String type);
    
    List<Relationship> findByLocation(String location);
    
    @Query("SELECT r FROM Relationship r WHERE r.active = true AND r.vendorId = :vendorId")
    List<Relationship> findActiveByVendorId(@Param("vendorId") int vendorId);
    
    @Query("SELECT r FROM Relationship r WHERE r.active = true AND r.advisorId = :advisorId")
    List<Relationship> findActiveByAdvisorId(@Param("advisorId") Integer advisorId);
    
    @Query("SELECT r FROM Relationship r WHERE r.reportingLastDate < :date AND r.active = true")
    List<Relationship> findOverdueReports(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(r) FROM Relationship r WHERE r.active = true AND r.vendorId = :vendorId")
    long countActiveByVendorId(@Param("vendorId") int vendorId);
    
    @Query("SELECT r FROM Relationship r WHERE r.householdId = :householdId AND r.active = true ORDER BY r.name")
    List<Relationship> findActiveByHouseholdIdOrderByName(@Param("householdId") int householdId);
    
    void deleteByVendorId(int vendorId);
    
    boolean existsByNameAndVendorId(String name, int vendorId);
    
    @Query("SELECT r.name FROM Relationship r WHERE r.id = :relationshipId AND r.active = true")
    Optional<String> findActiveRelationshipNameById(@Param("relationshipId") int relationshipId);
    
    @Query("SELECT r.relationshipManagerId FROM Relationship r WHERE r.id = :relationshipId AND r.active = true")
    Optional<Integer> findRelationshipManagerIdById(@Param("relationshipId") int relationshipId);
    
    @Query("SELECT r FROM Relationship r WHERE r.id IN :relationshipIds AND r.active = true")
    List<Relationship> findRelationshipsByIds(@Param("relationshipIds") List<Integer> relationshipIds);
}
