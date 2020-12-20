package org.ishafoundation.dwaraapi;

import org.ishafoundation.dwaraapi.db.dao.master.VersionDao;
import org.ishafoundation.dwaraapi.db.model.master.reference.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@EnableScheduling
@ComponentScan({"org.ishafoundation.dwaraapi","org.ishafoundation.videopub","org.ishafoundation.digitization"})
@SpringBootApplication
public class DwaraApiApplication {
	
	@Autowired
	private VersionDao versionDao;	
	
	public static void main(String[] args) {
		SpringApplication.run(DwaraApiApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void validateDbVersion() throws Exception {
		Version version = versionDao.findTopByOrderByVersion();
		String dbVersion = "2.0.3";
		if(version == null || !version.getVersion().equals(dbVersion))
			throw new Exception("DB version mismatch. Upgrade DB to " + dbVersion);
	}
}
