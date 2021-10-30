package org.ishafoundation.dwaraapi.api.resp.dwarahover;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class DwaraHoverFileListDTO {

	private String pathName;
	private long size;
	private int id;
	private String artifactClass_id;
	private List<String> proxyPathName;

}
