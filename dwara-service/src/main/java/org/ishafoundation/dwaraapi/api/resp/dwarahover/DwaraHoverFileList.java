package org.ishafoundation.dwaraapi.api.resp.dwarahover;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@ToString
@Builder
public class DwaraHoverFileList {

	private int id;
	private String path_name;
	private String short_path_name;
	private Long size;
	private String size_in;
	private String artifact_class_id;
	private List<String> proxy_path_name;
	private List<DwaraHoverTranscriptListDTO> transcripts;


	public static Optional<List<DwaraHoverFileList>> build(List<DwaraHoverFileListDTO> gotResponse) {
		if (gotResponse == null) {
			return Optional.empty();
		}

		List<DwaraHoverFileList> dwaraHoverFileLists = new ArrayList<>();

		gotResponse.forEach(hoverList -> {
			DwaraHoverFileList response = DwaraHoverFileList.builder()
					.id(hoverList.getId() == 0 ? 0 : hoverList.getId())
					.path_name(hoverList.getPathName())
					.short_path_name(hoverList.getPathName().contains("/") ? StringUtils.substringBefore(hoverList.getPathName(), "/") + "..." + hoverList.getPathName().substring(hoverList.getPathName().length() - 10) : StringUtils.substring(hoverList.getPathName(), 0, 40) + "..." + hoverList.getPathName().substring(hoverList.getPathName().length() - 10))
					.size(hoverList.getSize())
					.size_in("B")
					.artifact_class_id(!StringUtils.isEmpty(hoverList.getArtifactClass_id()) ? hoverList.getArtifactClass_id() : null)
					.proxy_path_name(hoverList.getProxyPathName())
					.transcripts(hoverList.getTranscripts())
					.build();


			dwaraHoverFileLists.add(response);
		});

		return Optional.of(dwaraHoverFileLists);
	}


}
