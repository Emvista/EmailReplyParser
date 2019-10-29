package com.edlio.emailreplyparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;



public class EmailParser {
	
	static final Pattern FR_SIG_PATTERN = Pattern.compile( "((^Envoyé depuis mon (\\s*\\w+){1,3}$)|(^-\\w|^\\s?__|^\\s?--|^\u2013|^\u2014))", Pattern.DOTALL);
	static final Pattern FR_SIG_PATTERN2 = Pattern.compile( "^Téléchargez Outlook pour iOS.*", Pattern.DOTALL);
	static final Pattern EN_SIG_PATTERN = Pattern.compile( "((^Sent from my (\\s*\\w+){1,3}$)|(^-\\w|^\\s?__|^\\s?--|^\u2013|^\u2014))", Pattern.DOTALL);
	static final Pattern QUOTE_PATTERN = Pattern.compile("(^>+)", Pattern.DOTALL);
	private static List<Pattern> compiledQuoteHeaderPatterns = new ArrayList<>();
	
	private List<String> quoteHeadersRegex = new ArrayList<>();
	private List<FragmentDTO> fragments = new ArrayList<>();
	private int maxParagraphLines;
	private int maxNumCharsEachLine;
	private static List<String> courtesyHeaders;
	
	/**
	 * Initialize EmailParser.
	 */
	public EmailParser() {
		quoteHeadersRegex.add("^(Le\\s(.{1,500})a(.{1,2})écrit([ | ])*:)");
		quoteHeadersRegex.add("^(On\\s(.{1,500})wrote([ | ])*:)");
		quoteHeadersRegex.add("^\\[Logo\\]");
		quoteHeadersRegex.add("[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Objet|Sujet|Subject]( )*:( )*[^\\n]+");
		quoteHeadersRegex.add("[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Envoyé|Date]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Objet|Sujet|Subject]( )*:( )*[^\\n]+");
		quoteHeadersRegex.add("[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Envoyé|Date]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Objet|Sujet|Subject]( )*:( )*[^\\n]+");
		quoteHeadersRegex.add("[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}");
		
		
		
		quoteHeadersRegex.add("[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Object|Subject]( )*:( )*[^\\n]+");
		quoteHeadersRegex.add("From( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Sent|Date]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}To( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Object|Subject]( )*:( )*[^\\n]+");
		quoteHeadersRegex.add("[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Sent|Date]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Object|Subject]( )*:( )*[^\\n]+");
		quoteHeadersRegex.add("[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Sent|Date]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Object|Subject]( )*:( )*[^\\n]+");
		quoteHeadersRegex.add("[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}");
		quoteHeadersRegex.add("Date( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}");
		maxParagraphLines = 6;
		maxNumCharsEachLine = 200;
		compileQuoteHeaderRegexes();
		
		courtesyHeaders = List.of("bonjour", "bjr", "salut", "slt", "coucou", "cc", "bonsoir", "bsr", "cher", "chers",
				"chère", "chères", "hello", "hi", "re", "hola", "hey");
	}

	/**
	 * Splits the given email text into a list of {@link Fragment} and returns the {@link Email} object. 
	 * @param emailText
	 * @param senderMail
	 * @param username
	 * @return
	 */
	public Email parse(String emailText, String senderMail, String username, boolean removeCourtesyHeaders) {
		
		Email email =  parse(emailText);
		email =  removeSenderSignature(email, senderMail, username);
		if(removeCourtesyHeaders) {
			return removeCourtesyHeaders(email);
		}
		// TODO : removeCourtesyFooters ?
		
		return email;
	}
	
	/**
	 * Splits the given email text into a list of {@link Fragment} and returns the {@link Email} object. 
	 * @param emailText
	 * @param senderMail
	 * @param username
	 * @return
	 */
	public Email parseEnodedEmail(String emailText, String senderMail, String username, boolean removeCourtesyHeaders) {
		
		return  parse(decodeBase64Email(emailText), senderMail, username,removeCourtesyHeaders);
	}
	
	public String decodeBase64Email(String body) {

		return org.apache.commons.codec.binary.StringUtils.newStringUtf8(Base64.decodeBase64(body));
		
	}
	
	public String encodeBase64Email(String body) {

		return org.apache.commons.codec.binary.StringUtils.newStringUtf8(Base64.encodeBase64(body.getBytes()));
		
	}
	/**
	 * Splits the given email text into a list of {@link Fragment} and returns the {@link Email} object. 
	 * 
	 * @param emailText
	 * @return
	 */
	public Email parse(String emailText) {
		fragments = new ArrayList<>();
		
		
		// Normalize line endings
		emailText = emailText.replace("\r\n", "\n");
		
		FragmentDTO fragment = null;
		
		// Split body to multiple lines.
		String[] lines = new StringBuilder(emailText).toString().split("\n");
		/* Reverse the array.
		 * 
		 * Reversing the array makes us to parse from the bottom to the top.  
		 * This way we can check for quote headers lines above quoted blocks
		 */
		ArrayUtils.reverse(lines);
		
		/* Paragraph for multi-line quote headers.
		 * Some clients break up the quote headers into multiple lines.
	         */
		List<String> paragraph = new ArrayList<>();
		
		// Scans the given email line by line and figures out which fragment it belong to.
		
		for (String line : lines){
			// Strip new line at the end of the string 
			line = StringUtils.stripEnd(line, "\n");
			// Strip empty spaces at the end of the string
			line = StringUtils.stripEnd(line, null);
			
			/* If the fragment is not null and we hit the empty line,
			 * we get the last line from the fragment and check if the last line is either
			 * signature and quote headers.
			 * If it is, add fragment to the list of fragments and delete the current fragment.
			 * Also, delete the paragraph.
			 */
			if (fragment != null && line.isEmpty()) {
				String last = fragment.lines.get(fragment.lines.size()-1);
				if (isSignature(last)) {
					fragment.isSignature = true;
					addFragment(fragment);
					
					fragment = null;
				} 
				else if (isQuoteHeader(paragraph)) {
					fragment.isQuoted = true;
					addFragment(fragment);
					
					fragment = null;
				}
				paragraph.clear();
			}
			
			// Check if the line is a quoted line.
			boolean isQuoted = isQuote(line);
			
			/*
			 * If fragment is empty or if the line does not matches the current fragment,
			 * create new fragment.
			 */
			if (fragment == null || !isFragmentLine(fragment, line, isQuoted)) {
				
				if (fragment != null) {
					addFragment(fragment);
				}
				fragment = new FragmentDTO();
				fragment.isQuoted = isQuoted;
				fragment.lines = new ArrayList<String>();
			}
			
			// Add line to fragment and paragraph
			fragment.lines.add(line);	
			if (!line.isEmpty()) {
				paragraph.add(line);
			}
		}
		
		if (fragment != null)
			addFragment(fragment);
		
		return createEmail(fragments);
	}
	
	/**
	 * Returns existing quote headers regular expressions.
	 * 
	 * @return
	 */
	public List<String> getQuoteHeadersRegex() {
		return this.quoteHeadersRegex;
	}
	
	/**
	 * Sets quote headers regular expressions.
	 * 
	 * @param newRegex
	 */
	public void setQuoteHeadersRegex(List<String> newRegex) {
		this.quoteHeadersRegex = newRegex;
	}
	
	/**
	 * Gets max number of lines allowed for each paragraph when checking quote headers.
	 * @return
	 */
	public int getMaxParagraphLines() {
		return this.maxParagraphLines;
	}
	
	/**
	 * Sets max number of lines allowed for each paragraph when checking quote headers.
	 * 
	 * @param maxParagraphLines
	 */
	public void setMaxParagraphLines(int maxParagraphLines) {
		this.maxParagraphLines = maxParagraphLines;
	}
	
	/**
	 * Gets max number of characters allowed for each line when checking quote headers.
	 * 
	 * @return
	 */
	public int getMaxNumCharsEachLine() {
		return maxNumCharsEachLine;
	}
	
	/**
	 * Sets max number of characters allowed for each line when checking quote headers.
	 * @param maxNumCharsEachLine
	 */
	public void setMaxNumCharsEachLine(int maxNumCharsEachLine) {
		this.maxNumCharsEachLine = maxNumCharsEachLine;
	}
	
	/**
	 * Creates {@link Email} object from List of fragments.
	 * @param fragmentDTOs
	 * @return
	 */
	protected Email createEmail(List<FragmentDTO> fragmentDTOs) {
		List <Fragment> fs = new ArrayList<>();
		Collections.reverse(fragmentDTOs);
		for (FragmentDTO f : fragmentDTOs) {
			Collections.reverse(f.lines);
			String content = new StringBuilder(StringUtils.join(f.lines,"\n")).toString();
			Fragment fr = new Fragment(content, f.isHidden, f.isSignature, f.isQuoted);
			fs.add(fr);
		}
		return new Email(fs);
	}
	
	/**
	 * Compile all the quote headers regular expressions before the parsing.
	 * 
	 */
	private void compileQuoteHeaderRegexes() {
		for (String regex : quoteHeadersRegex) {
			compiledQuoteHeaderPatterns.add(Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL));
		}
	}
	
	/**
	 * Check if the line is a signature.
	 * @param line
	 * @return
	 */
	private boolean isSignature(String line) {
		return FR_SIG_PATTERN.matcher(line).find() ||  FR_SIG_PATTERN2.matcher(line).find() || EN_SIG_PATTERN.matcher(line).find() ;
		
	}
	
	/**
	 * Checks if the line is quoted line.
	 * @param line
	 * @return
	 */
	private boolean isQuote(String line) {
		return QUOTE_PATTERN.matcher(line).find();
	}
	
	/**
	 * Checks if lines in the fragment are empty. 
	 * @param fragment
	 * @return
	 */
	private boolean isEmpty(FragmentDTO fragment) {
		return StringUtils.join(fragment.lines,"").isEmpty();
	}
	
	/**
	 * If the line matches the current fragment, return true.  
	 * Note that a common reply header also counts as part of the quoted Fragment, 
	 * even though it doesn't start with `>`.
	 * 
	 * @param fragment
	 * @param line
	 * @param isQuoted
	 * @return
	 */
	private boolean isFragmentLine(FragmentDTO fragment, String line, boolean isQuoted) {
		return fragment.isQuoted == isQuoted || (fragment.isQuoted && (isQuoteHeader(Arrays.asList(line)) || line.isEmpty()));
	}
	
	/**
	 * Add fragment to fragments list.
	 * @param fragment
	 */
	private void addFragment(FragmentDTO fragment) {
		if (fragment.isQuoted || fragment.isSignature || isEmpty(fragment)) 
			fragment.isHidden = true;
		
		fragments.add(fragment);
	}
	
	/**
	 * Checks if the given multiple-lines paragraph has one of the quote headers.
	 * Returns false if it doesn't contain any of the quote headers, 
	 * if paragraph lines are greater than maxParagraphLines, or line has more than maxNumberCharsEachLine characters.
	 *   
	 * @param paragraph
	 * @return
	 */
	private boolean isQuoteHeader(List<String> paragraph) {
		if (paragraph.size() > maxParagraphLines)
			return false;
		for (String line : paragraph) {
			if (line.length() > maxNumCharsEachLine)
				return false;
		}
		Collections.reverse(paragraph);
		String content = new StringBuilder(StringUtils.join(paragraph,"\n")).toString();
		for(Pattern p : compiledQuoteHeaderPatterns) {

			if (p.matcher(content).find()) {
				return true;
			}
		}
		
		return false;

	}	
	/**
	 * Remove accents from given value
	 * @param a
	 * @return
	 */
	private static String stripAccent(String a) {
	    return StringUtils.stripAccents(a).toLowerCase();
	   
	}
	/**
	 * Removes sender signature
	 * @param email
	 * @param senderMail
	 * @param username
	 * @return
	 */
	private Email removeSenderSignature(Email email, String senderMail, String username) {
		String[] emailLines = email.getVisibleText().split("\n");
		
		
		int stop = emailLines.length;
		for(int i=emailLines.length- 1;i>=0;i--) {
			String emailLine =emailLines[i].trim();
			if(emailLine.startsWith(senderMail) || emailLine.endsWith(senderMail) 
					|| emailLine.startsWith("<"+senderMail+">") || emailLine.endsWith("<"+senderMail+">") 
					|| emailLine.startsWith("<mailto:"+senderMail+">") || emailLine.endsWith("<mailto:"+senderMail+">") 
					|| containsUsername(emailLine, username)) {
				stop=i;
			}
		}
		List<String> signaturelines = new ArrayList<>();
		for(int i=stop;i<emailLines.length;i++) {
			signaturelines.add(emailLines[i]);
		}
		List<String> visiblelines = new ArrayList<>();
		for(int i=0;i<stop;i++) {
			visiblelines.add(emailLines[i]);
		}
		List<Fragment> frags = new ArrayList<>();
		
		String content = new StringBuilder(StringUtils.join(signaturelines,"\n")).toString();
		Fragment fr = new Fragment(content+"\n"+email.getHiddenText(), true, true, false);
		frags.add(fr);
		
		content = new StringBuilder(StringUtils.join(visiblelines,"\n")).toString();
		fr = new Fragment(content, false, false, false);
		frags.add(fr);
		

		
		return new Email(frags);
	}
	
	private static boolean containsUsername(String line, String username) {
		if(!username.isEmpty() && stripAccent(line).startsWith(stripAccent(username))){
			return true;
		}
		String[] fullname = username.split(" ");
		if(!username.isEmpty() && fullname.length==2) {
			return  stripAccent(line).startsWith(stripAccent(fullname[1])+" "+stripAccent(fullname[0]));
		}
		return false;
		
	}
	
	private Email removeCourtesyHeaders(Email email) {
		String body = email.getVisibleText();
		List<Fragment> frags = new ArrayList<>();
		for(Fragment f : email.getFragments()) {
			if(f.isHidden() && !frags.contains(f)) {
				frags.add(f);
			}
		}
		
		String nl = "\n";
		if (body == null || body.isEmpty())
			return email;
		StringBuilder newMail = new StringBuilder();
		String firstLine = "";
		String[] lines = body.trim().split(nl);

		if (lines.length == 0)
			return email;
		for (String form : courtesyHeaders) {
			String regex = "^" + form + "( \\p{L}+)*( )*(,)*";
			Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(lines[0].trim());
			boolean out =false;
			if (m.matches()) {
				firstLine = "";
				out=true;

			}
			else if (Arrays.asList(lines[0].toLowerCase().replace(",", "").split("\\s+")).contains(form)) {

				firstLine = lines[0].replaceAll("(?i)" + form, "");
				out=true;
			} else {
				firstLine = lines[0];
			}
			if(out) {
				break;
			}
		}
		newMail.append(firstLine + nl);
		for (int i = 1; i < lines.length; i++)
			newMail.append(lines[i] + nl);

		newMail = new StringBuilder(handleLinks(newMail.toString()).replaceAll("[\\\r\\\n]{2,}", "\\\n").replaceAll("\\\r\\\n"," \\\r\\\n" ).replaceAll("\\\n"," \\\n" ));
		
		Fragment hiddenF = new Fragment(newMail.toString().trim(), false, false, false);
		frags.add(hiddenF);
		return new Email(frags);

	}
	
	public static String handleLinks(String body) {
		return body.replaceAll("\\b((http|https)\\://)?[a-zA-Z0-9\\./\\?\\:@\\-_=#]+\\.([a-zA-Z0-9\\&\\./\\?\\:@\\-_=#])+/{0,1}",
				"Link");
	}
}
