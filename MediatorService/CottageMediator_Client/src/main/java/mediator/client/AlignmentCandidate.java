package mediator.client;

public class AlignmentCandidate {

    private final String myName;
    private final String remoteName;
    private final String remoteUri;
    private final double score;

    public AlignmentCandidate(String myName, String remoteName, String remoteUri, double score) {
        this.myName = myName;
        this.remoteName = remoteName;
        this.remoteUri = remoteUri;
        this.score = score;
    }

    public String getMyName()     { return myName; }
    public String getRemoteName() { return remoteName; }
    public String getRemoteUri()  { return remoteUri; }
    public double getScore()      { return score; }
}
