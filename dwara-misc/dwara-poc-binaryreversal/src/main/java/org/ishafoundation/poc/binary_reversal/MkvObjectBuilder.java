package org.ishafoundation.poc.binary_reversal;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.ishafoundation.poc.binary_reversal.Timecode.Type;

import com.google.code.ebmlviewer.core.EbmlDecoder;
import com.google.code.ebmlviewer.core.VariableLengthInteger;
import com.google.code.ebmlviewer.elements.ElementDescriptor;
import com.google.code.ebmlviewer.elements.ElementDescriptors;
import com.google.code.ebmlviewer.elements.ElementType;
import com.google.code.ebmlviewer.io.EbmlFile;
import com.google.code.ebmlviewer.io.EbmlFileEntry;

public class MkvObjectBuilder {

	// TODO : Identify which track is which? Segment.Tracks.TrackEntry[1].Video
	static HashMap<Integer, String> trackId_Type = new HashMap<Integer, String>();
	
	static Mkv mkvObj = new Mkv();

	static HashMap<Integer, String> identifyTracks(EbmlFileEntry ebmlFileEntry){
		trackId_Type.put(1, "Video");
		trackId_Type.put(2, "Audio");
		trackId_Type.put(3, "Audio");
		trackId_Type.put(4, "Audio");
		trackId_Type.put(5, "Audio");
		return trackId_Type;
	}
	
	static String getBaseTimecode(EbmlFileEntry tags) throws Exception {

		String baseTimecode = null;
		List<EbmlFileEntry> tagList = tags.getEntries(); // [Tag, Tag ...]	
		for (EbmlFileEntry tag : tagList) {
			List<EbmlFileEntry> simpleTagList = null;
			try {
				simpleTagList = tag.getEntries(); // getting the first tags' simpletaglist [Tag.SimpleTag, Tag.SimpleTag ...]
			}
			catch (Exception e) {
				// TODO: handle exception
				continue;
			}

			for (EbmlFileEntry simpleTag : simpleTagList) {
				List<EbmlFileEntry> simpleTagChildren = simpleTag.getEntries();
				if(simpleTagChildren.size() == 0)
					continue;
				EbmlFileEntry child1 = simpleTagChildren.get(0);
				String name = getName(child1);
				if(name.equals("TagName") && getData(child1).equals("TIMECODE")) {
					EbmlFileEntry child2 = simpleTagChildren.get(1);
					String child2name = getName(child2);
					if(child2name.equals("TagString")) {
						baseTimecode = getData(child2);
						break;
					}
				}
			}

			if(baseTimecode != null)
				break;
		}
		System.out.println("baseTimecode : " + baseTimecode);
		mkvObj.setBaseTimecode(baseTimecode);
		return baseTimecode;
	}
	
	static void handleCluster(EbmlFileEntry ebmlFileEntry, InputStream is) throws Exception{
		System.out.println("**********  " + clusterCnt + "  **********");

//		if(clusterCnt > 10)
//			return;
		Frame frame = new Frame();
		List<EbmlFileEntry> clusterChildren = ebmlFileEntry.getEntries();
		int sbCnt = 0;
		for (EbmlFileEntry clusterChild : clusterChildren) {
			String name = getName(clusterChild);
			//System.out.println("name : " +  name);
			if(name.equals("Timecode")) {
				frame.setTimecode(Integer.parseInt(getData(clusterChild)));
			}
			else if(name.equals("SimpleBlock")) {
				sbCnt++;
				if(trackId_Type.get(sbCnt) == null)
					System.out.println("sbCnt not mapped" + sbCnt + ":" + ebmlFileEntry);
				else {
					Track track = null;
					String sb = handleSimpleBlock(clusterChild);
//					if(clusterCnt < 11)
//						System.out.println(sb);
					String first8 = sb.substring(0, 8);
					if(first8.matches("8[0-9]{5}80"))
						sb = sb.substring(8);

					if(trackId_Type.get(sbCnt).equals("Video")) {
						track = new VideoTrack();
						frame.setVideoTrack((VideoTrack) track);
					}
					else {
						AudioTrack at = new AudioTrack();
						at.setId(sbCnt - 1);
						track = at;
						frame.getAudioTracks().add((AudioTrack) track);
					}
					
					track.setData(sb);
//					if(clusterCnt < 11)
//						System.out.println(track.getData());
				}
			}			
		}
		mkvObj.getFrames().add(frame);
	}

	static void handleTimecode(EbmlFileEntry ebmlFileEntry) throws Exception{
		System.out.println("tc : " + getData(ebmlFileEntry));
	}

	static String getData(EbmlFileEntry entry) throws Exception{
		String valueText = null;
		ElementDescriptor descriptor = dMap.get(entry.getIdentifier());
		// data
		if ( descriptor != null && descriptor.getType() != ElementType.BINARY && descriptor.getType() != ElementType.MASTER ) {
			try {
				EbmlDecoder decoder = new EbmlDecoder();
				ByteBuffer data = ByteBuffer.allocate( Math.min( 8 * 1024, ( int ) entry.getSize().getPlainValue() ) );
				entry.read( data );
				data.flip();

				switch ( descriptor.getType() ) {
				case SIGNED_INTEGER:
					long signedInteger = decoder.decodeSignedInteger( data, ( int ) entry.getSize().getPlainValue() );
					valueText = String.format( "%,d (%<#x)", signedInteger );
					break;
				case UNSIGNED_INTEGER:
					long unsignedInteger = decoder.decodeUnsignedInteger( data, ( int ) entry.getSize().getPlainValue() );
					valueText = unsignedInteger+"";//String.format( "%,d (%<#x)", unsignedInteger );
					break;
				case FLOATING_POINT:
					double floatingPoint = decoder.decodeFloatingPoint( data, ( int ) entry.getSize().getPlainValue() );
					valueText = String.format( "%,.10g (%<s)", floatingPoint );
					break;
				case ASCII_STRING:
					String asciiString = decoder.decodeAsciiString( data, ( int ) entry.getSize().getPlainValue() );
					valueText = String.format( "%s", asciiString );
					break;
				case UNICODE_STRING:
					String unicodeString = decoder.decodeUnicodeString( data, ( int ) entry.getSize().getPlainValue() );
					valueText = String.format( "%s", unicodeString );
					break;
				case DATE:
					long date = decoder.decodeDate( data, ( int ) entry.getSize().getPlainValue() );
					valueText = String.format( "%tF %<tT.%<tL", date );
					break;
					//            case BINARY:
					//                break;
					//            case MASTER:
					//                break;
				default:
					valueText = null;
				}
			} catch ( Exception e ) {
				// IllegalEncodedLengthException
				// BufferUnderflowException
				// CharacterCodingException
				//e.printStackTrace();
				System.err.println("Error during gettingData in " + entry);
			}

		}
		return valueText;
	}
	
	static String handleSimpleBlock(EbmlFileEntry ebmlFileEntry) throws Exception {
		int len = (int) ebmlFileEntry.getSize().getPlainValue();
		ByteBuffer bb = ByteBuffer.allocate(len);
		ebmlFileEntry.read(bb);
		return Hex.encodeHexString(bb);
	}
	
	static void handleSimpleBlock_Opt1(EbmlFileEntry ebmlFileEntry, int sbCnt) throws Exception {
		int len = (int) ebmlFileEntry.getSize().getPlainValue();
		System.out.println("len : " + len);
		//byte[] bb = new byte[len];
		ByteBuffer bb = ByteBuffer.allocate(len);
		ebmlFileEntry.read(bb);

		if(trackId_Type.get(sbCnt) == null)
			System.out.println("sbCnt not mapped" + sbCnt + ":" + ebmlFileEntry);
		else {
			if(trackId_Type.get(sbCnt).equals("Video"))
				System.out.println("v.data " + Hex.encodeHexString(bb));
			else
				System.out.println("a.data " + Hex.encodeHexString(bb));
		}
	}

	static void handleSimpleBlock_Opt2(EbmlFileEntry ebmlFileEntry, InputStream is, int sbCnt) throws Exception {
		CountingInputStream cis = (CountingInputStream) is;
		System.out.println("dp : " +  ebmlFileEntry.getDataPosition());
		System.out.println("bc : " +  cis.getByteCount());
		long skipCnt = ebmlFileEntry.getDataPosition() - cis.getByteCount();
		System.out.println("sc : " + skipCnt);
		cis.skip(skipCnt);
		int len = (int) ebmlFileEntry.getSize().getPlainValue();
		System.out.println("len : " + len);
		byte[] sb = new byte[len];
		cis.read(sb, 0, len);
		if(trackId_Type.get(sbCnt) == null)
			System.out.println("sbCnt not mapped" + sbCnt + ":" + ebmlFileEntry);
		else {
			if(trackId_Type.get(sbCnt).equals("Video"))
				System.out.println("v.data " + Hex.encodeHexString(sb));
			else
				System.out.println("a.data " + Hex.encodeHexString(sb));
		}
	}

	static String getName(EbmlFileEntry ebmlFileEntry) {
		return dMap.get(ebmlFileEntry.getIdentifier()) != null ? dMap.get(ebmlFileEntry.getIdentifier()).getName() : "N/A";
	}

	static int clusterCnt = 0;
	static void call(List<EbmlFileEntry> efel, InputStream is, String tab) throws Exception {

		for (EbmlFileEntry ebmlFileEntry : efel) {
			String name = getName(ebmlFileEntry);
			if(name.equals("EBML") || name.equals("SeekHead") || name.equals("Info") || name.equals("Cues")) 
				continue;
			else if(name.equals("Tags"))
				getBaseTimecode(ebmlFileEntry);
			else if(name.equals("Tracks"))
				identifyTracks(ebmlFileEntry);
			else if(name.equals("Cluster")) {
				clusterCnt++;
				handleCluster(ebmlFileEntry, is);
			}
			else {
				System.out.println(tab + "--" + name + "-" + 
						"ep :" + ebmlFileEntry.getEntryPosition() + ":" +
						"id :" + ebmlFileEntry.getIdentifier() + ":" +
						"sz :" + ebmlFileEntry.getSize().getPlainValue() + ":" +
						"dp :" + ebmlFileEntry.getDataPosition());

				List<EbmlFileEntry> efelChild = null;
				try {
					efelChild = ebmlFileEntry.getEntries();
					if(efelChild.size() > 0)
						call(efelChild, is, tab + "\\t");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
					System.err.println("Error in " + ebmlFileEntry);
					//System.out.println("Skipping this " + efelChild.toString() + ":" + e1.getMessage());
				}
			}
		}
	}

	static Map<VariableLengthInteger, ElementDescriptor> dMap = ElementDescriptors.getDefaultDescriptors();

	public static void main(String[] args) throws Exception {
		int bufferSize = 524288;
		//String sourceFilePathname = "C:\\Users\\prakash\\sample-ntsc_tmp.mkv";
		String sourceFilePathname = "C:\\Users\\prakash\\P22267_sample-v210-mkvmergedForAlignment.mkv";
		EbmlFile ef = new EbmlFile(sourceFilePathname);
		InputStream is = new CountingInputStream(new BufferedInputStream(new FileInputStream(sourceFilePathname), bufferSize));

		List<EbmlFileEntry> efel = ef.getEntries();

		call(efel,is,"\\t");
		
		buildMxf();
	}
	
	private static void buildMxf() throws Exception {
		String templateFilePathname = "C:\\Users\\prakash\\MxfFrameTemplate.tmpl";
		String templateContent = FileUtils.readFileToString(new File(templateFilePathname));
		//Hex.decodeHex(templateContent);

		String headerFilePathname = "C:\\Users\\prakash\\P22267_sample_mxf.hdr";
		byte[] headerByteArray = FileUtils.readFileToByteArray(new File(headerFilePathname));
		String headerContent = new String(Hex.encodeHex(headerByteArray));
		
		String footerFilePathname = "C:\\Users\\prakash\\P22267_sample.ftr";
		byte[] footerByteArray = FileUtils.readFileToByteArray(new File(footerFilePathname));
		String footerContent = new String(Hex.encodeHex(footerByteArray));
		
		StringBuilder sb =  new StringBuilder();
		sb.append(headerContent);
		
		//System.out.println(mkvObj.getBaseTimecode());
		Timecode baseTimecode = new Timecode(mkvObj.getBaseTimecode(), Type.TYPE_VIDEO_PAL);
		List<Frame> frames = mkvObj.getFrames();
		int cnt = 0;
		for (Frame frame : frames) {
			cnt++;
			String frameContent = templateContent;
			System.out.println(cnt + ":::" + frame.getTimecode());
			if(cnt > 1)
				baseTimecode.addFrame();
			System.out.println(baseTimecode);
			System.out.println(baseTimecode.getCode());
			frameContent = frameContent.replace("<<REVERSED_TIMECODE>>", new String(Hex.encodeHex(Hex.decodeHex(getReversedTimeCode(baseTimecode.getCode())+"00000000"))));
			frameContent = frameContent.replace("<<V_DATA>>", frame.getVideoTrack().getData());
			List<AudioTrack> audioTracks = frame.getAudioTracks();
			for (AudioTrack nthAudioTrack : audioTracks) {
				frameContent = frameContent.replace("<<A" + nthAudioTrack.getId() + "_DATA>>", nthAudioTrack.getData());
			}
			sb.append(frameContent);
		}
		sb.append(footerContent);
		
		byte[] origMxfByteArray = FileUtils.readFileToByteArray(new File("C:\\Users\\prakash\\P22267_sample.mxf"));
		System.out.println("orig : " + getChecksum(origMxfByteArray));
		
		
		byte[] generatedMxfByteArray = Hex.decodeHex(sb.toString().toUpperCase());
		FileUtils.writeByteArrayToFile(new File("C:\\Users\\prakash\\P22267_sample_reverse_generated.mxf"), generatedMxfByteArray);
		System.out.println("gen : " + getChecksum(generatedMxfByteArray));

//		FileUtils.writeStringToFile(new File("C:\\Users\\prakash\\P22267_sample-string.mxf"), new String(Hex.encodeHex(origMxfByteArray)).toUpperCase());
//		FileUtils.writeStringToFile(new File("C:\\Users\\prakash\\P22267_sample_reverse_generated-string.mxf"), sb.toString().toUpperCase());

		System.out.println("Done");
		
	}
	
	public static String getChecksum(byte[] byteArray){
		DigestInputStream digestInputStream=null ;
		StringBuffer sb = new StringBuffer();
		try {
			MessageDigest messageDigest=MessageDigest.getInstance("MD5") ;
			digestInputStream=new DigestInputStream(new ByteArrayInputStream(byteArray), messageDigest);
		    while(digestInputStream.read()>=0);
		    
		    for(byte b: messageDigest.digest())
		    	sb.append(String.format("%02x",b));
		    
		} catch(IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			if(digestInputStream!=null) try {digestInputStream.close();} catch (IOException e) {}
		}
		return sb.toString();
	}
	
	private static String getReversedTimeCode(String timecode){	
		
	    String timeCode1stN2ndDigit = timecode.substring(0, 2);
	    String timeCode3rdN4thDigit = timecode.substring(3, 5);
	    String timeCode5thN6thDigit = timecode.substring(6, 8);
	    String timeCode7thN8thDigit = timecode.substring(9, 11);
	    
	    String reversedTimeCode = timeCode7thN8thDigit + timeCode5thN6thDigit + timeCode3rdN4thDigit + timeCode1stN2ndDigit;
	    return reversedTimeCode;
	}
}
