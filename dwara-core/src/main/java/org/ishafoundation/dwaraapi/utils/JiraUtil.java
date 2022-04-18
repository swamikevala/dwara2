package org.ishafoundation.dwaraapi.utils;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.enumreferences.JiraTransition;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.Filter;

@Component
public class JiraUtil {
	
	@Autowired
	private Configuration configuration;
	
	private static final Logger logger = LoggerFactory.getLogger(JiraUtil.class);

	/*
		GET - https://servicedesk.isha.in/rest/api/2/issue/VP-22641/transitions
		
		POST - https://servicedesk.isha.in/rest/api/2/issue/VP-22641/transitions
			
		{
		    "transition": {
		        "id": "585"
		    },
		    "fields" : {
		        "customfield_69300" :
		        "Output/Destination_Directory"
		    }
		}
	*/
	public static boolean updateJiraWorkflow(String jiraTicketId, JiraTransition transition, String outputFolder) {
		
		if(jiraTicketId == null) {
			logger.trace("Jira Ticket id is null. So cant update jira...");
			return false;
		}
		
		String transitionUrl = "https://servicedesk.isha.in/rest/api/2/issue/" + jiraTicketId + "/transitions";
		
		String jiraUserName = "archives.script";
		String jiraPwd = "@utomate";
		
		
		String encodedCreds = Base64.getEncoder().encodeToString((jiraUserName + ":" + jiraPwd).getBytes());
		String authHeader = "Basic " + encodedCreds;
		try {
			String jiraTransitions = HttpClientUtil.getIt(transitionUrl, authHeader);

			Filter transitionName = Filter.filter(Criteria.where("name").eq(transition.getTransitionAsInJira()));

			List<Map<String, Object>> transitionBlock = JsonPathUtil.getArray(jiraTransitions, "transitions[?]", transitionName);
			Integer transtionId = null;
			if(transitionBlock.size() > 0)
				transtionId = Integer.parseInt((String) transitionBlock.get(0).get("id"));
			
			
			
			JSONObject payloadJson = new JSONObject();			
			if(transtionId != null) {
				JSONObject idObj = new JSONObject();
				idObj.put("id", transtionId);
				
				payloadJson.put("transition", idObj);

				if(transition == JiraTransition.waiting_for_footage) {
					JSONObject customFieldObj = new JSONObject();
					customFieldObj.put("customfield_69300", outputFolder);

					payloadJson.put("fields", customFieldObj);
				}
				
				HttpClientUtil.postIt(transitionUrl, authHeader, payloadJson.toString()); // Anything other than 200s throws error...
				
				logger.info(jiraTicketId + " updated successfully");
			}
			else {
				logger.warn(jiraTicketId + " doesnt support the transition" + transition.getTransitionAsInJira());
			}
		} catch (Exception e) {
			logger.error("Unable to update Jira for " + jiraTicketId + "::" + e.getMessage(), e);
		}

		
		return false;
		
	}
	
	public static void main(String[] args) {
		// updateJiraWorkflow("VP-22611", JiraTransition.waiting_for_footage, "abcde");
		
		updateJiraWorkflow("VP-22611", JiraTransition.footage_request_closed, null);
		
		updateJiraWorkflow("VP-22641", JiraTransition.waiting_for_footage, "abcdefghi");
	}

}
