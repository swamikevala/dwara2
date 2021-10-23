package org.ishafoundation.dwaraapi.api.resp.dwarahover;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Builder
public class DwaraHoverFileListCount {
	private long TotalCount;
	private List<DwaraHoverFileList> DwaraHoverFileList;

	public static Optional<List<DwaraHoverFileListCount>> build(List<DwaraHoverFileList> gotResponse, long totalCount) {
		if (gotResponse == null) {
			return Optional.empty();
		}

		List<DwaraHoverFileListCount> dwaraHoverFileLists = new ArrayList<>();
		DwaraHoverFileListCount response = DwaraHoverFileListCount.builder().TotalCount(totalCount).DwaraHoverFileList(gotResponse).build();

		dwaraHoverFileLists.add(response);

		return Optional.of(dwaraHoverFileLists);
	}

}
