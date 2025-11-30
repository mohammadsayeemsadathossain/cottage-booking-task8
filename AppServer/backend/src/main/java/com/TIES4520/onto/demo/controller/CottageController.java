package com.TIES4520.onto.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.TIES4520.onto.demo.model.BookingSuggestion;
import com.TIES4520.onto.demo.model.Cottage;
import com.TIES4520.onto.demo.service.CottageService;

@RestController
@RequestMapping("/api/cottages")
public class CottageController {

    @Autowired
    private CottageService service;

    @GetMapping
    public ResponseEntity<List<Cottage>> getCottages() {
        return ResponseEntity.ok(service.getCottages());
    }

    @PostMapping
    public ResponseEntity<Void> addCottage(@RequestBody Cottage cottage) {
        service.addCottage(cottage);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateCottages(@RequestBody Cottage cottage) {
    	// Need to check booking before updating
        service.updateCottages(cottage);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCottage(@PathVariable String id) {
    	// Need to check booking before deleting
        service.deleteCottage(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<BookingSuggestion>> suggestions(
    		@RequestParam(required = false) String bookerName,
            @RequestParam int requiredPlaces,
            @RequestParam int requiredBedrooms,
            @RequestParam int maxLakeDistanceMeters,
            @RequestParam(required = false) String city,
            @RequestParam int maxCityDistanceMeters,
            @RequestParam String startDay,     // dd.MM.yyyy
            @RequestParam int requiredDays,    // nights
            @RequestParam int maxStartShiftDays
    ) {
        var results = service.searchSuggestions(
                bookerName,
                requiredPlaces, requiredBedrooms, maxLakeDistanceMeters,
                city, maxCityDistanceMeters,
                startDay, requiredDays, maxStartShiftDays
        );
        return ResponseEntity.ok(results);
    }
}
