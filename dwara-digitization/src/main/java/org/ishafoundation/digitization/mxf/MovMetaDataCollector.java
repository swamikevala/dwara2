package org.ishafoundation.digitization.mxf;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.ishafoundation.dwaraapi.PfrConstants;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("video-digi-2020-mov-meta-extract")//mxf-meta-collection")
@Primary
@Profile({ "!dev & !stage" })
public class MovMetaDataCollector implements IProcessingTask {
    
    private static final Logger logger = LoggerFactory.getLogger(MovMetaDataCollector.class);

	@Autowired
	protected CommandLineExecuter commandLineExecuter;	
	
	private String headerHexPattern = "06 0E 2B 34 02 53 01 01 0D 01 03 01 14 02 01 00".replaceAll("\\s", "");
	private String footerHexPattern = "06 0E 2B 34 02 05 01 01 0D 01 02 01 01 04 04 00".replaceAll("\\s", "");
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		LogicalFile logicalFile = processContext.getLogicalFile();
		String destinationDirPath = processContext.getOutputDestinationDirPath();

		
		String sourceFilePathname = logicalFile.getAbsolutePath();
		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		
		FileUtils.forceMkdir(new File(destinationDirPath));
		
		String hdrTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.MOV_META_EXTN;
//		String ftrTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.FTR_EXTN;
//		String qcrTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.QC_REPORT_EXTN;
		
		extractAndSaveHeader(sourceFilePathname, hdrTargetLocation);
//		extractAndSaveFooter(sourceFilePathname, ftrTargetLocation);
		
		
//		// copies the QCReport file;
//		File qcReportFile = logicalFile.getSidecarFile("qcr");
//		FileUtils.copyFile(qcReportFile, new File(qcrTargetLocation));
		
		// TODO : better this...
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setDestinationPathname(hdrTargetLocation);
		processingtaskResponse.setIsComplete(true);
		processingtaskResponse.setIsCancelled(false);
		processingtaskResponse.setStdOutResponse("");
		processingtaskResponse.setFailureReason("");

		return processingtaskResponse;
	}
	
    private void extractAndSaveHeader(String sourceFilePathname, String hdrTargetLocation) throws Exception {
    	int bufferSize = 524288;
    	byte[] headerIdentifierPattern = Hex.decodeHex(headerHexPattern);
    	
    	InputStream is = new BufferedInputStream(new FileInputStream(sourceFilePathname), bufferSize);
    	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    	
        final byte[] buffer = new byte[bufferSize];
        int n = 0;
        long count = 1;
        while (-1 != (n = is.read(buffer))) {
        	logger.trace(""+count);
        	byteArrayOutputStream.write(buffer, 0, n);
        	byte[] data = byteArrayOutputStream.toByteArray();
        	int headerLength = HexPatternFinderUtil.indexOf(data, headerIdentifierPattern);
        	
        	if(headerLength != -1) {
        		logger.trace("found it after " + count + " loops");
            	byte[] headerData = new byte[headerLength];
            	logger.trace("headerLength " + headerLength);
            	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            	InputStream bis = new BufferedInputStream(byteArrayInputStream, bufferSize);
            	IOUtils.read(bis, headerData, 0, headerLength);
            	IOUtils.write(headerData, new FileOutputStream(hdrTargetLocation));
        		break;
        	}
        	if(count == 5) {
        		logger.trace("Couldnt find header even after " + count + " loops. Is it even possible?");
        		break;
        	}
        	count += 1;
        }
	}
    
    private void extractAndSaveFooter(String sourceFilePathname, String ftrTargetLocation) throws Exception {
    	long srcFileSize = new File(sourceFilePathname).length();
    	int bufferSize = 524288;
    	byte[] footerIdentifierPattern = Hex.decodeHex(footerHexPattern);

        for (int count = 1; count <= 5; count++) {
        	logger.trace(""+count);
        	long chunkByteSize = bufferSize * count;
        	
        	byte[] data = extractFooterChunk(sourceFilePathname, srcFileSize - chunkByteSize);
        	int footerIndexPos = HexPatternFinderUtil.indexOf(data, footerIdentifierPattern);
        	logger.trace("footerIndexPos " + footerIndexPos);
        	if(footerIndexPos != -1) {
        		logger.trace("found it after " + count + " loops");
            	
            	int footerLength = (int) (chunkByteSize - footerIndexPos);
            	logger.trace("footerLength " + footerLength);
            	
            	byte[] footerData = new byte[footerLength];
            	
            	// TODO : Just do this with array itself
            	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            	//InputStream bis = new BufferedInputStream(byteArrayInputStream, bufferSize);
            	byteArrayInputStream.skip(footerIndexPos);
            	IOUtils.read(byteArrayInputStream, footerData, 0, footerLength);
            	
            	IOUtils.write(footerData, new FileOutputStream(ftrTargetLocation));
        		break;
        	}
        	else {
        		logger.trace("couldnt found the footer. retrying again...");
        	}
        }
	}
    
	private byte[] extractFooterChunk(String filePathName, long bytesToBeSkipped) throws Exception{
		byte[] data = null;
		String extractedChunkTmpFile = filePathName.replace(PfrConstants.MXF_EXTN, PfrConstants.FTR_EXTN + ".tmp");
		String ddCommand = "dd if=" + filePathName + " skip=" + bytesToBeSkipped + " iflag=skip_bytes,count_bytes of=" + extractedChunkTmpFile;
		
		logger.trace("ddCommand " + ddCommand);
//		List<String> ddCommandList = new ArrayList<String>();
//		ddCommandList.add("sh");
//		ddCommandList.add("-c");
//		ddCommandList.add("cd " + tmpLocationPath + " ; " + ddCommand);
		
		CommandLineExecutionResponse ddExtractCommandLineExecutionResponse = commandLineExecuter.executeCommand(ddCommand);
		if(ddExtractCommandLineExecutionResponse.isComplete()) {
			logger.trace("ddCommand response - "+ ddExtractCommandLineExecutionResponse);
			data = FileUtils.readFileToByteArray(new File(extractedChunkTmpFile));
        	// delete tmp file
        	new File(extractedChunkTmpFile).delete();
		}
		
		return data;
	}
}
