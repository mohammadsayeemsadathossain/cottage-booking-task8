package com.TIES4520.onto.demo.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TIES4520.onto.demo.service.SswapServiceG09;

@RestController
@RequestMapping("/CottageService/g09")
public class SswapControllerG09 {

    @Autowired
    private ResourceLoader resourceLoader;
    
    @Autowired
    private SswapServiceG09 sswapService;

    // Define the RDF Media Type for Turtle
    public static final String TEXT_TURTLE = "text/turtle";
    
    /**
     * Handles the GET request to return the Service Description (RDG).
     * The RDG is a static file, and this is how the service is "exposed"
     * for discovery in the Semantic Web.
     * Maps to: http://localhost:8080/sswapdemo/api/searchCottageService
     */
    @GetMapping(
        value = "/api/searchCottageService", 
        produces = TEXT_TURTLE
    )
    public ResponseEntity<String> getServiceDescription() throws IOException {
        // Load the static RDG file (SearchCottage_RDG.txt)
        Resource resource = resourceLoader.getResource("classpath:g09_searchCottageService_RDG.ttl");
        String rdgContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        
        return ResponseEntity.ok()
                             .contentType(MediaType.valueOf(TEXT_TURTLE))
                             .body(rdgContent);
    }
    
    /**
     * Handles the POST request to execute the service logic.
     * It accepts an RRG (Request RDF Graph) and returns a RIG (Response RDF Graph).
     */
    @PostMapping(
        value = "/api/searchCottageService", 
        consumes = TEXT_TURTLE, 
        produces = TEXT_TURTLE
    )
    public ResponseEntity<String> executeSearch(
        @RequestBody String rrgTurtleContent
    ) {
        try {
            // 1. Parse the RRG to extract search parameters
            // 2. Execute the business logic (searchSuggestions from CottageService)
            // 3. Convert the results into a RIG (Response RDF Graph)
            String rigTurtleContent = sswapService.executeSearchCottageService(rrgTurtleContent);
            
            return ResponseEntity.ok()
                                 .contentType(MediaType.valueOf(TEXT_TURTLE))
                                 .body(rigTurtleContent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .contentType(MediaType.TEXT_PLAIN)
                                 .body("Error executing service: " + e.getMessage());
        }
    }
}