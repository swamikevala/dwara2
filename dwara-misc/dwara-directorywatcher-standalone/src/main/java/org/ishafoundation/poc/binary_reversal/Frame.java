package org.ishafoundation.poc.binary_reversal;

import java.util.ArrayList;
import java.util.List;

public class Frame {
	
	private int timecode;
	
	private VideoTrack videoTrack;
	
	private List<AudioTrack> audioTracks = new ArrayList<AudioTrack>();

	public int getTimecode() {
		return timecode;
	}

	public void setTimecode(int timecode) {
		this.timecode = timecode;
	}

	public VideoTrack getVideoTrack() {
		return videoTrack;
	}

	public void setVideoTrack(VideoTrack videoTrack) {
		this.videoTrack = videoTrack;
	}

	public List<AudioTrack> getAudioTracks() {
		return audioTracks;
	}

	public void setAudioTracks(List<AudioTrack> audioTracks) {
		this.audioTracks = audioTracks;
	}
}
