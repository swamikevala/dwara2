package org.ishafoundation.videopub.pfr;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public abstract class PFRFile {
	
	public abstract PFRComponentFile extractHeader(String destination) throws IOException;
	
	
	public abstract PFRComponentFile extractFooter(String destination) throws IOException;
	
	
	public abstract PFRComponentFile extractIndex(String destination) throws IOException;
	
	
	public abstract PFRComponentFile extractEssence(String destination, long startByte, long bytes) throws IOException;
	
	
	protected static void copyBytes(FileInputStream in, FileOutputStream out, long start, long count) throws IOException {
		
	    FileChannel ifc = in.getChannel();
	    FileChannel ofc = out.getChannel();
	    ifc.transferTo(start, count, ofc);
	}
	
}

