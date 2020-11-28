package org.ishafoundation.dwaraapi.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateAndTimeUtil {
	
	static Logger DnTlogger = LoggerFactory.getLogger(DateAndTimeUtil.class);
	
	// Weird Nodeum returns dates in different formats in different version upgrades. Not able to make out which format is being used consistently. So making use of both the formats to see if we can format it...
	private static SimpleDateFormat NODEUM_RESP_DATE_N_TIME_FORMAT1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat NODEUM_RESP_DATE_N_TIME_FORMAT2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public static Calendar getCalendarFromString(String dateStringToBeConverted, String dateFormat){
		Calendar calendarObj = null; // dont default it with Calendar.getInstance(); - If exception then it will mislead.
		
		try {
			SimpleDateFormat INPUT_DATE_N_TIME_FORMAT = new SimpleDateFormat(dateFormat);
			Date date = INPUT_DATE_N_TIME_FORMAT.parse(dateStringToBeConverted);
			calendarObj = Calendar.getInstance();
			calendarObj.setTime(date);
		} catch (Exception e) {
			DnTlogger.error("unable to format date - " + dateStringToBeConverted);
		}

		return calendarObj;
	}

	
	// TODO - Check on the timezone
	public static Calendar getDbCalendarFromNodeumResp(String dateToBeFormattedDbStyle){
		Calendar calendarObjForDb = null; // dont default it with Calendar.getInstance(); - If exception then it will mislead.
		
		try {
			Date date = getDbDateFromNodeumResp(dateToBeFormattedDbStyle);
			calendarObjForDb = Calendar.getInstance();
			calendarObjForDb.setTime(date);
		} catch (Exception e) {
			DnTlogger.error("unable to format date - " + dateToBeFormattedDbStyle);
		}

		return calendarObjForDb;
	}
	
	public static Calendar getDateForCatalogNamePrefix(String dateToBeFormattedDbStyle){
		String[] acceptedDatePatterns = {"MMM-yyyy", "MMM-yy"}; // make it configurable...
		Calendar calendarObj = null;
		for (int i = 0; i < acceptedDatePatterns.length; i++) {
			SimpleDateFormat format1 = new SimpleDateFormat(acceptedDatePatterns[i]);
 			
			try {
				Date date = format1.parse(dateToBeFormattedDbStyle);
				calendarObj = Calendar.getInstance();
				calendarObj.setTime(date);				
				return calendarObj;
			} catch (Exception e) {
				//DnTlogger.warn("unable to format date - " + dateToBeFormattedDbStyle + ", falling back to pattern2");
			}
		}
		return calendarObj;// if none of the pattern matches...
	}

	public static Date getDbDateFromNodeumResp(String dateToBeFormattedDbStyle){
		Date date = null; 
		
		try {
			//NODEUM_RESP_DATE_N_TIME_FORMAT1.setTimeZone(TimeZone.getTimeZone(timeZone));
			
			date = NODEUM_RESP_DATE_N_TIME_FORMAT1.parse(dateToBeFormattedDbStyle);

		} catch (Exception e) {
			DnTlogger.warn("unable to format date using Pattern 1 - falling back to  " + dateToBeFormattedDbStyle);
			try {
				//NODEUM_RESP_DATE_N_TIME_FORMAT2.setTimeZone(TimeZone.getTimeZone(timeZone));
				
				date = NODEUM_RESP_DATE_N_TIME_FORMAT2.parse(dateToBeFormattedDbStyle);

			} catch (Exception e1) {
				
				DnTlogger.error("unable to format date - " + dateToBeFormattedDbStyle, e1);
			}			
			
		}

		return date;
	}
	
	public static Duration getElapsedTime(String jobStartedTimestamp, String jobFinishedTimestamp, LocalDateTime currentDateTime) {
		DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
		DateTimeFormatter format2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		LocalDateTime jobStartedLocalDateTime = null;
		try{
			jobStartedLocalDateTime = LocalDateTime.parse(jobStartedTimestamp, format1);
		}catch (Exception e) {
			DnTlogger.warn("unable to format date using Pattern 1 - falling back to  " + jobStartedTimestamp);
			try {
				jobStartedLocalDateTime = LocalDateTime.parse(jobStartedTimestamp, format2);
			}
			catch (Exception e1) {
				DnTlogger.error("unable to format date - " + jobStartedTimestamp, e1);
			}
		}
		
		//LocalDateTime jobFinishedLocalDateTime = null;
		LocalDateTime endLocalDateTime = null;
		if(jobFinishedTimestamp != null) {
			try{
				endLocalDateTime = LocalDateTime.parse(jobFinishedTimestamp, format1);
			}catch (Exception e) {
				DnTlogger.warn("unable to format date using Pattern 1 - falling back to  " + jobFinishedTimestamp);
				try {
					endLocalDateTime = LocalDateTime.parse(jobFinishedTimestamp, format2);
				}
				catch (Exception e1) {
					DnTlogger.error("unable to format date - " + jobFinishedTimestamp, e1);
				}
			}			
		}
		else{ // means job is not finished yet and having "job_finished": null, ...
			endLocalDateTime = currentDateTime;
		}

		Duration duration = Duration.between(endLocalDateTime, jobStartedLocalDateTime);
		return duration;
	}
	
	public static void main(String[] args) throws Exception {
		Pattern datePattern = Pattern.compile("_([0-9]{1,2}(-[0-9]{1,2})?-([A-Za-z]{3})-([0-9]{2,4}))(_)?");
		
		List<String> mediaLibraryFolderNamesList = FileUtils.readLines(new File("C:\\Users\\prakash\\src-code\\dwara-api-development\\dwara-api\\src\\main\\resources\\testcases\\MediaLibraryDirectoryNamesSamples.txt"));
		
		for (Iterator<String> iterator = mediaLibraryFolderNamesList.iterator(); iterator.hasNext();) {
			try {
				String mediaLibraryFolderName = (String) iterator.next();
				Matcher m = datePattern.matcher(mediaLibraryFolderName); 	
				 
				if(m.find()) {
					String eventDate = m.group(1);
					String month = m.group(3);
					String year = m.group(4);
				
			//		String[] mediaLibraryFolderNameParts = mediaLibraryFolderName.split("_"); 
			//		String eventDate = mediaLibraryFolderNameParts[3];
					
					String catalogName = "NO_DATE/" + mediaLibraryFolderName;
					if(eventDate.contains("-")) {
						Calendar formattedDate = DateAndTimeUtil.getDateForCatalogNamePrefix(month + "-" + year);
						if(formattedDate != null)
							catalogName = formattedDate.get(Calendar.YEAR) + "/" + (formattedDate.get(Calendar.MONTH) + 1) + "/" + mediaLibraryFolderName;
					}
			
					System.out.println(catalogName);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
