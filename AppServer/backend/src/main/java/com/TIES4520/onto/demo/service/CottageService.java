package com.TIES4520.onto.demo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.TIES4520.onto.demo.model.Booking;
import com.TIES4520.onto.demo.model.BookingSuggestion;
import com.TIES4520.onto.demo.model.Cottage;

@Service
public class CottageService {
	@Value("${rdf.endpoint.url}")
    private String repoUrl;
	
	@Value("${rdf.update.url}")
	private String updateUrl;
	
	private Repository getrepo() {
    	return new SPARQLRepository(repoUrl, updateUrl);
    }
	
	private String pfx() {
        return "PREFIX : <http://localhost:8080/cottageBooking/onto/CottageOntology.owl#>\n" +
               "PREFIX cottage: <http://localhost:8080/cottageBooking/data/cottage#>\n";
    }
	
	public List<Cottage> getCottages() {
        String sparql = pfx() +
            "SELECT ?cottage ?cottageID ?address ?imageURL ?capacity " +
            "?numberOfBedrooms ?distanceToLake ?cityName ?distanceToCity WHERE {" +
             " ?cottage a :Cottage ; " +
             " :cottageID ?cottageID ; " +
             " :address ?address ; " +
             " :imageURL ?imageURL ; " +
             " :capacity ?capacity ; " +
             " :numberOfBedrooms ?numberOfBedrooms ; " +
             " :distanceToLake ?distanceToLake ; " +
             " :nearestCity ?city . " +
             " ?city a :Location ; " +
             " :cityName ?cityName ;" +
             ":distanceToCity ?distanceToCity . }";
        
        Repository repo = getrepo();
        try (RepositoryConnection conn = repo.getConnection()) {
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
            try (TupleQueryResult rs = q.evaluate()) {
                List<Cottage> out = new ArrayList<>();
                while (rs.hasNext()) {
                    BindingSet bs = rs.next();
                    Cottage x = new Cottage();
                    x.setIri(bs.getValue("cottage").stringValue());
                    x.setCottageID(s(bs,"cottageID"));
                    x.setAddress(s(bs,"address"));
                    x.setImageURL(s(bs,"imageURL"));
                    x.setCapacity(i(bs,"capacity"));
                    x.setNumberOfBedrooms(i(bs,"numberOfBedrooms"));
                    x.setDistanceToLake(i(bs,"distanceToLake"));
                    x.setCityName(s(bs,"cityName"));
                    x.setDistanceToCity(i(bs,"distanceToCity"));
                    out.add(x);
                }
                return out;
            }
        }
    }

	public void addCottage(Cottage c) {
		String base = "http://localhost:8080/cottageBooking/data/cottage#";
	    String iriStr = base + esc(c.getCottageID());
	    String iri = "<" + iriStr + ">";
	    String cityIri = "<" + iriStr + "_City>";
	    
	    String sparql = pfx() + 
	            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
	            "INSERT DATA {\n" +
	            "  " + iri + " a :Cottage ;\n" +
	            "       :cottageID \"" + esc(c.getCottageID()) + "\" ;\n" +
	            "       :address \"" + esc(c.getAddress()) + "\" ;\n" +
	            "       :imageURL \"" + esc(c.getImageURL()) + "\" ;\n" +
	            "       :capacity " + litInt(c.getCapacity()) + " ;\n" +
	            "       :numberOfBedrooms " + litInt(c.getNumberOfBedrooms()) + " ;\n" +
	            "       :distanceToLake " + litInt(c.getDistanceToLake()) + " ;\n" +
	            "       :nearestCity " + cityIri + " .\n" +
	            "\n" +
	            "  " + cityIri + " a :Location ;\n" +
	            "       :cityName \"" + esc(nullToEmpty(c.getCityName())) + "\" ;\n" +
	            "       :distanceToCity " + litInt(c.getDistanceToCity()) + " .\n" +
	            "}";
        
        Repository repo = getrepo();
        try (RepositoryConnection conn = repo.getConnection()) {
        	conn.prepareUpdate(QueryLanguage.SPARQL, sparql).execute();
        }
    }

	public void updateCottages(Cottage c) {
        deleteCottage(c.getCottageID());
        addCottage(c);
    }

	public void deleteCottage(String id) {
        String iri = "<http://localhost:8080/cottageBooking/data/cottage#" + esc(id) + ">";
        String sparql = pfx(); // + """
//            DELETE {
//              ?s ?p ?o .
//            } WHERE {
//              { %IRI ?p ?o BIND(%IRI AS ?s) }
//              UNION
//              { %IRI :nearestCity ?city . ?city ?p ?o BIND(?city AS ?s) }
//            }
//        """.replace("%IRI", iri);
        
        Repository repo = getrepo();
        try (RepositoryConnection conn = repo.getConnection()) {
            conn.prepareUpdate(sparql).execute();
        }
    }

	public List<BookingSuggestion> searchSuggestions(
	        String bookerName,
	        int requiredPlaces,
	        int requiredBedrooms,
	        int maxLakeDistanceMeters,
	        String city,
	        int maxCityDistanceMeters,
	        String startDay_ddMMyyyy,
	        int requiredDays,
	        int maxStartShiftDays
	) {
	    java.time.format.DateTimeFormatter IN = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");

	    java.time.LocalDate baseStart = java.time.LocalDate.parse(startDay_ddMMyyyy, IN);
	    List<BookingSuggestion> out = new ArrayList<>();

	    // Iterate over all shifts: -maxShift ... +maxShift
	    for (int shift = -maxStartShiftDays; shift <= maxStartShiftDays; shift++) {
	        java.time.LocalDate s = baseStart.plusDays(shift);
	        java.time.LocalDate e = s.plusDays(requiredDays);
	        String sStr = s.toString(); // yyyy-MM-dd
	        String eStr = e.toString();
	        
	        String cityFilter = (city != null && !city.isBlank())
	                ? "FILTER (lcase(str(?cityName)) = \"" + esc(city.toLowerCase(new Locale("fi", "FI"))) + "\")\n" : "";

	        // Similar to searchAvailable() but returns the columns we need and filters out overlaps
	        String sparql = pfx() + 
	            "SELECT ?c ?cid ?addr ?img ?cap ?beds ?lake ?cityName ?cityDist WHERE {\n" +
	            "  ?c a :Cottage ; :cottageID ?cid ; :address ?addr ; :imageURL ?img ;\n" +
	            "     :capacity ?cap ; :numberOfBedrooms ?beds ; :distanceToLake ?lake ; :nearestCity ?city .\n" +
	            "  ?city :cityName ?cityName ; :distanceToCity ?cityDist .\n" +
	            "  FILTER(?cap >= " + requiredPlaces + ")\n" +
	            "  FILTER(?beds >= " + requiredBedrooms + ")\n" +
	            "  FILTER(?lake <= " + maxLakeDistanceMeters + ")\n" +
	            cityFilter +
	            "  FILTER(?cityDist <= " + maxCityDistanceMeters + ")\n" +
	            "  FILTER NOT EXISTS {\n" +
	            "    ?c :hasBooking ?bk . ?bk :startDate ?bS ; :endDate ?bE .\n" +
	            "    FILTER( ?bS < xsd:date(\"" + eStr + "\") && ?bE > xsd:date(\"" + sStr + "\") )\n" +
	            "  }\n" +
	            "}\n";

	        System.out.println("Suggession Query: " + sparql);
	        Repository repo = getrepo();
	        try (RepositoryConnection conn = repo.getConnection()) {
	            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
	            try (TupleQueryResult rs = q.evaluate()) {
	                while (rs.hasNext()) {
	                    BindingSet bs = rs.next();
	                    BookingSuggestion sug = new BookingSuggestion();
	                    sug.setBookerName(bookerName);
	                    sug.setCottageID(s(bs,"cid"));
	                    sug.setAddress(s(bs,"addr"));
	                    sug.setImageURL(s(bs,"img"));
	                    sug.setCapacity(i(bs,"cap"));
	                    sug.setNumberOfBedrooms(i(bs,"beds"));
	                    sug.setDistanceToLake(i(bs,"lake"));
	                    sug.setCityName(s(bs,"cityName"));
	                    sug.setDistanceToCity(i(bs,"cityDist"));
	                    sug.setStartDate(sStr);
	                    sug.setEndDate(eStr);
	                    out.add(sug);
	                }
	            }
	        }
	    }
	    return out;
	}
    
    private static String s(BindingSet b, String name){
        var v = b.getValue(name); return v==null?null:v.stringValue();
    }
    
    private static int i(BindingSet b, String name){
        var v = b.getValue(name); return v==null?0:Integer.parseInt(v.stringValue());
    }
    
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
    
    private static String litInt(Integer n){
        return n==null? "0" : Integer.toString(n);
    }
    
    private static String nullToEmpty(String s){ return s==null?"":s; }
}
