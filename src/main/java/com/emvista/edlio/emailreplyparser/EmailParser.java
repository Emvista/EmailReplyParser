package com.emvista.edlio.emailreplyparser;

import com.emvista.lexicon.api.TermApi;
import com.emvista.lexicon.enums.SourceName;
import com.emvista.utils.FilesTools;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class EmailParser {

    @Autowired
    TermApi termApi;

    private static final Logger log = LoggerFactory.getLogger(EmailParser.class);

    private static final Pattern FR_SIG_PATTERN = Pattern
            .compile("(^Envoyé (depuis|de) mon .*)", Pattern.DOTALL);
    private static final Pattern FR_SIG_PATTERN2 = Pattern.compile("^Téléchargez Outlook pour .*", Pattern.DOTALL);
    private static final Pattern EN_SIG_PATTERN = Pattern
            .compile("((^Sent from my (\\s*\\w+){1,3}$)|(^-\\w|^\\s?__|^\\s?--|^\u2013|^\u2014))", Pattern.DOTALL);
    private static final Pattern QUOTE_PATTERN = Pattern.compile("(^>+)", Pattern.DOTALL);

    private static List<Pattern> compiledQuoteHeaderPatterns = new ArrayList<>();

    private List<String> quoteHeadersRegex = new ArrayList<>();
    private List<FragmentDTO> fragments = new ArrayList<>();
    private int maxParagraphLines;
    private int maxNumCharsEachLine;
    private static final List<String> courtesyHeaders = List.of("bonjour", "bjr", "salut", "slt", "coucou", "cc", "bonsoir"
            , "bsr", "cher", "chers",
            "chère", "chères", "hello", "hi", "re", "hola", "hey");

    /**
     * Initialize EmailParser.
     */
    @Autowired
    public EmailParser() {
        quoteHeadersRegex.add("^((Le\\s)?(.{1,500})a(.{1,2})écrit([ | ])*:)");
        quoteHeadersRegex.add("^(On\\s(.{1,500})wrote([ | ])*:)");
        quoteHeadersRegex.add("^\\[Logo\\]");
        quoteHeadersRegex.add("^(\\*)*\\[image: .*");
        quoteHeadersRegex.add(
                "[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0," +
                        "2}[Objet|Sujet|Subject]( )*:( )*[^\\n]+");
        quoteHeadersRegex.add(
                "[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Envoyé|Date]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0," +
                        "2}[Objet|Sujet|Subject]( )*:( )*[^\\n]+");

//		"^((De|Envoyé|Objet|Sujet|Subject|À|Cc|Date)( )*:( )*[^\\n]+\\n?)+"
//		"^((From|Sent|Object|Subjet|To|Cc|Date)( )*:( )*[^\\n]+\\n?)+"

        quoteHeadersRegex.add(
                "[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Envoyé|Date]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0," +
                        "2}[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}");

        quoteHeadersRegex.add(
                "[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0," +
                        "2}[Envoyé|Date]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Objet|Sujet|Subject]( )*:( )*[^\\n]+");
        quoteHeadersRegex
                .add("[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[De|À]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}");

        quoteHeadersRegex.add(
                "[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0," +
                        "2}[Object|Subject]( )*:( )*[^\\n]+");
        quoteHeadersRegex.add(
                "From( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Sent|Date]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}To( )*:( " +
                        ")*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Object|Subject]( )*:( )*[^\\n]+");
        quoteHeadersRegex.add(
                "[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Sent|Date]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0," +
                        "2}[Object|Subject]( )*:( )*[^\\n]+");
        quoteHeadersRegex.add(
                "[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0," +
                        "2}[Sent|Date]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[Object|Subject]( )*:( )*[^\\n]+");
        quoteHeadersRegex
                .add("[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}");
        quoteHeadersRegex
                .add("Date( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}[From|To]( )*:( )*[^\\n]+\\n?([^\\n]+\\n?){0,2}");
        maxParagraphLines = 6;
        maxNumCharsEachLine = 200;
        compileQuoteHeaderRegexes();


    }

    /**
     * Splits the given email text into a list of {@link Fragment} and returns the
     * {@link Email} object.
     *
     * @param emailText : the textual content of the email
     * @param senderMail : the sender mail address
     * @param username : the user name
     * @return an email object
     */
    public Email parse(String emailText, String senderMail, String username, boolean removeCourtesyHeaders) {

        Email email = parse(emailText);
        email = removeSenderSignature(email, senderMail, username);
        if (removeCourtesyHeaders) {
            return removeCourtesyHeaders(email);
        }

        return email;
    }

    /**
     * Splits the given email text into a list of {@link Fragment} and returns the
     * {@link Email} object.
     *
     * @param emailText : the textual content of the email
     * @param senderMail : the sender email address
     * @param username the user name
     * @return an email
     */
    public Email parseEncodedEmail(String emailText, String senderMail, String username,
                                   boolean removeCourtesyHeaders) {

        return parse(decodeBase64Email(emailText), senderMail, username, removeCourtesyHeaders);
    }

    public String decodeBase64Email(String body) {
        return org.apache.commons.codec.binary.StringUtils.newStringUtf8(Base64.decodeBase64(body));

    }

    public String encodeBase64Email(String body) {

        return org.apache.commons.codec.binary.StringUtils.newStringUtf8(Base64.encodeBase64(body.getBytes()));

    }

    private int getPreviousIndexLine(String[] lines, int lineIndex) {
        for (int i = lineIndex - 1; i >= 0; i--) {
            if (!lines[i].isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Splits the given email text into a list of {@link Fragment} and returns the
     * {@link Email} object.
     *
     * @param emailText : the textual content of the email
     * @return a parsed email
     */
    public Email parse(String emailText) {
        fragments = new ArrayList<>();
        // Normalize line endings
        emailText = emailText.replace("\r\n", "\n").replace(" ", " ");

        FragmentDTO fragment = null;

        // Split body to multiple lines.
        String[] lines = new StringBuilder(emailText).toString().split("\n");
        for (int i = 1; i < lines.length; i++) {

            int previousIndex = getPreviousIndexLine(lines, i);


            if (!lines[i].isEmpty() && previousIndex != -1 &&
                    StringUtils.isAllUpperCase(lines[i].subSequence(0, 1)) &&
                    !termApi.getTermsByFormAndSource(lines[i].split(" ")[0].toLowerCase(), SourceName.LEFFF).isEmpty() &&
                    !lines[previousIndex].matches(".*\\p{Punct}( )*$")) {
                lines[previousIndex] = lines[previousIndex] + " .";
            }
        }
        /*
         * Reverse the array.
         *
         * Reversing the array makes us to parse from the bottom to the top. This way we
         * can check for quote headers lines above quoted blocks
         */
        ArrayUtils.reverse(lines);

        /*
         * Paragraph for multi-line quote headers. Some clients break up the quote
         * headers into multiple lines.
         */
        List<String> paragraph = new ArrayList<>();

        // Scans the given email line by line and figures out which fragment it belong
        // to.

        for (String line : lines) {
            // Strip new line at the end of the string
            line = StringUtils.stripEnd(line, "\n");
            // Strip empty spaces at the end of the string
            line = StringUtils.stripEnd(line, null);

            /*
             * If the fragment is not null and we hit the empty line, we get the last line
             * from the fragment and check if the last line is either signature and quote
             * headers. If it is, add fragment to the list of fragments and delete the
             * current fragment. Also, delete the paragraph.
             */
            if (fragment != null && line.isEmpty()) {
                String last = fragment.getLines().get(fragment.getLines().size() - 1);
                if (isSignature(last)) {
                    fragment.setSignature(true);
                    addFragment(fragment);

                    fragment = null;
                } else if (isQuoteHeader(paragraph)) {
                    fragment.setQuoted(true);
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
                fragment.setQuoted(isQuoted);
                fragment.setLines(new ArrayList<>());
            }

            // Add line to fragment and paragraph
            fragment.getLines().add(line);
            if (!line.isEmpty()) {
                paragraph.add(line);
            }
        }

        if (fragment != null)
            addFragment(fragment);

        return createEmail(fragments);
    }

    /**
     *
     * @return existing quote headers regular expressions.
     */
    public List<String> getQuoteHeadersRegex() {
        return this.quoteHeadersRegex;
    }

    /**
     * Sets quote headers regular expressions.
     *
     * @param newRegex : the regular expression to set
     */
    public void setQuoteHeadersRegex(List<String> newRegex) {
        this.quoteHeadersRegex = newRegex;
    }

    /**

     *
     * @return  the  maximum number of lines allowed for each paragraph when checking quote
     *    headers.
     */
    public int getMaxParagraphLines() {
        return this.maxParagraphLines;
    }

    /**
     * Sets max number of lines allowed for each paragraph when checking quote
     * headers.
     *
     * @param maxParagraphLines : the number of lines allowed for each paragraph
     */
    public void setMaxParagraphLines(int maxParagraphLines) {
        this.maxParagraphLines = maxParagraphLines;
    }

    /**
     *
     * @return the max number of characters allowed for each line when checking quote
     *      headers.
     */
    public int getMaxNumCharsEachLine() {
        return maxNumCharsEachLine;
    }

    /**
     * Sets max number of characters allowed for each line when checking quote
     * headers.
     *
     * @param maxNumCharsEachLine : the maximum number of characters allowed
     */
    public void setMaxNumCharsEachLine(int maxNumCharsEachLine) {
        this.maxNumCharsEachLine = maxNumCharsEachLine;
    }

    /**
     * Creates {@link Email} object from List of fragments.
     *
     * @param fragmentDTOs : a list of fragments
     * @return a parsed email
     */
    protected Email createEmail(List<FragmentDTO> fragmentDTOs) {
        List<Fragment> fs = new ArrayList<>();
        Collections.reverse(fragmentDTOs);
        for (FragmentDTO f : fragmentDTOs) {
            Collections.reverse(f.getLines());
            String content = new StringBuilder(StringUtils.join(f.getLines(), "\n")).toString();
            Fragment fr = new Fragment(content, f.isHidden(), f.isSignature(), f.isQuoted());
            fs.add(fr);
        }
        return new Email(fs);
    }

    /**
     * Compile all the quote headers regular expressions before the parsing.
     */
    private void compileQuoteHeaderRegexes() {
        for (String regex : quoteHeadersRegex) {
            compiledQuoteHeaderPatterns.add(Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL));
        }
    }

    /**
     * @param line : a string line
     * @return true if the line is a signature. else false.
     */
    private boolean isSignature(String line) {
        return FR_SIG_PATTERN.matcher(line).find() || FR_SIG_PATTERN2.matcher(line).find()
                || EN_SIG_PATTERN.matcher(line).find();

    }

    /**
     *
     * @param line : a sentence
     * @return true if the line is quoted line. else false
     */
    private boolean isQuote(String line) {
        return QUOTE_PATTERN.matcher(line).find();
    }

    /**
     *
     * @param fragment : the fragment to analyze
     * @return true if lines in the fragment are empty. else false
     */
    private boolean isEmpty(FragmentDTO fragment) {
        return StringUtils.join(fragment.getLines(), "").isEmpty();
    }

    /**
     * Note that a common
     * reply header also counts as part of the quoted Fragment, even though it
     * doesn't start with `>`.
     *
     * @param fragment : the fragment to analyze
     * @param line : a sentence
     * @param isQuoted : if the fragment must be quoted or not
     * @return true if the line matches the current fragment, else false
     */
    private boolean isFragmentLine(FragmentDTO fragment, String line, boolean isQuoted) {
        return fragment.isQuoted() == isQuoted
                || (fragment.isQuoted() && (isQuoteHeader(Collections.singletonList(line)) || line.isEmpty()));
    }

    /**
     * Add fragment to fragments list.
     *
     * @param fragment : the fragment to analyze
     */
    private void addFragment(FragmentDTO fragment) {
        if (fragment.isQuoted() || fragment.isSignature() || isEmpty(fragment))
            fragment.setHidden(true);

        fragments.add(fragment);
    }

    /**
     * Checks if the given multiple-lines paragraph has one of the quote headers.
     * Returns false if it doesn't contain any of the quote headers, if paragraph
     * lines are greater than maxParagraphLines, or line has more than
     * maxNumberCharsEachLine characters.
     *
     * @param paragraph : the paragraph to analyz
     * @return true if the given multiple-lines paragraph has one of the quote headers. , else false
     */
    private boolean isQuoteHeader(List<String> paragraph) {
        if (paragraph.size() > maxParagraphLines)
            return false;
        for (String line : paragraph) {
            if (line.length() > maxNumCharsEachLine)
                return false;
        }
        Collections.reverse(paragraph);
        String content = new StringBuilder(StringUtils.join(paragraph, "\n")).toString();
        for (Pattern p : compiledQuoteHeaderPatterns) {

            if (p.matcher(content).find()) {
                return true;
            }
        }

        return false;

    }

    /**
     * Remove accents from given value
     *
     * @param a a string value
     * @return a string without accents
     */
    private static String stripAccent(String a) {
        return StringUtils.stripAccents(a).toLowerCase();

    }

    /**
     * Removes sender signature
     *
     * @param email : the email to parse
     * @param senderMail :  the sender email address
     * @param username : the user name
     * @return  parsed email without the sender's signature
     */
    private Email removeSenderSignature(Email email, String senderMail, String username) {
        String[] emailLines = email.getVisibleText().split("\n");

        int stop = emailLines.length - 1;
        for (int i = emailLines.length - 1; i >= 0; i--) {
            String emailLine = emailLines[i].trim().replace("*", "");
            if (emailLine.startsWith(senderMail)
                    || emailLine.endsWith(senderMail)
                    || emailLine.startsWith("<" + senderMail + ">")
                    || emailLine.endsWith("<" + senderMail + ">")
                    || emailLine.contains("<mailto:" + senderMail + ">")
                    || emailLine.endsWith("<mailto:" + senderMail + ">")
                    || containsUsername(emailLine, username)) {
                stop = i;
            }
        }
        List<String> signaturelines = new ArrayList<>();

        for (int i = stop; i < emailLines.length; i++) {
            signaturelines.add(emailLines[i]);
        }
        List<String> visiblelines = new ArrayList<>();
        int stop2 = getFooterLine(emailLines, stop);
        if (stop2 != -1 && stop2 != -2) {
            stop = stop2;
        } else if (stop == emailLines.length - 1) {
            stop = emailLines.length;
        }
        for (int i = 0; i < stop; i++) {
            visiblelines.add(emailLines[i]);
        }
        List<Fragment> frags = new ArrayList<>();

        String content = new StringBuilder(StringUtils.join(signaturelines, "\n")).toString();
        Fragment fr = new Fragment(content + "\n" + email.getHiddenText(), true, true, false);
        frags.add(fr);

        content = new StringBuilder(StringUtils.join(visiblelines, "\n")).toString();
        fr = new Fragment(content, false, false, false);
        frags.add(fr);

        return new Email(frags);
    }

    /**
     *
     * @param line : the line to analyze
     * @param username : the user name
     * @return true if the given line contains the user name
     */
    private static boolean containsUsername(String line, String username) {
        if (!username.isEmpty() && stripAccent(line.replace("*", "")).startsWith(stripAccent(username))) {
            return true;
        }
        String[] fullname = username.split(" ");
        if (!username.isEmpty() && fullname.length == 2) {
            return stripAccent(line.replace("*", ""))
                    .startsWith(stripAccent(fullname[1]) + " " + stripAccent(fullname[0]));
        }
        return false;

    }

    /**
     *
     * @param emailLines : the content of the email
     * @param stop :  the line id where footers end
     * @return the line id where footers begin
     */
    private static int getFooterLine(String[] emailLines, int stop) {
        InputStream footersFile = FilesTools.getFileGeneric("footers.txt", EmailParser.class);
        List<String> footers;
        try {
            footers = IOUtils.readLines(footersFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("footers.txt file no found !!!");
            return -1;
        }
        int out = -2;
        for (int i = stop; i >= 0 && out != -1 && stop < emailLines.length; i--) {
            for (String form : footers) {
                Pattern p = Pattern.compile("^" + form + "( )*(,|\\.)*( )*$", Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(emailLines[i].trim());
                if (m.matches()) {
                    out = i;
                    break;
                }
            }
        }
        return out;
    }

    /**
     *
     * @param email : the email to parse
     * @return a parsed email without the courtesy headers
     */
    private Email removeCourtesyHeaders(Email email) {
        String body = email.getVisibleText();
        List<Fragment> frags = new ArrayList<>();
        for (Fragment f : email.getFragments()) {
            if (f.isHidden() && !frags.contains(f)) {
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
            String regex = "^" + form + "( \\p{L}+)*( )*(,|!|🙂)*"; // TODO : list all possible emojis ?
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(lines[0].trim());
            boolean out = false;
            if (m.matches()) {
                firstLine = "";
                out = true;

            } else if (Arrays.asList(lines[0].toLowerCase().replace(",", "").split("\\s+")).contains(form)) {
                firstLine = lines[0].replaceAll("(?i)" + form + " (,|!|🙂|et)", "");
                out = true;
            } else {
                firstLine = lines[0];
            }
            if (out) {
                break;
            }
        }
        newMail.append(firstLine + nl);
        for (int i = 1; i < lines.length; i++)
            newMail.append(lines[i] + nl);

        newMail = new StringBuilder(newMail.toString().replaceAll("[\\\r\\\n]{2,}", "\\\n")
                .replaceAll("\\\r\\\n", " \\\r\\\n").replaceAll("\\\n", " \\\n"));

        Fragment hiddenF = new Fragment(newMail.toString().trim(), false, false, false);
        frags.add(hiddenF);
        return new Email(frags);

    }

}
