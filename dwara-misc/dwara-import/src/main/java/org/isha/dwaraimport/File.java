package org.isha.dwaraimport;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Getter;
import lombok.Setter;

@JacksonXmlRootElement(localName="File")
@Setter
@Getter
public class File {
	
	@JacksonXmlProperty(isAttribute=true, localName="volumeStartBlock")
	private String volumeStartBlock; // volume start block
	@JacksonXmlProperty(isAttribute=true, localName="volumeEndBlock")
	private String volumeEndBlock; // volume start block
	@JacksonXmlProperty(isAttribute=true, localName="archiveBlock")
	private String archiveblock; // archive start block
	@JacksonXmlProperty(isAttribute=true, localName="size") 
	private String size;
	@JacksonXmlProperty(isAttribute=true, localName="checksum")
	private String checksum;
	@JacksonXmlProperty(isAttribute=true, localName="directory")
	private String directory;
	@JacksonXmlText
	private String name;

}
