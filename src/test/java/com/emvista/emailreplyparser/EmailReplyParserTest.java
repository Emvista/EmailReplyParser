package com.emvista.emailreplyparser;


import com.emvista.emailreplyparser.model.Email;
import com.emvista.lexiconreader.MultiTenantMongoDBFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest()
@ContextConfiguration(classes = { MultiTenantMongoDBFactory.class})
public class EmailReplyParserTest {

    @Autowired
    EmailReplyParser emailReplyParser;

    @Test
    public void testReadWithNullContent() {
        Email email = emailReplyParser.read(null);
        assertEquals("", email.getVisibleText());
    }

    @Test
    public void testReadWithEmptyContent() {
        Email email = emailReplyParser.read("");
        assertEquals("", email.getVisibleText());
    }

    @Test
    public void testParseReply() {
        assertEquals("Hi,\n\nYou can list the keys for the bucket and call delete for each. "
                        + "Or if you\nput the keys (and kept track of them in your test) you can delete them\none at a time "
                        + "(without incurring the cost of calling list first.)\n\nSomething like:\n\n        "
                        + "String bucket = \"my_bucket\";\n        BucketResponse bucketResponse = riakClient.listBucket(bucket);\n        "
                        + "RiakBucketInfo bucketInfo = bucketResponse.getBucketInfo();\n\n        for(String key : bucketInfo.getKeys()) "
                        + "{\n            riakClient.delete(bucket, key);\n        }\n\n\nwould do it.\n\nSee also\n\nhttp://wiki.basho" +
						".com/REST-API.html#Bucket-operations\n\nwhich says\n\n"
                        + "\"At the moment there is no straightforward way to delete an entire\nBucket. There is, however, an open ticket" +
						" for the feature. To delete all\nthe keys in a bucket, "
                        + "you'll need to delete them all individually.\"",
                emailReplyParser.parseReply(FixtureGetter.getFixture("email_2.txt")));
    }

    @Test
    public void testParseOutSentFromIPhone() {
        assertEquals("Here is another email .",
                emailReplyParser.parseReply(FixtureGetter.getFixture("email_iphone.txt")));
    }

    @Test
    public void testParseOutSentFromBlackBerry() {
        assertEquals("Here is another email .",
                emailReplyParser.parseReply(FixtureGetter.getFixture("email_blackberry.txt")));
    }

    @Test
    public void testDoNotParseOutSendFromInRegularSentence() {
        assertEquals("Here is another email .\n\nSent from my desk, is much easier then my mobile phone.",
                emailReplyParser.parseReply(FixtureGetter.getFixture("email_sent_from_my_not_signature.txt")));
    }

    @Test
    public void testParseOutJustTopForOutlookReply() {
        assertEquals("Outlook with a reply",
                emailReplyParser.parseReply(FixtureGetter.getFixture("email_2_1.txt")));
    }

    @Test
    public void testRetainsBullets() {
        assertEquals("test 2 this should list second\n\nand have spaces\n\nand retain this formatting\n\n\n   - how about bullets\n   - and another .",
                emailReplyParser.parseReply(FixtureGetter.getFixture("email_bullets.txt")));
    }

    @Test
    public void testUnquotedReply() {
        assertEquals("This is my reply.",
                emailReplyParser.parseReply(FixtureGetter.getFixture("email_unquoted_reply.txt")));
    }

}
