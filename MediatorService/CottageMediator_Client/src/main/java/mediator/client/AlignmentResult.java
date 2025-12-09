package mediator.client;

import java.util.Map;

import com.hp.hpl.jena.rdf.model.Property;

public class AlignmentResult {

    private final Map<String, Property> alignmentMap;
    private final Map<String, AlignmentCandidate> uiMap;
    private final boolean ourOntology;

    public AlignmentResult(Map<String, Property> alignmentMap,
                           Map<String, AlignmentCandidate> uiMap,
                           String[] candidates,
                           boolean ourOntology) {
        this.alignmentMap = alignmentMap;
        this.uiMap = uiMap;
        this.ourOntology = ourOntology;
    }

    public Map<String, Property> getAlignmentMap() {
        return alignmentMap;
    }

    public Map<String, AlignmentCandidate> getUiMap() {
        return uiMap;
    }

    public boolean isOurOntology() {
        return ourOntology;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AlignmentResult {\n");
        sb.append("  ourOntology = ").append(ourOntology).append("\n");
        sb.append("  alignments:\n");
        for (Map.Entry<String, AlignmentCandidate> e : uiMap.entrySet()) {
            AlignmentCandidate ac = e.getValue();
            sb.append("    ")
              .append(ac.getMyName())
              .append(" -> ")
              .append(ac.getRemoteName())
              .append(" (uri=")
              .append(ac.getRemoteUri())
              .append(", score=")
              .append(ac.getScore())
              .append(")\n");
        }
        sb.append("}");
        return sb.toString();
    }
}