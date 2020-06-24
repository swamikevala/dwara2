package org.ishafoundation.dwaraapi.db.utils;

import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class DomainUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(DomainUtilTest.class);

	@Autowired
	private DomainUtil domainUtil;

	@Test
	public void test_a_NullDomain() {
		Artifact artifact = domainUtil.getDomainSpecificArtifact(null, 6);
		logger.debug(artifact.getClass().getName());
	}
	
	@Test
	public void test_b_SpecificDomain() {
		Artifact artifact = domainUtil.getDomainSpecificArtifact(Domain.two, 1);
		logger.debug(artifact.getClass().getName());
	}
}
