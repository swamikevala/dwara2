package org.ishafoundation.dwaraapi.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="ffmpeg")
public class FfmpegThreadConfiguration {
	
	private FfmpegThreadProps videoProxyLowGen;

	private FfmpegThreadProps videoDigi2020PreservationGen;
	
	private FfmpegThreadProps videoDigi2020QcGen;

	public FfmpegThreadProps getVideoProxyLowGen() {
		return videoProxyLowGen;
	}

	public void setVideoProxyLowGen(FfmpegThreadProps videoProxyLowGen) {
		this.videoProxyLowGen = videoProxyLowGen;
	}

	public FfmpegThreadProps getVideoDigi2020PreservationGen() {
		return videoDigi2020PreservationGen;
	}

	public void setVideoDigi2020PreservationGen(FfmpegThreadProps videoDigi2020PreservationGen) {
		this.videoDigi2020PreservationGen = videoDigi2020PreservationGen;
	}

	public FfmpegThreadProps getVideoDigi2020QcGen() {
		return videoDigi2020QcGen;
	}

	public void setVideoDigi2020QcGen(FfmpegThreadProps videoDigi2020QcGen) {
		this.videoDigi2020QcGen = videoDigi2020QcGen;
	}

}
