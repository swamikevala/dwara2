package org.ishafoundation.dwaraapi.ltowala.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.DomainAttributeConverter;
import org.ishafoundation.dwaraapi.db.dao.master.DomainDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepositoryUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.ltowala.api.resp.Artifact;
import org.ishafoundation.dwaraapi.ltowala.api.resp.File;
import org.ishafoundation.dwaraapi.ltowala.api.resp.LtoWalaResponse;
import org.ishafoundation.dwaraapi.ltowala.api.resp.Volume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ForSolutionSameerService {

	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private DomainDao domainDao;

	@Autowired
	private DomainAttributeConverter domainAttributeConverter;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private FileRepositoryUtil fileRepositoryUtil;
	
	// To narrow down the job to a specific copy
	private int copyNumber = 1; // Could be any copy...
	
	public LtoWalaResponse dataForLtoWala(LocalDateTime startDateTime, LocalDateTime endDateTime, boolean listFiles, boolean needVolumeDetails) throws Exception {
		
		
		LtoWalaResponse ltoWalaResponse = new LtoWalaResponse();
		ltoWalaResponse.setStartDate(getDateForUI(startDateTime));
		ltoWalaResponse.setEndDate(getDateForUI(endDateTime));
		
		List<Artifact> artifactList = new ArrayList<Artifact>();
		List<Job> jobList = jobDao.findAllByCompletedAtBetweenAndStoragetaskActionIdAndGroupVolumeCopyNumber(startDateTime, endDateTime, Action.write, copyNumber);
		for (Job job : jobList) {
			Artifact artifact = new Artifact();
			int artifactId = job.getInputArtifactId();
			
			org.ishafoundation.dwaraapi.db.model.master.configuration.Domain domainFromDB = domainDao.findByDefaultTrue();
			Domain domain = domainAttributeConverter.convertToEntityAttribute(domainFromDB.getId()+"");
			ArtifactRepository artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
			org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact artifactFromDb = (org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact) artifactRepository.findById(artifactId).get();
			String artifactName = artifactFromDb.getName();
			artifact.setName(artifactName);
			artifact.setArtifactclass(artifactFromDb.getArtifactclass().getName());
			artifact.setTotalSize(artifactFromDb.getTotalSize());
			artifact.setCompletedAt(getDateForUI(job.getCompletedAt())); // TODO Change the field name
			artifact.setFileCount(artifactFromDb.getFileCount());
			
			List<File> fileList = new ArrayList<File>();
			List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList = fileRepositoryUtil.getArtifactFileList(artifactFromDb, domain);
			for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : artifactFileList) {
				String filePathname = nthFile.getPathname();
				if(listFiles || (!listFiles && filePathname.equals(artifactName))) {
					File file = new File();
					
					file.setId(nthFile.getId());
					file.setPathname(filePathname);
					file.setSize(nthFile.getSize());
					
					fileList.add(file);
					
					if(!listFiles)
						break;
				}
			}
			artifact.setFile(fileList);
			
			if(needVolumeDetails) {
				Volume volume = new Volume();
				String volumeId = job.getVolume().getId();
				volume.setBarcode(volumeId);
				ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
				ArtifactVolume artifactVolume = domainSpecificArtifactVolumeRepository.findByIdArtifactIdAndIdVolumeId(artifactId, volumeId);
	
				volume.setStartBlock(artifactVolume.getDetails().getStart_volume_block());
				
				artifact.setVolume(volume);
			}
			artifactList.add(artifact);
		}
		
		ltoWalaResponse.setArtifact(artifactList);
		
		
		return ltoWalaResponse;
	}
	
	protected String getDateForUI(LocalDateTime _tedAt) { // requestedAt, createdAt, startedAt
		String dateForUI = null;
		if(_tedAt != null) {
			ZonedDateTime zdt = _tedAt.atZone(ZoneId.of("UTC"));
			dateForUI = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").format(zdt);
		}
		return dateForUI;
	}
}
