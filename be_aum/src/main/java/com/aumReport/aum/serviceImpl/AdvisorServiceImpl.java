package com.aumReport.aum.serviceImpl;

import com.aumReport.aum.entity.Advisor;
import com.aumReport.aum.repo.AdvisorRepository;
import com.aumReport.aum.service.AdvisorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdvisorServiceImpl implements AdvisorService {

    @Autowired
    private AdvisorRepository advisorRepository;

    @Override
    public List<Advisor> getAllAdvisors() {
        return advisorRepository.findAll();
    }

    @Override
    public Optional<Advisor> getAdvisorById(Long id) {
        return advisorRepository.findById(id);
    }

    @Override
    public Optional<Advisor> getAdvisorByName(String name) {
        return advisorRepository.findByNameIgnoreCase(name);
    }

    @Override
    public Advisor saveAdvisor(Advisor advisor) {
        return advisorRepository.save(advisor);
    }

    @Override
    public Advisor updateAdvisor(Long id, Advisor advisor) {
        if (advisorRepository.existsById(id)) {
            advisor.setId(id.intValue());
            return advisorRepository.save(advisor);
        }
        return null;
    }

    @Override
    public void deleteAdvisor(Long id) {
        advisorRepository.deleteById(id);
    }

    @Override
    public List<Integer> reassignAdvisorForCustodians(int newAdvisorId, int oldAdvisorId, List<Integer> custodianIds, List<Long> acIds) {
        return advisorRepository.reassignAdvisorForCustodians(newAdvisorId, oldAdvisorId, custodianIds);
    }
}
