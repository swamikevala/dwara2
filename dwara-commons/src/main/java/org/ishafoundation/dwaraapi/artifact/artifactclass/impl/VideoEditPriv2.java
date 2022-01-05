package org.ishafoundation.dwaraapi.artifact.artifactclass.impl;

import org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_.VideoEdited;
import org.springframework.stereotype.Component;

@Component("video-edit-priv2")
public class VideoEditPriv2 extends VideoEdited{
	
	private String EDITED_CODE_REGEX = "^ZX[\\d]+";
}