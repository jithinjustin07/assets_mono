package com.aumReport.aum.serviceImpl;

import com.aumReport.aum.entity.Account;
import com.aumReport.aum.repo.AccountRepository;
import com.aumReport.aum.service.AccountService;
import com.aumReport.aum.specification.AccountSpecification;
import com.aumReport.aum.utils.RelationshipDataHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private RelationshipDataHelper relationshipDataHelper;

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    @Override
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Account updateAccount(Long id, Account account) {
        if (accountRepository.existsById(id)) {
            account.setId(id.intValue());
            return accountRepository.save(account);
        }
        return null;
    }

    @Override
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    @Override
    public List<Account> getDistinctAccountsByVendorId(Long vendorId) {
        return accountRepository.findAll(
                AccountSpecification.distinctByVendor(vendorId)
        );
    }

    @Override
    public List<Account> getDistinctAccountsByVendorIdAndCustodianIds(Long vendorId, List<Integer> custodianIds) {
        return accountRepository.findAll(
                AccountSpecification.distinctByVendorAndCustodianIds(vendorId, custodianIds)
        );
    }

    @Override
    public void deleteAccountByNumber(String number) {
        accountRepository.deleteByNumber(number);
    }
    
    @Override
    public void setAdvisorNamesForAccounts(List<Account> accounts) {
        List<Long> accountIds = accounts.stream()
                .map(account -> (long) account.getId())
                .toList();
        
        List<Object[]> results = accountRepository.findAdvisorAndCustodianNamesByAccountIds(accountIds);
        
        Map<Long, Map<String, String>> namesMap = results.stream()
                .collect(Collectors.toMap(
                        result -> ((Integer) result[0]).longValue(),
                        result -> {
                            Map<String, String> names = new HashMap<>();
                            names.put("advisor", (String) result[1]);
                            names.put("custodian", (String) result[2]);
                            return names;
                        }
                ));
        
        for (Account account : accounts) {
            Map<String, String> names = namesMap.get((long) account.getId());
            if (names != null && names.get("advisor") != null) {
                account.setAdvisor(names.get("advisor"));
            }
            if(names != null && names.get("advisor") == null){
                account.setAdvisor("Avestar");
            }
        }
    }
    
    @Override
    public String getCustodianNameByAccountId(Long accountId) {
        return accountRepository.findCustodianNameByAccountId(accountId);
    }
    
    @Override
    public Map<Long, Map<String, String>> getAdvisorAndCustodianNamesByAccountIds(List<Long> accountIds) {
        List<Object[]> results = accountRepository.findAdvisorAndCustodianNamesByAccountIds(accountIds);
        
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> {
                            Map<String, String> names = new HashMap<>();
                            names.put("advisor", (String) result[1]);
                            names.put("custodian", (String) result[2]);
                            return names;
                        }
                ));
    }
    
    public void setRelationshipDataForAccounts(List<Account> accounts) {
        List<Integer> relationshipIds = accounts.stream()
                .map(Account::getRelationshipId)
                .distinct()
                .toList();
        
        if (relationshipIds.isEmpty()) return;
        
        Map<Integer, String> relationshipNames = relationshipDataHelper.getRelationshipNamesMap(relationshipIds);
        Map<Integer, String> managerNames = relationshipDataHelper.getRelationshipManagerNamesMap(relationshipIds);
        
        accounts.forEach(account -> {
            account.setRelationshipName(relationshipNames.get(account.getRelationshipId()));
            account.setRelationshipManager(managerNames.get(account.getRelationshipId()));
        });
    }
    
    public void setRelationshipDataForAccount(Account account) {
        account.setRelationshipName(relationshipDataHelper.getRelationshipName(account.getRelationshipId()));
        account.setRelationshipManager(relationshipDataHelper.getRelationshipManagerName(account.getRelationshipId()));
    }
    
    @Override
    public List<Account> getAccountsByNumber(String number) {
        return accountRepository.findAllByNumber(number);
    }
}
