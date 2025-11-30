package com.TIES4520.onto.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
//import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.TIES4520.onto.demo.model.User;

@Service
public class UserService {

    @Value("${rdf.endpoint.url}")
    private String repoUrl;
    
    private Repository getrepo() {
    	return new SPARQLRepository(repoUrl);
    }

    public List<User> getAllUsers(String orderBy) {
    	System.out.println("Get all users called. Service class.");
    	System.out.println("Endpoint: " + repoUrl);
        List<User> users = new ArrayList<>();
        String orderClause = (orderBy == null || orderBy.isBlank()) ? "" : "ORDER BY ?" + orderBy;
        String query = "PREFIX ex: <http://example.org/schema#>\n" +
                "SELECT ?id ?name ?email ?age ?created\n" +
                "WHERE {\n" +
                "  ?s a ex:User ;\n" +
                "     ex:id ?id ;\n" +
                "     ex:name ?name .\n" +
                "  OPTIONAL { ?s ex:email ?email . }\n" +
                "  OPTIONAL { ?s ex:age ?age . }\n" +
                "  OPTIONAL { ?s ex:createdDate ?created . }\n" +
                "}\n" + 
                orderClause;

        Repository repo = getrepo();
        repo.init();
        System.out.println("Query: " + query);
        try (RepositoryConnection conn = repo.getConnection()) {
        	System.out.println("Inside Try 1");
            TupleQuery tq = conn.prepareTupleQuery(query);
            try (TupleQueryResult rs = tq.evaluate()) {
            	System.out.println("Inside Try 2");
            	int userSize = 0;
            	if(rs != null) {
            		System.out.println("Resource retrived");
            		
            	}
                while (rs.hasNext()) {
                	userSize++;
                    BindingSet bs = rs.next();
                    users.add(new User(
                            bs.getValue("id").stringValue(),
                            bs.getValue("name").stringValue(),
                            bs.getValue("email").stringValue(),
                            bs.hasBinding("age") ? Integer.valueOf(bs.getValue("age").stringValue()) : null
                    ));
                }
            }
        } finally {
            repo.shutDown();
        }
        System.out.println("User size: " + users.size());
        return users;
    }

    public List<User> getAllUsersWithFilter(String filter, String orderBy) {
    	List<User> users = new ArrayList<>();
    	
    	String orderClause = (orderBy == null || orderBy.isBlank()) ? "" : "ORDER BY ?" + orderBy;
        String filterClause = (filter == null || filter.isBlank()) ? "" : filter + "\n";
        String sparql = "PREFIX ex: <http://example.org/schema#>\n" +
                "SELECT ?id ?name ?email ?age ?created\n" +
                "WHERE {\n" +
                "  ?s a ex:User ;\n" +
                "     ex:id ?id ;\n" +
                "     ex:name ?name .\n" +
                "  OPTIONAL { ?s ex:email ?email . }\n" +
                "  OPTIONAL { ?s ex:age ?age . }\n" +
                "  OPTIONAL { ?s ex:createdDate ?created . }\n" +
                filterClause +
                "}\n" +
                orderClause;
        
        Repository repo = getrepo();
        repo.init();
        
        try (RepositoryConnection conn = repo.getConnection()) {
            TupleQuery tq = conn.prepareTupleQuery(sparql);
            try (TupleQueryResult rs = tq.evaluate()) {
                while (rs.hasNext()) {
                    BindingSet bs = rs.next();
                    users.add(new User(
                            bs.getValue("id").stringValue(),
                            bs.getValue("name").stringValue(),
                            bs.getValue("email").stringValue(),
                            bs.hasBinding("age") ? Integer.valueOf(bs.getValue("age").stringValue()) : null
                    ));
                }
            }
        } finally {
            repo.shutDown();
        }
        
        return users;
    }
    
    public void addUser(User user) {
    	String sparql = String.format(
                "PREFIX ex: <http://example.org/schema#>\\n" +
                "INSERT DATA { _:u a ex:User ; ex:id \"%s\" ; ex:name \"%s\" ; %s %s }",
                user.getId(), esc(user.getName()),
                user.getEmail() == null ? "" : "ex:email \"" + esc(user.getEmail()) + "\" ;",
                user.getAge() == null ? "" : "ex:age \"" + user.getAge() + "\" ;"
        );


        Repository repo = getrepo();
        repo.init();
        
        try (RepositoryConnection conn = repo.getConnection()) {
            conn.prepareUpdate(sparql).execute();
        } finally {
            repo.shutDown();
        }
    }

    public void updateUsers(String conditionFilter, User updatedValues) {
    	String insertPart = "";
        if (updatedValues.getName() != null) insertPart += String.format("?s ex:name \"%s\" .\\n", esc(updatedValues.getName()));
        if (updatedValues.getEmail() != null) insertPart += String.format("?s ex:email \"%s\" .\\n", esc(updatedValues.getEmail()));
        if (updatedValues.getAge() != null) insertPart += String.format("?s ex:age \"%d\" .\\n", updatedValues.getAge());

        String condition = (conditionFilter == null) ? "" : conditionFilter;

        String sparql = String.format("PREFIX ex: <http://example.org/schema#>\\n" +
                "DELETE { ?s ex:name ?oldName . ?s ex:email ?oldEmail . ?s ex:age ?oldAge . }\\n" +
                "INSERT { %s }\\n" +
                "WHERE { ?s a ex:User . OPTIONAL { ?s ex:name ?oldName . } OPTIONAL { ?s ex:email ?oldEmail . } OPTIONAL { ?s ex:age ?oldAge . } %s }",
                insertPart, condition);
        
        Repository repo = getrepo();
        repo.init();

        try (RepositoryConnection conn = repo.getConnection()) {
        	Update update = conn.prepareUpdate(QueryLanguage.SPARQL, sparql);
            update.execute();
        } finally {
            repo.shutDown();
        }
    }
    
    public void deleteUser(String filter) {
    	String condition = (filter == null || filter.isBlank()) ? "?s a ex:User ." : filter;
        String sparql = String.format("PREFIX ex: <http://example.org/schema#>\\n" +
                "DELETE WHERE { %s ?s ?p ?o . }", condition);


        Repository repo = getrepo();
        repo.init();

        try (RepositoryConnection conn = repo.getConnection()) {
            conn.prepareUpdate(sparql).execute();
        } finally {
            repo.shutDown();
        }
        
    }
    
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\\\", "\\\\\\\\").replace("\"", "\\\\\"").replace("\n", "\\\\n");
    }
    
}
