package org.ishafoundation.dwaraapi.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestControllerTest {

	@Autowired 
	TestController testController;
	
	@Test
	public void ingest() {
		try {
		//testController.triggerSample();
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("im here too");
		}
		System.out.println("im here");
	}

}
