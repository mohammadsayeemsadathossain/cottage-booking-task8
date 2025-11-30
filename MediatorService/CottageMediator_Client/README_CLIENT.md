==========================================================
COTTAGE MEDIATOR CLIENT - DOCUMENTATION
==========================================================

PROJECT: Task 7-2 Cottage Booking Mediator Service
AUTHOR: [Your Name]
COMPONENT: Client Part (Frontend)
DATE: [Today's Date]

==========================================================
WHAT THIS CLIENT DOES:
==========================================================

1. Provides a web interface for users to:
   - Enter the URL of the SSWAP Cottage Booking Service
   - Enter search criteria for cottages (9 input fields)
   - View search results (cottage details)

2. Sends user input to MediatorServlet via HTTP POST

3. Receives JSON response and displays cottages

==========================================================
FILES INCLUDED:
==========================================================

Frontend Files (WebContent folder):
- index.html          : Main HTML interface
- mediatorClient.js   : JavaScript for form handling and AJAX

Backend Files (src/mediator.client package):
- MediatorServlet.java : Servlet that receives frontend requests

==========================================================
HOW IT WORKS:
==========================================================

FLOW:
User fills form → JavaScript collects data → Sends to MediatorServlet 
→ Servlet processes → Returns JSON → JavaScript displays results

CURRENT STATUS:
✅ Frontend interface complete
✅ Form validation working
✅ AJAX communication working
✅ Dummy response display working

⚠️ TODO (for Server Part developer):
- Replace createDummyResponse() method with actual SSWAP logic
- Implement: Send GET to service URL to get RDG
- Implement: Parse RDG and create RIG from user input
- Implement: POST RIG to service and get RRG
- Implement: Parse RRG and extract cottage data
- Implement: Return actual cottage data as JSON

==========================================================
TESTING THE CLIENT:
==========================================================

1. Start Tomcat server in Eclipse
2. Open browser: http://localhost:8080/CottageMediator_Client/
3. Enter any test data and click Search
4. Should see 2 dummy cottages (Lakeside and Forest View)

==========================================================
FOR SERVER DEVELOPER:
==========================================================

Location to add SSWAP logic:
File: src/mediator/client/MediatorServlet.java
Method: doPost()
Line: ~54 (where it says "TODO: This is where your teammate...")

Input parameters available:
- serviceURL
- bookerName
- numberOfPeople
- numberOfBedrooms
- maxDistanceFromLake
- cityName
- maxCityDistance
- numberOfDays
- startDate
- possibleShift

Expected JSON output format:
{
  "cottages": [
    {
      "bookingNumber": "BK-001",
      "bookerName": "User Name",
      "cottageName": "Cottage Name",
      "cottageAddress": "Address",
      "imageURL": "http://...",
      "capacity": "6",
      "numberOfBedrooms": "3",
      "distanceFromLake": "50",
      "cityName": "Jyväskylä",
      "cityDistance": "15",
      "bookingStartDate": "2025-12-01",
      "bookingEndDate": "2025-12-08"
    }
  ]
}

==========================================================
LIBRARIES INCLUDED:
==========================================================

All necessary SSWAP and HTTP libraries are in:
WebContent/WEB-INF/lib/

Key libraries:
- SSWAP API jars (from example project)
- Apache HttpClient (for HTTP GET/POST)
- Logging libraries

==========================================================
REFERENCE FILES:
==========================================================

Cottage RDG: Task-7_1/03_SearchCottage_RDG.txt
Cottage RIG: Task-7_1/03.1_SearchCottage_RIG.txt
Cottage RRG: Task-7_1/03.2_SearchCottage_RRG.txt

Example SSWAP Client: SSWAP_Client_2019 project

==========================================================
NOTES:
==========================================================

- Project uses Java 8 (required for SSWAP library)
- Frontend uses vanilla JavaScript (no frameworks)
- Servlet returns JSON format
- All input validation done on client side
- Server URL is configurable by user

==========================================================