package org.ishafoundation.dwaraapi.tape.drive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.db.dao.master.TapedriveDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.tape.drive.status.DriveStatusDetails;
import org.ishafoundation.dwaraapi.tape.drive.status.MtStatus;
import org.ishafoundation.dwaraapi.tape.drive.status.MtStatusResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Component
@Primary
//@Profile("default") works
@Profile({ "!dev & !test" })
public class TapeDriveManagerImpl extends AbstractTapeDriveManagerImpl{
	
	private static final Logger logger = LoggerFactory.getLogger(TapeDriveManagerImpl.class);

	@Autowired
	private TapedriveDao tapedriveDao;
	
	@Autowired
	private CommandLineExecuter commandLineExecuter;
	
		
	// To write Nth medialibrary the tape head should be pointing at file Number N
	// For e.g., if 5 medialibrary already in volume and to write the 6th mediaLibrary on tape, we need to position tapeHead on FileNumber = 5 - Remember Tape fileNumbers starts with 0
	// Reference - http://etutorials.org/Linux+systems/how+linux+works/Chapter+13+Backups/13.6+Tape+Drive+Devices/
	@Override
	public DriveStatusDetails setTapeHeadPositionForWriting(int tapelibraryId, int dataTransferElementNo) {
		DriveStatusDetails dsd = null;
		
		try {
			String dataTransferElementName = getDriveName(tapelibraryId, dataTransferElementNo);
			
			MtStatus mtStatus = getMtStatus(tapelibraryId, dataTransferElementName);
			int currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
			int currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
			logger.trace("b4 setTapeHeadPosition - dataTransferElementName " + dataTransferElementName + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);
		
		
			eod(dataTransferElementName);

			mtStatus = getMtStatus(tapelibraryId, dataTransferElementName);
			currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
			currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
			logger.trace("after eod - dataTransferElementName " + dataTransferElementName + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);

			if(currentFileNumberTapeHeadPointingTo > 0) {
				bsf(dataTransferElementName, 1);
				fsf(dataTransferElementName, 1);
			}
			else {
				rewind(dataTransferElementName);
			}
			
			mtStatus = getMtStatus(tapelibraryId, dataTransferElementName);
			currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
			currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
			logger.trace("after setTapeHeadPosition - dataTransferElementName " + dataTransferElementName + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);
	
			
			dsd = new DriveStatusDetails();
			dsd.setDriveSNo(dataTransferElementNo);
			dsd.setMtStatus(mtStatus);
		}catch (Exception e) {
			logger.error("Unable to setTapeHeadPositionForWriting " + e.getMessage()); e.printStackTrace();
		}
		return dsd;
	}

	// if blockNo is not requested to be seeked...
	@Override
	public DriveStatusDetails setTapeHeadPositionForReading(int tapelibraryId, int dataTransferElementNo, int blockNumberToSeek) {
		String dataTransferElementName = null;
		MtStatus mtStatus = null;
		DriveStatusDetails dsd = null;
		try {
			dataTransferElementName = getDriveName(tapelibraryId, dataTransferElementNo);
			dsd = new DriveStatusDetails();
			dsd.setDriveSNo(dataTransferElementNo);
			
			seek(dataTransferElementName, blockNumberToSeek);
			
			// after seeking mt status responds with fileNo = -1 and blockNo = -1, so we had to do this...
			CommandLineExecutionResponse  cler = tell(dataTransferElementName);
			Pattern tellRespRegExPattern = Pattern.compile("At block ([0-9]*).");
			Matcher tellRespRegExMatcher = tellRespRegExPattern.matcher(cler.getStdOutResponse());
			int blockNumber = -9;
			if(tellRespRegExMatcher.find()) {
				blockNumber = Integer.parseInt(tellRespRegExMatcher.group(1));
			}
			mtStatus = getMtStatus(tapelibraryId, dataTransferElementName);
			// TODO - Is fileNumber needed?? mtStatus.setFileNumber(fileNumber);
			mtStatus.setBlockNumber(blockNumber);
			dsd.setMtStatus(mtStatus);
			return dsd;		
		}
		catch (Exception e) {
			logger.error("Unable to setTapeHeadPositionForReading " + e.getMessage()); e.printStackTrace();
			
		}

//		mtStatus = getMtStatus(dataTransferElementName);
//		
//		int currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
//		int currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
//		System.out.println("after setTapeHeadPosition - dataTransferElementName " + dataTransferElementName +  ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);
//		
//		dsd.setMtStatus(mtStatus);
		
		return dsd;
	}
	
	private String getDriveName(int tapelibraryId, int driveSNo) {
		Tapedrive tapedrive = tapedriveDao.findByTapelibraryIdAndElementAddress(tapelibraryId, driveSNo); // TODO Cache this...
		return tapedrive.getDeviceWwid();
	}

	// drivename has to be unique even on different libraries... so need to pass tapelibraryid???
	@Override
	protected MtStatus getMtStatus(int tapelibraryId, String driveName){
		String mtStatusResponse = callMtStatus(driveName);
		MtStatus mtStatus = MtStatusResponseParser.parseMtStatusResponse(mtStatusResponse);
		return mtStatus;
	}

	private String callMtStatus(String driveName) {
		String mtStatusResponse = null;
		String mtStatusResponseFileName = driveName.replace("/", "_") + "_status.err";
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("mt -f " + driveName + " status", mtStatusResponseFileName);
		if(cler.isComplete())
			mtStatusResponse = cler.getStdOutResponse();
		return mtStatusResponse;
	}
	
	private CommandLineExecutionResponse eod(String dataTransferElementName) throws Exception{
		return commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " eod");
	}

	private CommandLineExecutionResponse fsf(String dataTransferElementName, int noOfBlocks) throws Exception{
		return commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " fsf " + noOfBlocks);
	}

	private CommandLineExecutionResponse bsf(String dataTransferElementName, int noOfBlocks) throws Exception{
		return commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " bsf " + noOfBlocks);
	}

	private CommandLineExecutionResponse rewind(String dataTransferElementName) throws Exception {
		return commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " rewind");
	}
	
	private CommandLineExecutionResponse seek(String dataTransferElementName, int blockNo) throws Exception{
		return commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " seek " + blockNo);
	}
	
	private CommandLineExecutionResponse tell(String dataTransferElementName) throws Exception{
		 return commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " tell");
	}
}
