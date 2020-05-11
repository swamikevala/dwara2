package org.ishafoundation.dwaraapi.db.model.transactional;

public class ActionColumns {
	
	private IngestColumns ingest;
	
	private RestoreColumns restore;
	
	private FormatColumns format;

	
	public IngestColumns getIngest() {
		return ingest;
	}

	public void setIngest(IngestColumns ingest) {
		this.ingest = ingest;
	}

	public RestoreColumns getRestore() {
		return restore;
	}

	public void setRestore(RestoreColumns restore) {
		this.restore = restore;
	}

	public FormatColumns getFormat() {
		return format;
	}

	public void setFormat(FormatColumns format) {
		this.format = format;
	}
}
