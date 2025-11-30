package com.TIES4520.onto.demo.controller;

import com.TIES4520.onto.demo.model.User;
import com.TIES4520.onto.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService service;

    @GetMapping
    public ResponseEntity<List<User>>  getAll(@RequestParam(required = false) String orderBy) { 
    	return ResponseEntity.ok(service.getAllUsers(orderBy)); 
    }

    @GetMapping("/filter")
    public ResponseEntity<List<User>> getWithFilter(@RequestParam String filter, @RequestParam(required = false) String orderBy) { 
    	return ResponseEntity.ok(service.getAllUsersWithFilter(filter, orderBy)); 
    }
    
    @PostMapping
    public ResponseEntity<Void> add(@RequestBody User u) { 
    	service.addUser(u); 
    	return ResponseEntity.ok().build();
    }
    
    @PutMapping
    public ResponseEntity<Void> updateUsers(@RequestParam String condition, @RequestBody User user) { 
    	service.updateUsers(condition, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsers(@PathVariable String id) { 
    	service.deleteUser(id); 
    	return ResponseEntity.ok().build();
    }
}
