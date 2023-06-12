package org.ishafoundation.dwaraapi.utils;

import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SMSUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(SMSUtil.class);
	
	public static void sendSMS(String commaSeparatedMobileNos, String messagePart) {
		
		String templateId = "1007882215251861110";
		String smsTemplate = "Namaskaram Dwara alert %s Pranam Isha Volunteer";
		
		sendSMS(commaSeparatedMobileNos, templateId, String.format(smsTemplate, messagePart));
	}

	public static void sendSMS(String commaSeparatedMobileNos, String templateId, String message) {
		try {
			
			logger.trace(commaSeparatedMobileNos + ":" + templateId + ":" + message);
			String apiKey = "hPCbVjHmC0KpYNH1ReE3xw";
			String senderId = "ISHAAR";
			String url = "http://sms.vstcbe.com/api/mt/SendSMS"
						+ "?APIKey=" + apiKey
						+ "&senderid=" + senderId
						+ "&channel=Trans"
						+ "&route=3"
						+ "&flashsms=0"
						+ "&DCS=0"
						+ "&dlttemplateid=" + templateId
						+ "&number=" + commaSeparatedMobileNos
						+ "&text=" + URLEncoder.encode(message,"UTF-8");
			
			logger.trace("url - " + url);
			String respBody = HttpClientUtil.getIt(url);
			logger.trace("respBody - " + respBody);
		} catch (Exception e) {
			logger.error("Unable to send sms - " + e.getMessage(), e);
		}
	}
	
	public static void main(String[] args) {
		String commaSeparatedMobileNos = "9566476577,9489045149";
		String templateId = "1007882215251861110";
		String smsTemplate = "Namaskaram Dwara alert %s Pranam Isha Volunteer";
		String message = String.format(smsTemplate, "A99999L1,B99999L1,C99999L1");
		
		sendSMS(commaSeparatedMobileNos, templateId, message);
	}

}
