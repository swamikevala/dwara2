package org.ishafoundation.dwaraapi.api.resp.dwarahover;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class DwaraHoverTranscriptListDTO {

	private String title;
	private String link;
	@JsonIgnore
	private String searchQuery;


}
