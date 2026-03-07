package com.aumReport.aum.service;

import com.aumReport.aum.entity.Advisor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface AdvisorService {

    List<Advisor> getAllAdvisors();

    Optional<Advisor> getAdvisorById(Long id);

    Optional<Advisor> getAdvisorByName(String name);

    Advisor saveAdvisor(Advisor advisor);

    Advisor updateAdvisor(Long id, Advisor advisor);

    void deleteAdvisor(Long id);

    /**
     * Reassigns advisor on relationships whose linked accounts have
     * custodian IDs in the given list and whose current advisor matches
     * oldAdvisorId.
     *
     * @return number of rows updated
     */
    List<Integer> reassignAdvisorForCustodians(int newAdvisorId, int oldAdvisorId, List<Integer> custodianIds, List<Long> acIds);
}
