package org.ishafoundation.videopub.pfr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import tv.amwa.maj.io.mxf.FooterPartitionPack;
import tv.amwa.maj.io.mxf.HeaderPartitionPack;
import tv.amwa.maj.io.mxf.MAJMXFStreamException;
import tv.amwa.maj.io.mxf.MXFBuilder;
import tv.amwa.maj.io.mxf.MXFStream;
import tv.amwa.maj.io.mxf.PartitionPack;
import tv.amwa.maj.io.mxf.RandomIndexPack;
import tv.amwa.maj.io.mxf.UL;


public class PFRFileMXF extends PFRFile {

	private File source;
	private HeaderPartitionPack hpp;
	private FooterPartitionPack fpp;
	private RandomIndexPack rip;
	
	private long headerSize;
	private long footerOffset;
	private long footerSize;
	private long indexSize;
	
	public static final byte[] headerKey =          {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x05, 0x01, 0x01, 0x0d, 0x01, 0x02, 0x01, 0x01, 0x02, 0x04, 0x00};
	public static final byte[] footerKey =          {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x05, 0x01, 0x01, 0x0d, 0x01, 0x02, 0x01, 0x01, 0x04, 0x04, 0x00};
	public static final byte[] ripKey =             {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x05, 0x01, 0x01, 0x0d, 0x01, 0x02, 0x01, 0x01, 0x11, 0x01, 0x00};
	public static final byte[] indexKey =           {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x53, 0x01, 0x01, 0x0d, 0x01, 0x02, 0x01, 0x01, 0x10, 0x01, 0x00};
	public static final byte[] systemElementKey =   {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x05, 0x01, 0x01, 0x0d, 0x01, 0x03, 0x01, 0x04, 0x01, 0x01, 0x00};
    public static final byte[] fillKey =            {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x03, 0x01, 0x02, 0x10, 0x01, 0x00, 0x00, 0x00};
    public static final byte[] fillKeyLegacy =      {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x01, 0x03, 0x01, 0x02, 0x10, 0x01, 0x00, 0x00, 0x00};
    public static final byte[] primerKey =          {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x05, 0x01, 0x01, 0x0d, 0x01, 0x02, 0x01, 0x01, 0x05, 0x01, 0x00};
    public static final byte[] materialPackageKey = {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x53, 0x01, 0x01, 0x0d, 0x01, 0x01, 0x01, 0x01, 0x01, 0x36, 0x00};
    public static final byte[] srcPackageKey =      {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x53, 0x01, 0x01, 0x0d, 0x01, 0x01, 0x01, 0x01, 0x01, 0x37, 0x00};                                
    public static final byte[] prefaceKey =         {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x53, 0x01, 0x01, 0x0d, 0x01, 0x01, 0x01, 0x01, 0x01, 0x2f, 0x00};
    public static final byte[] sequenceKey =        {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x53, 0x01, 0x01, 0x0d, 0x01, 0x01, 0x01, 0x01, 0x01, 0x0f, 0x00};
    public static final byte[] tcComponentKey =     {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x53, 0x01, 0x01, 0x0d, 0x01, 0x01, 0x01, 0x01, 0x01, 0x14, 0x00};
    public static final byte[] sourceClipKey =      {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x53, 0x01, 0x01, 0x0d, 0x01, 0x01, 0x01, 0x01, 0x01, 0x11, 0x00};
    public static final byte[] genPicEssDescKey =   {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x53, 0x01, 0x01, 0x0d, 0x01, 0x01, 0x01, 0x01, 0x01, 0x28, 0x00};
    public static final byte[] identificationKey =  {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x53, 0x01, 0x01, 0x0d, 0x01, 0x01, 0x01, 0x01, 0x01, 0x30, 0x00};
	
    // identification set info
    public static final String companyName = "Isha Foundation Archives";
    public static final String productVersionString = "1.0.0";
    public static final String productName = "Dwara Media Toolkit";
    public static final byte[] productUID = {0x08, 0x2a, 0x3f, 0x59, 0x25, 0x1b, 0x4d, 0x40, 0x2d, 0x2c, 0x21, 0x09, 0x1d, 0x1c, 0x04, 0x00};

    
	public PFRFileMXF(File source) throws IOException {
		
		if(!source.exists() || source.isDirectory()) { 
			throw new IllegalArgumentException(
					"File does not exist");
		} else if (!validateFile(new PFRComponentFile(source, PFRComponentType.HEADER))) {
			throw new IllegalArgumentException(
	                "File does not start with MXF header key");
		}
		
		this.source = source;
		
		MXFBuilder.registerMXF();
		readOffsets();
	}
	
	
	private void readOffsets() throws IOException {
		
		InputStream in = new FileInputStream(source);
		long consumed = readPack(in); // read header pp
		
		headerSize = consumed + hpp.getHeaderByteCount();
		footerOffset = hpp.getFooterPartition();
		indexSize = hpp.getIndexByteCount();
		
		in.skip(footerOffset - consumed);
		consumed = readPack(in); // read footer pp
		
		rip = MXFStream.readRandomIndexPack(in);
		footerSize = consumed + rip.getLength();

		in.close();
	}
	
	
	private long readPack(InputStream in) throws IOException {
		
		PartitionPack pp = MXFStream.readPartitionPack(in);
		long ppSize = 20 + pp.getEncodedSize();
		
		UL key = MXFStream.readSingleKey(in);
		if (keyMatch(key.getUniversalLabel(), PFRFileMXF.fillKey, 256)) { // ignore 9th byte (so legacy fill key is matched)
        	ppSize += (16 + MXFStream.readPastFill(in));
        }
		
		if (pp instanceof HeaderPartitionPack) {
			this.hpp = (HeaderPartitionPack) pp;
		} else if (pp instanceof FooterPartitionPack) {
			this.fpp = (FooterPartitionPack) pp;
		}
		
		return ppSize;
	}
	
	
	protected static boolean validateFile(PFRComponentFile pFile) throws IOException {
		
		byte[] key = null;
		
		switch(pFile.getType()) {
		case HEADER:
			key = headerKey;
			break;
		case FOOTER:
			key = footerKey;
			break;
		case INDEX:
			key = indexKey;
			break;
		case ESSENCE:
			key = systemElementKey; // Each essence unit starts with a system element
			break;
		}
		
		byte[] pFileKey = new byte[16];
		InputStream in = new FileInputStream(pFile.getFile());
		
		if (in.read(pFileKey) != pFileKey.length) { 
			throw new MAJMXFStreamException("Insufficient bytes in file to read a complete key"); 
		}
		in.close();
		
		if (!keyMatch(pFileKey, key)) {
			return false;
		}
		return true;
	}
	
	
	private PFRComponentFile extractPFRComponentFile(String destination, PFRComponentType type, long startByte, long byteCount) throws IOException {
		
		FileInputStream in = new FileInputStream(source);
		
		File file = new File(destination, source.getName() + "." + type.getExtension());
		file.createNewFile();
		FileOutputStream out = new FileOutputStream(file);
		
		PFRFile.copyBytes(in, out, startByte, byteCount);
		PFRComponentFile pFile = new PFRComponentFile(file, type);
		
		in.close();
		out.close();
		
		if (validateFile(pFile)) {
			return pFile;
		} else {
			throw new RuntimeException(
	                "PFRComponentFile has invalid key"); 
		}
	}
	
	
	public PFRComponentFile extractHeader(String destination) throws IOException {
		
		PFRComponentType type = PFRComponentType.HEADER;
		return extractPFRComponentFile(destination, type, 0, headerSize);
	}
	
	
	public PFRComponentFile extractFooter(String destination) throws IOException {
		
		PFRComponentType type = PFRComponentType.FOOTER;
		return extractPFRComponentFile(destination, type, footerOffset, footerSize);
	}
	
	
	public PFRComponentFile extractIndex(String destination) throws IOException {
		
		PFRComponentType type = PFRComponentType.INDEX;
		return extractPFRComponentFile(destination, type, headerSize, indexSize);
	}
	

	public PFRComponentFile extractEssence(String destination, long startByte, long bytes) throws IOException {
		
		PFRComponentType type = PFRComponentType.ESSENCE;
		long essenceOffset = headerSize + indexSize;
		return extractPFRComponentFile(destination, type, essenceOffset + startByte, bytes);
	}
	
	
	public static boolean keyMatch(byte[] key1, byte[] key2, int ignore) {
		
		// ignore parameter is a value between 0 and 2^15 which is treated as a bitmap of length 16. The bytes in the corresponding on bit positions are ignored
		// example: ignore = 256, means the 9th byte from the right will be ignored in the comparison
		
        for (int i = 0; i < 16; i++) {
            if ((((ignore>>(16-(i+1)))&1) == 0) && (key1[i] != key2[i])) {
                return false;
            }
        }
        return true;
    }
	
	
	public static boolean keyMatch(byte[] key1, byte[] key2) {
        
        return keyMatch(key1, key2, 0);
    }
	
}
