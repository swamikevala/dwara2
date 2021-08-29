package org.ishafoundation.dwaraapi.hotfixes;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.service.ArtifactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class Artifactclass_Fixer {

	private static final Logger logger = LoggerFactory.getLogger(Artifactclass_Fixer.class);

	@Autowired
	private ArtifactService artifactService;

	@Autowired
	private ArtifactRepository artifactRepository;

	@PostMapping(value = "/fixArtifactclass", produces = "application/json")
	public ResponseEntity<String> fixArtifactclass() {

		// Refer the list from Artifactclass_Fixer.csv -- The first four in the file
		// need to be manually done and the below collection is the remaing to be
		// automated
		List<String> artifactListToBeChanged = new ArrayList<String>();
		// artifactListToBeChanged.add("Prev Seq Code,Artifact Id,Current
		// ArtifactClass,New Artifactclass");

		// Added for testing
//		artifactListToBeChanged.add("K1000,13284,video-digi-2020-priv2,video-digi-2020-pub");
//		artifactListToBeChanged.add("K1001,13286,video-digi-2020-pub,video-digi-2020-edit-pub");

		artifactListToBeChanged.add("GR10,27654,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR11,27661,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR13,27657,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR14,27656,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR15,27643,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR16,27652,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR17,27642,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR18,27641,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR19,27645,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR20,27662,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR21,27649,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR22,27650,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR23,27634,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR24,27637,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR25,27640,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR26,27635,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR28,27636,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR29,27647,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR30,27638,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR31,27655,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR32,27664,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR33,27644,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR34,27663,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR35,27658,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR36,27639,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR37,27660,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR39,27713,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR40,27659,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR41,29193,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR42,27936,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR44,27941,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR45,28206,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR46,27937,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR47,27934,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR48,27939,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR49,28334,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR50,27928,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR53,27927,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR54,27931,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR55,27945,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR56,27942,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR57,27933,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR58,27925,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR59,28362,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR60,28907,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR61,27943,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR62,27944,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR63,27938,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR66,27930,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR67,27935,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR68,27929,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR69,27932,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR7,27648,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR70,28316,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR71,28317,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR72,28315,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("GR8,29195,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X1,27940,video-digi-2020-edit-pub,video-digi-2020-pub,X1_COPY");
		artifactListToBeChanged.add("X10,25717,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X11,25811,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X12,25839,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X13,25828,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X16,25809,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X17,25821,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X18,25824,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X19,25823,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X20,25884,video-digi-2020-edit-priv1,video-digi-2020-priv1");
		artifactListToBeChanged.add("X21,25888,video-digi-2020-edit-priv1,video-digi-2020-priv1");
		artifactListToBeChanged.add("X22,25889,video-digi-2020-edit-priv1,video-digi-2020-priv1");
		artifactListToBeChanged.add("X23,25886,video-digi-2020-edit-priv1,video-digi-2020-priv1");
		artifactListToBeChanged.add("X24,25887,video-digi-2020-edit-priv1,video-digi-2020-priv1");
		artifactListToBeChanged.add("X25,25885,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X26,26199,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X27,26200,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X28,26198,video-digi-2020-edit-priv2,video-digi-2020-priv2");
		artifactListToBeChanged.add("X4,27926,video-digi-2020-edit-pub,video-digi-2020-pub,X4_COPY");
		artifactListToBeChanged.add("X5,25716,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X6,25719,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X7,25718,video-digi-2020-edit-priv1,video-digi-2020-priv1");
		artifactListToBeChanged.add("X8,25715,video-digi-2020-edit-pub,video-digi-2020-pub");
		artifactListToBeChanged.add("X9,25720,video-digi-2020-edit-pub,video-digi-2020-pub");

		String status = "Done";
		for (String nthArtifact : artifactListToBeChanged) {
			String[] fields = nthArtifact.split(",");
			String prevSeqCode = fields[0];
			String artifactId = fields[1];
			String currArtifactClass = fields[2];
			String newArtifactClass = fields[3];
			try {
				logger.info("Updating - " + prevSeqCode);
				artifactService.changeArtifactclass(Integer.parseInt(artifactId), newArtifactClass, true);
				logger.info("Completed Updating - " + prevSeqCode);

				ArtifactRepository<Artifact> artifactRepository = null;
				Artifact requestedArtifact = null; // get the artifact details from DB
				requestedArtifact = artifactRepository.findById(Integer.parseInt(artifactId));
				/*
				 * Domain[] domains = Domain.values();
				 * 
				 * for (Domain nthDomain : domains) { //artifactRepository =
				 * domainUtil.getDomainSpecificArtifactRepository(nthDomain); requestedArtifact
				 * = artifactRepository.findById(Integer.parseInt(artifactId));
				 * 
				 * if(artifactEntity != null) { requestedArtifact = artifactEntity.get(); break;
				 * }
				 * 
				 * }
				 */
				requestedArtifact.setPrevSequenceCode(prevSeqCode); // prevSeqCode was not set
				artifactRepository.save(requestedArtifact);

				logger.info(prevSeqCode + " set in " + artifactId);
			} catch (Exception e) {
				logger.error("Unable to change Artifactclass " + artifactId + " : " + e.getMessage());
				status = "Check app logs";
			}

		}
		return ResponseEntity.status(HttpStatus.OK).body(status);
	}
}
