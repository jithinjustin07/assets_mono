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
}
