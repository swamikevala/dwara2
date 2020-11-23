package org.ishafoundation.digitization.preservation;

public class Cues {

	private String timestamp=null;
	private int clusterPosition = 0;

	
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public int getClusterPosition() {
		return clusterPosition;
	}
	public void setClusterPosition(int clusterPosition) {
		this.clusterPosition = clusterPosition;
	}
	@Override
	public String toString() {
		return getTimestamp() + ":" + getClusterPosition();
	}
}
