package com.aumReport.aum.serviceImpl;

import com.aumReport.aum.entity.Account;
import com.aumReport.aum.entity.AddeparAccount;
import com.aumReport.aum.entity.BlackDiamondAccount;
import com.aumReport.aum.entity.Custodian;
import com.aumReport.aum.entity.Holding;
import com.aumReport.aum.entity.Relationship;
import com.aumReport.aum.entity.Vendor;
import com.aumReport.aum.enums.VendorType;
import com.aumReport.aum.repo.AccountRepository;
import com.aumReport.aum.repo.AddeparAccountRepository;
import com.aumReport.aum.repo.BlackDiamondAccountRepository;
import com.aumReport.aum.repo.CustodianRepository;
import com.aumReport.aum.repo.HoldingRepository;
import com.aumReport.aum.repo.RelationshipRepository;
import com.aumReport.aum.repo.VendorRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aumReport.aum.service.FileUploadService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    CustodianRepository custodianRepository;

    @Autowired
    RelationshipRepository relationshipRepository;

    @Autowired
    HoldingRepository holdingRepository;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    private BlackDiamondAccountRepository blackDiamondAccountRepository;

    @Autowired
    private AddeparAccountRepository addeparAccountRepository;

    // Helper method to safely get string value from cell
    private String getCellStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void blackDiamondUpload(MultipartFile file) throws IOException {
        
        // Validate filename contains "bd"
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().contains("bd")) {
            throw new IllegalArgumentException("Filename must contain 'bd' for Black Diamond upload");
        }

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        List<Account> batch = new ArrayList<>();
        List<Holding> holdingsBatch = new ArrayList<>();
        List<String> marketValues = new ArrayList<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        Map<String, Custodian> custodianMap = custodianRepository
                .findAll()
                .stream()
                .collect(Collectors.toMap(
                        c -> c.getName().toLowerCase(),
                        c -> c
                ));

        Map<String, Relationship> relationshipMap = relationshipRepository
                .findAll()
                .stream()
                .collect(Collectors.toMap(
                        r -> r.getName().toLowerCase(),
                        r -> r
                ));

        Map<String, Vendor> vendorMap = vendorRepository
                .findAll()
                .stream()
                .collect(Collectors.toMap(
                        v -> v.getName().toLowerCase(),
                        v -> v
                ));

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);

            String accountName = getCellStringValue(row, 7);
            String accountNumber = getCellStringValue(row, 6);
            String isAum = getCellStringValue(row, 10);
            String marketValue = getCellStringValue(row, 17);
            String asOfDate = getCellStringValue(row, 18);
            String advisor = getCellStringValue(row, 19);

            String custodianName = getCellStringValue(row, 8);
            String relationshipName = getCellStringValue(row, 2);

            Account account = new Account();
            account.setAdvisor(advisor);
            account.setName(accountName);
            account.setNumber(accountNumber);
            account.setCreatedTimestamp(LocalDateTime.now());
            account.setUpdatedTimestamp(LocalDateTime.now());
            account.setAum("yes".equalsIgnoreCase(isAum.trim()));
            
            // Parse asOfDate from "02/13/2026" format
            if (asOfDate != null && !asOfDate.trim().isEmpty()) {
                LocalDateTime parsedDate = LocalDateTime.parse(asOfDate.trim() + " 00:00:00", 
                    DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));
                account.setAsOfDate(parsedDate);
            }

            // Handle relationship
            Relationship relationship = relationshipMap.get(relationshipName.toLowerCase());

            // IF relationship not present → create new one
            if (relationship == null) {

                relationship = new Relationship();
                relationship.setName(relationshipName);
                relationship.setCreatedTimestamp(LocalDateTime.now());
                relationship.setUpdatedTimestamp(LocalDateTime.now());
                relationship.setActive(true);
                relationship.setImplementationFlag(false);

                relationship = relationshipRepository.save(relationship);

                relationshipMap.put(relationshipName.toLowerCase(), relationship);
            }

            account.setRelationshipId(relationship.getId());

            Custodian custodian = custodianMap.get(custodianName.toLowerCase());

            // IF custodian not present → create new one
            if (custodian == null) {

                custodian = new Custodian();
                custodian.setName(custodianName);
                custodian.setCreatedTimestamp(LocalDateTime.now());
                custodian.setUpdatedTimestamp(LocalDateTime.now());

                custodian = custodianRepository.save(custodian);

                custodianMap.put(custodianName.toLowerCase(), custodian);
            }

            account.setCustodianId(custodian.getId());

            // Handle vendor - always use "Black Diamond" as vendor name
            String vendorName = VendorType.Black_Diamond.getValue();
            Vendor vendor = vendorMap.get(vendorName.toLowerCase());

            // IF vendor not present → create new one
            if (vendor == null) {

                vendor = new Vendor();
                vendor.setName(vendorName);
                vendor.setCreatedTimestamp(LocalDateTime.now());
                vendor.setUpdatedTimestamp(LocalDateTime.now());

                vendor = vendorRepository.save(vendor);

                vendorMap.put(vendorName.toLowerCase(), vendor);
            }

            account.setVendorId(vendor.getId());

            batch.add(account);
            marketValues.add(marketValue);

            if (batch.size() == 1000) {
                List<Account> savedAccounts = accountRepository.saveAll(batch);
                
                // Create holdings for saved accounts
                for (int j = 0; j < savedAccounts.size(); j++) {
                    Account savedAccount = savedAccounts.get(j);
                    String holdingMarketValue = marketValues.get(j);
                    
                    if (holdingMarketValue != null && !holdingMarketValue.trim().isEmpty()) {
                        Holding holding = new Holding();
                        holding.setAccountId(savedAccount.getId());
                        holding.setValue(Double.parseDouble(holdingMarketValue.replace(",", "").trim()));
                        holding.setCreatedTimestamp(LocalDateTime.now());
                        holding.setUpdatedTimestamp(LocalDateTime.now());
                        
                        holdingsBatch.add(holding);
                    }
                }
                
                // Save holdings batch if it reaches 1000
                if (holdingsBatch.size() >= 1000) {
                    holdingRepository.saveAll(holdingsBatch);
                    holdingsBatch.clear();
                }
                
                batch.clear();
                marketValues.clear();
            }
        }

        if (!batch.isEmpty()) {
            List<Account> savedAccounts = accountRepository.saveAll(batch);
            
            // Create holdings for remaining saved accounts
            for (int j = 0; j < savedAccounts.size(); j++) {
                Account savedAccount = savedAccounts.get(j);
                String holdingMarketValue = marketValues.get(j);
                
                if (holdingMarketValue != null && !holdingMarketValue.trim().isEmpty()) {
                    Holding holding = new Holding();
                    holding.setAccountId(savedAccount.getId());
                    holding.setValue(Double.parseDouble(holdingMarketValue.replace(",", "").trim()));
                    holding.setCreatedTimestamp(LocalDateTime.now());
                    holding.setUpdatedTimestamp(LocalDateTime.now());
                    
                    holdingsBatch.add(holding);
                }
            }
        }

        // Save any remaining holdings
        if (!holdingsBatch.isEmpty()) {
            holdingRepository.saveAll(holdingsBatch);
        }

        workbook.close();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addeparUpload(MultipartFile file) throws IOException {
        
        // Validate filename contains "addepar"
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().contains("addepar")) {
            throw new IllegalArgumentException("Filename must contain 'addepar' for Addepar upload");
        }

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

//        // Delete first 3 rows (indices 0, 1, 2)
//        for (int i = 2; i >= 0; i--) {
//            Row row = sheet.getRow(i);
//            if (row != null) {
//                sheet.removeRow(row);
//            }
//        }

        List<Account> batch = new ArrayList<>();
        List<Holding> holdingsBatch = new ArrayList<>();
        List<String> marketValues = new ArrayList<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        Map<String, Custodian> custodianMap = custodianRepository
                .findAll()
                .stream()
                .collect(Collectors.toMap(
                        c -> c.getName().toLowerCase(),
                        c -> c
                ));

        Map<String, Relationship> relationshipMap = relationshipRepository
                .findAll()
                .stream()
                .collect(Collectors.toMap(
                        r -> r.getName().toLowerCase(),
                        r -> r
                ));

        Map<String, Vendor> vendorMap = vendorRepository
                .findAll()
                .stream()
                .collect(Collectors.toMap(
                        v -> v.getName().toLowerCase(),
                        v -> v
                ));

        for (int i = 4; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            String accountName = getCellStringValue(row, 0);
            String accountNumber = getCellStringValue(row, 6);
            String isAum = getCellStringValue(row, 10);
            String asOfDate = getCellStringValue(row, 15); // Feb 13, 2026
            String marketValue = getCellStringValue(row, 14); //  $29,295,521.90

            String custodianName = getCellStringValue(row, 8);
            String relationshipName = getCellStringValue(row, 5);

            Account account = new Account();
            account.setName(accountName);
            account.setNumber(accountNumber);
            account.setCreatedTimestamp(LocalDateTime.now());
            account.setUpdatedTimestamp(LocalDateTime.now());
            account.setAum("yes".equalsIgnoreCase(isAum.trim()));

            // Parse asOfDate from "Feb 13, 2026" or "Feb 2, 2024" format
            if (asOfDate != null && !asOfDate.trim().isEmpty()) {
                try {
                    LocalDateTime parsedDate = LocalDateTime.parse(asOfDate.trim() + " 00:00:00",
                            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));
                    account.setAsOfDate(parsedDate);
                } catch (Exception e) {
                    try {
                        // Try alternative format for single digit days: "MMM d, yyyy"
                        LocalDateTime parsedDate = LocalDateTime.parse(asOfDate.trim() + " 00:00:00",
                                DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm:ss"));
                        account.setAsOfDate(parsedDate);
                    } catch (Exception e2) {
                        // If both formats fail, skip setting the date
                        System.err.println("Failed to parse date: " + asOfDate);
                    }
                }
            }

            // Handle relationship
            Relationship relationship = relationshipMap.get(relationshipName.toLowerCase());

            // IF relationship not present → create new one
            if (relationship == null) {

                relationship = new Relationship();
                relationship.setName(relationshipName);
                relationship.setCreatedTimestamp(LocalDateTime.now());
                relationship.setUpdatedTimestamp(LocalDateTime.now());
                relationship.setActive(true);
                relationship.setImplementationFlag(false);

                relationship = relationshipRepository.save(relationship);

                relationshipMap.put(relationshipName.toLowerCase(), relationship);
            }

            account.setRelationshipId(relationship.getId());

            Custodian custodian = custodianMap.get(custodianName.toLowerCase());

            // IF custodian not present → create new one
            if (custodian == null) {

                custodian = new Custodian();
                custodian.setName(custodianName);
                custodian.setCreatedTimestamp(LocalDateTime.now());
                custodian.setUpdatedTimestamp(LocalDateTime.now());

                custodian = custodianRepository.save(custodian);

                custodianMap.put(custodianName.toLowerCase(), custodian);
            }

            account.setCustodianId(custodian.getId());

            // Handle vendor - always use "Black Diamond" as vendor name
            String vendorName = VendorType.Addepar.getValue();
            Vendor vendor = vendorMap.get(vendorName.toLowerCase());

            // IF vendor not present → create new one
            if (vendor == null) {

                vendor = new Vendor();
                vendor.setName(vendorName);
                vendor.setCreatedTimestamp(LocalDateTime.now());
                vendor.setUpdatedTimestamp(LocalDateTime.now());

                vendor = vendorRepository.save(vendor);

                vendorMap.put(vendorName.toLowerCase(), vendor);
            }

            account.setVendorId(vendor.getId());

            batch.add(account);
            marketValues.add(marketValue);

            if (batch.size() == 1000) {
                List<Account> savedAccounts = accountRepository.saveAll(batch);

                // Create holdings for saved accounts
                for (int j = 0; j < savedAccounts.size(); j++) {
                    Account savedAccount = savedAccounts.get(j);
                    String holdingMarketValue = marketValues.get(j);

                    if (holdingMarketValue != null && !holdingMarketValue.trim().isEmpty()) {
                        Holding holding = new Holding();
                        holding.setAccountId(savedAccount.getId());
                        // Parse market value from "$29,295,521.90" format
                        String cleanValue = holdingMarketValue.replace("$", "").replace(",", "").trim();
                        
                        // Skip if value is "-" or cannot be parsed as a number
                        if (!"-".equals(cleanValue) && !cleanValue.isEmpty()) {
                            try {
                                holding.setValue(Double.parseDouble(cleanValue));
                                holding.setCreatedTimestamp(LocalDateTime.now());
                                holding.setUpdatedTimestamp(LocalDateTime.now());
                                holdingsBatch.add(holding);
                            } catch (NumberFormatException e) {
                                // Skip if value cannot be parsed as double
                                System.err.println("Failed to parse market value: " + holdingMarketValue);
                            }
                        }
                    }
                }

                // Save holdings batch if it reaches 1000
                if (holdingsBatch.size() >= 1000) {
                    holdingRepository.saveAll(holdingsBatch);
                    holdingsBatch.clear();
                }

                batch.clear();
                marketValues.clear();
            }
        }

        if (!batch.isEmpty()) {
            List<Account> savedAccounts = accountRepository.saveAll(batch);

            // Create holdings for remaining saved accounts
            for (int j = 0; j < savedAccounts.size(); j++) {
                Account savedAccount = savedAccounts.get(j);
                String holdingMarketValue = marketValues.get(j);

                if (holdingMarketValue != null && !holdingMarketValue.trim().isEmpty()) {
                    Holding holding = new Holding();
                    holding.setAccountId(savedAccount.getId());
                    // Parse market value from "$29,295,521.90" format
                    String cleanValue = holdingMarketValue.replace("$", "").replace(",", "").trim();
                    
                    // Skip if value is "-" or cannot be parsed as a number
                    if (!"-".equals(cleanValue) && !cleanValue.isEmpty()) {
                        try {
                            holding.setValue(Double.parseDouble(cleanValue));
                            holding.setCreatedTimestamp(LocalDateTime.now());
                            holding.setUpdatedTimestamp(LocalDateTime.now());
                            holdingsBatch.add(holding);
                        } catch (NumberFormatException e) {
                            // Skip if value cannot be parsed as double
                            System.err.println("Failed to parse market value: " + holdingMarketValue);
                        }
                    }
                }
            }
        }

        // Save any remaining holdings
        if (!holdingsBatch.isEmpty()) {
            holdingRepository.saveAll(holdingsBatch);
        }

        workbook.close();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void investmentTypeUpload(MultipartFile file) throws IOException {

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().contains("alternative")) {
            throw new IllegalArgumentException("Insert valid AvestarOne Data Provider list");
        }
        
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        // Read all rows from the sheet (skip header row)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue; // Skip null rows
            }

            // Read Account Number (column 0) and Data Provider (column 2)
            String accountNumber = getCellStringValue(row, 0);
            String dataProvider = getCellStringValue(row, 2);

            // Skip if account number is empty
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                continue;
            }

            // Search for accounts by number (handle multiple accounts with same number)
            List<Account> accounts = accountRepository.findAllByNumber(accountNumber.trim());
            
            // If accounts found, update the alternativeInvestmentType field for all of them
            if (!accounts.isEmpty() && dataProvider != null && !dataProvider.trim().isEmpty()) {
                for (Account account : accounts) {
                    account.setAlternativeInvestmentType(dataProvider.trim());
                    
                    // Update custodian ID based on alternative investment type
                    if(account.getAlternativeInvestmentType().contains("Internal")) {
                        // Search for "Internal Investment" custodian
                        Optional<Custodian> internalCustodian = custodianRepository.findByNameIgnoreCase("Internal Alternative");
                        if (internalCustodian.isPresent()) {
                            account.setCustodianId(internalCustodian.get().getId());
                        } else {
                            // Create "Internal Investment" custodian if not found
                            Custodian newCustodian = new Custodian();
                            newCustodian.setName("Internal Alternative");
                            newCustodian.setCreatedTimestamp(LocalDateTime.now());
                            newCustodian.setUpdatedTimestamp(LocalDateTime.now());
                            Custodian savedCustodian = custodianRepository.save(newCustodian);
                            account.setCustodianId(savedCustodian.getId());
                        }
                    } if(account.getAlternativeInvestmentType().contains("External")) {
                        // Search for "External Investment" custodian
                        Optional<Custodian> externalCustodian = custodianRepository.findByNameIgnoreCase("External Alternative");
                        if (externalCustodian.isPresent()) {
                            account.setCustodianId(externalCustodian.get().getId());
                        } else {
                            // Create "External Investment" custodian if not found
                            Custodian newCustodian = new Custodian();
                            newCustodian.setName("External Alternative");
                            newCustodian.setCreatedTimestamp(LocalDateTime.now());
                            newCustodian.setUpdatedTimestamp(LocalDateTime.now());
                            Custodian savedCustodian = custodianRepository.save(newCustodian);
                            account.setCustodianId(savedCustodian.getId());
                        }
                    }
                }
                
                // Save all updated accounts
                accountRepository.saveAll(accounts);

            }
        }

        workbook.close();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Deprecated
    public void relationshipManager(MultipartFile file) throws IOException {
        
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        // Read all rows from the sheet (skip header row)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue; // Skip null rows
            }

            // Read Household name (column 0) and Primary RM (column 1)
            String householdName = getCellStringValue(row, 0);
            String primaryRM = getCellStringValue(row, 1);

            // Skip if household name is empty
            if (householdName == null || householdName.trim().isEmpty()) {
                continue;
            }

            // Search for relationship by name
            Optional<Relationship> relationshipOpt = relationshipRepository.findByName(householdName.trim());
            
            if (relationshipOpt.isPresent()) {
                // Relationship found - check if relationship manager is empty
                Relationship relationship = relationshipOpt.get();
                
                if (relationship.getRelationshipManager() == null || 
                    relationship.getRelationshipManager().trim().isEmpty()) {
                    
                    // Update relationship manager if it's empty
                    if (primaryRM != null && !primaryRM.trim().isEmpty()) {
                        relationship.setRelationshipManager(primaryRM.trim());
                        relationshipRepository.save(relationship);
                    }
                }
                // If manager already exists, do nothing
            } else {
                // Relationship not found - create new one
                if (primaryRM != null && !primaryRM.trim().isEmpty()) {
                    Relationship newRelationship = new Relationship();
                    newRelationship.setName(householdName.trim());
                    newRelationship.setRelationshipManager(primaryRM.trim());
                    newRelationship.setCreatedTimestamp(LocalDateTime.now());
                    newRelationship.setUpdatedTimestamp(LocalDateTime.now());
                    newRelationship.setActive(true);
                    newRelationship.setImplementationFlag(false);
                    
                    relationshipRepository.save(newRelationship);
                }
            }
        }

        workbook.close();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addeparAdvisorUpdate(MultipartFile file) throws IOException {
        // Implementation to be added
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bdAdvisorUpdate(MultipartFile file) throws IOException {
//        String filename = file.getOriginalFilename();
//        if (filename == null || !filename.toLowerCase().contains("advisor")) {
//            throw new IllegalArgumentException("Insert valid Black Diamond Advisor update file");
//        }
        
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        // Read all rows from the sheet (skip header row)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue; // Skip null rows
            }

            // Read Account Name (column 6, index 6) and Advisor (column 18, index 18)
            String accountName = getCellStringValue(row, 6);
            String advisor = getCellStringValue(row, 19);

            // Skip if account name is empty
            if (accountName == null || accountName.trim().isEmpty()) {
                continue;
            }

            // Search for accounts by name
            List<Account> accounts = accountRepository.findByName(accountName.trim());
            
            // If accounts found, update the advisor field for all of them
            if (!accounts.isEmpty() && advisor != null && !advisor.trim().isEmpty()) {
                for (Account account : accounts) {
                    account.setAdvisor(advisor.trim());
                }
                
                // Save all updated accounts
                accountRepository.saveAll(accounts);
            }
        }

        workbook.close();
    }

        @Override
        @Transactional
        public void blackDiamondUploadV2(MultipartFile file) throws IOException {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            List<BlackDiamondAccount> batch = new ArrayList<>();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

            for (Row row : sheet) {

                if (row.getRowNum() == 0) {
                    continue; // skip header
                }

                BlackDiamondAccount entity = new BlackDiamondAccount();

                entity.setAccountIsOpen(parseBoolean(getCellValue(row.getCell(0))));
                entity.setRelationshipId(getCellValue(row.getCell(1)));
                entity.setRelationshipName(getCellValue(row.getCell(2)));

                entity.setPortfolioId(getCellValue(row.getCell(3)));
                entity.setPortfolioName(getCellValue(row.getCell(4)));

                entity.setAccountId(getCellValue(row.getCell(5)));
                entity.setAccountNumber(getCellValue(row.getCell(6)));
                entity.setAccountName(getCellValue(row.getCell(7)));

                entity.setCustodian(getCellValue(row.getCell(8)));
                entity.setGoal(getCellValue(row.getCell(9)));

                entity.setAum(getCellValue(row.getCell(10)));

                entity.setTaxStatus(getCellValue(row.getCell(11)));
                entity.setAccountReportingTarget(getCellValue(row.getCell(12)));
                entity.setAccountBenchmark(getCellValue(row.getCell(13)));

                entity.setAssetId(parseLong(getCellValue(row.getCell(14))));
                entity.setAssetName(getCellValue(row.getCell(15)));

                entity.setClassName(getCellValue(row.getCell(16)));

                entity.setMarketValue(new BigDecimal(getCellValue(row.getCell(17))));

                entity.setAsOfDate(LocalDate.parse(getCellValue(row.getCell(18)), formatter));

                entity.setAdvisor(getCellValue(row.getCell(20)));

                entity.setIsSupervised(toSupervisedBoolean(getCellValue(row.getCell(19))));



                batch.add(entity);
            }

            blackDiamondAccountRepository.saveAll(batch);

            workbook.close();
        }

        Boolean toSupervisedBoolean(String value) {
            if (value == null || value.isBlank()) return null;
            return value.equalsIgnoreCase("Supervised") || value.equalsIgnoreCase("Unsupervised");
        }

        @Override
        @Transactional
        public void addeparUploadV2(MultipartFile file) throws IOException {

            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            List<AddeparAccount> batch = new ArrayList<>();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

            for (Row row : sheet) {

                if (row.getRowNum() < 3) {
                    continue; // skip first 3 rows
                }

                if (row.getRowNum() == 3) {
                    continue; // skip header row
                }

                AddeparAccount entity = new AddeparAccount();

                entity.setHoldingAccount(getCellValue(row.getCell(0)));

                entity.setEntityId(parseLong(getCellValue(row.getCell(1))));

                entity.setModelType(getCellValue(row.getCell(2)));

                entity.setDirectOwnerId(parseLong(getCellValue(row.getCell(3))));

                entity.setTopLevelOwnerId(parseLong(getCellValue(row.getCell(4))));

                entity.setTopLevelOwner(getCellValue(row.getCell(5)));

                entity.setAcAccountNumber(getCellValue(row.getCell(6)));

                entity.setAcGoal(getCellValue(row.getCell(7)));

                entity.setAcCustodian(getCellValue(row.getCell(8)));

                entity.setAcAvestar(parseBoolean(getCellValue(row.getCell(9))));

                entity.setAcAum(parseBoolean(getCellValue(row.getCell(10))));

                entity.setAcReportingTarget(getCellValue(row.getCell(11)));

                entity.setAcAssetClass(getCellValue(row.getCell(12)));

                entity.setAcSubAssetClass(getCellValue(row.getCell(13)));

                entity.setAdjustedValueUsd(parseCurrency(getCellValue(row.getCell(14))));

                entity.setAcLastActivityDate(parseDate(row.getCell(15), formatter));
                entity.setIsSupervised(toSupervisedBoolean(getCellValue(row.getCell(16))));

                batch.add(entity);
            }

            addeparAccountRepository.saveAll(batch);

            workbook.close();
        }


    private String getCellValue(Cell cell) {

        if (cell == null) return "";

        switch (cell.getCellType()) {

            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            default:
                return "";
        }
    }

    private Long parseLong(String value) {

        if (value == null || value.isEmpty()) return null;

        return (long) Double.parseDouble(value);
    }

    private Boolean parseBoolean(String value) {

        if (value == null) return false;

        return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
    }

    private BigDecimal parseCurrency(String value) {

        if (value == null || value.isEmpty()) return BigDecimal.ZERO;

        if(value.equalsIgnoreCase("-")){
            return BigDecimal.ZERO;
        }

        value = value.replace("$", "").replace(",", "");

        return new BigDecimal(value);
    }

    private LocalDate parseDate(Cell cell, DateTimeFormatter formatter) {

        if (cell == null) return null;

        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue();
            if (value == null || value.isEmpty() || value.equalsIgnoreCase("-")) {
                return null;
            }
            return LocalDate.parse(value, formatter);
        }

        if (cell.getCellType() == CellType.NUMERIC) {

            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }

            // Excel serial date case
            double excelDate = cell.getNumericCellValue();
            return DateUtil.getLocalDateTime(excelDate).toLocalDate();
        }

        if (cell.getCellType() == CellType.STRING) {

            String value = cell.getStringCellValue();

            if (value == null || value.isEmpty()) return null;

            return LocalDate.parse(value, formatter);
        }

        return null;
    }



}
