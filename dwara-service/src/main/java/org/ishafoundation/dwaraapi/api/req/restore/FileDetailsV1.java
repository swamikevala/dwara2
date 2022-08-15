package org.ishafoundation.dwaraapi.api.req.restore;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.json.Frame;

public class FileDetailsV1 {
	
	private Integer id;
	
	private List<Frame> frame;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Frame> getFrame() {
		return frame;
	}

	public void setFrame(List<Frame> frame) {
		this.frame = frame;
	}
}
