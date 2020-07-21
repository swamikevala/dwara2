package org.ishafoundation.dwaraapi.job.creation;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.api.req.ingest.UserRequest;
import org.ishafoundation.dwaraapi.service.ArtifactService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.lingala.zip4j.ZipFile;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Ingest_Test{

	@Autowired
	ArtifactService artifactService;
	
	//String readyToIngestPath =  "C:\\data\\user\\pgurumurthy\\ingest\\pub-video";
	String readyToIngestPath =  "C:\\data\\ingested";
	
	@Test
	public void test_b_Ingest() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			URL fileUrl = this.getClass().getResource("/testcases/ingest/ingest_request.json");

			String testIngestArtifactName1 =  "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9";
			String artifact_name_1 = extractZip(testIngestArtifactName1);
			
			String testIngestArtifactName2 = "Shiva-Shambho_Everywhere_18-Nov-1980_Drone";
			String artifact_name_2 = extractZip(testIngestArtifactName2);

//			String testIngestArtifactName3 = "Cauvery-Calling_Day1-Sadhguru-Talking-With-People_Palace-Grounds-Bengaluru_02-Sep-2019_GoProApr6";
//			String artifact_name_3 = extractZip(testIngestArtifactName3);

			String postBodyJson = FileUtils.readFileToString(new File(fileUrl.getFile()));
			postBodyJson = postBodyJson.replace("<<artifact_name_1>>", artifact_name_1);
			postBodyJson = postBodyJson.replace("<<artifact_name_2>>", artifact_name_2);
//			postBodyJson = postBodyJson.replace("<<artifact_name_3>>", artifact_name_3);
			
			UserRequest ur = mapper.readValue(postBodyJson, new TypeReference<UserRequest>() {});
			artifactService.ingest(ur);
			
			// Delete the files after the creation is done...
//			FileUtils.deleteDirectory(new File(readyToIngestPath + File.separator + artifact_name_1));
//			FileUtils.deleteDirectory(new File(readyToIngestPath + File.separator + artifact_name_2));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private String extractZip(String testIngestArtifactName) throws Exception {	
		
		URL fileUrl = JobCreator_Ingest_Test.class.getResource("/" + testIngestArtifactName + ".zip");
		ZipFile zipFile = new ZipFile(fileUrl.getFile());

		zipFile.extractAll(readyToIngestPath);
		
		String ingestFileSourcePath = readyToIngestPath + File.separator + testIngestArtifactName;
		String artifactNameToBeIngested = testIngestArtifactName + "_" + System.currentTimeMillis(); // TO have the artifact name uniqued...
		
		String artifactPath = readyToIngestPath + File.separator +  artifactNameToBeIngested;
		FileUtils.moveDirectory(new File(ingestFileSourcePath), new File(artifactPath));
		return artifactNameToBeIngested;
		
	}
}
