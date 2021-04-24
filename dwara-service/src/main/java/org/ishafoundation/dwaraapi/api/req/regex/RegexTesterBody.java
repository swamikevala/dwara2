package org.ishafoundation.dwaraapi.api.req.regex;

public class RegexTesterBody {
	
	private String regex;
	
	private String text;

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}