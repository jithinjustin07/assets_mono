package com.aumReport.aum.serviceImpl;

import com.aumReport.aum.entity.Account;
import com.aumReport.aum.entity.Custodian;
import com.aumReport.aum.entity.Holding;
import com.aumReport.aum.entity.Relationship;
import com.aumReport.aum.entity.Vendor;
import com.aumReport.aum.repo.AccountRepository;
import com.aumReport.aum.repo.CustodianRepository;
import com.aumReport.aum.repo.HoldingRepository;
import com.aumReport.aum.repo.RelationshipRepository;
import com.aumReport.aum.repo.VendorRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aumReport.aum.service.FileUploadService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public void blackDiamondUpload(MultipartFile file) throws IOException {

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
            String asOfDate = getCellStringValue(row, 18);
            String marketValue = getCellStringValue(row, 17);

            String custodianName = getCellStringValue(row, 8);
            String relationshipName = getCellStringValue(row, 2);

            Account account = new Account();
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
                relationship.setLocation("Default");
                relationship.setType("Default");
                relationship.setVersionName("v1.0");

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
            String vendorName = "Black Diamond";
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
    
}
