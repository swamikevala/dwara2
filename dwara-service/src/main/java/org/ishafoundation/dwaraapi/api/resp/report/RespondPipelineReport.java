package org.ishafoundation.dwaraapi.api.resp.report;

import java.util.List;

public class RespondPipelineReport {
    public List<String> ingestedArtifacts;
    public List<String> inprogress;
    public List<String> inprogressBeforeYesterday;
    public List<String> completed;
    public List<String> copy1WriteFailed;
    public List<String> copy2WriteFailed;
    public List<String> copy3WriteFailed;
    public List<String> proxyGenFailed;
    public List<String> mapUpdateFailed;
    public List<String> inStaged3Days;
}
