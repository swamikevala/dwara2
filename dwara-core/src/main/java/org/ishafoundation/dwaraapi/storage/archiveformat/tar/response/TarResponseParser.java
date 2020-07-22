package org.ishafoundation.dwaraapi.storage.archiveformat.tar.response;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * Class responsible for parsing the below command's console output

[root@test-ingest 1G-5G]# tar cvvv -R -b 1024 -f /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1684087499-nst Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9
block 0: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/
block 1: -rwxrwxrwx root/root         0 2019-08-13 15:33 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/._.DS_Store
block 2: -rwxrwxrwx root/root         0 2019-08-13 15:33 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/.DS_Store
block 3: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/
block 4: -rwxrwxrwx root/root  12878883 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074746.mp4
block 25160: -rwxrwxrwx root/root  24063587 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074810.mp4
block 72161: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/1 CD/
block 72162: -rwxrwxrwx root/root   7353665 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/1 CD/20190701_071239.mp4

*************************************** ERROR Scenario ****************************************

 TODO - ??? Swami to help

*/


public class TarResponseParser {

	static Logger logger = LoggerFactory.getLogger(TarResponseParser.class);
	
	public TarResponse parseTarResponse(String tarCommandResponse){
		TarResponse tarResponse = new TarResponse();
		Scanner scanner = new Scanner(tarCommandResponse);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();

			// block 25160: -rwxrwxrwx root/root  24063587 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074810.mp4
			// block <<archive start block>>: <<file permissions>> <<user/group>  <<file size>> <<date n time>> <<file path name>>

			// 25160 - archive start block
			// -rwxrwxrwx - file permissions
			// root/root - user/group
			// 24063587 - file size in bytes
			// 2019-07-25 13:51 - date n time
			// Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074810.mp4 - file path name
			String fileAndAttributesRegEx = "block (.[0-9]*): (.[^ ]*) (.[^ ]*)([ ]*)(.[^ ]*) (.[0-9- :]*) (.*)";
			// OR
			//String fileAndAttributesRegEx = "block (.[0-9]*): (.[a-z-]*) (.[a-z/]*)([ ]*)(.[0-9]*) (.[0-9- :]*) (.*)"; 

			Pattern fileAndAttributesRegExPattern = Pattern.compile(fileAndAttributesRegEx);

			Matcher fileAndAttributesRegExMatcher = fileAndAttributesRegExPattern.matcher(line);


			if(fileAndAttributesRegExMatcher.matches()) {
				org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.components.File file = new org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.components.File();
				
				String archiveBlockOffset = fileAndAttributesRegExMatcher.group(1);
				String fileSizeAsString = fileAndAttributesRegExMatcher.group(5);
				String filePathName = fileAndAttributesRegExMatcher.group(7);
				
				file.setFilePathName(filePathName);
				file.setFileSize(Long.parseLong(fileSizeAsString));
				file.setArchiveBlock(Integer.parseInt(archiveBlockOffset));

				tarResponse.getFileList().add(file);
			}
		}
		scanner.close();
		return tarResponse;
	}
	
	public static void main(String[] args) throws IOException {
		TarResponseParser tarResponseParser = new TarResponseParser();
		String tarCommandOutput = FileUtils.readFileToString(new File(args[0]));
		TarResponse tarResponse = tarResponseParser.parseTarResponse(tarCommandOutput);
		System.out.println(tarResponse);
	}
}
