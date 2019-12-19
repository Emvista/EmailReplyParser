package com.edlio.emailreplyparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class EmailParserTest {

	
	
	
	
	@Test
	public void test() {
		EmailParser parser = new EmailParser();

		String encodedTtext = "U2FsdXQsCgpPbiBz4oCZb3JnYW5pc2UgY29tbWVudCA/CgpBIHBsdXMKSm9zZXR0ZSBGb3J0aXNoIC0gU3RhZ2nDqHJlIGNoZXogS0FMSVBTQQpqb3NldHRlLmZvcnRpc2hAa2FsaXBzYS5jb20KKzMzIDIgOTIgODkgMDIgNTUKCj4gT24gMjEgTWF5IDIwMTksIGF0IDA2OjM2LCBMdWMgQmFzc29u";
		Email email = parser.parseEncodedEmail(encodedTtext,"josette.fortish@kalipsa.com","",false);
		String result = parser.encodeBase64Email(email.getVisibleText());
		
		String expected = "U2FsdXQsCgpPbiBz4oCZb3JnYW5pc2UgY29tbWVudCA/";
		
		assertEquals(expected, result);
		
		encodedTtext = "U2FsdXQsCgpPbiBz4oCZb3JnYW5pc2UgY29tbWVudCA/CgpBIHBsdXMKSm9zZXR0ZSBGb3J0aXNoIC0gU3RhZ2nDqHJlIGNoZXogS0FMSVBTQQpqb3NldHRlLmZvcnRpc2hAa2FsaXBzYS5jb20KKzMzIDIgOTIgODkgMDIgNTUKCj4gT24gMjEgTWF5IDIwMTksIGF0IDA2OjM2LCBMdWMgQmFzc29u";
		email = parser.parseEncodedEmail(encodedTtext,"josette.fortish@kalipsa.com","FortîSh Josette",false);
		result = parser.encodeBase64Email(email.getVisibleText());
		expected = "U2FsdXQsCgpPbiBz4oCZb3JnYW5pc2UgY29tbWVudCA/";
		assertEquals(expected, result);
		
		encodedTtext = "U2FsdXQsCgpPbiBz4oCZb3JnYW5pc2UgY29tbWVudCA/CgpBIHBsdXMKSm9zZXR0ZSBGb3J0aXNoIC0gU3RhZ2nDqHJlIGNoZXogS0FMSVBTQQpqb3NldHRlLmZvcnRpc2hAa2FsaXBzYS5jb20KKzMzIDIgOTIgODkgMDIgNTUKCj4gT24gMjEgTWF5IDIwMTksIGF0IDA2OjM2LCBMdWMgQmFzc29u";
		email = parser.parseEncodedEmail(encodedTtext,"josette.fortish@kalipsa.com","FortîSh Josette",true);
		result = parser.encodeBase64Email(email.getVisibleText());
		expected = "T24gc+KAmW9yZ2FuaXNlIGNvbW1lbnQgPw==";
		assertEquals(expected, result);
		
		
		
		
	}
	@Test
	public void testQuoteHeaders() {
		EmailParser parser = new EmailParser();
		String expected = "UmUgIQpDZWNpIGVzdCB1biBzZWNvbmQgdGVzdA==";
		
		String encodedTtext  = "UmUgIQpDZWNpIGVzdCB1biBzZWNvbmQgdGVzdAoKX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX18KRGUgOiBqb3NldHRlLmZvcnRpc2hAaG90bWFpbC5jb20gPGpvc2V0dGUuZm9ydGlzaEBob3RtYWlsLmNvbT4KRW52b3nDqSA6IG1hcmRpIDUgbm92ZW1icmUgMjAxOSAxNTozMArDgCA6IGpvc2V0dGUuZm9ydGlzaEBrYWxpcHNhLmNvbSA8am9zZXR0ZS5mb3J0aXNoQGthbGlwc2EuY29tPgpPYmpldCA6IFJFOiBUZXN0IGRlcyBDQyBldCBDQ0k=";
		Email email = parser.parseEncodedEmail(encodedTtext,"blabla","",false);	
		String result = parser.encodeBase64Email(email.getVisibleText());
		assertEquals(expected, result);
		
		
		encodedTtext  = "UmUgIQpDZWNpIGVzdCB1biBzZWNvbmQgdGVzdAoKX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX18KRGUgOiBqb3NldHRlLmZvcnRpc2hAaG90bWFpbC5jb20gPGpvc2V0dGUuZm9ydGlzaEBob3RtYWlsLmNvbT4KRW52b3nDqSA6IG1hcmRpIDUgbm92ZW1icmUgMjAxOSAxNTozMArDgCA6IGpvc2V0dGUuZm9ydGlzaEBrYWxpcHNhLmNvbSA8am9zZXR0ZS5mb3J0aXNoQGthbGlwc2EuY29tPgpPYmpjZXQgOiBSRTogVGVzdCBkZXMgQ0MgZXQgQ0NJ";
		email = parser.parseEncodedEmail(encodedTtext,"blabla","",false);	
		result = parser.encodeBase64Email(email.getVisibleText());
		assertEquals(expected, result);
		
		encodedTtext  = "UmUgIQpDZWNpIGVzdCB1biBzZWNvbmQgdGVzdAoKX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX18KCsOAIDogam9zZXR0ZS5mb3J0aXNoQGthbGlwc2EuY29tIDxqb3NldHRlLmZvcnRpc2hAa2FsaXBzYS5jb20+CkRlIDogam9zZXR0ZS5mb3J0aXNoQGhvdG1haWwuY29tIDxqb3NldHRlLmZvcnRpc2hAaG90bWFpbC5jb20+CgpPYmpldCA6IFJFOiBUZXN0IGRlcyBDQyBldCBDQ0kKRW52b3nDqSA6IG1hcmRpIDUgbm92ZW1icmUgMjAxOSAxNTozMA==";
		email = parser.parseEncodedEmail(encodedTtext,"blabla","",false);	
		result = parser.encodeBase64Email(email.getVisibleText());
		assertEquals(expected, result);
		
		encodedTtext  = "UmUgIQpDZWNpIGVzdCB1biBzZWNvbmQgdGVzdAoKX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX18KCsOAIDogam9zZXR0ZS5mb3J0aXNoQGthbGlwc2EuY29tIDxqb3NldHRlLmZvcnRpc2hAa2FsaXBzYS5jb20+CkRlIDogam9zZXR0ZS5mb3J0aXNoQGhvdG1haWwuY29tIDxqb3NldHRlLmZvcnRpc2hAaG90bWFpbC5jb20+CkNDOgpPYmpldCA6IFJFOiBUZXN0IGRlcyBDQyBldCBDQ0kKRW52b3nDqSA6IG1hcmRpIDUgbm92ZW1icmUgMjAxOSAxNTozMA==";
		email = parser.parseEncodedEmail(encodedTtext,"blabla","",false);	
		result = parser.encodeBase64Email(email.getVisibleText());
		assertEquals(expected, result);
		
		encodedTtext  = "UmUgIQpDZWNpIGVzdCB1biBzZWNvbmQgdGVzdAoKX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX18KCsOAIDogam9zZXR0ZS5mb3J0aXNoQGthbGlwc2EuY29tIDxqb3NldHRlLmZvcnRpc2hAa2FsaXBzYS5jb20+CkZyb20gOiBqb3NldHRlLmZvcnRpc2hAaG90bWFpbC5jb20gPGpvc2V0dGUuZm9ydGlzaEBob3RtYWlsLmNvbT4KQ0M6Ck9iamV0IDogUkU6IFRlc3QgZGVzIENDIGV0IENDSQpEYXRlIDogbWFyZGkgNSBub3ZlbWJyZSAyMDE5IDE1OjMw";
		email = parser.parseEncodedEmail(encodedTtext,"blabla","",false);	
		result = parser.encodeBase64Email(email.getVisibleText());
		assertEquals(expected, result);
		
		encodedTtext  = "UmUgIQpDZWNpIGVzdCB1biBzZWNvbmQgdGVzdAoKX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX18KClRvOiA/Pz8/CkZyb20gOiBqb3NldHRlLmZvcnRpc2hAaG90bWFpbC5jb20gPGpvc2V0dGUuZm9ydGlzaEBob3RtYWlsLmNvbT4KQ0M6Ck9iamV0IDogUkU6IFRlc3QgZGVzIENDIGV0IENDSQpEYXRlIDogbWFyZGkgNSBub3ZlbWJyZSAyMDE5IDE1OjMw";
		email = parser.parseEncodedEmail(encodedTtext,"blabla","",false);	
		result = parser.encodeBase64Email(email.getVisibleText());
		assertEquals(expected, result);
		
		encodedTtext  = "UmUgIQpDZWNpIGVzdCB1biBzZWNvbmQgdGVzdAoKRGF0ZSA6IGxlIDI4IHNlcHRlbWJyZSAKQSBwbHVzID0p";
		email = parser.parseEncodedEmail(encodedTtext,"blabla","",false);	
		result = parser.encodeBase64Email(email.getVisibleText());
		
		assertFalse(result.equals(expected));
		
	}
	
	
	@Test
	public void testReadsSimpleBody() {
		Email email = new EmailParser().parse(FixtureGetter.getFixture("email_1.txt"));
		List<Fragment> fragments = email.getFragments();
		
		assertEquals(3, fragments.size());
		assertEquals("Hi folks\n\nWhat is the best way to clear a Riak bucket of all key, values after\nrunning a test?\nI am currently using the Java HTTP API.\n", fragments.get(0).getContent());
		for(Fragment f : fragments) {
			assertFalse(f.isQuoted());
		}
		assertFalse(fragments.get(0).isSignature());
		assertTrue(fragments.get(1).isSignature());
		assertTrue(fragments.get(2).isSignature());
		
		assertFalse(fragments.get(0).isHidden());
		assertTrue(fragments.get(1).isHidden());
		assertTrue(fragments.get(2).isHidden());
		
		assertEquals("-Abhishek Kona\n\n", fragments.get(1).getContent());
	}

	@Test
	public void testReadsTopPost() {
		Email email = new EmailParser().parse(FixtureGetter.getFixture("email_3.txt"));
		List<Fragment> fragments = email.getFragments();
		
		assertEquals(5, fragments.size());
		
		assertFalse(fragments.get(0).isQuoted());
		assertFalse(fragments.get(1).isQuoted());
		assertTrue(fragments.get(2).isQuoted());
		assertFalse(fragments.get(3).isQuoted());
		assertFalse(fragments.get(4).isQuoted());
		
		assertFalse(fragments.get(0).isSignature());
		assertTrue(fragments.get(1).isSignature());
		assertFalse(fragments.get(2).isSignature());
		assertFalse(fragments.get(3).isSignature());
		assertTrue(fragments.get(4).isSignature());
		
		assertFalse(fragments.get(0).isHidden());
		assertTrue(fragments.get(1).isHidden());
		assertTrue(fragments.get(2).isHidden());
		assertTrue(fragments.get(3).isHidden());
		assertTrue(fragments.get(4).isHidden());
		
		Pattern pattern = Pattern.compile("Oh thanks.\n\nHavin");
		Matcher matcher = pattern.matcher(fragments.get(0).getContent());
		assertTrue(matcher.find());
		
		pattern = Pattern.compile("^-A");
		matcher = pattern.matcher(fragments.get(1).getContent());
		assertTrue(matcher.find());
		
		pattern = Pattern.compile("On");
		matcher = pattern.matcher(fragments.get(2).getContent());
		assertTrue(matcher.find());
		
		pattern = Pattern.compile("^_");
		matcher = pattern.matcher(fragments.get(4).getContent());
		assertTrue(matcher.find());
	}
	
	@Test
	public void testReadsBottomPost() {
		Email email = new EmailParser().parse(FixtureGetter.getFixture("email_2.txt"));
		List<Fragment> fragments = email.getFragments();
		
		assertEquals(6, fragments.size());
		
		assertEquals("Hi,", fragments.get(0).getContent());
		
		Pattern pattern = Pattern.compile("You can list");
		Matcher matcher = pattern.matcher(fragments.get(2).getContent());
		assertTrue(matcher.find());
		
		pattern = Pattern.compile("On");
		matcher = pattern.matcher(fragments.get(1).getContent());
		assertTrue(matcher.find());
		
		pattern = Pattern.compile(">");
		matcher = pattern.matcher(fragments.get(3).getContent());
		assertTrue(matcher.find());
		
		pattern = Pattern.compile("^_");
		matcher = pattern.matcher(fragments.get(5).getContent());
		assertTrue(matcher.find());
	}
	
	@Test
	public void testRecognizesDateStringAboveQuote() {
		Email email = new EmailParser().parse(FixtureGetter.getFixture("email_4.txt"));
		List<Fragment> fragments = email.getFragments();
		
		Pattern pattern = Pattern.compile("Awesome");
		Matcher matcher = pattern.matcher(fragments.get(0).getContent());
		assertTrue(matcher.find());
		
		pattern = Pattern.compile("On");
		matcher = pattern.matcher(fragments.get(1).getContent());
		assertTrue(matcher.find());
		
		pattern = Pattern.compile("Loader");
		matcher = pattern.matcher(fragments.get(1).getContent());
		assertTrue(matcher.find());
		
	}
	
	@Test
	public void testDoesNotModifyInputString() {
		String input = "The Quick Brown Fox Jumps Over The Lazy Dog";
		Email email = new EmailParser().parse(input);
		List<Fragment> fragments = email.getFragments();
		
		assertEquals("The Quick Brown Fox Jumps Over The Lazy Dog", fragments.get(0).getContent());
		
	}
	
	@Test
	public void testComplexBodyWithOnlyOneFragment() {
		Email email = new EmailParser().parse(FixtureGetter.getFixture("email_5.txt"));
		List<Fragment> fragments = email.getFragments();
		
		assertEquals(1, fragments.size());
	}
	
	@Test
	public void testDealsWithMultilineReplyHeaders() {
		Email email = new EmailParser().parse(FixtureGetter.getFixture("email_6.txt"));
		List<Fragment> fragments = email.getFragments();
		
		Pattern pattern = Pattern.compile("I get");
		Matcher matcher = pattern.matcher(fragments.get(0).getContent());
		assertTrue(matcher.find());
		
		pattern = Pattern.compile("On");
		matcher = pattern.matcher(fragments.get(1).getContent());
		assertTrue(matcher.find());
		
		pattern = Pattern.compile("Was this");
		matcher = pattern.matcher(fragments.get(2).getContent());
		assertTrue(matcher.find());
	}
	
	@Test
	public void testGetVisibleTextReturnsOnlyVisibleFragments() {
		Email email = new EmailParser().parse(FixtureGetter.getFixture("email_2_1.txt"));
		List<Fragment> fragments = email.getFragments();
		
		List<String> visibleFragments = new ArrayList<String>();
		for (Fragment fragment : fragments) {
			if (!fragment.isHidden())
				visibleFragments.add(fragment.getContent());
		}
		assertEquals(StringUtils.stripEnd(StringUtils.join(visibleFragments,"\n"), null), email.getVisibleText());
	}
	
	@Test
	public void testReadsEmailWithCorrectSignature() {
		Email email = new EmailParser().parse(FixtureGetter.getFixture("correct_sig.txt"));
		List<Fragment> fragments = email.getFragments();
		
		assertEquals(2, fragments.size());
		
		assertFalse(fragments.get(0).isQuoted());
		assertFalse(fragments.get(1).isQuoted());
		
		assertFalse(fragments.get(0).isSignature());
		assertTrue(fragments.get(1).isSignature());
		
		assertFalse(fragments.get(0).isHidden());
		assertTrue(fragments.get(1).isSignature());
		
		Pattern pattern = Pattern.compile("^--\nrick");
		Matcher matcher = pattern.matcher(fragments.get(1).getContent());
		assertTrue(matcher.find());
	}
	
	@Test
	public void testOneIsNotOn() {
		Email email = new EmailParser().parse(FixtureGetter.getFixture("email_one_is_not_on.txt"));
		List<Fragment> fragments = email.getFragments();
		
		Pattern pattern = Pattern.compile("One outstanding question");
		Matcher matcher = pattern.matcher(fragments.get(0).getContent());
		assertTrue(matcher.find());
		
		pattern = Pattern.compile("On Oct 1, 2012");
		matcher = pattern.matcher(fragments.get(1).getContent());
		assertTrue(matcher.find());
	}
	
	//@Test
	public void testCustomQuoteHeader() {
		EmailParser parser = new EmailParser();
		parser.getQuoteHeadersRegex().add("^(\\d{4}(.+)rta:)");
		
		Email email = parser.parse(FixtureGetter.getFixture("email_custom_quote_header.txt"));
		assertEquals("Thank you!", email.getVisibleText());
	}
	
	@Test
	public void testCustomQuoteHeader2() {
		EmailParser parser = new EmailParser();
		parser.getQuoteHeadersRegex().add("^(From\\: .+ .+test\\@webdomain\\.com.+)");
		
		Email email = parser.parse(FixtureGetter.getFixture("email_customer_quote_header_2.txt"));
		assertEquals("Thank you very much.", email.getVisibleText());
	}
	
	@Test
	public void testAbnormalQuoteHeader1() {
		EmailParser parser = new EmailParser();
		
		Email email = parser.parse(FixtureGetter.getFixture("email_abnormal_quote_header_1.txt"));
		assertEquals("Thank you kindly!", email.getVisibleText());
	}
	
	@Test
	public void testAbnormalQuoteHeader2() {
		EmailParser parser = new EmailParser();
		
		Email email = parser.parse(FixtureGetter.getFixture("email_abnormal_quote_header_2.txt"));
		assertEquals(
			"Thank you very much for your email!\n" + 
			"\n" + 
			"em\u2014dash coming through..", 
			email.getVisibleText());
	}
	
	@Test
	public void testAbnormalQuoteHeader3() {
		EmailParser parser = new EmailParser();
		
		Email email = parser.parse(FixtureGetter.getFixture("email_abnormal_quote_header_3.txt"));
		assertEquals(
			"Hi Daniel,\n" + 
			"\n" + 
			"\n" + 
			"Thank you very much for your email.\n" + 
			"\n" + 
			"Sincerely,\n" + 
			"Homer Simpson\n" + 
			"Nuclear Safety Inspector\n" + 
			"\n" + 
			"nuclear power plant, sector 7-G", 
			email.getVisibleText()
		);
	}
	
	@Test
	public void testAbnormalQuoteHeader4() {
		EmailParser parser = new EmailParser();
		
		Email email = parser.parse(FixtureGetter.getFixture("email_abnormal_quote_header_4.txt"));
		assertEquals(
			"From: Homer Simpson\n" + 
			"To: Support\n" + 
			"\n" + 
			"En\u2013dash coming through~\n" + 
			"\n" + 
			"Thank you very much for your email!", 
			email.getVisibleText()
		);
	}
	
	@Test
	public void testAbnormalQuoteHeader5() {
		EmailParser parser = new EmailParser();
		
		Email email = parser.parse(FixtureGetter.getFixture("email_abnormal_quote_header_5.txt"));
		assertEquals(
			"Hello from outlook.com!", 
			email.getVisibleText()
		);
	}

	@Test
	public void testAbnormalQuoteHeaderLong() {
		EmailParser parser = new EmailParser();
		
		Email email = parser.parse(FixtureGetter.getFixture("email_abnormal_quote_header_long.txt"));
		assertEquals(
			"*Caution* This is a really long email.",
			email.getVisibleText()
		);
	}
	
	@Test
	public void testEmDashSignature() {
		EmailParser parser = new EmailParser();
		
		Email email = parser.parse(FixtureGetter.getFixture("email_em_dash.txt"));
		assertEquals("Thank you.", email.getVisibleText());
	}
	
	@Test
	public void testEnDashSignature() {
		EmailParser parser = new EmailParser();
		
		Email email = parser.parse(FixtureGetter.getFixture("email_en_dash.txt"));
		assertEquals("Thank you.", email.getVisibleText());
	}

	@Test
	public void testDashesBetweenWords() {
		EmailParser parser = new EmailParser();
		
		Email email = parser.parse(FixtureGetter.getFixture("email_dashes_between_words.txt"));
		assertEquals(
			"The text below is not a signature!\n" + 
			"\n" + 
			"Parsing works correctly with mulit--dash between the words.\n" +
			"\n" +
			"This__also!", 
			email.getVisibleText()
		);
	}
	

}
