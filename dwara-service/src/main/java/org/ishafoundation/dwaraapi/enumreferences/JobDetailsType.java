package org.ishafoundation.dwaraapi.enumreferences;

public enum JobDetailsType {
	no, // no details needed
	placeholder, // flowelement as basis - generetes Job representation even for jobs not existing in DB
	grouped_placeholder, // groups placeholder jobs based on copies...
	vanilla; // plain jobs' details straight from DB 
}
