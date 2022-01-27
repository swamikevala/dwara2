package org.ishafoundation.dwaraapi.scheduler;

import org.ishafoundation.dwaraapi.api.req.restore.RestoreUserRequest;
import org.ishafoundation.dwaraapi.api.resp.restore.RestoreResponse;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestApprovalDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TRestoreBucketDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.RequestApproval;
import org.ishafoundation.dwaraapi.db.model.transactional.RestoreBucketFile;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Priority;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.FileService;
import org.ishafoundation.dwaraapi.service.RestoreBucketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class ScheduledApprovedBucketRestorer {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledApprovedBucketRestorer.class);
    @Autowired
    private TRestoreBucketDao tRestoreBucketDao;
    @Autowired
    private RestoreBucketService restoreBucketService;
    @Autowired
    private FileService fileService;
    @Autowired
    private RequestApprovalDao requestApprovalDao;
@Autowired
private UserDao userDao;
    @Scheduled(cron ="0 0/5 * * * ?")
    public void restoreApproved() {
        List<TRestoreBucket> tRestoreBucketList = tRestoreBucketDao.findByApprovalStatus("approved");
        //System.out.println("Started restore Approver");
        for (TRestoreBucket tRestoreBucket : tRestoreBucketList) {
            logger.info("Start restoring approved bucket " + tRestoreBucket.getId());
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
           // System.out.println("File Id " +restoreUserRequest.getFileIds());
            restoreUserRequest.setFlow("restore-flow");
           // System.out.println("restoreUserRequest.Copy " +restoreUserRequest.getCopy());
            //System.out.println(restoreUserRequest.toString());
           // System.out.println("restoreUserRequest.DestinationPAth " +restoreUserRequest.getDestinationPath());
            //System.out.println("restoreUserRequest.OutputFolder " +restoreUserRequest.getOutputFolder());

            RestoreResponse restoreResponse;
            try {
                User user = userDao.findById(1).get();
                restoreResponse = fileService.restore(restoreUserRequest, Action.restore, restoreUserRequest.getFlow(),user);
                if (!Objects.isNull(restoreResponse) && restoreResponse.getFiles().size() == restoreUserRequest.getFileIds().size()) {
                    RequestApproval requestApproval = new RequestApproval(tRestoreBucket);
                    requestApprovalDao.save(requestApproval);
                    restoreBucketService.deleteBucket(tRestoreBucket.getId());
                }
            } catch (Exception e) {
                logger.error("Restore bucket " + tRestoreBucket.getId() + " failed");
                String errorMsg = "Unable to restore - " + e.getMessage();

                if (e instanceof DwaraException)
                    throw (DwaraException) e;
                else
                    throw new DwaraException(errorMsg, null);
            }

        }
    }

}
