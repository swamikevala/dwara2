package org.ishafoundation.dwaraapi.scheduler;

import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TRestoreBucketDao;
import org.ishafoundation.dwaraapi.db.model.transactional.RestoreBucketFile;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.ishafoundation.dwaraapi.service.EmailerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ScheduledEmailReader {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledEmailReader.class);
    @Autowired
    TRestoreBucketDao tRestoreBucketDao;
    @Autowired
    EmailerService emailerService;
    @Autowired
    UserDao userDao;

    @Scheduled(cron ="5 * * * * ? ")
    public void readEmail(){
        //hoe to find by approvestatus can't take input
        List<TRestoreBucket> tRestoreBucketfromDbs = tRestoreBucketDao.findByApprovalStatus("in_progress");
        logger.info("Started email reading");
        for(TRestoreBucket tRestoreBucket : tRestoreBucketfromDbs){
        logger.info("Reading for bucket : "+tRestoreBucket.getId());
            boolean found = emailerService.read(tRestoreBucket.getApproverEmail(),tRestoreBucket.getId());
            if(found){
                tRestoreBucket.setApprovalStatus("approved");
                tRestoreBucketDao.save(tRestoreBucket);
                String emailBody = "<p>Namaskaram</p>";
                emailBody += "<p>The following folders have been approved for the requested bucket "+ tRestoreBucket.getId()+"</p>";
                List<String> fileName = new ArrayList<>();
                for (RestoreBucketFile file: tRestoreBucket.getDetails()) {
                    emailBody +="<div> "+ file.getFilePathName()  +"</div>";
                }
                emailBody +="<p>Pranam , </p>";
                emailBody +="<p>Your favourite <b>Dwara</b> Application </p>";
                int requsterId= tRestoreBucket.getRequestedBy();
                String requesterEmail = userDao.findById(requsterId).get().getEmail();
                emailerService.setConcernedEmail(requesterEmail);
                System.out.println(requesterEmail);
                emailerService.setSubject("Need Approval for project: _"+tRestoreBucket.getId()+"_");
                emailerService.sendEmail(emailBody);
            }

        }

    }
}
