package com.emvista.emailreplyparser;

import com.emvista.emailreplyparser.model.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailReplyParser {


	@Autowired
	EmailParser parser;
	
	public Email read(String emailText) {
		if (emailText == null)
			emailText = "";
		return parser.parse(emailText);
	}
	
	


	public String parseReply(String emailText) {
		return read(emailText).getVisibleText();
	}

}
