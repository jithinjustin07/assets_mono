package com.aumReport.aum.controller;

import com.aumReport.aum.dto.DataResponse;
import com.aumReport.aum.service.AumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/aum")
public class AumController {

    @Autowired
    AumService aumService;


    @PostMapping("/upload")
    public ResponseEntity<String> uploadData(@RequestBody String request) {
        try {
            {
//                "task":"Addepar Upload",
//                    "filename":"Avestarone_Download_02-13-2026-Addepar(1).csv",
//
//                    "content":
            }
//            aumService.uploadData();
            return new ResponseEntity<>("Data uploaded successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to upload data: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/data")
    public ResponseEntity<List<DataResponse>> getData() {
        List<DataResponse> data = aumService.getData();
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

}
