package com.TIES4520.onto.demo.controller;

import java.util.List;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.TIES4520.onto.demo.model.Booking;
import com.TIES4520.onto.demo.model.BookingCreateRequest;
import com.TIES4520.onto.demo.service.BookingService;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {
	
	@Autowired
    private BookingService service;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody BookingCreateRequest req) {
        try {
            Booking b = service.create(req);
            return ResponseEntity.ok(b);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong");
        }
    }

    @GetMapping
    public ResponseEntity<List<Booking>> list(
            @RequestParam(required = false) String cottageID,
            @RequestParam(required = false) String bookerName) {
        return ResponseEntity.ok(service.list(cottageID, bookerName));
    }

    // Delete by booking number
    @DeleteMapping("/{bookingNumber}")
    public ResponseEntity<Void> delete(@PathVariable String bookingNumber) {
        service.deleteByNumber(bookingNumber);
        return ResponseEntity.ok().build();
    }
}
