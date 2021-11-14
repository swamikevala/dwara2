package org.ishafoundation.dwaraapi.service;
import com.squareup.okhttp.*;
import org.apache.commons.lang.StringUtils;
import org.ishafoundation.dwaraapi.scheduler.ScheduledStatusUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.*;
import javax.mail.Authenticator;
import javax.mail.internet.*;
import javax.mail.search.FlagTerm;

@Service
public class EmailerService {
    private static final Logger logger = LoggerFactory.getLogger(EmailerService.class);
        private  final String appEmail  = "private.archives.requests@gmail.com";
        private   String concernedEmail ;
        // generate password - https://support.google.com/mail/answer/185833?hl=en
        private  final String appEmailPassword = "zxmpasyyllvqgcum";
        private  String subject;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getConcernedEmail() {
        return concernedEmail;
    }

    public void setConcernedEmail(String concernedEmail) {
        this.concernedEmail = concernedEmail;
    }

    public  void sendEmail(String mailBody) {

            String to = this.concernedEmail;
            String from = this.appEmail;
            String host = "smtp.gmail.com";

// Get system properties
            Properties properties = System.getProperties();

// Setup mail server
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

// Get the Session object.// and pass username and password
            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {

                    return new PasswordAuthentication(appEmail, appEmailPassword);

                }

            });

// Used to debug SMTP issues
// session.setDebug(true);

            try {
// Create a default MimeMessage object.
                MimeMessage message = new MimeMessage(session);

// Set From: header field of the header.
                message.setFrom(new InternetAddress(from));

// Set To: header field of the header.
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

// Set Subject: header field
                message.setSubject(this.subject);

// Now set the actual message
               // message.setText(errorMsg);

                 message.setContent(mailBody, "text/html");


                System.out.println("sending...");
// Send message
                Transport.send(message);
                System.out.println("Sent message successfully....");
            } catch (MessagingException mex) {
                System.out.println(mex.getMessage());
            }

        }
    public boolean read(String approver_email, String bucketId) {

        Properties props = new Properties();

        try {
            props.load(new FileInputStream(new File("C:\\Users\\aumrit.sarangi.sp21\\git\\dwara2restoreBucket\\dwara-service\\src\\main\\resources\\smtp.properties")));
            Session session = Session.getDefaultInstance(props, null);
            String filteredMsg = "";
            String originalMsg = "";

            Store store = session.getStore("imaps");
            //change
            store.connect("smtp.gmail.com", "private.archives.requests@gmail.com", "zxmpasyyllvqgcum");

            Folder inbox = store.getFolder("inbox");
            // update the mail status as read
            inbox.open(Folder.READ_WRITE); // READ_ONLY
            int messageCountTotal = inbox.getMessageCount();

            //check only un-read messages
            Flags seen = new Flags(Flags.Flag.SEEN);// RECENT
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
            Message[] messages = inbox.search(unseenFlagTerm);
            int messageCount = messages.length;
            logger.info("Total Messages:- " + messageCount);
            // inbox.getMessages();

            System.out.println("-------------- Program scans for messages every 10 second ----------------");
            boolean found = false;
            for (int i = 0; i < messageCount; i++) {
                Message message = messages[i];
                //String extractedASD = extractASDFromSubject(message.getSubject());
                if (checkBucketIdInSubject(message.getSubject(),bucketId) && message.getFrom().equals(approver_email)) {

                logger.info("Inside message");

                originalMsg = getTextFromMessage(message).trim();
                List<String> originalMsgArray =Arrays.asList( originalMsg.split("\n"));
                // not to consider old mails
                   List<String> filterdMsg = new ArrayList<>();
                    for (String line: originalMsgArray) {
                        line=line.trim();
                        if(!line.equals("")){
                            filterdMsg.add(line);
                        }
                    }


                for(String m:filterdMsg) {
                    if (StringUtils.indexOfAny(m.toLowerCase(), new String[] { "approve", "approved" }) != -1) {
                        if (StringUtils.indexOfAny(m.toLowerCase(), new String[] { "what", "why", "how", "when", "who", "not", "don’t" }) == -1) {
                            found =true;
                        }
                    }
                }
                if(found) {
                    inbox.close(true);
                    store.close();
                    logger.info("inside found");
                    return found;
                }

                System.out.println("Mail Subject:- " + message.getSubject());
               // changeJiraStatus(extractedASD);
                System.out.println("---------------------------------");

            }}

            inbox.close(true);
            store.close();
            return found;

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        BodyPart bodyPart = mimeMultipart.getBodyPart(0);
        if (bodyPart.getContent() instanceof MimeMultipart) {
            result = result + ((MimeMultipart) bodyPart.getContent()).getBodyPart(0).getContent();
        }
        return result + bodyPart.getContent();
        /*
         * for (int i = count-1; i < count; i++) { BodyPart bodyPart =
         * mimeMultipart.getBodyPart(i); if (bodyPart.isMimeType("text/plain")) { result
         * = result + "\n" + bodyPart.getContent(); break; // without break same text
         * appears twice in my tests } else if (bodyPart.isMimeType("text/html")) {
         * String html = (String) bodyPart.getContent(); result = result + "\n" +
         * org.jsoup.Jsoup.parse(html).text(); break; } else if (bodyPart.getContent()
         * instanceof MimeMultipart) { result = result +
         * getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()); } } return
         * result;
         */
    }

    private void changeJiraStatus(String YourKey) {
//		String YourKey = "ASD-7894";
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"transition\":{\"id\":\"1001\"},\"status\":\"Closed\"}");
        // RequestBody body = RequestBody.create(mediaType, "{\"transition\":{\"id\":\"1031\"},\"status\":\"Re-Open\"}");
        Request request = new Request.Builder()
                .url("https://servicedesk.isha.in/rest/api/2/issue/" + YourKey + "/transitions").method("POST", body)
                .addHeader("X-ExperimentalApi", "opt-in").addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Basic YXJjaGl2ZXMuc2NyaXB0OkB1dG9tYXRl")
                .addHeader("Cookie",
                        "JSESSIONID=D1718948344FF0ECD35C322B44BC9976; atlassian.xsrf.token=BM7Y-45G6-BET8-426R_66db1591acfc526d95fa5a3b84685514921ad199_lin")
                .build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println(
                    response.isSuccessful() ? "Sucessfully changed jira status" : "Failed to change jira status");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean checkBucketIdInSubject(String subject , String bucketId) {
        List<String> subjectArray = Arrays.asList( subject.split("_"));
        if (subjectArray.contains(bucketId))
            return true;
        else
            return false;
    }

}


