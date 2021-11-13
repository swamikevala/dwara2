package org.ishafoundation.dwaraapi.service;
import org.springframework.stereotype.Service;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;
@Service
public class EmailerService {

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
    }

