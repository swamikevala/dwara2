package org.ishafoundation.dwaraapi.hotfixes;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.InterArtifactlabel;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.LabelManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class HotFix_2_1_07 {
	private static final Logger logger = LoggerFactory.getLogger(HotFix_2_1_07.class);

	@Autowired
	private LabelManagerImpl labelManager;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Value("${filesystem.temporarylocation}")
	private String filesystemTemporarylocation;
	
	@PostMapping(value="/hotFix_2_1_07", produces = "application/json")
    public ResponseEntity<String> hotFix_2_1_07(@RequestParam int artifactId, @RequestParam String volumeId, @RequestParam String fileName) throws Exception{
//		
		ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(Domain.ONE);
		Artifact artifact = artifactRepository.findById(artifactId).get();
		
	    ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(Domain.ONE);
	    ArtifactVolume artifactVolume = domainSpecificArtifactVolumeRepository.findByIdArtifactIdAndIdVolumeId(artifactId, volumeId);

		
	    InterArtifactlabel artifactlabel = labelManager.generateArtifactLabel(artifact, artifactVolume.getVolume(), artifactVolume.getDetails().getStartVolumeBlock(), artifactVolume.getDetails().getEndVolumeBlock());
	    String label = labelManager.labelXmlAsString(artifactlabel);
		
		File file = new File(filesystemTemporarylocation + File.separator + fileName + ".xml");
		FileUtils.writeStringToFile(file, label);
		String response = file.getAbsolutePath() + " created"; 
		logger.trace(response);

		return ResponseEntity.status(HttpStatus.OK).body(response);
		
	}

}
