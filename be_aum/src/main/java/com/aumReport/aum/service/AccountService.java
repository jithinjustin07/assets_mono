package com.aumReport.aum.service;

import com.aumReport.aum.entity.Account;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public interface AccountService {
    
    List<Account> getAllAccounts();
    
    Optional<Account> getAccountById(Long id);
    
    Account saveAccount(Account account);
    
    Account updateAccount(Long id, Account account);
    
    void deleteAccount(Long id);

     List<Account> getDistinctAccountsByVendorId(Long vendorId);
     
     List<Account> getDistinctAccountsByVendorIdAndCustodianIds(Long vendorId, List<Integer> custodianIds);
     
     void deleteAccountByNumber(String number);
     
     void setAdvisorNamesForAccounts(List<Account> accounts);
     
     String getCustodianNameByAccountId(Long accountId);
     
     Map<Long, Map<String, String>> getAdvisorAndCustodianNamesByAccountIds(List<Long> accountIds);
     
     void setRelationshipDataForAccounts(List<Account> accounts);
}
