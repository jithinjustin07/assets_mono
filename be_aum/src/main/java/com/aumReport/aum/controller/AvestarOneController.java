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
@RequestMapping("/api/upload/v2")
public class AvestarOneController {

    @Autowired
    private FileUploadService service;

    @PostMapping("/bdV2")
    public ResponseEntity<String> blackDiamondUpload(@RequestParam("file") MultipartFile file) throws IOException {
        service.blackDiamondUploadV2(file);
        return ResponseEntity.ok("bd file uploaded successfully");
    }

    @PostMapping("/addeparV2")
    public ResponseEntity<String> addeparUpload(@RequestParam("file") MultipartFile file) throws IOException {
        service.addeparUploadV2(file);
        return ResponseEntity.ok("addepar file uploaded successfully");
    }


}
