package mediator.client;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Mediator Servlet - Client Part
 * This servlet receives requests from the frontend and will communicate
 * with the SSWAP Cottage Booking Service
 */
@WebServlet("/MediatorServlet")
public class MediatorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
                
                // TODO: This is where your teammate will add SSWAP communication logic
                // For now, return dummy response for testing
                String jsonResponse = createDummyResponse(bookerName);
                
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
}