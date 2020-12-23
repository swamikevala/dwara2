package org.ishafoundation.dwaraapi.job.creation;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.api.req.restore.RestoreUserRequest;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.service.FileService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Restore_Process_Test {
	

	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Restore_Process_Test.class);

	@Autowired
	FileService fileService;
		
	@Test
	public void test_Restore() {
		try {
			SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("pgurumurthy", "ShivaShambho"));
			
			ObjectMapper mapper = new ObjectMapper();
			URL fileUrl = this.getClass().getResource("/testcases/restore/restore_process_request.json");
			
			String postBodyJson = FileUtils.readFileToString(new File(fileUrl.getFile()));
			postBodyJson = postBodyJson.replace("<<file_id_1>>", "75");
			postBodyJson = postBodyJson.replace("<<file_id_2>>", "73");
			
			RestoreUserRequest ur = mapper.readValue(postBodyJson, new TypeReference<RestoreUserRequest>() {});
			fileService.restore(ur, Action.restore_process, ur.getFlow());
			
			// Delete the files after the creation is done...
//			FileUtils.deleteDirectory(new File(readyToIngestPath + File.separator + artifact_name_1));
//			FileUtils.deleteDirectory(new File(readyToIngestPath + File.separator + artifact_name_2));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
