package com.aumReport.aum.controller;

import com.aumReport.aum.entity.Relationship;
import com.aumReport.aum.service.RelationshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/relationships")
@CrossOrigin(origins = "*")
public class RelationshipController {

}
