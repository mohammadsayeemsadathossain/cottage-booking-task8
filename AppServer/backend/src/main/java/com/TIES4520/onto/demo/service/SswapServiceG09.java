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
public class SswapServiceG09 {
	
    @Autowired
    private CottageService cottageService;
	
    //Hardcode the exact ontology IRI used in the Group 09 RRG to avoid case-sensitivity issues
    private static final String G09_ONT_IRI = "http://localhost:8080/cottageBooking/onto/cottageOntology.owl#";

    @Value("${sswap.namespace.sswap}")
    private String SSWAP_IRI;

    @Value("${img.baseurl}")
    private String IMAGE_BASE_URL;

    private static final String G09_RES_IRI = "http://localhost:8080/cottageBooking/resources/";
    private static final String G09_SERVICE_NAME = "searchCottageService";
    
    
    /** Helper to get the first value of a property for the input subject URI. */
    private String getFirstValue(Model model, ValueFactory vf, Resource subject, String predicateLocalName) {
        // Use the specific G09_ONT_IRI for property resolution
        return model.filter(
            subject, 
            vf.createIRI(G09_ONT_IRI, predicateLocalName), 
            null
        ).stream().findFirst().map(stmt -> stmt.getObject().stringValue()).orElse(null);
    }
    
    public String executeSearchCottageService(String rrgTurtleContent) throws Exception {
        
        ValueFactory vf = SimpleValueFactory.getInstance();

        // Use the specific G09_ONT_IRI to create the target request class IRI
        final IRI G09_SEARCH_REQ = vf.createIRI(G09_ONT_IRI, "CottageSearchRequest");
        
        // 1. Parse the RRG to EXTRACT INPUT DATA
        // Note: The base URI is used for relative URI resolution during parsing.
        Model rrgModel = Rio.parse(new StringReader(rrgTurtleContent), G09_RES_IRI, RDFFormat.TURTLE);
        
        // 2. Find the subject of type :CottageSearchRequest
        Resource searchReqResource = rrgModel.filter(null, RDF.TYPE, G09_SEARCH_REQ).stream()
            .findFirst().map(stmt -> stmt.getSubject()).orElseThrow(() -> 
                new IllegalArgumentException("RRG structure is invalid (missing a resource typed as :CottageSearchRequest)."));
        
        // 3. Extract ALL input parameters
        String bookerName = getFirstValue(rrgModel, vf, searchReqResource, "bookerName");
        String requiredPlaces = getFirstValue(rrgModel, vf, searchReqResource, "hasCapacity");
        String requiredBedrooms = getFirstValue(rrgModel, vf, searchReqResource, "hasBedrooms");
        String maxLakeDistanceMeters = getFirstValue(rrgModel, vf, searchReqResource, "hasDistanceToLake");
        String city = getFirstValue(rrgModel, vf, searchReqResource, "hasNearestCity");
        String maxCityDistanceMeters = getFirstValue(rrgModel, vf, searchReqResource, "hasDistanceToCity"); 
        String startDate = getFirstValue(rrgModel, vf, searchReqResource, "hasStartDate"); 
        String requiredDays = getFirstValue(rrgModel, vf, searchReqResource, "requiredDays");
        String maxStartShiftDays = getFirstValue(rrgModel, vf, searchReqResource, "maxStartShift");
        
        // 4. Execute the business logic
        DateTimeFormatter IN = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter OUT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String startDay_ddMMyyyy = OUT.format(IN.parse(startDate));

        List<BookingSuggestion> suggestions = cottageService.searchSuggestions(
                bookerName, 
                Integer.parseInt(requiredPlaces), 
                Integer.parseInt(requiredBedrooms), 
                (int)Float.parseFloat(maxLakeDistanceMeters), 
                city, 
                (int)Float.parseFloat(maxCityDistanceMeters), 
                startDay_ddMMyyyy, 
                Integer.parseInt(requiredDays), 
                Integer.parseInt(maxStartShiftDays)
        );

        // 5. MANUALLY BUILD THE TURTLE OUTPUT STRING
        StringBuilder output = new StringBuilder();

        // Prefixes
        // FIX 4: Use the G09_ONT_IRI constant for the default prefix
        output.append("@prefix : <").append(G09_ONT_IRI).append("> .\n"); 
        output.append("@prefix sswap: <").append(SSWAP_IRI).append("> .\n");
        output.append("@prefix resource: <").append(G09_RES_IRI).append("> .\n");
        output.append("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
        output.append("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n\n");

        // Service Definition Block
        output.append("resource:").append(G09_SERVICE_NAME).append(" a sswap:Resource, :CottageSearchService ;\n");
        output.append("    sswap:providedBy resource:resourceProvider ;\n");
        output.append("    sswap:name \"Search Cottage Service\" ;\n");
        output.append("    sswap:oneLineDescription \"A semantic service that searchs available cottages.\" ;\n");
        output.append("    sswap:operatesOn [\n");
        output.append("        rdf:type sswap:Graph ;\n");
        output.append("        sswap:hasMapping [\n");
        output.append("            rdf:type sswap:Subject, :CottageSearchRequest ;\n");
        
        // Input Fields (using extracted values and correct G09 properties/types)
        output.append("            :bookerName \"").append(bookerName).append("\" ;\n");
        output.append("            :hasCapacity \"").append(requiredPlaces).append("\"^^xsd:integer ;\n");
        output.append("            :hasBedrooms \"").append(requiredBedrooms).append("\"^^xsd:integer ;\n");
        output.append("            :hasDistanceToLake \"").append(maxLakeDistanceMeters).append("\"^^xsd:float ;\n");
        output.append("            :hasNearestCity \"").append(city).append("\" ;\n");
        output.append("            :hasDistanceToCity \"").append(maxCityDistanceMeters).append("\"^^xsd:float ;\n");
        output.append("            :hasStartDate \"").append(startDate).append("\"^^xsd:date ;\n");
        output.append("            :requiredDays \"").append(requiredDays).append("\"^^xsd:integer ;\n");
        output.append("            :maxStartShift \"").append(maxStartShiftDays).append("\"^^xsd:integer ;");

        // Iterate through results and append sswap:mapsTo block
        for (int i = 0; i < suggestions.size(); i++) {
            BookingSuggestion sug = suggestions.get(i);
            

            output.append(" \n            sswap:mapsTo [\n");
            output.append("                rdf:type sswap:Object, :CottageSearchResponse ;\n");
            
            // Output Fields 
            output.append("                :bookerName \"").append(sug.getBookerName()).append("\" ;\n");
            output.append("                :bookingNumber \"\" ;\n");
            output.append("                :hasAddress \"").append(sug.getAddress()).append("\" ;\n");
            output.append("                :hasImage \"").append(IMAGE_BASE_URL).append(sug.getImageURL()).append("\" ;\n");
            output.append("                :hasCapacity \"").append(sug.getCapacity()).append("\"^^xsd:integer ;\n");
            output.append("                :hasBedrooms \"").append(sug.getNumberOfBedrooms()).append("\"^^xsd:integer ;\n");
            output.append("                :hasDistanceToLake \"").append(sug.getDistanceToLake()).append("\"^^xsd:float ;\n");
            output.append("                :hasNearestCity \"").append(sug.getCityName()).append("\" ;\n");
            output.append("                :hasDistanceToCity \"").append(sug.getDistanceToCity()).append("\"^^xsd:float ;\n");
            output.append("                :hasStartDate \"").append(sug.getStartDate()).append("\"^^xsd:date ;\n");
            output.append("                :hasEndDate \"").append(sug.getEndDate()).append("\"^^xsd:date\n");
            
            output.append("            ]").append(";").append("\n");
        }
        
        output.append("        ]\n"); 
        output.append("    ] .\n"); 

        return output.toString();
    }
}