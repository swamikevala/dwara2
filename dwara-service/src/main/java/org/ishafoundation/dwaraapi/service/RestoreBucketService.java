package org.ishafoundation.dwaraapi.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TRestoreBucketDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.File;
import org.ishafoundation.dwaraapi.db.model.transactional.RestoreBucketFile;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class RestoreBucketService extends DwaraService {
	private static final Logger logger = LoggerFactory.getLogger(RestoreBucketService.class);
	@Autowired
	TRestoreBucketDao tRestoreBucketDao;
	@Autowired
	FileDao file1Dao;
	@Autowired
	ArtifactDao artifact1Dao;
	@Autowired
	EmailerService emailerService;
    @Autowired
    UserDao userDao;
	RestTemplate restTemplate;

	public RestoreBucketService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}
	public TRestoreBucket createBucket(String id) {
		int createdBy = getUserObjFromContext().getId();
		TRestoreBucket tRestoreBucket = new TRestoreBucket(id, createdBy, LocalDateTime.now());

		tRestoreBucketDao.save(tRestoreBucket);
		return tRestoreBucket;
	}

	public void deleteBucket(String id) {
		tRestoreBucketDao.deleteById(id);
	}

	public TRestoreBucket updateBucket(List<Integer> fileIds, String id, boolean add) {

		Optional<TRestoreBucket> result = tRestoreBucketDao.findById(id);
		TRestoreBucket tRestoreBucketFromDb = result.get();

		if (add) {
			List<RestoreBucketFile> restoreBucketFiles = new ArrayList<>();
			for (int fileid : fileIds) {
				List<RestoreBucketFile> restoreBucketFile = createFile(fileid);
				restoreBucketFiles.addAll(restoreBucketFile);

			}
			if (tRestoreBucketFromDb.getDetails() != null)
				tRestoreBucketFromDb.addDetails(restoreBucketFiles);
			else
				tRestoreBucketFromDb.setDetails(restoreBucketFiles);
		} else {
			List<RestoreBucketFile> restoreBucketFiles = tRestoreBucketFromDb.getDetails();
			List<RestoreBucketFile> temp = new ArrayList<>();
			for (RestoreBucketFile file : restoreBucketFiles) {

				if (fileIds.contains(file.getFileID()))
					temp.add(file);
			}
			restoreBucketFiles.removeAll(temp);

			tRestoreBucketFromDb.setDetails(restoreBucketFiles);
		}
		tRestoreBucketDao.save(tRestoreBucketFromDb);
		return tRestoreBucketFromDb;

	}

	// only one file is added
	public TRestoreBucket getFileList(String id, List<String> proxyPaths) throws Exception {

		TRestoreBucket tRestoreBucketFromDb = tRestoreBucketDao.findById(id).get();
		List<RestoreBucketFile> presentFiles = tRestoreBucketFromDb.getDetails();
		List<Integer> presentIds = new ArrayList<>();
		if (presentFiles == null) {
			presentFiles = new ArrayList<>();
		}
		for (RestoreBucketFile restoreBucketFile : presentFiles) {
			presentIds.add(restoreBucketFile.getFileID());
		}

		List<File> proxyFiles = file1Dao.findByPathnameIn(proxyPaths);

		// List<RestoreBucketFile> ogFiles =new ArrayList<>();
		for (File file : proxyFiles) {

			File ogFile = file.getFileRef();
			if (presentIds.contains(ogFile.getId())) {
				return null;
			}
			String appendUrlTOProxy = "";
			if (ogFile.getArtifact().getArtifactclass().getId().contains("-priv")) {
				appendUrlTOProxy = "http://172.18.1.24/mam/private/";
			} else
				appendUrlTOProxy = "http://172.18.1.24/mam/public/";

			RestoreBucketFile restoreBucketFile = new RestoreBucketFile();
			restoreBucketFile.setFileID(ogFile.getId());
			restoreBucketFile.setFileSize(String.valueOf(ogFile.getSize()));
			restoreBucketFile.setFilePathName(ogFile.getPathname());
			List<String> previewProxyPaths = new ArrayList<>();

			previewProxyPaths.add(appendUrlTOProxy + proxyPaths.get(proxyFiles.indexOf(file)));
			if (restoreBucketFile.getPreviewProxyPath() != null)
				restoreBucketFile.addPreviewProxyPath(previewProxyPaths);
			else
				restoreBucketFile.setPreviewProxyPath(previewProxyPaths);
			restoreBucketFile.setArtifactId(ogFile.getArtifact().getId());
			restoreBucketFile.setArtifactClass(ogFile.getArtifact().getArtifactclass().getId());
			presentFiles.add(restoreBucketFile);
		}

		tRestoreBucketFromDb.setDetails(presentFiles);
		tRestoreBucketDao.save(tRestoreBucketFromDb);
		return tRestoreBucketFromDb;

	}

	private List<RestoreBucketFile> createFile(int id) {
		File ogFile = file1Dao.findById(id).get();
		// Artifact artifact = artifact1Dao.findByName(ogFile.getPathname());
		List<RestoreBucketFile> restoreBucketFiles = new ArrayList<>();
		RestoreBucketFile restoreBucketFile = new RestoreBucketFile();
		restoreBucketFile.setFileID(ogFile.getId());
		restoreBucketFile.setFileSize(String.valueOf(ogFile.getSize()));
		restoreBucketFile.setFilePathName(ogFile.getPathname());
		restoreBucketFile.setArtifactId(ogFile.getArtifact().getId());
		restoreBucketFile.setArtifactClass(ogFile.getArtifact().getArtifactclass().getId());
		List<String> previewProxyPaths = new ArrayList<>();
		String appendUrlTOProxy = "";
		if (ogFile.getArtifact().getArtifactclass().getId().contains("-priv")) {
			appendUrlTOProxy = "http://172.18.1.24/mam/private/";
		} else
			appendUrlTOProxy = "http://172.18.1.24/mam/public/";
		// Artifact artifact = (Artifact) artifact1Dao.findByName(ogFile.getPathname());
		if (artifact1Dao.existsByName(ogFile.getPathname())) {
			Artifact artifact = (Artifact) artifact1Dao.findByName(ogFile.getPathname());

			List<Artifact> proxyArtifacts = artifact1Dao.findAllByArtifactRef(artifact);
			if (proxyArtifacts.size() > 0) {
				Artifact proxyArtifact = proxyArtifacts.get(0);
				List<File> proxyVideos = file1Dao.findAllByArtifactIdAndPathnameEndsWith(proxyArtifact.getId(), ".mp4");

				for (File file : proxyVideos) {
					previewProxyPaths.add(appendUrlTOProxy + file.getPathname());
				}

			}
		}

		else {
			List<File> proxyFiles = file1Dao.findAllByFileRefIdAndPathnameEndsWith(ogFile.getId(), ".mp4");
			for (File file : proxyFiles) {
				previewProxyPaths.add(appendUrlTOProxy + file.getPathname());
			}

		}
		restoreBucketFile.setPreviewProxyPath(previewProxyPaths);

		restoreBucketFiles.add(restoreBucketFile);
		return restoreBucketFiles;

	}

	public List<TRestoreBucket> getAprrovedNull() {
        int userId =getUserObjFromContext().getId();
        return tRestoreBucketDao.findByApprovalStatusNullAndCreatedBy(userId);
	}

	public void sendMail(TRestoreBucket tRestoreBucket) {
		//String sendUrl= "http://localhost:9090/dwarahelper/sendEmail";
		String sendUrl= "http://172.18.1.24:9090/dwarahelper/sendEmail";
		String emailBody = "<p>Namaskaram</p>";
		User requester= userDao.findById(tRestoreBucket.getRequestedBy()).get();
        String requesterName= requester.getName();
        emailBody += "<p>A private request has been raised by"+requesterName+"</p>";
        emailBody += "<p>The following folders in <span style='color:red'>red</span> need your approval.</p>";
		List<String> fileName = new ArrayList<>();
		for (RestoreBucketFile file : tRestoreBucket.getDetails()) {
            String css ="";
            if(file.getArtifactClass().contains("priv")){
                css="color:red";
            }
            emailBody +="<div style='"+css+"'> "+ file.getFilePathName()  +"</div>";
		}
		emailBody += "<p>Please reply with <b><approved></b> if you wish to approve </p>";
		/*emailerService.setConcernedEmail(tRestoreBucket.getApproverEmail());
        emailerService.setSubject("Need Approval for project: _"+tRestoreBucket.getId()+"_. Priority: "+ tRestoreBucket.getPriority());
        emailerService.setRequesterEmail(requesterName);
		emailerService.sendEmail(emailBody);*/
		String sendUrlTemplate= UriComponentsBuilder.fromHttpUrl(sendUrl)
				.queryParam("concernedEmail" , tRestoreBucket.getApproverEmail() )
				.queryParam("subject","Need Approval for project: _"+tRestoreBucket.getId()+"_. Priority: "+ tRestoreBucket.getPriority())
				.queryParam("emailBody", emailBody)
				.queryParam("requesterEmail" , requester.getEmail())
				.encode()
				.toUriString();

		ResponseEntity<String> response1
				= restTemplate.getForEntity( sendUrlTemplate, String.class);
	}


	public String getElapsedTime(LocalDateTime createdTime){

		long requestedAgo = System.currentTimeMillis()/1000-createdTime.toEpochSecond(ZoneOffset.of("+05:30"));
		long hours = requestedAgo/3600;
		long minutes = (requestedAgo-hours*3600)/60;
		String timeElapsed = hours+" Hours "+minutes + " Minutes ";
		return timeElapsed;
	}

}
