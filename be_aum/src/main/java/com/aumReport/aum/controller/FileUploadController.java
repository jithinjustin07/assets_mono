package com.aumReport.aum.controller;


import com.aumReport.aum.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService service;

    @PostMapping("/bd")
    public ResponseEntity<String> blackDiamondUpload(@RequestParam("file") MultipartFile file) throws IOException {
        service.blackDiamondUpload(file);
        return ResponseEntity.ok("bd file uploaded successfully");
    }

    @PostMapping("/addepar")
    public ResponseEntity<String> addeparUpload(@RequestParam("file") MultipartFile file) throws IOException {
        service.addeparUpload(file);
        return ResponseEntity.ok("addepar file uploaded successfully");
    }

    @PostMapping("/investment/type")
    public ResponseEntity<String> investmentType(@RequestParam("file") MultipartFile file) throws IOException {
        service.investmentTypeUpload(file);
        return ResponseEntity.ok("investment type file uploaded successfully");
    }

@PostMapping("/addeparadvisorupdate")
    public ResponseEntity<String> addeparAdvisorUpdate(@RequestParam("file") MultipartFile file) throws IOException {
        service.addeparAdvisorUpdate(file);
        return ResponseEntity.ok("addepar advisor update file uploaded successfully");
    }

    @PostMapping("/bdadvisorupdate")
    public ResponseEntity<String> bdAdvisorUpdate(@RequestParam("file") MultipartFile file) throws IOException {
        service.bdAdvisorUpdate(file);
        return ResponseEntity.ok("bd advisor update file uploaded successfully");
    }




}
