package org.ishafoundation.dwaraapi.api.resp.dwarahover;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class DwaraHoverFileList {

	private int id;
	private String path_name;
	private String short_path_name;
	private String date;
	private Long size;
	private String size_in;
	private String artifact_class_id;
	private List<String> proxy_path_name;
	private List<DwaraHoverTranscriptListDTO> transcripts;

//	private static SimpleDateFormat artifactEventDateFormat = new SimpleDateFormat("dd-mmm-yyyy");
	private static Pattern artifactEventDatePattern = Pattern.compile("_(([0-9]{2})-([A-Z][a-z]{2})-([0-9]{4}))");
	
	public static Optional<List<DwaraHoverFileList>> build(List<DwaraHoverFileListDTO> gotResponse) {
		if (gotResponse == null) {
			return Optional.empty();
		}

		List<DwaraHoverFileList> dwaraHoverFileLists = new ArrayList<>();

		gotResponse.forEach(hoverList -> {
			String filePathname = hoverList.getPathName();
			String artifactName = filePathname.contains("/") ? StringUtils.substringBefore(filePathname, "/") : filePathname;
//			Matcher m = artifactEventDateFormat.matcher(fileName);
			Matcher m = artifactEventDatePattern.matcher(artifactName);
			String artifactEventDate = m.find() ? m.group(1) : "";		
			DwaraHoverFileList response = DwaraHoverFileList.builder()
					.id(hoverList.getId() == 0 ? 0 : hoverList.getId())
					.path_name(filePathname)
					.short_path_name(filePathname.contains("/") ? StringUtils.substringBefore(filePathname, "/") + "..." + filePathname.substring(filePathname.length() - 10) : StringUtils.substring(filePathname, 0, 40) + "..." + filePathname.substring(filePathname.length() - 10))
					.size(hoverList.getSize())
					.size_in("B")
					.artifact_class_id(!StringUtils.isEmpty(hoverList.getArtifactClass_id()) ? hoverList.getArtifactClass_id() : null)
					.proxy_path_name(hoverList.getProxyPathName())
					.date(artifactEventDate)
					.transcripts(hoverList.getTranscripts())
					.build();


			dwaraHoverFileLists.add(response);
		});

		return Optional.of(dwaraHoverFileLists);
	}


}
