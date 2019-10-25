package com.edlio.emailreplyparser;

public class Fragment {
	private String content;
	private boolean isHidden;
	private boolean isSignature;
	private boolean isQuoted;
	
	public Fragment(String content, boolean isHidden, boolean isSignature, boolean isQuoted) {
		this.content 		= content;
		this.isHidden 		= isHidden;
		this.isSignature 	= isSignature;
		this.isQuoted 		= isQuoted;
	}

	public String getContent() {
		return content;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public boolean isSignature() {
		return isSignature;
	}

	public boolean isQuoted() {
		return isQuoted;
	}
	
	public boolean isEmpty() {
		return "".equals(this.content.replace("\n", ""));
	}
	
	public void reverseSignature() {
		if(this.isSignature)
			this.isSignature = false;
		else
			this.isSignature = true;
	}
}
