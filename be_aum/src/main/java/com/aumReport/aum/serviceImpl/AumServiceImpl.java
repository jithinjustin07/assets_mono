package com.aumReport.aum.serviceImpl;

import com.aumReport.aum.dto.DataResponse;
import com.aumReport.aum.entity.Account;
import com.aumReport.aum.entity.Vendor;
import com.aumReport.aum.enums.AdvisorType;
import com.aumReport.aum.enums.VendorType;
import com.aumReport.aum.repo.AccountRepository;
import com.aumReport.aum.service.AccountService;
import com.aumReport.aum.service.AdvisorService;
import com.aumReport.aum.service.AumService;
import com.aumReport.aum.service.HoldingService;
import com.aumReport.aum.service.VendorService;
import com.aumReport.aum.utils.AccountDataHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AumServiceImpl implements AumService {

    @Autowired
    VendorService vendorService;

    @Autowired
    AccountService accountService;

    @Autowired
    AdvisorService advisorService;
    
    @Autowired
    HoldingService holdingService;
    
    @Autowired
    AccountDataHelper accountDataHelper;
    @Autowired
    private AccountRepository accountRepository;

    @Override
    public List<DataResponse> getData() {
        return getData(null);
    }

    @Override
    public List<DataResponse> getData(Boolean aum) {
        List<DataResponse> dataResponses = new ArrayList<>();
        dataResponses.addAll(getBlackDiamondData());
        dataResponses.addAll(getAddeparData());
        dataResponses.removeIf(account ->
                account.accountNumber == null || account.accountNumber.trim().isEmpty() ||
                        account.accountName == null ||  account.accountName.trim().isEmpty()
        );

        // Filter by AUM status if parameter is provided
        if (aum != null) {
            dataResponses = dataResponses.parallelStream().filter(dr -> dr.getAum() == aum).toList();
        }

        dataResponses.forEach(response -> {
            // Change custodian "manual account" to "manual"
            if ("manual account".equalsIgnoreCase(response.getDataProvider())) {
                response.setDataProvider("manual");
            }
            
            // If advisor is blank, fill with "avestar"
            if (response.getAdvisor() == null || response.getAdvisor().trim().isEmpty() || response.getAdvisor().equalsIgnoreCase("No Advisor Value Assigned")) {
                response.setAdvisor(AdvisorType.AVESTAR.getValue());
            }
            
            // If relationship name contains "family office", set advisor to "FOS"
            if (response.getRelationshipName() != null && 
                response.getRelationshipName().toLowerCase().contains("family office")) {
                response.setAdvisor("Avestar");
                response.setDataProvider("FOS");
            }
            if(response.getAccountName().contains("MAHADEVIA")){
                response.setAum(true);
                response.setAua(null);
                response.setDataProvider("External Investment");
            }
            if(response.getDataProvider().equalsIgnoreCase("Alternative Investment") ){
                response.setDataProvider("manual");
            }
        });
        
        return dataResponses;
    }

    @Override
    public String uploadData(String data) {
        return "";
    }

    List<DataResponse> getAddeparData() {
    Optional<Vendor> addeparVendors = vendorService.getVendorByName(VendorType.Addepar.getValue());
    if (addeparVendors.isPresent()) {
        Long addeparVendorId = (long) addeparVendors.get().getId();

        List<Account> filteredAccounts = new ArrayList<>();

        // Fetch accounts for the Addepar vendor
        filteredAccounts.addAll(accountService.getDistinctAccountsByVendorId(addeparVendorId));

        // Task 1: Remove accounts having no account number and account name
        filteredAccounts.removeIf(account -> 
            account.getNumber() == null || account.getNumber().trim().isEmpty() ||
            account.getName() == null || account.getName().trim().isEmpty()
        );

        // Task 2: Handle duplicates based on account.name with priority rules
        Map<String, List<Account>> accountsByName = filteredAccounts.stream()
                .collect(Collectors.groupingBy(Account::getName, Collectors.toList()));
        
        filteredAccounts = accountsByName.entrySet().stream()
                .map(entry -> {
                    List<Account> duplicateAccounts = entry.getValue();
                    if (duplicateAccounts.size() == 1) {
                        return duplicateAccounts.get(0);
                    }
                    
                    // Set relationship data for all accounts to access relationship names
                    accountService.setRelationshipDataForAccounts(duplicateAccounts);
                    
                    // Priority 1: If any relationship name contains "patwa" (case-insensitive)
                    Optional<Account> patwaAccount = duplicateAccounts.stream()
                            .filter(account -> account.getRelationshipName() != null &&
                                    account.getRelationshipName().toLowerCase().contains("patwa"))
                            .findFirst();
                    if (patwaAccount.isPresent()) {
                        return patwaAccount.get();
                    }
                    
                    // Priority 2: If no "patwa" but one contains "avestar"
                    Optional<Account> avestarAccount = duplicateAccounts.stream()
                            .filter(account -> account.getRelationshipName() != null &&
                                    account.getRelationshipName().toLowerCase().contains("avestar"))
                            .findFirst();
                    if (avestarAccount.isPresent()) {
                        return avestarAccount.get();
                    }
                    
                    // Priority 3: If neither "patwa" nor "avestar", keep one containing "family office"
                    Optional<Account> familyOfficeAccount = duplicateAccounts.stream()
                            .filter(account -> account.getRelationshipName() != null &&
                                    account.getRelationshipName().toLowerCase().contains("family office"))
                            .findFirst();
                    return familyOfficeAccount.orElseGet(duplicateAccounts::getFirst);
                    
                    // If none match, return the first one
                })
                .collect(Collectors.toList());

        // Task 3: Handle duplicates based on account.number - keep only one if all other fields match exactly
        Map<String, List<Account>> accountsByNumber = filteredAccounts.stream()
                .collect(Collectors.groupingBy(Account::getNumber));
        
        List<Account> task3Result = new ArrayList<>();
        for (Map.Entry<String, List<Account>> entry : accountsByNumber.entrySet()) {
            List<Account> duplicateAccounts = entry.getValue();
            if (duplicateAccounts.size() == 1) {
                task3Result.add(duplicateAccounts.get(0));
            } else {
                // Find accounts with identical fields (excluding relationship_id)
                Map<String, List<Account>> identicalGroups = duplicateAccounts.stream()
                        .collect(Collectors.groupingBy(account -> 
                            account.getName() + "|" + 
                            account.getVendorId() + "|" + 
                            account.getCustodianId() + "|" +
                            account.isManaged() + "|" +
                            account.isAum()
                        ));
                
                // Keep only one from each identical group
                for (List<Account> group : identicalGroups.values()) {
                    task3Result.add(group.get(0));
                }
            }
        }
        filteredAccounts = task3Result;

        // Task 4: Set custodian IDs based on account number format
        for (Account account : filteredAccounts) {
            if (account.getCustodianId() == null) {
                String accountNumber = account.getNumber();
                
                // If account number is exactly 8 digits -> Schwab
                if (accountNumber != null && accountNumber.matches("^\\d{8}$")) {
                    // Find Schwab custodian by name
                    // Note: You'll need to implement getCustodianByName in your service
//                     account.setCustodianId(getCustodianIdByName("Schwab"));
                }
                // If account number is 9 characters with 3 alphabets + 6 numbers -> Pershing
                else if (accountNumber != null && accountNumber.matches("^[a-zA-Z]{3}\\d{6}$")) {
                    // Find Pershing custodian by name
                    // account.setCustodianId(getCustodianIdByName("Pershing"));
                }
                // Default case: set to "-" custodian
                else {
                    // account.setCustodianId(getCustodianIdByName("-"));
                }
            }
        }

     //    Set advisor names and filter accounts

        
        // Set relationship data for accounts
        accountService.setRelationshipDataForAccounts(filteredAccounts);
        
        // Batch fetch custodian names and holding values for performance
        List<Long> accountIds = filteredAccounts.stream()
                .map(account -> (long) account.getId())
                .toList();

        Map<Long, Map<String, String>> namesMap = accountDataHelper.getAdvisorAndCustodianNamesByAccountIds(accountIds);

        // Map filtered accounts to DataResponse
        return filteredAccounts.stream()
                .map(account -> {
                    DataResponse response = new DataResponse();
                    response.setAccountNumber(account.getNumber());
                    response.setAccountName(account.getName());
                    
                    Map<String, String> names = namesMap.get((long) account.getId());
                    response.setDataProvider(names != null ? names.get("custodian") : null);
                    
                    response.setIsSupervised(account.isManaged());

                    Map<Integer, Double> marketValueMap =
                            holdingService.getTotalValuesByAccountIds(accountIds);

                    Double totalValue = marketValueMap.get(account.getId());
                    response.setMarketValue(totalValue != null ? totalValue : 0.0);
                    
                    response.setAum(account.isAum());
                    if(account.getAlternativeInvestmentType()!=null && account.getAlternativeInvestmentType().contains("External")){
                        response.setAua(true);
                    }

                    response.setAdvisor(account.getAdvisor() != null ? account.getAdvisor() : "Avestar");
                    response.setRelationshipManager(account.getRelationshipManager());
                    response.setRelationshipName(account.getRelationshipName());
                    response.setAsOfDate(account.getAsOfDate() != null ? account.getAsOfDate().toString() : null);
                    response.setStartDate(account.getCreatedTimestamp() != null ? account.getCreatedTimestamp().toString() : null);
                    response.setPlatform(VendorType.Addepar.getValue());
                    return response;
                })
                .collect(Collectors.toList());
    }
    return new ArrayList<>();
}

    List<DataResponse> getBlackDiamondData() {
        Optional<Vendor> blackDiamondVendors = vendorService.getVendorByName(VendorType.Black_Diamond.getValue());
        if (blackDiamondVendors.isPresent()) {
            Long blackDiamondVendorId = (long) blackDiamondVendors.get().getId();

            List<Account> filteredAccounts = new ArrayList<>();

            // Fetch accounts for the Black Diamond vendor
            filteredAccounts.addAll(accountService.getDistinctAccountsByVendorId(blackDiamondVendorId));

            // Remove account with account_number MOORINGCAP_2 from filtered accounts
            filteredAccounts.removeIf(account -> "MOORINGCAP_2".equals(account.getNumber()));

            // Remove duplicate accounts based on account number
            Map<String, List<Account>> accountsByNumber = filteredAccounts.stream()
                    .collect(Collectors.groupingBy(Account::getNumber));
            
            filteredAccounts = accountsByNumber.entrySet().stream()
                    .map(entry -> {
                        List<Account> duplicateAccounts = entry.getValue();
                        // If there are duplicates, keep only one account
                        if (duplicateAccounts.size() > 1) {
                            long trueCount = duplicateAccounts.stream()
                                    .filter(Account::isManaged)
                                    .count();
                            long falseCount = duplicateAccounts.stream()
                                    .filter(account -> !account.isManaged())
                                    .count();
                            
                            // If there are multiple true values, keep only one
                            if (trueCount >= 1) {
                                return duplicateAccounts.stream()
                                        .filter(Account::isManaged)
                                        .findFirst()
                                        .orElse(duplicateAccounts.get(0));
                            }
                            // If there are only false values, keep only one
                            else {
                                return duplicateAccounts.stream()
                                        .filter(account -> !account.isManaged())
                                        .findFirst()
                                        .orElse(duplicateAccounts.get(0));
                            }
                        } else {
                            return duplicateAccounts.get(0);
                        }
                    })
                    .collect(Collectors.toList());

            // Set advisor names and filter accounts

            
            // Set relationship data for accounts
            accountService.setRelationshipDataForAccounts(filteredAccounts);
            
            // Batch fetch custodian names and holding values for performance
            List<Long> accountIds = filteredAccounts.stream()
                    .map(account -> (long) account.getId())
                    .toList();

            Map<Long, Map<String, String>> namesMap = accountDataHelper.getAdvisorAndCustodianNamesByAccountIds(accountIds);


// Map filtered accounts to DataResponse
            return filteredAccounts.stream()
                    .map(account -> {
                        DataResponse response = new DataResponse();
                        response.setAccountNumber(account.getNumber());
                        response.setAccountName(account.getName());
                        
                        Map<String, String> names = namesMap.get((long) account.getId());
                        response.setDataProvider(names != null ? names.get("custodian") : null);
                        
                        response.setIsSupervised(account.isManaged());

                        Map<Integer, Double> marketValueMap =
                                holdingService.getTotalValuesByAccountIds(accountIds);

                        Double totalValue = marketValueMap.get(account.getId());
                        response.setMarketValue(totalValue != null ? totalValue : 0.0);

                        response.setAum(account.isAum());
                        
                        if(account.getAlternativeInvestmentType()!=null && account.getAlternativeInvestmentType().contains("External")){
                        response.setAua(true);
                        }

                        response.setAdvisor(account.getAdvisor() != null ? account.getAdvisor() : "Avestar");
                        response.setRelationshipManager(account.getRelationshipManager());
                        response.setRelationshipName(account.getRelationshipName());
                        response.setAsOfDate(account.getAsOfDate() != null ? account.getAsOfDate().toString() : null);
                        response.setStartDate(account.getCreatedTimestamp() != null ? account.getCreatedTimestamp().toString() : null);
                        response.setPlatform(VendorType.Black_Diamond.getValue());
                        return response;
                    })
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    

}
