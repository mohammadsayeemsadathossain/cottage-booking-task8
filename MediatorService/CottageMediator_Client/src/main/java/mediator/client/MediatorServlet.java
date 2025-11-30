package mediator.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Mediator Servlet - Client Part
 * This servlet receives requests from the frontend and will communicate
 * with the SSWAP Cottage Booking Service
 */
@WebServlet("/MediatorServlet")
public class MediatorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String CF_NS = "http://localhost:9090/cottageBooking/onto/CottageOntology.owl#";

    /**
     * Constructor
     */
    public MediatorServlet() {
        super();
    }

    /**
     * Handle GET requests
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * Handle POST requests from frontend
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            // Get request type
            String reqType = request.getParameter("reqType");
            
            if ("searchCottage".equals(reqType)) {
                // Get all parameters from frontend
                String serviceURL = request.getParameter("serviceURL");
                String bookerName = request.getParameter("bookerName");
                String numberOfPeople = request.getParameter("numberOfPeople");
                String numberOfBedrooms = request.getParameter("numberOfBedrooms");
                String maxDistanceFromLake = request.getParameter("maxDistanceFromLake");
                String cityName = request.getParameter("cityName");
                String maxCityDistance = request.getParameter("maxCityDistance");
                String numberOfDays = request.getParameter("numberOfDays");
                String startDate = request.getParameter("startDate");
                String possibleShift = request.getParameter("possibleShift");
                
                // Log received parameters
                System.out.println("=== Mediator Client Received Request ===");
                System.out.println("Service URL: " + serviceURL);
                System.out.println("Booker Name: " + bookerName);
                System.out.println("Number of People: " + numberOfPeople);
                System.out.println("========================================");
                
                // Basic validation
                if (serviceURL == null || serviceURL.trim().isEmpty()) {
                    out.write("{\"error\":\"Service URL is missing\"}");
                    return;
                }
                
                // 2. GET RDG from remote SSWAP service (step 3-4 of task description)
                try {
                    String rdgTurtle = httpGet(serviceURL, "text/turtle");
                    System.out.println("=== RDG received from remote service ===");
                    System.out.println(rdgTurtle);
                    System.out.println("========================================");
                } catch (Exception e) {
                    System.err.println("Warning: could not fetch RDG from service: " + e.getMessage());
                    // We continue anyway; the service can still be invoked.
                }
                
                // 3. Build RRG (Request RDF Graph) in Turtle based on user input
                String rrgTurtle = buildSearchRrg(
                        bookerName,
                        numberOfPeople,
                        numberOfBedrooms,
                        maxDistanceFromLake,
                        cityName,
                        maxCityDistance,
                        numberOfDays,
                        startDate,
                        possibleShift
                );
                System.out.println("=== RRG Turtle (request to remote service) ===");
                System.out.println(rrgTurtle);
                System.out.println("================================================");

                // 4. POST RRG to remote SSWAP service, receive RIG (Response RDF Graph)
                String rigTurtle = httpPost(serviceURL, "text/turtle", rrgTurtle);

                System.out.println("=== RIG Turtle (response from remote service) ===");
                System.out.println(rigTurtle);
                System.out.println("=================================================");

                // 5. Parse RIG Turtle into Java objects
                List<CottageResult> cottages = parseRigToResults(rigTurtle);

                String jsonResponse = buildJsonResponse(cottages);
                out.write(jsonResponse);
            } else {
                out.write("{\"error\": \"Invalid request type\"}");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            out.write("{\"error\": \"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
            out.close();
        }
    }
    
    /**
     * Create dummy response for testing the client interface
     * Your teammate will replace this with actual SSWAP communication
     */
    private String createDummyResponse(String bookerName) {
        return "{"
            + "\"cottages\": ["
            + "    {"
            + "        \"bookingNumber\": \"BK-001\","
            + "        \"bookerName\": \"" + bookerName + "\","
            + "        \"cottageName\": \"Lakeside Cottage\","
            + "        \"cottageAddress\": \"123 Lake Road, Jyväskylä\","
            + "        \"imageURL\": \"https://via.placeholder.com/300x200?text=Lakeside+Cottage\","
            + "        \"capacity\": \"6\","
            + "        \"numberOfBedrooms\": \"3\","
            + "        \"distanceFromLake\": \"50\","
            + "        \"cityName\": \"Jyväskylä\","
            + "        \"cityDistance\": \"15\","
            + "        \"bookingStartDate\": \"2025-12-01\","
            + "        \"bookingEndDate\": \"2025-12-08\""
            + "    },"
            + "    {"
            + "        \"bookingNumber\": \"BK-002\","
            + "        \"bookerName\": \"" + bookerName + "\","
            + "        \"cottageName\": \"Forest View Cottage\","
            + "        \"cottageAddress\": \"456 Forest Path, Jyväskylä\","
            + "        \"imageURL\": \"https://via.placeholder.com/300x200?text=Forest+View\","
            + "        \"capacity\": \"4\","
            + "        \"numberOfBedrooms\": \"2\","
            + "        \"distanceFromLake\": \"200\","
            + "        \"cityName\": \"Jyväskylä\","
            + "        \"cityDistance\": \"10\","
            + "        \"bookingStartDate\": \"2025-12-01\","
            + "        \"bookingEndDate\": \"2025-12-08\""
            + "    }"
            + "]"
            + "}";
    }
    
    private String httpGet(String urlString, String accept) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if (accept != null) {
                conn.setRequestProperty("Accept", accept);
            }
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String body = streamToString(is);
            if (status < 200 || status >= 300) {
                throw new IOException("GET " + urlString + " failed with HTTP status " + status + " and body: " + body);
            }
            return body;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    private String httpPost(String urlString, String contentType, String body) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", contentType + "; charset=UTF-8");
            conn.setRequestProperty("Accept", "text/turtle");

            // Write request body
            conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().flush();

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String responseBody = streamToString(is);
            if (status < 200 || status >= 300) {
                throw new IOException("POST " + urlString + " failed with HTTP status " + status + " and body: " + responseBody);
            }
            return responseBody;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    private String streamToString(InputStream is) throws IOException {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }
    
    private List<CottageResult> parseRigToResults(String rigTurtle) {
        List<CottageResult> results = new ArrayList<>();

        if (rigTurtle == null || rigTurtle.trim().isEmpty()) {
            return results;
        }

        Model model = ModelFactory.createDefaultModel();
        model.read(new StringReader(rigTurtle), null, "TURTLE");

        Resource searchRespType = model.createResource(CF_NS + "SearchResp");

        Property pBookerName       = model.createProperty(CF_NS + "bookerName");
        Property pBookingNumber    = model.createProperty(CF_NS + "bookingNumber");
        Property pCottageID        = model.createProperty(CF_NS + "cottageID");
        Property pCottageName      = model.createProperty(CF_NS + "cottageName");
        Property pCottageAddress   = model.createProperty(CF_NS + "cottageAddress");
        Property pImageURL         = model.createProperty(CF_NS + "imageURL");
        Property pCapacity         = model.createProperty(CF_NS + "capacity");
        Property pNumberOfBedrooms = model.createProperty(CF_NS + "numberOfBedrooms");
        Property pDistanceFromLake = model.createProperty(CF_NS + "distanceFromLake");
        Property pCityName         = model.createProperty(CF_NS + "cityName");
        Property pCityDistance     = model.createProperty(CF_NS + "cityDistance");
        Property pBookingStartDate = model.createProperty(CF_NS + "bookingStartDate");
        Property pBookingEndDate   = model.createProperty(CF_NS + "bookingEndDate");

        ResIterator it = model.listResourcesWithProperty(RDF.type, searchRespType);
        while (it.hasNext()) {
            Resource r = it.next();

            CottageResult cr = new CottageResult();
            cr.bookerName       = getLiteral(r, pBookerName);
            cr.bookingNumber    = getLiteral(r, pBookingNumber);
            cr.cottageID        = getLiteral(r, pCottageID);
            cr.cottageName      = getLiteral(r, pCottageName);
            cr.cottageAddress   = getLiteral(r, pCottageAddress);
            cr.imageURL         = getLiteral(r, pImageURL);
            cr.capacity         = getLiteral(r, pCapacity);
            cr.numberOfBedrooms = getLiteral(r, pNumberOfBedrooms);
            cr.distanceFromLake = getLiteral(r, pDistanceFromLake);
            cr.cityName         = getLiteral(r, pCityName);
            cr.cityDistance     = getLiteral(r, pCityDistance);
            cr.bookingStartDate = getLiteral(r, pBookingStartDate);
            cr.bookingEndDate   = getLiteral(r, pBookingEndDate);

            results.add(cr);
        }

        return results;
    }
    
    private String buildSearchRrg(String bookerName,
            String numberOfPeople,
            String numberOfBedrooms,
            String maxDistanceFromLake,
            String cityName,
            String maxCityDistance,
            String numberOfDays,
            String startDate,
            String possibleShift) {
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append("@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
    	sb.append("@prefix sswap: <http://sswapmeet.sswap.info/sswap/> .\n");
    	sb.append("@prefix cf:    <").append(CF_NS).append("> .\n");
    	// res: prefix is not strictly used by backend logic, but we keep it
    	sb.append("@prefix res:   <http://localhost:9090/CottageService/> .\n\n");

    	sb.append("res:searchCottageService\n");
    	sb.append("  rdf:type sswap:Resource ;\n");
    	sb.append("  sswap:operatesOn [\n");
    	sb.append("    rdf:type sswap:Graph ;\n");
    	sb.append("    sswap:hasMapping [\n");
    	sb.append("      rdf:type sswap:Subject , cf:SearchReq ;\n");
    	sb.append("      cf:bookerName          \"").append(escapeTurtle(bookerName)).append("\" ;\n");
    	sb.append("      cf:numberOfPeople      \"").append(escapeTurtle(numberOfPeople)).append("\" ;\n");
    	sb.append("      cf:numberOfBedrooms    \"").append(escapeTurtle(numberOfBedrooms)).append("\" ;\n");
    	sb.append("      cf:cityName            \"").append(escapeTurtle(cityName)).append("\" ;\n");
    	sb.append("      cf:maxDistanceFromLake \"").append(escapeTurtle(maxDistanceFromLake)).append("\" ;\n");
    	sb.append("      cf:nearestCity         \"").append(escapeTurtle(maxCityDistance)).append("\" ;\n");
    	sb.append("      cf:numberOfDays        \"").append(escapeTurtle(numberOfDays)).append("\" ;\n");
    	sb.append("      cf:startDate           \"").append(escapeTurtle(startDate)).append("\" ;\n");
    	sb.append("      cf:possibleShift       \"").append(escapeTurtle(possibleShift)).append("\" \n");
    	sb.append("    ]\n");
    	sb.append("  ] .\n");

    	return sb.toString();
    }
    
    private String buildJsonResponse(List<CottageResult> cottages) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"cottages\":[");

        for (int i = 0; i < cottages.size(); i++) {
            CottageResult c = cottages.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{");
            sb.append("\"bookingNumber\":\"").append(escapeJson(c.bookingNumber)).append("\",");
            sb.append("\"bookerName\":\"").append(escapeJson(c.bookerName)).append("\",");
            sb.append("\"cottageID\":\"").append(escapeJson(c.cottageID)).append("\",");
            sb.append("\"cottageName\":\"").append(escapeJson(c.cottageName)).append("\",");
            sb.append("\"cottageAddress\":\"").append(escapeJson(c.cottageAddress)).append("\",");
            sb.append("\"imageURL\":\"").append(escapeJson(c.imageURL)).append("\",");
            sb.append("\"capacity\":\"").append(escapeJson(c.capacity)).append("\",");
            sb.append("\"numberOfBedrooms\":\"").append(escapeJson(c.numberOfBedrooms)).append("\",");
            sb.append("\"distanceFromLake\":\"").append(escapeJson(c.distanceFromLake)).append("\",");
            sb.append("\"cityName\":\"").append(escapeJson(c.cityName)).append("\",");
            sb.append("\"cityDistance\":\"").append(escapeJson(c.cityDistance)).append("\",");
            sb.append("\"bookingStartDate\":\"").append(escapeJson(c.bookingStartDate)).append("\",");
            sb.append("\"bookingEndDate\":\"").append(escapeJson(c.bookingEndDate)).append("\"");
            sb.append("}");
        }

        sb.append("]}");
        return sb.toString();
    }
    
    private String escapeTurtle(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
    
    private String getLiteral(Resource r, Property p) {
        Statement st = r.getProperty(p);
        if (st == null) return "";
        if (!st.getObject().isLiteral()) return st.getObject().toString();
        Literal lit = st.getObject().asLiteral();
        return lit.getString();
    }
    
    private static class CottageResult {
        String bookingNumber;
        String bookerName;
        String cottageID;
        String cottageName;
        String cottageAddress;
        String imageURL;
        String capacity;
        String numberOfBedrooms;
        String distanceFromLake;
        String cityName;
        String cityDistance;
        String bookingStartDate;
        String bookingEndDate;
    }
}