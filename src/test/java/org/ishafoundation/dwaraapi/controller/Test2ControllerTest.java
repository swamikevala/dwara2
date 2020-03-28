package org.ishafoundation.dwaraapi.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test2ControllerTest {

	@Autowired 
	Test2Controller test2Controller;
	
	@Test
	public void ingest() {
		try {
		//test2Controller.updateDB();
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("im here too");
		}
		System.out.println("im here");
	}

}
