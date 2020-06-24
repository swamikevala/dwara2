package org.ishafoundation.dwaraapi.dbunittests.ingest;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IngestSetupUtil {
//	
//	public static org.ishafoundation.dwaraapi.api.req.ingest.UserRequest setupLibraryForIngest() throws Exception {
//		String testIngestLibraryName =  "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9";
//		URL fileUrl = IngestSetupUtil.class.getResource("/" + testIngestLibraryName + ".zip");
//		ZipFile zipFile = new ZipFile(fileUrl.getFile());
//		String readyToIngestPath =  "C:\\data\\user\\pgurumurthy\\ingest\\pub-video";
//		zipFile.extractAll(readyToIngestPath);
//		
//		String ingestFileSourcePath = readyToIngestPath + File.separator + testIngestLibraryName;
//		String libraryNameToBeIngested = testIngestLibraryName + System.currentTimeMillis(); // TO have the library name uniqued...
//		String libraryPath = readyToIngestPath + File.separator +  libraryNameToBeIngested;
//		FileUtils.moveDirectory(new File(ingestFileSourcePath), new File(libraryPath));
//		
//		List<LibraryParams> libraryParamsList = new ArrayList<LibraryParams>();
//		
//		LibraryParams libraryParams = new LibraryParams();
//		libraryParams.setSourcePath(readyToIngestPath);
//		libraryParams.setName(libraryNameToBeIngested);
//		
//		libraryParamsList.add(libraryParams);
//		
//		org.ishafoundation.dwaraapi.api.req.ingest.UserRequest userRequest = new org.ishafoundation.dwaraapi.api.req.ingest.UserRequest();
//		userRequest.setLibraryclass("pub-video");
//		userRequest.setLibrary(libraryParamsList);
//		
//		return userRequest;
//	}
//
}
