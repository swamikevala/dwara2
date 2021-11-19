package org.ishafoundation.dwaraapi.scheduler;

import org.ishafoundation.dwaraapi.api.req.restore.RestoreUserRequest;
import org.ishafoundation.dwaraapi.api.resp.restore.RestoreResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestApprovalDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TRestoreBucketDao;
import org.ishafoundation.dwaraapi.db.model.transactional.RequestApproval;
import org.ishafoundation.dwaraapi.db.model.transactional.RestoreBucketFile;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Priority;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.FileService;
import org.ishafoundation.dwaraapi.service.RestoreBucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

//@Component
public class ScheduledApprovedBucketRestorer {

    @Autowired
    private TRestoreBucketDao tRestoreBucketDao;
    @Autowired
    private RestoreBucketService restoreBucketService;
    @Autowired
    private FileService fileService;
    @Autowired
    private RequestApprovalDao requestApprovalDao;

    @Scheduled(cron ="0 0/5 * * * ?")
    public void restoreApproved() {
        List<TRestoreBucket> tRestoreBucketList = tRestoreBucketDao.findByApprovalStatus("approved");
        for (TRestoreBucket tRestoreBucket : tRestoreBucketList) {
            RestoreUserRequest restoreUserRequest = new RestoreUserRequest();
            restoreUserRequest.setCopy(1);

            Priority priority = Priority.valueOf(tRestoreBucket.getPriority().toLowerCase());
            restoreUserRequest.setPriority(priority);
            restoreUserRequest.setDestinationPath(tRestoreBucket.getDestinationPath());
            restoreUserRequest.setOutputFolder(tRestoreBucket.getId());
            List<Integer> fileIds = new ArrayList<>();
            for (RestoreBucketFile file : tRestoreBucket.getDetails()) {
                fileIds.add(file.getFileID());
            }
            restoreUserRequest.setFileIds(fileIds);
            System.out.println(fileIds);
            restoreUserRequest.setFlow("restore-flow");
            RestoreResponse restoreResponse;
            try {
                restoreResponse = fileService.restore(restoreUserRequest, Action.restore_process, restoreUserRequest.getFlow());
                if (!Objects.isNull(restoreResponse) && restoreResponse.getFiles().size() == restoreUserRequest.getFileIds().size()) {
                    RequestApproval requestApproval = new RequestApproval(tRestoreBucket);
                    requestApprovalDao.save(requestApproval);
                    restoreBucketService.deleteBucket(tRestoreBucket.getId());
                }
            } catch (Exception e) {
                String errorMsg = "Unable to restore - " + e.getMessage();

                if (e instanceof DwaraException)
                    throw (DwaraException) e;
                else
                    throw new DwaraException(errorMsg, null);
            }

        }
    }

}
