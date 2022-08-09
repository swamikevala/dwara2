package org.ishafoundation.dwaraapi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ArtifactVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.FileVolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.File;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileVolume;
import org.ishafoundation.dwaraapi.storage.archiveformat.tar.TarBlockCalculatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CaptureFileVolumeEndBlockService extends DwaraService {

    private static final Logger logger = LoggerFactory.getLogger(CaptureFileVolumeEndBlockService.class);

    @Autowired
    private VolumeDao volumeDao;

    @Autowired
    private FileDao fileDao;
    
    @Autowired
    private FileVolumeDao fileVolumeDao;
    
    @Autowired
    private ArtifactVolumeDao artifactVolumeDao;
    
    public void fileVolumeEndBlock(List<String> volumeId) {
        List<FileVolume> fileVolumeBlockList;
        List<Volume> volumeGroupList = new ArrayList<>();
        if(volumeId.size() > 0) {
          for (String id : volumeId) {
                Optional<Volume> volume = volumeDao.findById(id);
                volumeGroupList.add(volume.get());
            }

        } else {
            volumeGroupList = (List<Volume>) volumeDao.findAll();
        }

        for (Volume nthVolume : volumeGroupList) {        	
        	String nthVolumeId = nthVolume.getId();
        	logger.info("EBC - Capturing end block for volume - " + nthVolumeId);

            fileVolumeBlockList = fileVolumeDao.findAllByIdVolumeId(nthVolumeId); // .stream().collect(Collectors.toList());
            HashMap<Integer, org.ishafoundation.dwaraapi.db.model.transactional.File> fileIdToFileObj = new LinkedHashMap<Integer, org.ishafoundation.dwaraapi.db.model.transactional.File>();
            ArtifactVolume av = null;
            for (int i = 0; i < fileVolumeBlockList.size(); i++) {
            	
            	FileVolume nthFileVolume = fileVolumeBlockList.get(i);
            	int fileId = nthFileVolume.getId().getFileId(); 
            	try {
                	File fileDBObj = fileIdToFileObj.get(fileId);
                	if(fileDBObj == null) {
                		fileIdToFileObj.clear();
                		fileDBObj = fileDao.findById(fileId).get();
                		int artifactId = fileDBObj.getArtifact().getId();
                		av = artifactVolumeDao.findByIdArtifactIdAndIdVolumeId(artifactId, nthVolumeId);
	            		List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileList = fileDao.findAllByArtifactId(artifactId);
	            		
	            		for (Iterator<org.ishafoundation.dwaraapi.db.model.transactional.File> iterator = artifactFileList.iterator(); iterator.hasNext();) {
	            			org.ishafoundation.dwaraapi.db.model.transactional.File nthFile = iterator.next();
	            			fileIdToFileObj.put(nthFile.getId(), nthFile);
	            		}
                	}
            		
	            	long fileArchiveBlock = nthFileVolume.getArchiveBlock();
	            	Integer fileHeaderBlocks = nthFileVolume.getHeaderBlocks();
	            	Integer vsb = nthFileVolume.getVolumeStartBlock();
	            	if(fileHeaderBlocks == null)
	            		fileHeaderBlocks = 3;
	            	
	            	long fileSize = fileDBObj.getSize();
	            	            	
	            	int archiveformatBlocksize = nthVolume.getArchiveformat().getBlocksize();
	            	int volumeBlockSize = nthVolume.getDetails().getBlocksize();
	            	double blockingFactor = TarBlockCalculatorUtil.getBlockingFactor(archiveformatBlocksize, volumeBlockSize);
	            	int volumeEndBlock = TarBlockCalculatorUtil.getFlooredFileVolumeEndBlock(fileArchiveBlock, fileHeaderBlocks, fileSize, archiveformatBlocksize, blockingFactor);
	            	Integer avVsb = av.getDetails().getStartVolumeBlock();
	            	nthFileVolume.setVolumeEndBlock(avVsb + volumeEndBlock);	            	
	            	logger.debug("EBC - filePathName - " + fileDBObj.getPathname() + " avVsb - " + avVsb + " fvsb - " + vsb + " fveb - " + volumeEndBlock + " cfveb - " + (avVsb + volumeEndBlock));
	            	fileVolumeDao.save(nthFileVolume);
            	}
            	catch (Exception e) {
					logger.error("EBC - Unable to captured end block for fileId - " + fileId, e);
				}

//                if (i == fileVolumeBlockList.size() - 1) {
//                    int fileBlockSize = (int) Math.ceil((double)fileList.get(fileVolumeBlockList.get(i).getId().getFileId()).getSize() / fileVolumeIterator.getDetails().getBlocksize());
//                    FileVolume fileVolume = fileVolumeBlockList.get(i);
//                    endBlock = fileVolume.getVolumeStartBlock() + (fileBlockSize - 1);
//                    fileVolume.setVolumeEndBlock(endBlock == NULL ? NULL : endBlock);
//                    fileVolumeDao.save(fileVolume);
//                } else {
//                    FileVolume fileVolume = fileVolumeBlockList.get(i);
//                    FileVolume fileVolume1 = fileVolumeBlockList.get(i + 1);
//
//                    if (fileVolume.getVolumeStartBlock() != NULL) {
//                        if (fileVolume.getVolumeStartBlock().equals(fileVolume1.getVolumeStartBlock())) {
//                            endBlock = fileVolume1.getVolumeStartBlock();
//                        } else {
//                            endBlock = fileVolume1.getVolumeStartBlock() - 1;
//                        }
//
//                        if (endBlock != fileVolume.getVolumeStartBlock()) {
//                            if (fileList.get(fileVolume.getId().getFileId()).getId() == fileVolume.getId().getFileId() && fileList.get(fileVolume.getId().getFileId()).getSize() != 0) {
//                                long fileBlockSize = (long) Math.ceil((double)fileList.get(fileVolume.getId().getFileId()).getSize() / fileVolumeIterator.getDetails().getBlocksize());
//                                if (fileBlockSize == (endBlock - fileVolume.getVolumeStartBlock()) - 3 || fileBlockSize == (endBlock - fileVolume.getVolumeStartBlock()) - 2 || fileBlockSize == (endBlock - fileVolume.getVolumeStartBlock()) + 1 || fileBlockSize == (endBlock - fileVolume.getVolumeStartBlock())) {
//                                    //logger.info("File Id : {" + fileVolume.getId().getFileId() + "} -- End Block and File Block size is equal.");
//                                } else if (fileBlockSize != (endBlock - fileVolume.getVolumeStartBlock()) + 2) {
//                                    logger.error("File Id : {" + fileVolume.getId().getFileId() +"} of VolumeId : {" + fileVolumeIterator.getId() + "} -- End Block is not done -- where -- End block difference with Start Block = {" + (endBlock - fileVolume.getVolumeStartBlock()) + "} -- and -- File Block Size = {" + fileBlockSize + "}");
//                                    endBlock = NULL;
//                                }
//                            }
//
//                        }
//
//                    } else {
//                        endBlock = NULL;
//                        logger.error("Volume Start Block is NULL for FileId: ", +fileVolume.getId().getFileId());
//                    }
//                    fileVolume.setVolumeEndBlock(endBlock == NULL ? NULL : endBlock);
//                    fileVolumeDao.save(fileVolume);
//                }
            }
        }
    }
}

