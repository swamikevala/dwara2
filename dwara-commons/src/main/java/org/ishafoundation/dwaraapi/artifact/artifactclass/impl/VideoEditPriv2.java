package org.ishafoundation.dwaraapi.artifact.artifactclass.impl;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_.VideoEdited;
import org.springframework.stereotype.Component;

@Component("video-edit-priv2")
public class VideoEditPriv2 extends VideoEdited{
	
	@PostConstruct
	public void setup() {
		setEDITED_CODE_REGEX("^ZX[\\d]+");
	}
}