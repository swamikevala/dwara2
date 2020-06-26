package org.ishafoundation.dwaraapi.api.req.ingest;

//@Getter
public class RequestParams{
	
	private String artifact_name;

	private String prev_sequence_code;

	private Boolean rerun;

	private Integer rerun_no;

	private Integer[] skip_storagetasks;

	private Integer[] skip_processingtasks;

	
	public String getArtifact_name() {
		return artifact_name;
	}

	public void setArtifact_name(String artifact_name) {
		this.artifact_name = artifact_name;
	}

	public String getPrev_sequence_code() {
		return prev_sequence_code;
	}

	public void setPrev_sequence_code(String prev_sequence_code) {
		this.prev_sequence_code = prev_sequence_code;
	}

	public Boolean getRerun() {
		return rerun;
	}

	public void setRerun(Boolean rerun) {
		this.rerun = rerun;
	}

	public Integer getRerun_no() {
		return rerun_no;
	}

	public void setRerun_no(Integer rerun_no) {
		this.rerun_no = rerun_no;
	}

	public Integer[] getSkip_storagetasks() {
		return skip_storagetasks;
	}

	public void setSkip_storagetasks(Integer[] skip_storagetasks) {
		this.skip_storagetasks = skip_storagetasks;
	}

	public Integer[] getSkip_processingtasks() {
		return skip_processingtasks;
	}

	public void setSkip_processingtasks(Integer[] skip_processingtasks) {
		this.skip_processingtasks = skip_processingtasks;
	}
}
