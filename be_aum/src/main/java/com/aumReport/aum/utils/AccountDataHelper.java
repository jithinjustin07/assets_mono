package com.aumReport.aum.utils;

import com.aumReport.aum.repo.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;

@Component
public class AccountDataHelper {
    
    @Autowired
    private AccountRepository accountRepository;

    @Cacheable(value = "advisorCustodianNames", key = "#accountIds")
    public Map<Long, Map<String, String>> getAdvisorAndCustodianNamesByAccountIds(List<Long> accountIds) {
        List<Object[]> results = accountRepository.findAdvisorAndCustodianNamesByAccountIds(accountIds);
        
        return results.stream()
                .collect(Collectors.toMap(
                        result -> ((Integer) result[0]).longValue(),
                        result -> {
                            Map<String, String> names = new HashMap<>();
                            names.put("advisor", (String) result[1]);
                            names.put("custodian", (String) result[2]);
                            return names;
                        }
                ));
    }
}
