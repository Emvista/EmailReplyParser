package com.emvista.edlio.emailreplyparser;

import java.util.List;


public class FragmentDTO {
	private List<String> lines;
	private boolean isHidden = false;
	private boolean isSignature = false;
	private boolean isQuoted = false;
	
	public FragmentDTO() {
		super();
	}

	public List<String> getLines() {
		return lines;
	}

	public void setLines(List<String> lines) {
		this.lines = lines;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public boolean isSignature() {
		return isSignature;
	}

	public void setSignature(boolean isSignature) {
		this.isSignature = isSignature;
	}

	public boolean isQuoted() {
		return isQuoted;
	}

	public void setQuoted(boolean isQuoted) {
		this.isQuoted = isQuoted;
	}
	
	
	
}
