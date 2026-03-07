package com.aumReport.aum.repo;

import com.aumReport.aum.entity.Advisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdvisorRepository extends JpaRepository<Advisor, Long> {

    Optional<Advisor> findByNameIgnoreCase(String name);

    /**
     * Reassigns the advisor on all relationships whose linked accounts
     * belong to any of the given custodian IDs and whose current advisor
     * matches the given old advisor ID.
     *
     * Equivalent SQL:
     * UPDATE relationship r
     * SET advisor_id = :newAdvisorId
     * FROM household_entity he
     * JOIN account a ON a.entity_id = he.id
     * WHERE r.id = he.relationship_id
     * AND r.advisor_id = :oldAdvisorId
     * AND a.custodian_id IN (:custodianIds)
     */
    @Modifying
    @Transactional
    @Query(value = """
            SELECT a.id
            FROM relationship r
            JOIN household_entity he ON r.id = he.relationship_id
            JOIN account a ON a.entity_id = he.id
            WHERE r.advisor_id = :oldAdvisorId
            AND a.custodian_id IN (:custodianIds);
            """, nativeQuery = true)
    List<Integer> reassignAdvisorForCustodians(@Param("newAdvisorId") int newAdvisorId,
            @Param("oldAdvisorId") int oldAdvisorId,
            @Param("custodianIds") List<Integer> custodianIds);
}
