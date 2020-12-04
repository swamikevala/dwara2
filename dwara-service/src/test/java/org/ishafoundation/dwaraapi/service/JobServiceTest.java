package org.ishafoundation.dwaraapi.service;

import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.job.JobResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;


@RunWith(SpringRunner.class)
@SpringBootTest
public class JobServiceTest {

	@Autowired
	JobService jobService;
	
	@Test
	public void test_a() throws Exception {
		List<JobResponse> jobResList = jobService.getJobs(2);
		ObjectMapper mapper = new ObjectMapper(); 
		System.out.println(mapper.writeValueAsString(jobResList));
	}
	
}
