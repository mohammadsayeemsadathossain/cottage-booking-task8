package com.TIES4520.onto.demo.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.TIES4520.onto.demo.model.Booking;
import com.TIES4520.onto.demo.model.BookingCreateRequest;

@Service
public class BookingService {
	@Value("${rdf.endpoint.url}")
    private String repoUrl;
	
	@Value("${rdf.update.url}")
	private String updateUrl;
	
	private Repository getrepo() {
    	return new SPARQLRepository(repoUrl, updateUrl);
    }
	
	private static final DateTimeFormatter IN = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	
	private String pfx() {
        return "PREFIX : <http://localhost:8080/cottageBooking/onto/CottageOntology.owl#>\n" +
               "PREFIX cottage: <http://localhost:8080/cottageBooking/data/cottage#>\n" +
        		"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>";
    }
	
    public Booking create(BookingCreateRequest req) {
        if (req == null || req.bookerName == null || req.bookerName.isBlank()
                || req.cottageID == null || req.cottageID.isBlank()
                || req.startDay == null || req.startDay.isBlank()
                || req.requiredDays < 1) {
            throw new IllegalArgumentException("Invalid booking request");
        }

        LocalDate s = LocalDate.parse(req.startDay, IN);
        LocalDate e = s.plusDays(req.requiredDays);
        String sStr = s.toString(); // yyyy-MM-dd
        String eStr = e.toString();

        String findCottageIri = pfx() +
        	    "SELECT ?c WHERE { " +
        	    "?c a :Cottage ; " +
        	    ":cottageID ?cid . " +
        	    "FILTER (str(?cid) = \"" + req.cottageID + "\") " +
        	    "} LIMIT 1";

        String cottageIri;
        Repository repo = getrepo();
        System.out.println("Cottage list before booking: " + findCottageIri);
        
        try (RepositoryConnection conn = repo.getConnection()) {
            TupleQuery tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, findCottageIri);
            try (TupleQueryResult rs = tq.evaluate()) {
                if (!rs.hasNext()) throw new IllegalArgumentException("Cottage not found: " + req.cottageID);
                cottageIri = rs.next().getValue("c").stringValue();
            }
        }

        // Availability check (ASK)
        String ask = pfx() +
        		"ASK WHERE { " +
        	    "<" + cottageIri + "> a :Cottage . " +
        	    "FILTER NOT EXISTS { " +
        	    "<" + cottageIri + "> :hasBooking ?bk . " +
        	    "?bk :startDate ?bS ; :endDate ?bE . " +
        	    "FILTER ( ?bS < xsd:date(\"" + eStr + "\") && ?bE > xsd:date(\"" + sStr + "\") ) " +
        	    "} " +
        	    "}";


        boolean available;
        try (RepositoryConnection conn = repo.getConnection()) {
            BooleanQuery bq = conn.prepareBooleanQuery(QueryLanguage.SPARQL, ask);
            available = bq.evaluate();
        }
        if (!available) {
            throw new IllegalStateException("Cottage " + req.cottageID + " is not available for " + sStr + " to " + eStr);
        }

        // Create booking IRI + insert
        String bookingNumber = UUID.randomUUID().toString();
        String bookingIri = "http://localhost:8080/cottageBooking/data/cottage#" + "Booking_" + bookingNumber;

        String insert = pfx() +
            "INSERT DATA {\n" +
            "  <" + cottageIri + "> :hasBooking <" + bookingIri + "> .\n" +
            "  <" + bookingIri + "> rdf:type :Booking ;\n" +                  // instead of 'a :Booking'
            "      :bookingNumber \"" + bookingNumber + "\" ;\n" +
            "      :bookerName \"" + esc(req.bookerName) + "\" ;\n" +
            "      :startDate \"" + sStr + "\"^^xsd:date ;\n" +               // <-- typed literal
            "      :endDate   \"" + eStr + "\"^^xsd:date .\n" +               // <-- typed literal
            "}";

        System.out.println("Insert booking: " + insert);
        try (RepositoryConnection conn = repo.getConnection()) {
            conn.prepareUpdate(QueryLanguage.SPARQL, insert, "").execute();
        }

        Booking b = new Booking();
        b.setBookingNumber(bookingNumber);
        b.setBookerName(req.bookerName);
        b.setCottageID(req.cottageID);
        b.setCottageIri(cottageIri);
        b.setStartDate(sStr);
        b.setEndDate(eStr);
        b.setNumberOfDays(req.requiredDays);
        return b;
    }
    
    public List<Booking> list(String cottageID, String bookerName) {
    	String query = pfx() +
    		    "SELECT ?cottage ?booking ?bookingNumber ?bookerName ?startDate ?endDate WHERE { " +
    		    "  ?cottage a :Cottage ; " +
    		    "           :hasBooking ?booking . " +
    		    "  ?booking a :Booking ; " +
    		    "           :bookingNumber ?bookingNumber ; " +
    		    "           :bookerName ?bookerName ; " +
    		    "           :startDate ?startDate ; " +
    		    "           :endDate ?endDate . " +
    		    "} ORDER BY ?cottage ?startDate";

        List<Booking> out = new ArrayList<>();
        Repository repo = getrepo();
        try (RepositoryConnection conn = repo.getConnection()) {
            TupleQuery tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            try (TupleQueryResult rs = tq.evaluate()) {
                while (rs.hasNext()) {
                    BindingSet b = rs.next();
                    Booking x = new Booking();
                    System.out.println(b);
                    x.setCottageIri(b.getValue("cottage").stringValue());
                    
                    IRI cottageIri = SimpleValueFactory.getInstance()
                            .createIRI(x.getCottageIri());
                    String cQuery = pfx() +
                            "SELECT ?id WHERE { <" + cottageIri + "> :cottageID ?id }";

                    TupleQuery ctq = conn.prepareTupleQuery(QueryLanguage.SPARQL, cQuery);
                    try (TupleQueryResult rs2 = ctq.evaluate()) {
                        if (rs2.hasNext()) {
                            BindingSet bs = rs2.next();
                            String cID = bs.getValue("id").stringValue();
                            x.setCottageID(cID);
                        }
                    }
                    
                    
                    x.setBookingNumber(b.getValue("bookingNumber").stringValue());
                    x.setBookerName(b.getValue("bookerName").stringValue());
                    x.setStartDate(b.getValue("startDate").stringValue());
                    x.setEndDate(b.getValue("endDate").stringValue());
                    out.add(x);
                }
            }
        }
        return out;
    }
    
    public void deleteByNumber(String bookingNumber) {
        if (bookingNumber == null || bookingNumber.isBlank())
            throw new IllegalArgumentException("bookingNumber required");

        String delete = pfx() +
        	    "DELETE { " +
        	    "  ?c :hasBooking ?bk . " +
        	    "  ?bk ?p ?o . " +
        	    "} WHERE { " +
        	    "  ?bk a :Booking ; " +
        	    "      :bookingNumber \"" + esc(bookingNumber) + "\" ; " +
        	    "      ?p ?o . " +
        	    "  OPTIONAL { ?c :hasBooking ?bk . } " +
        	    "}";

        Repository repo = getrepo();
        try (RepositoryConnection conn = repo.getConnection()) {
            conn.prepareUpdate(QueryLanguage.SPARQL, delete, "").execute();
        }
    }
    
    private static String esc(String s){
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");
    }
}
