package org.ishafoundation.dwaraapi.storage.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupedJobsCollection {
	private Set<Integer> priorityOrder;
	private Map<String, List<StorageJob>> volumeTag_volumeTagGroupedJobs;
	
	public Set<Integer> getPriorityOrder() {
		return priorityOrder;
	}
	public void setPriorityOrder(Set<Integer> priorityOrder) {
		this.priorityOrder = priorityOrder;
	}
	public Map<String, List<StorageJob>> getVolumeTag_volumeTagGroupedJobs() {
		return volumeTag_volumeTagGroupedJobs;
	}
	public void setVolumeTag_volumeTagGroupedJobs(Map<String, List<StorageJob>> volumeTag_volumeTagGroupedJobs) {
		this.volumeTag_volumeTagGroupedJobs = volumeTag_volumeTagGroupedJobs;
	}
}
