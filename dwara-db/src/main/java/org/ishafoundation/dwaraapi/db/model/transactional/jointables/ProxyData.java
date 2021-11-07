package org.ishafoundation.dwaraapi.db.model.transactional.jointables;

public class ProxyData {
    public int artifactId;
    public String proxyVolumeId;
    public String proxyStatus;
    public ProxyData(int artifactId, String proxyVolumeId, String proxyStatus) {
        this.artifactId = artifactId;
        this.proxyVolumeId = proxyVolumeId;
        this.proxyStatus = proxyStatus;
    }
}
