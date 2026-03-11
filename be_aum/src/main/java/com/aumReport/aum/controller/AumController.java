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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/aum")
public class AumController {

    @Autowired
    AumService aumService;

    @GetMapping("/data")
    public ResponseEntity<List<DataResponse>> getData(@RequestParam(value = "aum", required = false) Boolean aum) {
        List<DataResponse> data = aumService.getData(aum);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

}
