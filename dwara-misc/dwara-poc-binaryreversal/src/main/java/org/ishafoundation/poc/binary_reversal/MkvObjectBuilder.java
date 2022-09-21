package org.ishafoundation.poc.binary_reversal;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
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
	
	//static Mkv mkvObj = new Mkv();
	
	private static String TEMPLATE_CONTENT = null;
	private static Timecode BASE_TIMECODE = null;
	private static Timecode RUNNING_TIMECODE = null;
	private static Map<Integer, String> FRAME_TIMECODE_MAP = null;
	private static String BINARY_REVERSED_MXF_FILEPATHNAME = null;
	private static int FRAME_COUNTER = 0;
	private static String AUDIOTRACK_TMPL_PAL = "060E2B34010201010D0103011604010<<AUDIO_TRACK_INDEX>>83001680<<AUDIO_DATA>>";
	private static String AUDIOTRACK_TMPL_NTSC = "060E2B34010201010D0103011604010<<AUDIO_TRACK_INDEX>>830012<<AUDIO_SIZE>><<AUDIO_DATA>>060E2B34010101020301021001000000830000<<AUDIO_SIZE_SUFFIXER>>"; // based on size either 00 or 030000 gets replaced
	
	private static Type VIDEO_TYPE = Type.TYPE_VIDEO_PAL;
	
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
		BASE_TIMECODE = new Timecode(baseTimecode, VIDEO_TYPE);
		//mkvObj.setBaseTimecode(baseTimecode);
		return baseTimecode;
	}
	
	static void handleCluster(EbmlFileEntry ebmlFileEntry) throws Exception{
		System.out.println("**********  " + CLUSTER_CNT + "  **********");

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
						VariableLengthInteger size = clusterChild.getSize();
						AudioTrack at = new AudioTrack();
						at.setId(sbCnt - 1);
						at.setSize(size);
						track = at;
						frame.getAudioTracks().add((AudioTrack) track);
					}
					
					track.setData(sb);
//					if(clusterCnt < 11)
//						System.out.println(track.getData());
				}
			}			
		}
		
		String frameContent = TEMPLATE_CONTENT;
		
		String currentFrameTimecodeString = FRAME_TIMECODE_MAP.get(FRAME_COUNTER);
 
		if(currentFrameTimecodeString == null) { // if its not an impacted frame per logs then add frame to the previous frame's timecode
			if(RUNNING_TIMECODE == null)
				RUNNING_TIMECODE = BASE_TIMECODE;
			else
				RUNNING_TIMECODE.addFrame();
			
			currentFrameTimecodeString = RUNNING_TIMECODE.getCode();
		}
		else {
			try{
				RUNNING_TIMECODE = new Timecode(currentFrameTimecodeString, VIDEO_TYPE);
			}catch (Exception e) {
				// swallow it - if error that means it contains the frame part problematic
			}
		}
		System.out.println(FRAME_COUNTER + " - TC - " + currentFrameTimecodeString);
		
		frameContent = frameContent.replace("<<REVERSED_TIMECODE>>", new String(Hex.encodeHex(Hex.decodeHex(getReversedTimeCode(currentFrameTimecodeString)+"00000000"))));
		frameContent = frameContent.replace("<<V_DATA>>", frame.getVideoTrack().getData());
		List<AudioTrack> audioTracks = frame.getAudioTracks();
		for (AudioTrack nthAudioTrack : audioTracks) {
			
			String audioTrack = null; 
			if(VIDEO_TYPE == Type.TYPE_VIDEO_NTSC) {
				audioTrack = AUDIOTRACK_TMPL_NTSC;
				if(nthAudioTrack.getSize().getPlainValue() == 4807)
					audioTrack = audioTrack.replace("<<AUDIO_SIZE>>", "C3").replace("<<AUDIO_SIZE_SUFFIXER>>", "03000000"); 
				else
					audioTrack = audioTrack.replace("<<AUDIO_SIZE>>", "C6").replace("<<AUDIO_SIZE_SUFFIXER>>", "00"); 
			}
			else if(VIDEO_TYPE == Type.TYPE_VIDEO_PAL)
				audioTrack = AUDIOTRACK_TMPL_PAL;
			frameContent = frameContent + audioTrack.replace("<<AUDIO_TRACK_INDEX>>", (nthAudioTrack.getId() - 1) + "").replace("<<AUDIO_DATA>>", nthAudioTrack.getData());
		}
		flushData(frameContent);

		FRAME_COUNTER++;
		//mkvObj.getFrames().add(frame);
	}
	
//	static void handleTimecode(EbmlFileEntry ebmlFileEntry) throws Exception{
//		System.out.println("tc : " + getData(ebmlFileEntry));
//	}

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
				System.out.println("[ERROR] during gettingData in " + entry);
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
	
	static String getName(EbmlFileEntry ebmlFileEntry) {
		return dMap.get(ebmlFileEntry.getIdentifier()) != null ? dMap.get(ebmlFileEntry.getIdentifier()).getName() : "N/A";
	}

	static int CLUSTER_CNT = 0;
	static void call(List<EbmlFileEntry> efel, String tab, boolean handleCluster) throws Exception {

		for (EbmlFileEntry ebmlFileEntry : efel) {
			String name = getName(ebmlFileEntry);
			if(name.equals("EBML") || name.equals("SeekHead") || name.equals("Info") || name.equals("Cues")) 
				continue;
			else if(name.equals("Tags"))
				getBaseTimecode(ebmlFileEntry);
			else if(name.equals("Tracks"))
				identifyTracks(ebmlFileEntry);
			else if(handleCluster && name.equals("Cluster")) {
				CLUSTER_CNT++;
				handleCluster(ebmlFileEntry);
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
						call(efelChild, tab + "\\t", handleCluster);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
					System.out.println("[ERROR] in " + ebmlFileEntry);
					//System.out.println("Skipping this " + efelChild.toString() + ":" + e1.getMessage());
				}
			}
		}
	}

	static Map<VariableLengthInteger, ElementDescriptor> dMap = ElementDescriptors.getDefaultDescriptors();


	
	static void usage() {
		// java /data/staged/ABC.v210_mkv /home/pgurumurthy/MxfFrameTemplate.tmpl /data/staged/mxf/ABC.hdr /data/staged/mxf/ABC.log /data/staged/mxf/ABC.ftr  /data/staged/ABC_gen.v210_mkv /data/transcoded-testing/ABC.MXF_md5  
		System.err.println("usage: java MkvObjectBuilder <v210-mkv-filepathname> <video-type> <mxf-frame-template-filepathname> <extracted-header-from-mxf> <log-from-prasad-filepathname> <extracted-footer-from-mxf> <binary-reversed-mxf-filepathname> <orig-mxf-md5-filepathname>");
		System.exit(-1);
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 8)
			usage();
		
		String v210MkvFilepathname = args[0];
		VIDEO_TYPE = args[1].equals("PAL") ? Type.TYPE_VIDEO_PAL : Type.TYPE_VIDEO_NTSC;
		String mxfFrameTemplateFilepathname = args[2];
		String extractedMxfHeader = args[3];
		String mxfLog = args[4];
		String extractedMxfFooter = args[5];
		BINARY_REVERSED_MXF_FILEPATHNAME = args[6];
		String originalMxfMd5Filepathname = args[7];
		
		
		int bufferSize = 524288;
		//String sourceFilePathname = "C:\\Users\\prakash\\sample-ntsc_tmp.mkv";
//		String sourceFilePathname = "C:\\Users\\prakash\\P22267_sample-v210-mkvmergedForAlignment.mkv";

		
		
		TEMPLATE_CONTENT = FileUtils.readFileToString(new File(mxfFrameTemplateFilepathname));
		//Hex.decodeHex(templateContent);

		
		byte[] headerByteArray = FileUtils.readFileToByteArray(new File(extractedMxfHeader));
		String headerContent = new String(Hex.encodeHex(headerByteArray));
		
		
		byte[] footerByteArray = FileUtils.readFileToByteArray(new File(extractedMxfFooter));
		String footerContent = new String(Hex.encodeHex(footerByteArray));
		
		flushData(headerContent, false);

		LogParser lp = new LogParser();
		FRAME_TIMECODE_MAP = lp.parseLog(mxfLog, VIDEO_TYPE);
		
		
		EbmlFile ef = new EbmlFile(v210MkvFilepathname);
		//InputStream is = new CountingInputStream(new BufferedInputStream(new FileInputStream(v210MkvFilepathname), bufferSize));
		List<EbmlFileEntry> efel = ef.getEntries();
		call(efel, "\\t", false);
		
		System.out.println("**************************Calling efel second time**********************");

		CLUSTER_CNT = 0;
		ef = new EbmlFile(v210MkvFilepathname);
		//is= new CountingInputStream(new BufferedInputStream(new FileInputStream(v210MkvFilepathname), bufferSize));
		efel = ef.getEntries();

		call(efel, "\\t", true);
		//System.out.println(mkvObj.getBaseTimecode());
		//Timecode baseTimecode = new Timecode(mkvObj.getBaseTimecode(), Type.TYPE_VIDEO_PAL);

		//List<Frame> frames = mkvObj.getFrames();
		
		flushData(footerContent);
		
//		byte[] origMxfDigest =  ChecksumUtil.getChecksum(new File(originalMxfMd5Filepathname), Checksumtype.md5);
//		System.out.println("orig : " + DatatypeConverter.printHexBinary(origMxfDigest).toUpperCase());
		System.out.println("orig : " + FileUtils.readFileToString(new File(originalMxfMd5Filepathname)));
		
		byte[] revBinariedMxfDigest =  ChecksumUtil.getChecksum(new File(BINARY_REVERSED_MXF_FILEPATHNAME), Checksumtype.md5);
		System.out.println("gen : " + DatatypeConverter.printHexBinary(revBinariedMxfDigest).toUpperCase());

//		FileUtils.writeStringToFile(new File("C:\\Users\\prakash\\P22267_sample-string.mxf"), new String(Hex.encodeHex(origMxfByteArray)).toUpperCase());
//		FileUtils.writeStringToFile(new File("C:\\Users\\prakash\\P22267_sample_reverse_generated-string.mxf"), sb.toString().toUpperCase());

		System.out.println("Done");
		
	}

	private static void flushData(String content, boolean append) throws Exception {
		byte[] generatedMxfByteArray = Hex.decodeHex(content.toUpperCase());
		FileUtils.writeByteArrayToFile(new File(BINARY_REVERSED_MXF_FILEPATHNAME), generatedMxfByteArray, append);
	}
	
	private static void flushData(String content) throws Exception {
		flushData(content, true);
	}
	
//	public static String getChecksum(byte[] byteArray){
//		DigestInputStream digestInputStream=null ;
//		StringBuffer sb = new StringBuffer();
//		try {
//			MessageDigest messageDigest=MessageDigest.getInstance("MD5") ;
//			digestInputStream=new DigestInputStream(new ByteArrayInputStream(byteArray), messageDigest);
//		    while(digestInputStream.read()>=0);
//		    
//		    for(byte b: messageDigest.digest())
//		    	sb.append(String.format("%02x",b));
//		    
//		} catch(IOException | NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} finally {
//			if(digestInputStream!=null) try {digestInputStream.close();} catch (IOException e) {}
//		}
//		return sb.toString();
//	}
	
	private static String getReversedTimeCode(String timecode){	
		
	    String timeCode1stN2ndDigit = timecode.substring(0, 2);
	    String timeCode3rdN4thDigit = timecode.substring(3, 5);
	    String timeCode5thN6thDigit = timecode.substring(6, 8);
	    String timeCode7thN8thDigit = timecode.substring(9, 11);
	    
	    String reversedTimeCode = timeCode7thN8thDigit + timeCode5thN6thDigit + timeCode3rdN4thDigit + timeCode1stN2ndDigit;
	    return reversedTimeCode;
	}
}
