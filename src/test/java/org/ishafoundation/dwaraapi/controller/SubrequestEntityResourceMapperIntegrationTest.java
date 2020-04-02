package org.ishafoundation.dwaraapi.controller;

import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.entrypoint.resource.mapper.SubrequestEntityResourceMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SubrequestEntityResourceMapperIntegrationTest {

    @Autowired
    SubrequestEntityResourceMapper subrequestEntityResourceMapper;

    @Autowired
    SubrequestDao subrequestDao;
    
    @Test
    public void givenSourceToDestination_whenMaps_thenCorrect() {
    	List<Subrequest> subrequestList = subrequestDao.findAllByRequestId(1);

    	for (Iterator<Subrequest> iterator = subrequestList.iterator(); iterator.hasNext();) {
			Subrequest subrequest = (Subrequest) iterator.next();
			
			org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Subrequest resourceSubrequest = subrequestEntityResourceMapper.entityToResource(subrequest);
			
			ObjectMapper mapper = new ObjectMapper(); 
			mapper.enable(SerializationFeature.INDENT_OUTPUT); 

			try {
				System.out.println("resp - " + mapper.writeValueAsString(resourceSubrequest));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
    }

}
