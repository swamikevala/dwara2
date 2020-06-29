package org.ishafoundation.dwaraapi;

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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Ingest_Test{

	@Autowired
	ArtifactService artifactService;
	
	@Test
	public void test_b_Ingest() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			URL fileUrl = this.getClass().getResource("/testcases/ingest/ingest_request.json");

			
			String postBodyJson = FileUtils.readFileToString(new File(fileUrl.getFile()));
			long suffix = System.currentTimeMillis();
			String artifact_name_1 = "10058_Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9"
					+ suffix;
			postBodyJson = postBodyJson.replace("<<artifact_name_1>>", artifact_name_1);
			String artifact_name_2 = "10058_Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9"
					+ (suffix + 1);
			postBodyJson = postBodyJson.replace("<<artifact_name_2>>", artifact_name_2);
			

			UserRequest ur = mapper.readValue(postBodyJson, new TypeReference<UserRequest>() {});
			artifactService.ingest(ur);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
