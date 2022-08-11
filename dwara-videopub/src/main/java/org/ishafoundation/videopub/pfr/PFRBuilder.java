package org.ishafoundation.videopub.pfr;

import java.io.File;
import java.io.IOException;

public abstract class PFRBuilder {
	
	
	public abstract long getByteOffset(int frame);
	
	
	public abstract File buildClip(String destination, PFRComponentFile header, PFRComponentFile footer, PFRComponentFile essence, int startFrame, int frames) throws Exception;
}


