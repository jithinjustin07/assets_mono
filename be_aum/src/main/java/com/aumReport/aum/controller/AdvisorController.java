package com.aumReport.aum.controller;

import com.aumReport.aum.entity.Advisor;
import com.aumReport.aum.service.AdvisorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/advisors")
public class AdvisorController {

    @Autowired
    private AdvisorService advisorService;

    @GetMapping
    public ResponseEntity<List<Advisor>> getAllAdvisors() {
        List<Advisor> advisors = advisorService.getAllAdvisors();
        return new ResponseEntity<>(advisors, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Advisor> getAdvisorById(@PathVariable Long id) {
        Optional<Advisor> advisor = advisorService.getAdvisorById(id);
        return advisor.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/by-name")
    public ResponseEntity<Advisor> getAdvisorByName(@RequestParam String name) {
        Optional<Advisor> advisor = advisorService.getAdvisorByName(name);
        return advisor.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Advisor> createAdvisor(@RequestBody Advisor advisor) {
        Advisor savedAdvisor = advisorService.saveAdvisor(advisor);
        return new ResponseEntity<>(savedAdvisor, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Advisor> updateAdvisor(@PathVariable Long id, @RequestBody Advisor advisor) {
        Advisor updatedAdvisor = advisorService.updateAdvisor(id, advisor);
        if (updatedAdvisor != null) {
            return new ResponseEntity<>(updatedAdvisor, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvisor(@PathVariable Long id) {
        advisorService.deleteAdvisor(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
