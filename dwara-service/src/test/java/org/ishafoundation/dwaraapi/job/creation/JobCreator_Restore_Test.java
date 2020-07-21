package org.ishafoundation.dwaraapi.job.creation;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.api.req.restore.UserRequest;
import org.ishafoundation.dwaraapi.service.FileService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Restore_Test {
	

	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Restore_Test.class);

	@Autowired
	FileService fileService;
		
	@Test
	public void test_Restore() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			URL fileUrl = this.getClass().getResource("/testcases/restore/restore_request.json");
			
			String postBodyJson = FileUtils.readFileToString(new File(fileUrl.getFile()));
			// entire artifact 
			//postBodyJson = postBodyJson.replace("<<file_id_1>>", "2");
			// 1 folder 
			//postBodyJson = postBodyJson.replace("<<file_id_1>>", "5");
			postBodyJson = postBodyJson.replace("<<file_id_1>>", "7");
			// just the 1 file postBodyJson = postBodyJson.replace("<<file_id_1>>", "8"); //60 entire artifact// 63 - 1 CD folder // 65 - 2 CD folder // 67 - just one file 
			
			UserRequest ur = mapper.readValue(postBodyJson, new TypeReference<UserRequest>() {});
			fileService.restore(ur);
			
			// Delete the files after the creation is done...
//			FileUtils.deleteDirectory(new File(readyToIngestPath + File.separator + artifact_name_1));
//			FileUtils.deleteDirectory(new File(readyToIngestPath + File.separator + artifact_name_2));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
