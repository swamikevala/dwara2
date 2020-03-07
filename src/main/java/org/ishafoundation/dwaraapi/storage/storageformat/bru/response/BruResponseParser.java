package org.ishafoundation.dwaraapi.storage.storageformat.bru.response;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.storage.storageformat.bru.response.components.ErrorDescription;
import org.ishafoundation.dwaraapi.storage.storageformat.bru.response.components.FilesProcessed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * Class responsible for parsing the below command's console output

bru -clOjvvvvvvvvv -L 7777 -QX -b 256K -f /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst 23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/


archive ID = 5e130d086274
label = 7777
buffer size = 256k bytes
media size = <unknown>
VL:c|0|1|109|-1|23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K
VL:c|0|1|841|-1|23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/MEDIAPRO.XML
VL:c|0|1|6|-1|23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/Sub
VL:c|0|1|6|-1|23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/Take
VL:c|0|1|42|-1|23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/.dwara-ignored
VL:c|0|1|4096|-1|23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/.dwara-ignored/._.DS_Store
VL:c|0|1|6148|-1|23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/.dwara-ignored/.DS_Store
VL:c|0|1|6|-1|23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/Thmbnl
VL:c|0|1|6|-1|23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/Edit
VL:c|0|1|90|-1|23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/Clip
VL:c|0|1|2522|-1|23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/Clip/ACTECHCAM10025M01.XML
VL:c|0|1|13213696|-1|23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/Clip/ACTECHCAM10025R01.BIM
P:1
VL:c|14592|1|2682179120|56|23100_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/Clip/ACTECHCAM10025.MXF (10231)
P:1
P:3
P:8
P:13
P:17
P:22
P:26
P:31
P:35
P:39
P:42
P:46
P:50
P:54
P:58
P:63
P:67
P:72
P:76
P:80
P:85
P:89
P:93
P:97
bru: [I181] wrote 1504256 blocks (3008512 KBytes) on volume [1], 0:00:12, 250709 KB/sec

 **** bru: execution summary ****

Started:                Mon Jan  6 16:03:44 2020
Completed:              Mon Jan  6 16:03:56 2020
Archive id:             5e130d086274
Messages:               0 warnings,  0 errors
Archive I/O:            1504256 blocks (3008512KB) written
Archive I/O:            0 blocks (0KB) read
Files written:          17 files (6 regular, 11 other)
Files read:             0 files (0 regular, 0 other)
Files skipped:          0 files
Volumes used:           1
Write errors:           0 soft,  0 hard
Read errors:            0 soft,  0 hard
Checksum errors:        0

*************************************** ERROR Scenario ****************************************

At the time of writing another thread/process unloaded the tape and so we got an error like below
*** Bru asking for user interaction - avoid this - how can we avoid this.. use -B option in all our commands

VL:c|75008|1|1105188303|20154|99999_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/Clip/LMA122789_01.MP4
P:1
P:9
P:18
P:28
bru: [I109] attention - assuming end of volume 1 (unknown size)
bru: [I181] wrote 210816 blocks (421632 KBytes) on volume [1], 0:00:14, 30116 KB/sec
bru: [W003] "/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst": warning - close error on archive: errno = 5, Input/output error
bru: [A121] load volume 2 - press ENTER to continue on device '/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst'
bru: [Q120] (C)ontinue  (L)abel  (N)ew device  (Q)uit [default: C] >> Q
bru: [E201] user entered Q => QUIT in routine [tty_newmedia]

 **** bru: execution summary ****

Started:                Sun Jan 12 21:39:02 2020
Completed:              Sun Jan 12 21:39:55 2020
Archive id:             5e1b449e2664
Messages:               1 warnings,  1 errors
Archive I/O:            211328 blocks (422656KB) written
Archive I/O:            0 blocks (0KB) read
Files written:          13 files (6 regular, 7 other)
Files read:             0 files (0 regular, 0 other)
Files skipped:          0 files
Volumes used:           1
Write errors:           4 soft,  0 hard
Read errors:            0 soft,  0 hard
Checksum errors:        0

*/


public class BruResponseParser {
	static final SimpleDateFormat formatWithSingleDigitDate = new SimpleDateFormat("EEE MMM  d HH:mm:ss yyyy"); // Mon Jan  6 16:03:56 2020
	static final SimpleDateFormat formatWithDoubleDigitDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy"); // Mon Jan 13 15:01:43 2020
	
	static Logger logger = LoggerFactory.getLogger(BruResponseParser.class);
	
	public BruResponse parseBruResponse(String bruResponse){
		BruResponse br = new BruResponse();
		Scanner scanner = new Scanner(bruResponse);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();

			String fileAndAttributesRegEx = "VL:(.[^\\|]*)\\|(.[^\\|]*)\\|(.[^\\|]*)\\|(.[^\\|]*)\\|(.[^\\|]*)\\|(.[^\\|]*)"; // VL:c|75008|1|1105188303|20154|99999_Daily-Mystic-Quote_Le-Meridien-Chennai_21-Dec-2019_FS7_4K/Clip/LMA122789_01.MP4
			String errorDescriptionRegEx = "bru: \\[([A-Z][0-9]{3})\\](.*)";
			String startedRegEx = "Started: *(.*)";
			String completedRegEx = "Completed: *(.*)";
			String messagesRegEx = "Messages: *([0-9]*) warnings,  ([0-9]*) errors";
			String blocksWrittenRegEx = "Archive I/O: *([0-9]*) blocks \\(([0-9]*)KB\\) written"; // Archive I/O:            211328 blocks (422656KB) written
			String filesWrittenRegEx = "Files written: *([0-9]*) files \\(([0-9]*) regular, ([0-9]*) other\\)"; // Files written:          13 files (6 regular, 7 other)
			String blocksReadRegEx = "Archive I/O: *([0-9]*) blocks \\(([0-9]*)KB\\) read"; // Archive I/O:            0 blocks (0KB) read
			String filesReadRegEx = "Files read: *([0-9]*) files \\(([0-9]*) regular, ([0-9]*) other\\)"; // Files read:             0 files (0 regular, 0 other)
			String filesSkippedRegEx = "Files skipped: *([0-9]*) files"; // Files skipped:          0 files
			String writeErrorsRegEx = "Write errors: *([0-9]*) soft,  ([0-9]*) hard";
			String readErrorsRegEx = "Read errors: *([0-9]*) soft,  ([0-9]*) hard";
			String checksumErrorsRegEx = "Checksum errors: *([0-9]*)";

			Pattern fileAndAttributesRegExPattern = Pattern.compile(fileAndAttributesRegEx);
			Pattern errorDescriptionRegExPattern = Pattern.compile(errorDescriptionRegEx);
			Pattern startedRegExPattern = Pattern.compile(startedRegEx);
			Pattern completedRegExPattern = Pattern.compile(completedRegEx);
			Pattern messagesRegExPattern = Pattern.compile(messagesRegEx);
			Pattern blocksWrittenRegExPattern = Pattern.compile(blocksWrittenRegEx);
			Pattern filesWrittenRegExPattern = Pattern.compile(filesWrittenRegEx);
			Pattern blocksReadRegExPattern = Pattern.compile(blocksReadRegEx);
			Pattern filesReadRegExPattern = Pattern.compile(filesReadRegEx);
			Pattern filesSkippedRegExPattern = Pattern.compile(filesSkippedRegEx);
			Pattern writeErrorsRegExPattern = Pattern.compile(writeErrorsRegEx);
			Pattern readErrorsRegExPattern = Pattern.compile(readErrorsRegEx);
			Pattern checksumErrorsRegExPattern = Pattern.compile(checksumErrorsRegEx);

			Matcher fileAndAttributesRegExMatcher = fileAndAttributesRegExPattern.matcher(line);
			Matcher errorDescriptionRegExMatcher = errorDescriptionRegExPattern.matcher(line);
			Matcher startedRegExMatcher = startedRegExPattern.matcher(line);
			Matcher completedRegExMatcher = completedRegExPattern.matcher(line);
			Matcher messagesRegExMatcher = messagesRegExPattern.matcher(line);
			Matcher blocksWrittenRegExMatcher = blocksWrittenRegExPattern.matcher(line);
			Matcher filesWrittenRegExMatcher = filesWrittenRegExPattern.matcher(line);
			Matcher blocksReadRegExMatcher = blocksReadRegExPattern.matcher(line);
			Matcher filesReadRegExMatcher = filesReadRegExPattern.matcher(line);
			Matcher filesSkippedRegExMatcher = filesSkippedRegExPattern.matcher(line);
			Matcher writeErrorsRegExMatcher = writeErrorsRegExPattern.matcher(line);
			Matcher readErrorsRegExMatcher = readErrorsRegExPattern.matcher(line);
			Matcher checksumErrorsRegExMatcher = checksumErrorsRegExPattern.matcher(line);


			if(fileAndAttributesRegExMatcher.matches()) {
				String operationType = fileAndAttributesRegExMatcher.group(1);
				br.setOperationType(operationType);
				org.ishafoundation.dwaraapi.storage.storageformat.bru.response.components.File file = new org.ishafoundation.dwaraapi.storage.storageformat.bru.response.components.File();

				file.setFileName(fileAndAttributesRegExMatcher.group(6));
				file.setBlockNumber(Integer.parseInt(fileAndAttributesRegExMatcher.group(5)));
				br.getFileList().add(file);
			}
			else if(errorDescriptionRegExMatcher.matches()) {
				ErrorDescription e = new ErrorDescription();
				e.setCode(errorDescriptionRegExMatcher.group(1));
				e.setDesc(errorDescriptionRegExMatcher.group(2));
				br.getErrorDescriptionList().add(e);
			}
			else if(startedRegExMatcher.matches()) {
				String startedAtAsString = startedRegExMatcher.group(1);
				long startedAt = parseDate(startedAtAsString);
				br.setStartedAt(startedAt);
			}
			else if(completedRegExMatcher.matches()) {
				String completedAtAsString = completedRegExMatcher.group(1);
				br.setCompletedAt(parseDate(completedAtAsString));
			}
			else if(messagesRegExMatcher.matches()) {
				br.setWarningCnt(Integer.parseInt(messagesRegExMatcher.group(1)));
				br.setErrorCnt(Integer.parseInt(messagesRegExMatcher.group(2)));
			}
			else if(StringUtils.equals(br.getOperationType(),"c") && blocksWrittenRegExMatcher.matches()) {
				int archiveBlocks = Integer.parseInt(blocksWrittenRegExMatcher.group(1));
				if(archiveBlocks != 0) {
					int archiveSize = Integer.parseInt(blocksWrittenRegExMatcher.group(2));
					
					br.setArchiveBlocks(archiveBlocks);
					br.setArchiveSize(archiveSize);
				}
			}
			else if(StringUtils.equals(br.getOperationType(),"c") && filesWrittenRegExMatcher.matches()) {
				int filesWritten = Integer.parseInt(filesWrittenRegExMatcher.group(1));
				if(filesWritten != 0) {
					int regularFilesCnt = Integer.parseInt(filesWrittenRegExMatcher.group(2));
					int otherFilesCnt = Integer.parseInt(filesWrittenRegExMatcher.group(3));
					
					FilesProcessed fp = new FilesProcessed();
					fp.setTotalNoOfFiles(filesWritten);
					fp.setRegularCnt(regularFilesCnt);
					fp.setOtherCnt(otherFilesCnt);
					br.setFilesProcessed(fp);
				}
			}
			else if(StringUtils.equals(br.getOperationType(),"x") && blocksReadRegExMatcher.matches()) {
				int archiveBlocks = Integer.parseInt(blocksReadRegExMatcher.group(1));
				if(archiveBlocks != 0) {
					int archiveSize = Integer.parseInt(blocksReadRegExMatcher.group(2));
					
					br.setArchiveBlocks(archiveBlocks);
					br.setArchiveSize(archiveSize);
				}				
			}
			else if(StringUtils.equals(br.getOperationType(),"x") && filesReadRegExMatcher.matches()) {
				int filesRead = Integer.parseInt(filesReadRegExMatcher.group(1));
				if(filesRead != 0) {
					int regularFilesCnt = Integer.parseInt(filesReadRegExMatcher.group(2));
					int otherFilesCnt = Integer.parseInt(filesReadRegExMatcher.group(3));
					
					FilesProcessed fp = new FilesProcessed();
					fp.setTotalNoOfFiles(filesRead);
					fp.setRegularCnt(regularFilesCnt);
					fp.setOtherCnt(otherFilesCnt);
					br.setFilesProcessed(fp);
				}
			}
			else if(filesSkippedRegExMatcher.matches()) {
				int filesSkipped = Integer.parseInt(filesSkippedRegExMatcher.group(1));
				br.setFilesSkipped(filesSkipped);
			}
			else if(StringUtils.equals(br.getOperationType(),"c") && writeErrorsRegExMatcher.matches()) {
				int softErrorCnt = Integer.parseInt(writeErrorsRegExMatcher.group(1));
				int hardErrorCnt = Integer.parseInt(writeErrorsRegExMatcher.group(2));
				br.setSoftErrorCnt(softErrorCnt);
				br.setHardErrorCnt(hardErrorCnt);
			}
			else if(StringUtils.equals(br.getOperationType(),"x") && readErrorsRegExMatcher.matches()) {
				int softErrorCnt = Integer.parseInt(readErrorsRegExMatcher.group(1));
				int hardErrorCnt = Integer.parseInt(readErrorsRegExMatcher.group(2));
				br.setSoftErrorCnt(softErrorCnt);
				br.setHardErrorCnt(hardErrorCnt);				
			}
			else if(checksumErrorsRegExMatcher.matches()) {
				int checksumErrorCnt = Integer.parseInt(checksumErrorsRegExMatcher.group(1));
				br.setChecksumErrorCnt(checksumErrorCnt);
			}
		}
		scanner.close();
		return br;
	}
	
	private long parseDate(String dateToBeParsed) {
		Date date = null;
		try {
			date = formatWithSingleDigitDate.parse(dateToBeParsed);
			logger.trace("formatWithSingleDigitDate : " + date.toString());
		} catch (ParseException e) {
			logger.error("unable to parse date using formatWithSingleDigitDate. Falling back to formatWithDoubleDigitDate", e);
			try {
				date = formatWithDoubleDigitDate.parse(dateToBeParsed);
				logger.trace("formatWithDoubleDigitDate : " + date.toString());
			} catch (ParseException e1) {
				logger.error("unable to parse date ", e1);
			}
		} 
		return date != null ? date.getTime() : 0;
	}
	
	public static void main(String[] args) throws IOException {
		BruResponseParser brp = new BruResponseParser();
		String jkl = FileUtils.readFileToString(new File(args[0]));
		BruResponse bruResponse = brp.parseBruResponse(jkl);
		System.out.println(bruResponse);
	}
}
