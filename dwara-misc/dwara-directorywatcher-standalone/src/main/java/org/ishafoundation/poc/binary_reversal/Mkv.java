package org.ishafoundation.poc.binary_reversal;

import java.util.ArrayList;
import java.util.List;

public class Mkv {
	
	private String baseTimecode;
	
	private List<Frame> frames = new ArrayList<Frame>();

	public String getBaseTimecode() {
		return baseTimecode;
	}

	public void setBaseTimecode(String baseTimecode) {
		this.baseTimecode = baseTimecode;
	}

	public List<Frame> getFrames() {
		return frames;
	}

	public void setFrames(List<Frame> frames) {
		this.frames = frames;
	}
}
