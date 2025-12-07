package com.TIES4520.onto.demo.service;

import java.io.StringReader;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.TIES4520.onto.demo.model.BookingSuggestion;

@Service
public class SswapServiceG06 {
	
    @Autowired
    private CottageService cottageService;
	
    // Note: These prefixes are hardcoded to match the G06 RDG provided.
    private static final String G06_ONT_PREFIX = "cottage";
    private static final String G06_ONT_IRI = "http://127.0.0.1:8000/ontology/cottage.owl#";
    private static final String G06_RES_PREFIX = "resource";
    private static final String G06_RES_IRI = "http://127.0.0.1:8000/resource/";
    private static final String SSWAP_IRI = "http://sswapmeet.sswap.info/sswap/";
    
    // Injected properties from your application.properties
    @Value("${img.baseurl}")
    private String IMAGE_BASE_URL;
    
    // --- Helper for extracting values based on G06 ontology ---
    /** Helper to get the first value of a property for the input subject URI. */
    private String getFirstValue(Model model, ValueFactory vf, Resource subject, String predicateLocalName) {
        return model.filter(
            subject, 
            vf.createIRI(G06_ONT_IRI, predicateLocalName), 
            null
        ).stream().findFirst().map(stmt -> stmt.getObject().stringValue()).orElse(null);
    }
    
    public String executeCottageBookingService(String rrgTurtleContent) throws Exception {
        
        ValueFactory vf = SimpleValueFactory.getInstance();

        // IRI for G06 Request Type
        final IRI G06_BOOKING_REQ = vf.createIRI(G06_ONT_IRI, "BookingRequest");
        
        // 1. Parse the RRG to EXTRACT INPUT DATA
        Model rrgModel = Rio.parse(new StringReader(rrgTurtleContent), G06_RES_IRI, RDFFormat.TURTLE);
        Resource searchReqResource = rrgModel.filter(null, RDF.TYPE, G06_BOOKING_REQ).stream()
            .findFirst().map(stmt -> stmt.getSubject()).orElseThrow(() -> 
                new IllegalArgumentException("RRG structure is invalid (missing a resource typed as cottage:BookingRequest)."));
        
        // 2. Extract ALL input parameters using G06 property names
        String bookerName = getFirstValue(rrgModel, vf, searchReqResource, "bookerName");
        // cottage:requiredPlaces -> numberOfPeople
        String requiredPlaces = getFirstValue(rrgModel, vf, searchReqResource, "requiredPlaces");
        // cottage:requiredBedrooms -> numberOfBedrooms
        String requiredBedrooms = getFirstValue(rrgModel, vf, searchReqResource, "requiredBedrooms");
        // cottage:maxDistanceLake -> maxDistanceFromLake
        String maxLakeDistanceMeters = getFirstValue(rrgModel, vf, searchReqResource, "maxDistanceLake");
        // cottage:city -> cityName
        String city = getFirstValue(rrgModel, vf, searchReqResource, "city");
        // cottage:maxDistanceCity -> nearestCity
        String maxCityDistanceMeters = getFirstValue(rrgModel, vf, searchReqResource, "maxDistanceCity"); 
        // cottage:startDate -> startDate
        String startDate = getFirstValue(rrgModel, vf, searchReqResource, "startDate"); 
        // cottage:requiredDays -> requiredDays
        String requiredDays = getFirstValue(rrgModel, vf, searchReqResource, "requiredDays");
        // cottage:maxShiftDays -> possibleShift
        String maxStartShiftDays = getFirstValue(rrgModel, vf, searchReqResource, "maxShiftDays");
        
        // 3. Execute the business logic
        DateTimeFormatter IN = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter OUT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String startDay_ddMMyyyy = OUT.format(IN.parse(startDate));

        List<BookingSuggestion> suggestions = cottageService.searchSuggestions(
                bookerName, 
                Integer.parseInt(requiredPlaces), 
                Integer.parseInt(requiredBedrooms), 
                Integer.parseInt(maxLakeDistanceMeters),
                city, 
                Integer.parseInt(maxCityDistanceMeters), 
                startDay_ddMMyyyy, 
                Integer.parseInt(requiredDays), 
                Integer.parseInt(maxStartShiftDays)
        );

        // 4. MANUALLY BUILD THE TURTLE OUTPUT STRING (Matching G06 RDG structure)
        StringBuilder output = new StringBuilder();

        // Prefixes
        output.append("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
        output.append("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n");
        output.append("@prefix sswap: <").append(SSWAP_IRI).append("> .\n");
        output.append("@prefix cottage: <").append(G06_ONT_IRI).append("> .\n");
        output.append("@prefix resource: <").append(G06_RES_IRI).append("> .\n");
        output.append("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n\n");

        // Service Definition Block
        output.append("resource:CottageBookingService a sswap:Resource, cottage:CottageBookingService ;\n");
        output.append("    sswap:providedBy resource:CottageBookingProvider ;\n");
        output.append("    sswap:name \"Cottage Booking SSWAP Service\" ;\n");
        output.append("    sswap:oneLineDescription \"A service that accepts a booking request and returns available cottages.\" ;\n");
        output.append("    sswap:operatesOn [\n");
        output.append("        rdf:type sswap:Graph ;\n");
        output.append("        sswap:hasMapping [\n");
        output.append("            rdf:type sswap:Subject, cottage:BookingRequest ;\n");
        
        // Input Fields (G06 property names)
        output.append("            cottage:bookerName \"").append(bookerName).append("\" ;\n");
        output.append("            cottage:requiredPlaces \"").append(requiredPlaces).append("\" ;\n");
        output.append("            cottage:requiredBedrooms \"").append(requiredBedrooms).append("\" ;\n");
        output.append("            cottage:maxDistanceLake \"").append(maxLakeDistanceMeters).append("\" ;\n");
        output.append("            cottage:city \"").append(city).append("\" ;\n");
        output.append("            cottage:maxDistanceCity \"").append(maxCityDistanceMeters).append("\" ;\n");
        output.append("            cottage:requiredDays \"").append(requiredDays).append("\" ;\n");
        output.append("            cottage:startDate \"").append(startDate).append("\" ;\n");
        output.append("            cottage:maxShiftDays \"").append(maxStartShiftDays).append("\" ;");

        // Iterate through results and append sswap:mapsTo block
        for (int i = 0; i < suggestions.size(); i++) {
            BookingSuggestion sug = suggestions.get(i);
            

            output.append(" \n            sswap:mapsTo [\n");
            output.append("                rdf:type sswap:Object, cottage:BookingSuggestion ;\n");
            
            // Output Fields (G06 property names)
            output.append("                cottage:bookerName \"").append(sug.getBookerName()).append("\" ;\n");
            output.append("                cottage:bookingNumber \"\" ;\n");
            output.append("                cottage:address \"").append(sug.getAddress()).append("\" ;\n");
            output.append("                cottage:imageUrl \"").append(IMAGE_BASE_URL).append(sug.getImageURL()).append("\" ;\n");
            output.append("                cottage:capacity \"").append(sug.getCapacity()).append("\" ;\n");
            output.append("                cottage:bedrooms \"").append(sug.getNumberOfBedrooms()).append("\" ;\n");
            output.append("                cottage:distanceToLake \"").append(sug.getDistanceToLake()).append("\" ;\n");
            output.append("                cottage:distanceToCity \"").append(sug.getDistanceToCity()).append("\" ;\n");
            output.append("                cottage:start \"").append(sug.getStartDate()).append("\"^^xsd:date ;\n");
            output.append("                cottage:end \"").append(sug.getEndDate()).append("\"^^xsd:date\n");
            
            output.append("            ]").append(";").append("\n");
        }
        
        output.append("        ]\n"); // Close sswap:hasMapping bracket
        output.append("    ] .\n"); // Close sswap:operatesOn bracket and add final period

        return output.toString();
    }
}