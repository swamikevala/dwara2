package org.ishafoundation.dwaraapi.scheduler;

import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TRestoreBucketDao;
import org.ishafoundation.dwaraapi.db.model.transactional.RestoreBucketFile;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.ishafoundation.dwaraapi.service.EmailerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

     RestTemplate restTemplate;

    public ScheduledEmailReader(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    @Scheduled(cron ="0 0/5 * * * ?")
    public void readEmail(){
        //hoe to find by approvestatus can't take input
           // String sendUrl= "http://localhost:9090/dwarahelper/sendEmail";
            //String readUrl ="http://localhost:9090/dwarahelper/readEmail";
         String sendUrl= "http://172.18.1.24:9090/dwarahelper/sendEmail";
        String readUrl ="http://172.18.1.24:9090/dwarahelper/readEmail";


        List<TRestoreBucket> tRestoreBucketfromDbs = tRestoreBucketDao.findByApprovalStatus("in_progress");
        //logger.info("Started email reading");
        for(TRestoreBucket tRestoreBucket : tRestoreBucketfromDbs){
        //logger.info("Reading for bucket : "+tRestoreBucket.getId());
            String readUrlTemplate= UriComponentsBuilder.fromHttpUrl(readUrl)
                    .queryParam("approverEmail" , tRestoreBucket.getApproverEmail() )
                    .queryParam("bucketId",tRestoreBucket.getId())
                    .encode()
                    .toUriString();
            ResponseEntity<String> response
                    = restTemplate.getForEntity( readUrlTemplate, String.class);
            String dateSent = response.getBody();
          // logger.info("found " +dateSent);
            if(!dateSent.equals("")){
             //   logger.info("Found" + dateSent);
                tRestoreBucket.setApprovalStatus("approved");
                tRestoreBucket.setApprovalDate(dateSent);
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
                String sendUrlTemplate= UriComponentsBuilder.fromHttpUrl(sendUrl)
                        .queryParam("concernedEmail" , requesterEmail )
                        .queryParam("subject","Need Approval for project: _"+tRestoreBucket.getId()+"_")
                        .queryParam("emailBody", emailBody)
                        .encode()
                        .toUriString();
                /*emailerService.setConcernedEmail(requesterEmail);
               // logger.info(requesterEmail);
                emailerService.setSubject("Need Approval for project: _"+tRestoreBucket.getId()+"_");
                emailerService.sendEmail(emailBody);*/
                ResponseEntity<String> response1
                        = restTemplate.getForEntity( sendUrlTemplate, String.class);
                }

        }

    }
}
