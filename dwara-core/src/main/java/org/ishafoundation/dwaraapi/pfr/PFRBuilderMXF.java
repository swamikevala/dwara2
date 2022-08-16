package org.ishafoundation.dwaraapi.pfr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import tv.amwa.maj.exception.BadParameterException;
import tv.amwa.maj.exception.IllegalPropertyValueException;
import tv.amwa.maj.exception.InsufficientSpaceException;
import tv.amwa.maj.industry.Forge;
import tv.amwa.maj.io.mxf.IndexEntry;
import tv.amwa.maj.io.mxf.IndexTable;
import tv.amwa.maj.io.mxf.IndexTableSegment;
import tv.amwa.maj.io.mxf.MXFBuilder;
import tv.amwa.maj.io.mxf.MXFStream;
import tv.amwa.maj.io.mxf.MXFStream.LengthAndConsumed;
import tv.amwa.maj.io.mxf.PartitionPack;
import tv.amwa.maj.io.mxf.RandomIndexItem;
import tv.amwa.maj.io.mxf.RandomIndexPack;
import tv.amwa.maj.io.mxf.UL;
import tv.amwa.maj.io.mxf.impl.IndexTableImpl;
import tv.amwa.maj.io.mxf.impl.IndexTableSegmentImpl;



public class PFRBuilderMXF extends PFRBuilder {

	private PFRComponentFile index;
	private IndexTable indexTable;
	
	public static final int MAX_INDEX_SEGMENT_SIZE = 240;   
	public static final int KAG_SIZE = 512; 
	
	public PFRBuilderMXF(PFRComponentFile index) throws IOException {
		
		if(!index.getFile().exists() || index.getFile().isDirectory()) { 
			throw new IllegalArgumentException(
					"File does not exist");
		} else if (!PFRFileMXF.validateFile(index)) {
			throw new IllegalArgumentException(
	                "File does not start with MXF index key");
		}
		
		this.index = index;
		loadIndex();
	}
	
	
	private void loadIndex() throws IOException {
		
		InputStream in = new FileInputStream(index.getFile());
		
		byte[] bytes = new byte[(int)index.getFile().length()];
		in.read(bytes);
		
        ByteBuffer indexTableBytes = ByteBuffer.wrap(bytes);
        in.close();

        indexTable = IndexTableImpl.createFromBuffer(indexTableBytes);
	}
	
	
	public long getByteOffset(int frame) throws IllegalArgumentException {
		
		return indexTable.streamOffset(frame, 0);
	}
	
	
	public File buildClip(String destination, PFRComponentFile header, PFRComponentFile footer, PFRComponentFile essence, int startFrame, int frames) throws Exception {
		int endFrame = startFrame + frames;
		String clipName = essence.getFile().getName().replace("." + PFRComponentType.ESSENCE.getExtension(), "_" + startFrame + "_" + endFrame + ".MXF");
		File clipFile = new File(destination, clipName); 
		FileOutputStream out = new FileOutputStream(clipFile);
		long totalLength = 0l;
		
		File headerFile = header.getFile();
		File indexFile = buildClipIndex(destination, startFrame, frames);
		File essenceFile = essence.getFile();
		File footerFile = footer.getFile();
		
		totalLength += headerFile.length();
		totalLength += indexFile.length();
		totalLength += essenceFile.length();
		
		headerFile = updateHeaderFile(headerFile, totalLength, startFrame, frames);
		FileInputStream in = new FileInputStream(headerFile);
		PFRFile.copyBytes(in, out, 0, headerFile.length());
		in.close();
		//headerFile.delete();
		
		in = new FileInputStream(indexFile);
		PFRFile.copyBytes(in, out, 0, indexFile.length());
		in.close();
		//indexFile.delete();
		
		in = new FileInputStream(essenceFile);
		PFRFile.copyBytes(in, out, 0, essenceFile.length());
		in.close();
		essenceFile.delete();
		
		footerFile = updateFooterFile(footerFile, totalLength, startFrame, frames);
		in = new FileInputStream(footerFile);
		PFRFile.copyBytes(in, out, 0, footerFile.length());
		in.close();
		footerFile.delete();
		
		out.close();
		return clipFile;
	}
	
	
	private File updateHeaderFile(File file, long fileLength, int startFrame, int frames) throws Exception {
		
		InputStream in = new FileInputStream(file);
		
		String newName = renameFile(file.getName(), startFrame, frames, false);
		File newFile = new File(file.getParent(), newName); 
		
		OutputStream out = new FileOutputStream(newFile);
		int newHeaderLength = 0; 
		
		PartitionPack pp = MXFStream.readPartitionPack(in);
		pp.setFooterPartition(fileLength);
		long headerByteCount = pp.getHeaderByteCount(); // check the length still matches after updating
		
		MXFStream.writePartitionPack(out, pp);
		int ppSize = 20 + pp.getEncodedSize();
		newHeaderLength += ppSize;
		
		int ppFillSize = (ppSize/KAG_SIZE +1)*KAG_SIZE - ppSize;
		MXFStream.writeFill(out, ppFillSize);
		newHeaderLength += ppFillSize;
		
		UL primerUL = MXFStream.readKey(in).getKey();
		if (!PFRFileMXF.keyMatch(primerUL.getUniversalLabel(), PFRFileMXF.primerKey)) { 
        	throw new IllegalArgumentException("Cannot find MXF Primer Pack Key");
		}
		
		LengthAndConsumed lac;
        lac = MXFStream.readBERLength(in);
        long primerLength = lac.getLength();
        long primerLengthEnc = lac.getConsumed();
        ByteBuffer primerBuffer = MXFStream.readValue(in, primerLength);
        newHeaderLength += (16 + lac.getTotal());
        
        UL fillUL = MXFStream.readSingleKey(in);
        if (!PFRFileMXF.keyMatch(fillUL.getUniversalLabel(), PFRFileMXF.fillKey, 256)) { 
        	throw new IllegalArgumentException("Expecting Fill Key");
		}
        long fill = MXFStream.readPastFill(in);
        MXFStream.writeKey(out, primerUL);
        MXFStream.writeBERLength(out, primerLength, (int) primerLengthEnc);
        MXFStream.writeValue(out, primerBuffer);
        MXFStream.writeFill(out, 16 + fill);
        newHeaderLength += (16 + fill);
		
		// iterate through local sets, rewriting them with updated values
		UL setUL = (UL) Forge.nilAUID();
		long setLength = 0l;
		long setLengthEncBytes = 0;
		ByteBuffer setValue;
		
		while(!PFRFileMXF.keyMatch(setUL.getUniversalLabel(), PFRFileMXF.fillKey, 256)) {
			
			setUL = MXFStream.readSingleKey(in);
			if (PFRFileMXF.keyMatch(setUL.getUniversalLabel(), PFRFileMXF.fillKey, 256)) {
				break;
			}
			
			lac = MXFStream.readBERLength(in);
			setLength = lac.getLength();
			setLengthEncBytes = lac.getConsumed();
			setValue = MXFStream.readValue(in, setLength);
			newHeaderLength += (16 + lac.getTotal());
			
			updateLocalSet(setUL, setValue, startFrame, frames);
			
			MXFStream.writeKey(out,  setUL);
			MXFStream.writeBERLength(out, setLength, (int) setLengthEncBytes);
			MXFStream.writeValue(out, setValue);
			
		}
		
		// add new identification set // Do this properly later using MAJ Framework methods
		/*
		IdentificationImpl identification = new IdentificationImpl();
		identification.setApplicationSupplierName(PFRFileMXF.companyName);
		identification.setApplicationName(PFRFileMXF.productName);
		identification.setApplicationVersionString(PFRFileMXF.productVersionString);
		identification.setApplicationProductID(new AUIDImpl(PFRFileMXF.productUID));
		identification.setFileModificationDate(Forge.now());
		
		MXFStream.writeKey(out,  new AUIDImpl(PFRFileMXF.identificationKey));
		setLength = MXFBuilder.lengthOfLocalSet(identification);
		newHeaderLength += (16 + 4 + setLength);
		
		
		MXFStream.writeBERLength(out, setLength, 4);
		PrimerPack primerPack = (PrimerPack) MXFBuilder.readFixedLengthPack((AUIDImpl) primerUL, primerBuffer);
		
		ByteBuffer identBuffer = ByteBuffer.allocate((int) setLength + 20); // MXFBuilder.writeLocalSet adds instanceID itself so need extra 20 bytes
		List<PropertyValue> forwardReferences = new ArrayList<PropertyValue>();
		MXFBuilder.writeLocalSet(identification, identBuffer, primerPack, forwardReferences);
		MXFStream.writeValue(out, identBuffer);
		*/
		// write header fill
		
//		int lastBit = newHeaderLength % KAG_SIZE;
//		fill = KAG_SIZE - lastBit;
//		if (fill > 0) {
//        	if (fill > 20) {
//        		MXFStream.writeFill(out, fill);
//                newHeaderLength += fill;
//        	} else {
//        		MXFStream.writeFill(out, fill + KAG_SIZE);
//                newHeaderLength += (fill + KAG_SIZE);
//        	}	
//        }
		
		fill = KAG_SIZE + headerByteCount - newHeaderLength;
		MXFStream.writeFill(out, fill);
		in.close();
		out.close();
		
		return newFile;
	}
	
	
	private void updateLocalSet(UL setUL, ByteBuffer buffer, int startFrame, int frames) throws NullPointerException, BadParameterException, IllegalPropertyValueException, InsufficientSpaceException {
		
		byte[] framesBytes = ByteBuffer.allocate(8).putLong(frames).array();
		byte[] startFrameBytes = ByteBuffer.allocate(8).putLong(startFrame).array();
		byte[] currentTimestamp = MXFUtils.timestampToBytes(Forge.now());
		byte[] timecodeBytes =  ByteBuffer.allocate(8).array();
		
		if (PFRFileMXF.keyMatch(setUL.getUniversalLabel(), PFRFileMXF.prefaceKey)) {
			MXFUtils.updateLocalSetPropertyValueBytes(setUL, buffer, (short)0x3B02, currentTimestamp);
			
		} else if (PFRFileMXF.keyMatch(setUL.getUniversalLabel(), PFRFileMXF.materialPackageKey)) {
			MXFUtils.updateLocalSetPropertyValueBytes(setUL, buffer, (short)0x4404, currentTimestamp);
			
		} else if (PFRFileMXF.keyMatch(setUL.getUniversalLabel(), PFRFileMXF.srcPackageKey)) {
			MXFUtils.updateLocalSetPropertyValueBytes(setUL, buffer, (short)0x4404, currentTimestamp);
			
		} else if (PFRFileMXF.keyMatch(setUL.getUniversalLabel(), PFRFileMXF.tcComponentKey)) {
			MXFUtils.updateLocalSetPropertyValueBytes(setUL, buffer, (short)0x0202, framesBytes);
			MXFUtils.getLocalSetPropertyValueBytes(setUL, buffer, (short)0x1501, timecodeBytes);
			long newTimecode = (new BigInteger(1, timecodeBytes)).longValue() + (new BigInteger(1, startFrameBytes)).longValue();
			byte[] newTimecodeBytes =  ByteBuffer.allocate(8).putLong(newTimecode).array();
			MXFUtils.updateLocalSetPropertyValueBytes(setUL, buffer, (short)0x1501, newTimecodeBytes);
			
		} else if (PFRFileMXF.keyMatch(setUL.getUniversalLabel(), PFRFileMXF.sequenceKey)) {
			MXFUtils.updateLocalSetPropertyValueBytes(setUL, buffer, (short)0x0202, framesBytes);
			
		} else if (PFRFileMXF.keyMatch(setUL.getUniversalLabel(), PFRFileMXF.sourceClipKey)) {
			MXFUtils.updateLocalSetPropertyValueBytes(setUL, buffer, (short)0x0202, framesBytes);
			
		} else if (PFRFileMXF.keyMatch(setUL.getUniversalLabel(), PFRFileMXF.genPicEssDescKey)) {
			MXFUtils.updateLocalSetPropertyValueBytes(setUL, buffer, (short)0x3002, framesBytes);
		}
			
	}
	
	
private File updateFooterFile(File file, long fileLength, int startFrame, int frames) throws IOException {
		
		InputStream in = new FileInputStream(file);
		
		String newName = renameFile(file.getName(), startFrame, frames, false);
		File newFile = new File(file.getParent(), newName); 
		
		OutputStream out = new FileOutputStream(newFile);
		
		PartitionPack pp = MXFStream.readPartitionPack(in);
		pp.setThisPartition(fileLength);
		pp.setFooterPartition(fileLength);
		int bodySID = pp.getBodySID();
		
		MXFStream.writePartitionPack(out, pp);
		int ppSize = 20 + pp.getEncodedSize();
		
		int ppFillSize = (ppSize/KAG_SIZE +1)*KAG_SIZE - ppSize;
		MXFStream.writeFill(out, ppFillSize);
		
		// Update Random Index Pack
		UL key = MXFStream.readSingleKey(in);
		if (PFRFileMXF.keyMatch(key.getUniversalLabel(), PFRFileMXF.fillKey, 256)) { // ignore 9th byte (so legacy fill key is matched)
        	MXFStream.readPastFill(in);
        }

		RandomIndexPack rip = MXFStream.readRandomIndexPack(in);
		RandomIndexItem[] rii = rip.getPartitionIndex();
		for (RandomIndexItem ri : rii) {
			if (ri.getBodySID() == bodySID) {
				ri.setByteOffset(fileLength);
			}
		}
		rip.setPartitionIndex(rii);
		MXFStream.writeRandomIndexPack(out, rip);
		in.close();
		out.close();
		
		return newFile;
	}
	

	private String renameFile(String filename, int startFrame, int frames, boolean removeExtension) {

		String[] parts = filename.split("\\.");   
		
		int n = parts.length;
		int endFrame = startFrame + frames;
		parts[n - 2] = parts[n - 2] + "_" + startFrame + "_" + endFrame;
		
		String clipName = String.join(".", parts);
		
		if (removeExtension) {
			clipName = clipName.substring(0, clipName.length() - 4);  
		}
		
		return clipName;
	}
	
	
	public File buildClipIndex(String destination, int startFrame, int frames) throws IOException {
		
		ArrayList<IndexTableSegment> newSegments = createIndexSegments(startFrame, frames);
		
		String fileName = renameFile(index.getFile().getName(), startFrame, frames, false);
		File file = new File(destination, fileName);
	
		OutputStream out = new FileOutputStream(file);
		
        long consumed = 0l;
        Iterator<IndexTableSegment> iterator = newSegments.iterator();

        while (iterator.hasNext()) {
        	
            IndexTableSegment segment = iterator.next();
            MXFStream.writeIndexTableSegment(out, segment);
            
            long segmentLength = (20 + MXFBuilder.lengthOfLocalSet(segment));
            consumed += segmentLength;
            long n = segmentLength / KAG_SIZE;
            long fillSize = (n+1)*KAG_SIZE - segmentLength;
            
//            if (fillSize > 0) {
//            	if (fillSize > 20) {
//            		MXFStream.writeFill(out, fillSize);
//                    consumed += (fillSize);
//            	} else {
//            		MXFStream.writeFill(out, fillSize + KAG_SIZE);
//                    consumed += (fillSize + KAG_SIZE);
//            	}	
//            }
            
            if (iterator.hasNext() && segmentLength % KAG_SIZE > 0) {
            	MXFStream.writeFill(out, fillSize);
                consumed += (fillSize);
            } else {
                MXFStream.writeFill(out, index.getFile().length() - consumed); // Make sure new index is same size as original
            }
        }
        
        out.close();
        return file;
	}
	
	
	private ArrayList<IndexTableSegment> createIndexSegments(int startFrame, int frames) throws IOException {

		ArrayList<IndexTableSegment> sourceIndexTableSegments = readIndexTableSegments();
		ArrayList<IndexEntry> sourceIndexEntries = new ArrayList<IndexEntry>();
		
        for (IndexTableSegment segment : sourceIndexTableSegments) {
            sourceIndexEntries.addAll(Arrays.asList(segment.getIndexEntryArray()));
        }
        
        ArrayList<IndexEntry> newIndexEntries = new ArrayList<IndexEntry>(sourceIndexEntries.subList(startFrame, startFrame + frames));
        long startFrameOffset = indexTable.streamOffset(startFrame, 0);

        // translate new index entries
        for (IndexEntry entry : newIndexEntries) {
            entry.setStreamOffset(entry.getStreamOffset() - startFrameOffset);
        }

        // make new segments
        ArrayList<IndexTableSegment> newIndexTableSegments = new ArrayList<IndexTableSegment>();
        
        int partialSegmentSize = newIndexEntries.size() % MAX_INDEX_SEGMENT_SIZE;
        int fullSegments = newIndexEntries.size() / MAX_INDEX_SEGMENT_SIZE;
        int totalSegments = (partialSegmentSize == 0) ? fullSegments : fullSegments + 1;

        IndexTableSegment sample = sourceIndexTableSegments.get(0);
        IndexEntry[] indexEntries = null;
        int segmentSize = MAX_INDEX_SEGMENT_SIZE;
        int entryCounter = 0;

        for (int i = 0; i < totalSegments; i++) {
            if (partialSegmentSize > 0 && i == totalSegments - 1) {
                segmentSize = partialSegmentSize;
            }
            indexEntries = newIndexEntries.subList(entryCounter, entryCounter + segmentSize).toArray(new IndexEntry[segmentSize]);
            newIndexTableSegments.add(buildSegment(indexEntries, entryCounter, segmentSize, sample));
            entryCounter += segmentSize;
        }
        
        return newIndexTableSegments;
    }
	
	
	public ArrayList<IndexTableSegment> readIndexTableSegments() throws IOException {

		File indexFile = index.getFile();
        InputStream in = new FileInputStream(indexFile);
        ArrayList<IndexTableSegment> indexSegments = new ArrayList<IndexTableSegment>();
        long consumed = 0l;

        while(consumed < indexFile.length()) {
 
            IndexTableSegment segment = MXFStream.readIndexTableSegment(in);
            consumed += (20 + MXFBuilder.lengthOfLocalSet(segment));
            indexSegments.add(segment);
            UL fillUL = MXFStream.readSingleKey(in);
            if (PFRFileMXF.keyMatch(fillUL.getUniversalLabel(), PFRFileMXF.fillKey, 256)) { // ignore 9th byte (so legacy fill key is matched)
            	consumed += (16 + MXFStream.readPastFill(in));
            }
        }
        in.close();
        return indexSegments;
    }

	
	private IndexTableSegment buildSegment(IndexEntry[] indexEntries, int startFrame, int frames, IndexTableSegment sample) {

        IndexTableSegment segment = new IndexTableSegmentImpl();
        
        segment.setIndexEditRate(sample.getIndexEditRate());
        segment.setIndexStartPosition(startFrame);
        segment.setIndexEntryArray(indexEntries);
        segment.setEditUnitByteCount(0);
        segment.setIndexSID(sample.getIndexSID());
        segment.setBodySID(sample.getBodySID());
        segment.setSliceCount(sample.getSliceCount());
        segment.setDeltaEntryArray(sample.getDeltaEntryArray());
        segment.setIndexDuration(frames);

        return segment;
    }

}

