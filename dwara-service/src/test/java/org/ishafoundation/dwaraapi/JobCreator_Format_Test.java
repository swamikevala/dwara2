package org.ishafoundation.dwaraapi;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.api.req.format.FormatRequest;
import org.ishafoundation.dwaraapi.service.VolumeService;
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
public class JobCreator_Format_Test {
	

	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Format_Test.class);

	@Autowired
	VolumeService volumeService;
		
	@Test
	public void test_Format() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			URL fileUrl = this.getClass().getResource("/testcases/finalize/finalize_request.json");
			
			String postBodyJson = FileUtils.readFileToString(new File(fileUrl.getFile()));
			
			FormatRequest fr = mapper.readValue(postBodyJson, new TypeReference<FormatRequest>() {});
			volumeService.format(fr);
			
			// Delete the files after the creation is done...
//			FileUtils.deleteDirectory(new File(readyToIngestPath + File.separator + artifact_name_1));
//			FileUtils.deleteDirectory(new File(readyToIngestPath + File.separator + artifact_name_2));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
