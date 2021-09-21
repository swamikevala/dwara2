package org.ishafoundation.dwaraapi.service;

import org.apache.commons.lang.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.sql.Types.NULL;


@Component
public class CaptureFileVolumeEndBlockService extends DwaraService {

    private static final Logger logger = LoggerFactory.getLogger(CaptureFileVolumeEndBlockService.class);

    @Autowired
    private DomainUtil domainUtil;

    @Autowired
    private VolumeDao volumeDao;

    public void fileVolumeEndBlock(String volumeId) {

        FileVolumeRepository<FileVolume> fileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domainUtil.getDefaultDomain());
        List<FileVolume> fileVolumeBlockList;
        List<Volume> volumeGroupList = null;
        Iterator<Volume> fileVolumeIterator;
        int endBlock;
        if(!StringUtils.isEmpty(volumeId)) {
            Volume volume = new Volume();
            volume.setId(volumeId);
            volumeGroupList.set(0, volume);

            fileVolumeIterator = volumeGroupList.stream().collect(Collectors.toList()).iterator();

        } else {
            volumeGroupList = (List<Volume>) volumeDao.findAll();
            fileVolumeIterator = volumeGroupList.stream().collect(Collectors.toList()).iterator();
        }


        FileRepository<File> fileRepository = domainUtil.getDomainSpecificFileRepository(domainUtil.getDefaultDomain());

        List<File> fileList = (List<File>) fileRepository.findAll();

        while (fileVolumeIterator.hasNext()) {
            fileVolumeBlockList = fileVolumeRepository.findAllByIdVolumeIdOrderByVolumeStartBlockAsc(fileVolumeIterator.next().getId()).stream().collect(Collectors.toList());
            for (int i = 0; i < fileVolumeBlockList.size(); i++) {

                if (i == fileVolumeBlockList.size()) {
                    long fileBlockSize = fileList.get(fileList.size()).getId() / fileVolumeIterator.next().getDetails().getBlocksize();
                    FileVolume fileVolume = fileVolumeBlockList.get(i);
                    endBlock = (int) (fileVolume.getVolumeStartBlock() + fileBlockSize);
                    fileVolume.setVolumeEndBlock(endBlock == NULL ? NULL : endBlock);
                    fileVolumeRepository.save(fileVolume);
                } else {
                    FileVolume fileVolume = fileVolumeBlockList.get(i);
                    FileVolume fileVolume1 = fileVolumeBlockList.get(i + 1);
                    if (fileVolume.getVolumeStartBlock() != NULL) {
                        if (fileVolume.getVolumeStartBlock().equals(fileVolume1.getVolumeStartBlock())) {
                            endBlock = fileVolume1.getVolumeStartBlock();
                        } else {
                            endBlock = fileVolume1.getVolumeStartBlock() - 1;
                        }

                        if (endBlock != fileVolume.getVolumeStartBlock()) {
                            for (File file : fileList) {
                                if (file.getId() == fileVolume.getId().getFileId()) {
                                    long fileBlockSize = file.getSize() / fileVolumeIterator.next().getDetails().getBlocksize();
                                    if (fileBlockSize == (endBlock - fileVolume.getVolumeStartBlock())) {
                                        logger.info("File Id : {" + fileVolume.getId().getFileId() + "} -- End Block and FileBlocksize is equal.");
                                    } else if (fileBlockSize != (endBlock - fileVolume.getVolumeStartBlock()) + 1) {
                                        endBlock = NULL;
                                        logger.error("File Id : {" + fileVolume.getId().getFileId() + "} -- End Block is not done properly.");
                                    }
                                    break;
                                }

                            }
                        }


                    } else {
                        endBlock = NULL;
                        logger.error("Volume Start Block is NULL for FileId: ", +fileVolume.getId().getFileId());
                    }
                    fileVolume.setVolumeEndBlock(endBlock == NULL ? NULL : endBlock);
                    fileVolumeRepository.save(fileVolume);
                }
            }

        }

    }
}

