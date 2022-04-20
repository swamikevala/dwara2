package org.ishafoundation.dwaraapi.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.autoloader.Tape;
import org.ishafoundation.dwaraapi.service.VolumeService;
import org.ishafoundation.dwaraapi.utils.SMSUtil;
import org.ishafoundation.dwaraapi.utils.TapeUsageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledRestoreTapesNotifier {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduledRestoreTapesNotifier.class);
	
	@Autowired
	private VolumeService volumeService;
	
	@Value("${restoreTapesNotifier.mobileNos}")
	private String commaSeparatedMobileNos;
	
	@Scheduled(cron = "${scheduler.restoreTapesNotifier.cronExpression}")
	public void notifyOps(){
		logger.info("***** Notifying Ops team on tapes to be loaded for restores *****");
		
		String templateId = "1007882215251861110";
		String smsTemplate = "Namaskaram Dwara alert %s Pranam Isha Volunteer";
		try {
			List<String> tapeBarcodeList = new ArrayList<String>();
			List<Tape> tapeList = volumeService.handleTapes();
			for (Tape nthTape : tapeList) {
				if(nthTape.getAction().equals("restore") && nthTape.getUsageStatus() == TapeUsageStatus.job_queued && nthTape.getAddress() == 0) {
					tapeBarcodeList.add(nthTape.getBarcode());
				}
			}

			int cnt = 0;
			StringBuilder sb = new StringBuilder();
			for (String nthTapeBarcode : tapeBarcodeList) {
				if(cnt > 0)
					sb.append(",");
				sb.append(nthTapeBarcode);
				
				cnt += 1;
				if(cnt == 3) {
					// cant take more than 3 in a msg...
					SMSUtil.sendSMS(commaSeparatedMobileNos, templateId, String.format(smsTemplate, sb.toString()));
					// reset 
					cnt = 0;
					sb = new StringBuilder();
				}
			}
			
			if(cnt > 0 && cnt < 3)
				SMSUtil.sendSMS(commaSeparatedMobileNos, templateId, String.format(smsTemplate, sb.toString()));
			
		} catch (Exception e) {
			logger.error("Unable to notify", e);
		}
	}
}