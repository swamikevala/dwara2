package org.ishafoundation.dwaraapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@EnableScheduling
@ComponentScan({"org.ishafoundation.dwaraapi","org.ishafoundation.videopub"})
@SpringBootApplication
public class DwaraApiApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(DwaraApiApplication.class, args);
	}
}
