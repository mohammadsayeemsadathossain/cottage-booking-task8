package mediator.client;

public class AlignmentCandidate {

    private String myName;
    private String remoteName;
    private String remoteUri;
    private double score;

    public AlignmentCandidate(String myName, String remoteName, String remoteUri, double score) {
        this.myName = myName;
        this.remoteName = remoteName;
        this.remoteUri = remoteUri;
        this.score = score;
    }

    public AlignmentCandidate() {
    	
    }
    
    public String getMyName()     { return myName; }
    public String getRemoteName() { return remoteName; }
    public String getRemoteUri()  { return remoteUri; }
    public double getScore()      { return score; }
}
