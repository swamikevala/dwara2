package org.isha.dwaraimport;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JacksonXmlRootElement(localName="Artifact")
@Setter
@Getter
public class Artifact {
	
	@JacksonXmlProperty(isAttribute=true, localName="startBlock")
	private String startblock; // archive start block
	@JacksonXmlProperty(isAttribute=true, localName="endBlock")
	private String endblock; // archive end block
	@JacksonXmlProperty(isAttribute = true, localName="artifactclassUid")
	private String artifactclassuid;
	@JacksonXmlProperty(isAttribute = true, localName="sequenceCode")
	private String sequencecode;
	@JacksonXmlProperty(isAttribute = true, localName="name")
	private String name;
	@JacksonXmlProperty(localName="File")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<File> file;
	

}