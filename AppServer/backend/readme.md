# General application detail
## Environment
Java J2EE version 11
Spring Boot
Maven
_Please see the pom.xml file for dependency detail._

## HTTP server
Apache Tomcat 9.*

## Development Environment
Eclipse

Import the application in Eclipse as existing maven project. It should resolve all the dependency issues.

## Application details
Application is a framework to test the query with SPRQL and RDF end point.
For RDF Endpoint: please check the configuration file in {base_path}/src/main/resources/appliction.properties
Update the endpoint according to your RDF database location/endpoint.

By default, the application contains only one module. User module. in this module it supports the following functions:
1. Get all users.
2. Get all users with filter.
3. Add user.
4. Update user.
6. Delete user.

Inside the following directory please find the appropriate Controller, Model and Service class.
Java file path: {base_path}/src/main/java/com/TIES4520/onto/demo/
Copy the UserController.java file and make your own controller file. Similarly make a copy or User.java file under model and UserService.java file under service, update to your appropriate model and service file.


## Run the application
To run the application in Tomcat, export the war file and deploy it in tomcat webapps directory.
Test the application [Application health check] with following url: http://{base_path_of_deployed_app}:{tomcat port, default 8080}/{applicationname}/api/ping/.
Sample path: http://localhost:8080/demo/api/ping/
You should receive a "APPI OK!" message.
