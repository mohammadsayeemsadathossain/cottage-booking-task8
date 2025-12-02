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
public class SswapService {
	
    @Autowired
    private CottageService cottageService;
	
    // Inject Namespaces from configuration properties
	@Value("${sswap.namespace.ontology}")
	private String CF;

	@Value("${sswap.namespace.service}")
	private String RES;
    
    @Value("${sswap.namespace.data}")
    private String DATA;
    
    @Value("${sswap.namespace.sswap}")
    private String SSWAP;

    @Value("${img.baseurl}")
    private String IMAGE_BASE_URL;
    
    /** Helper to get the first value of a property for the input subject URI. */
    private String getFirstValue(Model model, ValueFactory vf, Resource subject, String predicateLocalName) {
        return model.filter(
            subject, 
            vf.createIRI(CF, predicateLocalName), 
            null
        ).stream().findFirst().map(stmt -> stmt.getObject().stringValue()).orElse(null);
    }
    
    public String executeSearchService(String rrgTurtleContent) throws Exception {
        
        ValueFactory vf = SimpleValueFactory.getInstance();

        // IRIs for common use
        final IRI cfSearchReq = vf.createIRI(CF, "SearchReq");
        
        // 1. Parse the RRG only to EXTRACT INPUT DATA
        Model rrgModel = Rio.parse(new StringReader(rrgTurtleContent), RES, RDFFormat.TURTLE);
        Resource searchReqResource = rrgModel.filter(null, RDF.TYPE, cfSearchReq).stream()
            .findFirst().map(stmt -> stmt.getSubject()).orElseThrow(() -> 
                new IllegalArgumentException("RRG structure is invalid (missing a resource typed as cf:SearchReq)."));
        
        // 2. Extract ALL input parameters
        String bookerName = getFirstValue(rrgModel, vf, searchReqResource, "bookerName");
        String requiredPlaces = getFirstValue(rrgModel, vf, searchReqResource, "numberOfPeople");
        String requiredBedrooms = getFirstValue(rrgModel, vf, searchReqResource, "numberOfBedrooms");
        String maxLakeDistanceMeters = getFirstValue(rrgModel, vf, searchReqResource, "maxDistanceFromLake");
        String city = getFirstValue(rrgModel, vf, searchReqResource, "cityName");
        String maxCityDistanceMeters = getFirstValue(rrgModel, vf, searchReqResource, "nearestCity"); 
        String startDate = getFirstValue(rrgModel, vf, searchReqResource, "startDate"); 
        String requiredDays = getFirstValue(rrgModel, vf, searchReqResource, "numberOfDays");
        String maxStartShiftDays = getFirstValue(rrgModel, vf, searchReqResource, "possibleShift");
        
        System.out.println("--- Show all input params ---");
        System.out.println("bookerName: " + bookerName);
        System.out.println("requiredPlaces: " + requiredPlaces);
        System.out.println("requiredBedrooms: " + requiredBedrooms);
        System.out.println("boomaxLakeDistanceMeterskerName: " + maxLakeDistanceMeters);
        System.out.println("city: " + city);
        System.out.println("maxCityDistanceMeters: " + maxCityDistanceMeters);
        System.out.println("startDate: " + startDate);
        System.out.println("requiredDays: " + requiredDays);
        System.out.println("maxStartShiftDays: " + maxStartShiftDays);
        
        

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

        // 4. MANUALLY BUILD THE TURTLE OUTPUT STRING
        StringBuilder output = new StringBuilder();

        // Prefixes (matching your desired output format)
        output.append("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
        output.append("@prefix sswap: <http://sswapmeet.sswap.info/sswap/> .\n");
        output.append("@prefix cf: <http://localhost:8080/cottageBooking/onto/CottageOntology.owl#> .\n");
        // Using the RES prefix provided in your latest output for consistency
        output.append("@prefix res: <http://localhost:8080/sswapdemo/> .\n");
        output.append("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n\n");

        // Service Definition Block
        output.append("res:searchCottageService a sswap:Resource;\n");
        output.append("    sswap:providedBy res:provider;\n");
        output.append("    sswap:name \"Cottage Search Service (domain-only)\";\n");
        output.append("    sswap:oneLineDescription \"Find cottages matching user-provided criteria.\";\n");
        output.append("    sswap:operatesOn [\n");
        output.append("        rdf:type sswap:Graph ;\n");
        output.append("        sswap:hasMapping [\n");
        output.append("            rdf:type sswap:Subject, cf:SearchReq ;\n");
        
        // Input Fields
        output.append("            cf:bookerName \"").append(bookerName).append("\" ;\n");
        output.append("            cf:numberOfPeople \"").append(requiredPlaces).append("\" ;\n");
        output.append("            cf:numberOfBedrooms \"").append(requiredBedrooms).append("\" ;\n");
        output.append("            cf:cityName \"").append(city).append("\" ;\n");
        output.append("            cf:maxDistanceFromLake \"").append(maxLakeDistanceMeters).append("\" ;\n");
        output.append("            cf:nearestCity \"").append(maxCityDistanceMeters).append("\" ;\n");
        output.append("            cf:numberOfDays \"").append(requiredDays).append("\" ;\n");
        output.append("            cf:startDate \"").append(startDate).append("\" ;\n");
        output.append("            cf:possibleShift \"").append(maxStartShiftDays).append("\";\n");

        // Iterate through results and append sswap:mapsTo block
        for (int i = 0; i < suggestions.size(); i++) {
            BookingSuggestion sug = suggestions.get(i);
            
            // Add semicolon or period based on position
            String delimiter = (i == suggestions.size() - 1) ? "." : ","; 

            output.append(" \n            sswap:mapsTo [\n"); // Start mapsTo block
            output.append("                rdf:type sswap:Object, cf:SearchResp ;\n");
            
            // Output Fields (Untyped Literals)
            output.append("                cf:bookerName \"").append(sug.getBookerName()).append("\" ;\n");
            output.append("                cf:bookingNumber \"\" ;\n");
            output.append("                cf:cottageID \"").append(sug.getCottageID()).append("\" ;\n");
            output.append("                cf:cottageName \"Cottage ").append(sug.getCottageID()).append("\" ;\n");
            output.append("                cf:cottageAddress \"").append(sug.getAddress()).append("\" ;\n");
            output.append("                cf:imageURL \"").append(IMAGE_BASE_URL).append(sug.getImageURL()).append("\" ;\n");
            output.append("                cf:cityName \"").append(sug.getCityName()).append("\" ;\n");
            
            // Output Fields (Typed Literals)
            output.append("                cf:capacity \"").append(sug.getCapacity()).append("\"^^xsd:int ;\n");
            output.append("                cf:numberOfBedrooms \"").append(sug.getNumberOfBedrooms()).append("\"^^xsd:int ;\n");
            output.append("                cf:distanceFromLake \"").append(sug.getDistanceToLake()).append("\"^^xsd:int ;\n");
            output.append("                cf:cityDistance \"").append(sug.getDistanceToCity()).append("\"^^xsd:int ;\n");
            
            // Output Fields (Typed Dates - using the original yyyy-MM-dd format for xsd:date)
            output.append("                cf:bookingStartDate \"").append(sug.getStartDate()).append("\"^^xsd:date ;\n");
            output.append("                cf:bookingEndDate \"").append(sug.getEndDate()).append("\"^^xsd:date\n");
            
            output.append("            ]").append(";").append("\n"); // Close mapsTo bracket and add delimiter
        }
        
        output.append("        ]\n"); // Close sswap:hasMapping bracket
        output.append("    ] .\n"); // Close sswap:operatesOn bracket and add final period

        
        System.out.println(output.toString());
        return output.toString();
    }
}