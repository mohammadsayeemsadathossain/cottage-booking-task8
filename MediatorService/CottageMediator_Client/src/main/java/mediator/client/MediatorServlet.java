package mediator.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.similarity.JaroWinklerDistance;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

@WebServlet("/MediatorServlet")
public class MediatorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String SSWAP_NS = "http://sswapmeet.sswap.info/sswap/";
    private Map<String, AlignmentCandidate> latestAlignmentForUi = new HashMap<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    	request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String reqType = request.getParameter("reqType");
            if (!"searchCottage".equals(reqType)) {
                out.write("{\"error\":\"Invalid request type\"}");
                return;
            }

            String serviceURL          = request.getParameter("serviceURL");
            String bookerName          = request.getParameter("bookerName");
            String numberOfPeople      = request.getParameter("numberOfPeople");
            String numberOfBedrooms    = request.getParameter("numberOfBedrooms");
            String maxDistanceFromLake = request.getParameter("maxDistanceFromLake");
            String cityName            = request.getParameter("cityName");
            String maxCityDistance     = request.getParameter("maxCityDistance");
            String numberOfDays        = request.getParameter("numberOfDays");
            String startDate           = request.getParameter("startDate");
            String possibleShift       = request.getParameter("possibleShift");

            if (serviceURL == null || serviceURL.trim().isEmpty()) {
                serviceURL = "http://localhost:8080/sswapdemo/CottageService/api/searchCottageService";
            }

            System.out.println("=== MediatorServlet: searchCottage ===");
            System.out.println("Service URL       : " + serviceURL);
            System.out.println("Booker Name       : " + bookerName);
            System.out.println("Number of People  : " + numberOfPeople);
            System.out.println("Number of Bedrooms: " + numberOfBedrooms);
            System.out.println("Max Distance Lake : " + maxDistanceFromLake);
            System.out.println("City Name         : " + cityName);
            System.out.println("Max City Distance : " + maxCityDistance);
            System.out.println("Number of Days    : " + numberOfDays);
            System.out.println("Start Date        : " + startDate);
            System.out.println("Possible Shift    : " + possibleShift);
            System.out.println("=======================================");

            String rdgTurtle = httpGet(serviceURL, "text/turtle");
            System.out.println("=== RDG (Turtle) from remote service ===");
            System.out.println(rdgTurtle);
            System.out.println("========================================");

            RequestTemplate template = prepareRequestFromRdg(
                    rdgTurtle,
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

            if (template == null) {
                out.write("{\"error\":\"Could not interpret RDG from service\"}");
                return;
            }

            java.io.StringWriter sw = new java.io.StringWriter();
            template.model.write(sw, "TURTLE");
            String requestTurtle = sw.toString();

            System.out.println("=== RRG Turtle (request built from RDG) ===");
            System.out.println(requestTurtle);
            System.out.println("===========================================");

            String responseTurtle = httpPost(serviceURL, "text/turtle", requestTurtle);

            System.out.println("=== Response Turtle from backend ===");
            System.out.println(responseTurtle);
            System.out.println("====================================");

            List<CottageResult> cottages = parseResponseTurtle(responseTurtle, template.cfNamespace);

            String jsonResponse = buildJsonResponse(cottages, !template.isOurOntology);
            out.write(jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Unknown error";
            out.write("{\"error\":\"" + msg + "\"}");
        } finally {
            out.flush();
        }
    }


    private static class RequestTemplate {
        Model model;
        String cfNamespace;
        Boolean isOurOntology;
    }

    private RequestTemplate prepareRequestFromRdg(String rdgTurtle,
                                                  String bookerName,
                                                  String numberOfPeople,
                                                  String numberOfBedrooms,
                                                  String maxDistanceFromLake,
                                                  String cityName,
                                                  String maxCityDistance,
                                                  String numberOfDays,
                                                  String startDate,
                                                  String possibleShift) {

        Model m = ModelFactory.createDefaultModel();
        m.read(new StringReader(rdgTurtle), null, "TURTLE");   // RDG is Turtle

        Resource sswapResourceClass = m.createResource(SSWAP_NS + "Resource");
        Resource sswapSubjectClass  = m.createResource(SSWAP_NS + "Subject");
        Property operatesOn = m.createProperty(SSWAP_NS, "operatesOn");
        Property hasMapping = m.createProperty(SSWAP_NS, "hasMapping");

        ResIterator resIt = m.listResourcesWithProperty(RDF.type, sswapResourceClass);
        if (!resIt.hasNext()) {
            System.err.println("No sswap:Resource found in RDG");
            return null;
        }
        Resource serviceRes = resIt.next();

        Resource graph = serviceRes.getPropertyResourceValue(operatesOn);
        if (graph == null) {
            System.err.println("No sswap:operatesOn in RDG");
            return null;
        }
        Resource subject = graph.getPropertyResourceValue(hasMapping);
        if (subject == null) {
            System.err.println("No sswap:hasMapping in RDG");
            return null;
        }

        List<Resource> types = new ArrayList<>();
        for (Statement st : subject.listProperties(RDF.type).toList()) {
            if (st.getObject().isResource()) {
                types.add(st.getResource());
            }
        }

        Resource requestClass = null;
        for (Resource t : types) {
            String ns = t.getNameSpace();
            if (!SSWAP_NS.equals(ns) && !"http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(ns)) {
                requestClass = t;
                break;
            }
        }

        if (requestClass == null) {
            System.err.println("Could not find cf:SearchReq type in RDG");
            return null;
        }

        String cfNs = requestClass.getNameSpace();
        System.out.println("Detected cf namespace from RDG: " + cfNs);
        
        AlignmentResult result = buildAlignment(m, cfNs, cfNs);
        System.out.println("Detected alignments of RDG: " + result);
        
        RequestTemplate tpl = new RequestTemplate();
        
        if (result.isOurOntology()) {
        	setLiteral(subject, m.createProperty(cfNs + "bookerName"),          bookerName);
        	setLiteral(subject, m.createProperty(cfNs + "numberOfPeople"),      numberOfPeople);
        	setLiteral(subject, m.createProperty(cfNs + "numberOfBedrooms"),    numberOfBedrooms);
        	setLiteral(subject, m.createProperty(cfNs + "cityName"),            cityName);
        	setLiteral(subject, m.createProperty(cfNs + "maxDistanceFromLake"), maxDistanceFromLake);
        	setLiteral(subject, m.createProperty(cfNs + "nearestCity"),         maxCityDistance);
        	setLiteral(subject, m.createProperty(cfNs + "numberOfDays"),        numberOfDays);
        	setLiteral(subject, m.createProperty(cfNs + "startDate"),           startDate);
        	setLiteral(subject, m.createProperty(cfNs + "possibleShift"),       possibleShift);
        	
        	tpl.model = m;
            tpl.cfNamespace = cfNs;
        	tpl.isOurOntology = result.isOurOntology();
        } else {
        	
        }
        return tpl;
    }

    private void setLiteral(Resource subject, Property p, String value) {
        subject.removeAll(p);
        if (value == null) value = "";
        subject.addLiteral(p, value);
    }


    private List<CottageResult> parseResponseTurtle(String responseTurtle, String cfNs) {
        List<CottageResult> results = new ArrayList<>();
        if (responseTurtle == null || responseTurtle.trim().isEmpty()) return results;

        Model model = ModelFactory.createDefaultModel();
        model.read(new StringReader(responseTurtle), null, "TURTLE");

        Resource searchRespType = model.createResource(cfNs + "SearchResp");

        Property pBookerName       = model.createProperty(cfNs + "bookerName");
        Property pBookingNumber    = model.createProperty(cfNs + "bookingNumber");
        Property pCottageID        = model.createProperty(cfNs + "cottageID");
        Property pCottageName      = model.createProperty(cfNs + "cottageName");
        Property pCottageAddress   = model.createProperty(cfNs + "cottageAddress");
        Property pImageURL         = model.createProperty(cfNs + "imageURL");
        Property pCapacity         = model.createProperty(cfNs + "capacity");
        Property pNumberOfBedrooms = model.createProperty(cfNs + "numberOfBedrooms");
        Property pDistanceFromLake = model.createProperty(cfNs + "distanceFromLake");
        Property pCityName         = model.createProperty(cfNs + "cityName");
        Property pCityDistance     = model.createProperty(cfNs + "cityDistance");
        Property pBookingStartDate = model.createProperty(cfNs + "bookingStartDate");
        Property pBookingEndDate   = model.createProperty(cfNs + "bookingEndDate");

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

    private String getLiteral(Resource r, Property p) {
        Statement st = r.getProperty(p);
        if (st == null) return "";
        if (!st.getObject().isLiteral()) return st.getObject().toString();
        Literal lit = st.getObject().asLiteral();
        return lit.getString();
    }

    private String httpGet(String urlString, String accept) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if (accept != null && !accept.isEmpty()) {
                conn.setRequestProperty("Accept", accept);
            }
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String body = streamToString(is);
            if (status < 200 || status >= 300) {
                throw new IOException("GET " + urlString + " failed: HTTP " + status + " body: " + body);
            }
            return body;
        } finally {
            if (conn != null) conn.disconnect();
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
            conn.setReadTimeout(15000);

            conn.setRequestProperty("Content-Type", contentType);
            conn.setRequestProperty("Accept", "text/turtle");

            conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().flush();

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String responseBody = streamToString(is);
            if (status < 200 || status >= 300) {
                throw new IOException("POST " + urlString + " failed: HTTP " + status + " body: " + responseBody);
            }
            return responseBody;
        } finally {
            if (conn != null) conn.disconnect();
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

    private String buildJsonResponse(List<CottageResult> cottages, Boolean requiresMapping) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"requiresMapping\":").append(requiresMapping).append(",");
        sb.append("\"cottages\":[");

        for (int i = 0; i < cottages.size(); i++) {
            CottageResult c = cottages.get(i);
            if (i > 0) sb.append(",");
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
        System.out.println(sb);
        return sb.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
    
    private AlignmentResult buildAlignment(Model rdgModel, String cfNs, String alignmentId) {

        latestAlignmentForUi.clear();
        boolean allMatchPerfectly = true;

        Map<String, Property> candidates = new HashMap<>();
        rdgModel.listStatements().forEachRemaining(st -> {
            if (st.getPredicate() != null) {
                Property p = st.getPredicate();
                String ns = p.getNameSpace();
                if (ns != null && ns.equals(cfNs)) {
                    candidates.put(p.getLocalName(), p);   // LOCAL NAMES
                }
            }
        });

        // Debugging: see what candidates we actually have
        System.out.println("Candidate properties for namespace " + cfNs + ":");
        for (Map.Entry<String, Property> e : candidates.entrySet()) {
            System.out.println("  " + e.getKey() + " -> " + e.getValue().getURI());
        }

        String[] canonical = {
                "bookerName",
                "numberOfPeople",
                "numberOfBedrooms",
                "maxDistanceFromLake",
                "cityName",
                "nearestCity",
                "numberOfDays",
                "startDate",
                "possibleShift",
                "bookingNumber",
                "cottageID",
                "cottageName",
                "cottageAddress",
                "imageURL",
                "capacity",
                "distanceFromLake",
                "cityDistance",
                "bookingStartDate",
                "bookingEndDate"
        };

        Map<String, Property> alignmentMap = new HashMap<>();
        double perfectThreshold = 0.99;
        double lowConfidenceThreshold = 0.7;

        for (String canon : canonical) {
            double bestScore = -1.0;
            Property bestProp = null;
            String bestRemoteName = null;

            for (Map.Entry<String, Property> entry : candidates.entrySet()) {
                String remoteLocal = entry.getKey();
                System.out.println("==================================");
                System.out.println(canon);
                System.out.println(remoteLocal);
                double score = similarity(canon, remoteLocal);
                System.out.println(score);

                if (score > bestScore) {
                    bestScore = score;
                    bestProp = entry.getValue();
                    bestRemoteName = remoteLocal;
                }
            }

            if (bestProp != null) {
                alignmentMap.put(canon, bestProp);

                latestAlignmentForUi.put(
                        canon,
                        new AlignmentCandidate(canon, bestRemoteName, bestProp.getURI(), bestScore)
                );

                System.out.println("Alignment: " + canon + " -> " +
                        bestProp.getURI() + " (score=" + bestScore + ")");

                if (bestScore < lowConfidenceThreshold) {
                    System.out.println("  WARNING: low confidence for " + canon);
                }

                boolean perfect =
                        canon.equals(bestRemoteName) &&
                        bestScore >= perfectThreshold;

                if (!perfect) {
                    allMatchPerfectly = false;
                }
            } else {
                alignmentMap.put(canon, null);
                latestAlignmentForUi.put(
                        canon,
                        new AlignmentCandidate(canon, null, null, -1)
                );
                allMatchPerfectly = false;
            }
        }

//        saveAlignmentToFile(alignmentMap, alignmentId);  // if you have this

        return new AlignmentResult(alignmentMap, new HashMap<>(latestAlignmentForUi), allMatchPerfectly);
    }

    
    private double similarity(String a, String b) {
        if (a == null || b == null) return 1.0;
        JaroWinklerDistance dist = new JaroWinklerDistance();
        Double score = dist.apply(a.toLowerCase(), b.toLowerCase());
        
        if (score == null) {
        	return 0.0;
        }
        
        return 1.0 - score;
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

