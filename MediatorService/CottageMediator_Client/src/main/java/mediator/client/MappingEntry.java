package mediator.client;

import java.util.Map;

public class MappingEntry {

    private String URL;
    private Map<String, AlignmentCandidate> mapping;

    public MappingEntry() { }

    public MappingEntry(String URL, Map<String, AlignmentCandidate> mapping) {
        this.URL = URL;
        this.mapping = mapping;
    }

    public String getURL() { return URL; }
    public void setURL(String URL) { this.URL = URL; }

    public Map<String, AlignmentCandidate> getMapping() { return mapping; }
    public void setMapping(Map<String, AlignmentCandidate> mapping) {
        this.mapping = mapping;
    }
}

