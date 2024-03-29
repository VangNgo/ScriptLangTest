package com.gmail.vangnamngo.scriptlangtest.parser;

import com.gmail.vangnamngo.scriptlangtest.lexer.LexerToken;
import com.gmail.vangnamngo.scriptlangtest.lexer.EToken;
import com.gmail.vangnamngo.scriptlangtest.exception.TokenParseException;
import com.gmail.vangnamngo.scriptlangtest.utility.StringUtils;

import java.io.File;
import java.util.*;

public class FileLexer {
    private final static Set<String> KEYWORD_MAP = new HashSet<>();
    private final static Set<EToken> LINE_CONTINUATION_TOKENS = new HashSet<>();

    static {
        // Protection modifiers
        KEYWORD_MAP.add("public");
        KEYWORD_MAP.add("group");
        KEYWORD_MAP.add("directory");
        KEYWORD_MAP.add("private");

        // Flow
        KEYWORD_MAP.add("if");
        KEYWORD_MAP.add("elif");
        KEYWORD_MAP.add("else");
        KEYWORD_MAP.add("for");
        KEYWORD_MAP.add("while");
        KEYWORD_MAP.add("continue");
        KEYWORD_MAP.add("break");
        KEYWORD_MAP.add("return");
        KEYWORD_MAP.add("switch");
        KEYWORD_MAP.add("case");
        KEYWORD_MAP.add("default");

        // Data types
        KEYWORD_MAP.add("bool");
        KEYWORD_MAP.add("char");
        KEYWORD_MAP.add("int");
        KEYWORD_MAP.add("dec");
        KEYWORD_MAP.add("string");

        // End of line continuators
        LINE_CONTINUATION_TOKENS.add(EToken.OPERATOR);
        LINE_CONTINUATION_TOKENS.add(EToken.SET_OPERATOR);
        LINE_CONTINUATION_TOKENS.add(EToken.COMPARATOR);
        LINE_CONTINUATION_TOKENS.add(EToken.AND);
        LINE_CONTINUATION_TOKENS.add(EToken.OR);
        LINE_CONTINUATION_TOKENS.add(EToken.ASSIGN);
    }

    protected static boolean addKeyword(String keyword) {
        return KEYWORD_MAP.add(keyword);
    }

    protected static boolean isKeywordKnown(String keyword) {
        return KEYWORD_MAP.contains(keyword);
    }

    protected static boolean areAllKeywordsKnown(Collection<? extends String> keyword) {
        return KEYWORD_MAP.containsAll(keyword);
    }

    protected static boolean addLineContinuationToken(EToken tokenType) {
        return LINE_CONTINUATION_TOKENS.add(tokenType);
    }

    protected static boolean isLineContinuationToken(EToken tokenType) {
        return LINE_CONTINUATION_TOKENS.contains(tokenType);
    }

    protected static boolean areLineContinuationTokens(Collection<EToken> tokenTypes) {
        return LINE_CONTINUATION_TOKENS.containsAll(tokenTypes);
    }

    private List<LexerToken<?>> tList = null;
    private boolean wasTokenized = false;

    /**
     * The String representing the line that the tokenizer is currently processing.
     */
    protected String currStr = null;

    /**
     * The line number of the String the tokenizer is currently processing.
     */
    protected int line = 0;

    /**
     * The "column" that the tokenizer is currently on. Used to fetch a character from {@link #currStr}.
     */
    protected int col = 0;

    /**
     * The type of token that is being used to ignore new lines. Also eliminates indentation rules.
     */
    protected EToken newLineIgnoringToken = null;

    /**
     * Determines whether to set {@link #newLineIgnoringToken} to null after a line has been fully processed.
     * If set to true, then {@link #newLineIgnoringToken} will be set to null and this boolean will be set to false.
     */
    protected boolean newLineIgnoringTokenIsVolatile = false;

    protected int commentProcessing = 0;

    /**
     * Method to be overridden by subclasses that dictate rules for extra tokens.
     * This CANNOT override rules for built-in tokens!
     * @return False if the token does not match the token rules.
     * @throws TokenParseException When the token matches a rule but fails to parse
     */
    protected boolean tryExtraToken() throws TokenParseException {
        return false;
    }

    private int indent = 0;
    private int spacesInIndent = 0;

    /**
     * Attempts to tokenize the provided file. This is effectively {@code tokenize(file, false)}.
     * @see #tokenize(File, boolean)
     */
    public final List<LexerToken<?>> tokenize(File file) throws TokenParseException {
        return tokenize(file, false);
    }

    /**
     * Attempts to tokenize the provided file.
     * @param file The file to tokenize.
     * @param force Whether to tokenize the file anyway, even if it was already tokenized.
     * @return null if the file cannot be found, otherwise a list of tokens representing the contents of the file.
     * @throws TokenParseException If a character cannot be tokenized for any reason.
     */
    public final List<LexerToken<?>> tokenize(File file, boolean force) throws TokenParseException {
        if (wasTokenized && !force) {
            return tList;
        }

        tList = new ArrayList<>();
        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                boolean matchedToken;
                currStr = scan.nextLine();
                col = 0;
                line++;

                if (commentProcessing == 1) {
                    commentProcessing = 0;
                }

                if (line > 1 && !shouldSkipNewlineAndIndents()) {
                    tList.add(new LexerToken<>(EToken.NEWLINE, null, line - 1));
                    if (currStr.length() != 0) {
                        calculateIndent();
                    }
                }

                while (col < currStr.length()) {
                    char c = currStr.charAt(col);

                    if (commentProcessing == 2) {
                        if (c == '*') {
                            endFlexComment();
                        }
                        col++;
                        continue;
                    }
                    if (commentProcessing == 1) {
                        break;
                    }
                    if (commentProcessing < 0 || commentProcessing > 2) {
                        throw new TokenParseException("Invalid comment type \"" + commentProcessing + "\" while processing line " + line);
                    }

                    if (ignoreWhitespace()) {
                        continue;
                    }

                    switch (c) {
                        case '\'':
                            matchedToken = tryChar();
                            break;
                        case '\"':
                            matchedToken = tryString();
                            break;
                        case '#':
                            matchedToken = tryHeader();
                            break;
                        case '+':
                        case '-':
                        case '*':
                        case '/':
                        case '%':
                        case '&':
                        case '|':
                            matchedToken = tryOperatorCommentOrLogic();
                            break;
                        default:
                            if (StringUtils.isAlphanumericChar(c)) {
                                matchedToken = tryNumKeywordOrIdentifier();
                            }
                            else {
                                matchedToken = trySpecialChar() || tryExtraToken();
                            }
                    }

                    if (!matchedToken) {
                        throw new TokenParseException("Illegal character on line " + line + ": \"" + c + "\"");
                    }
                    col++;
                }
            }

            while (indent > 0) {
                indent--;
                tList.add(new LexerToken<>(EToken.DEDENT, null, line));
            }
            tList.add(new LexerToken<>(EToken.EOF, null, line));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        wasTokenized = true;
        return tList;
    }

    private boolean shouldSkipNewlineAndIndents() {
        boolean hadIgnoringToken = newLineIgnoringToken != null;
        if (newLineIgnoringTokenIsVolatile) {
            newLineIgnoringToken = null;
            newLineIgnoringTokenIsVolatile = false;
        }

        EToken lastToken = tList.size() >= 1 ? tList.get(tList.size() - 1).tokenType : null;
        return hadIgnoringToken || LINE_CONTINUATION_TOKENS.contains(lastToken);
    }

    private void calculateIndent() throws TokenParseException {
        char c = currStr.charAt(col);
        int indent = 0;
        int spaces = 0;
        while (Character.isWhitespace(c) && col < currStr.length()) {
            if (c == '\t') {
                indent++;
            }
            else if (c == ' ') {
                spaces++;
            }
            else {
                throw new TokenParseException("Illegal whitespace character on line " + line);
            }
            c = currStr.charAt(++col);
        }

        if (col >= currStr.length() || (spaces == 0 && indent == 0)) {
            return;
        }

        // Dynamically determine the number of spaces in an indent by the first indentation in the script
        if (spacesInIndent == 0) {
            spacesInIndent = spaces;
        }
        // Always ensure indentation consistency within a script
        if (spaces % spacesInIndent != 0) {
            throw new TokenParseException("Inconsistent indentation on line " + line);
        }
        else {
            indent += spaces / spacesInIndent;
        }

        if (this.indent != indent) {
            int increment = this.indent < indent ? 1 : -1;
            while (indent != this.indent) {
                tList.add(new LexerToken<>(increment > 0 ? EToken.INDENT : EToken.DEDENT, null, line));
                this.indent += increment;
            }
        }
    }

    private boolean ignoreWhitespace() {
        char c;
        boolean skipped = false;
        while (col < currStr.length()) {
            c = currStr.charAt(col);
            if (!Character.isWhitespace(c)) {
                break;
            }
            skipped = true;
            col++;
        }
        return skipped;
    }

    private boolean tryChar() throws TokenParseException {
        // We can ignore the "first" character in the string sequence since it'll just be the first quotation mark.
        col++;
        if (col >= currStr.length()) {
            throw new TokenParseException("Unterminated character literal on line " + line);
        }

        char c = currStr.charAt(col);
        if (c == '\\') {
            c = getEscaped(currStr.charAt(++col));
        }
        if (currStr.charAt(++col) != '\'') {
            throw new TokenParseException("Character literal not terminated on line " + line);
        }
        
        if (canLookAhead() && StringUtils.isAlphanumericChar(currStr.charAt(col + 1))) {
            throw new TokenParseException("Detected illegal character after a character literal on line " + line);
        }

        tList.add(new LexerToken<>(EToken.CHARACTER, c, line));
        return true;
    }

    private boolean tryString() throws TokenParseException {
        char c;
        StringBuilder str = new StringBuilder();
        // We can ignore the "first" character in the string sequence since it'll just be the first quotation mark.
        col++;
        while (col < currStr.length()) {
            c = currStr.charAt(col);
            if (c == '\\') {
                str.append(getEscaped(currStr.charAt(++col)));
                col++;
                continue;
            }
            if (c == '\"') {
                break;
            }
            str.append(c);
            col++;
        }
        if (col >= currStr.length()) {
            throw new TokenParseException("String literal not terminated on line " + line);
        }

        if (canLookAhead()) {
            boolean hasIllegalChar = StringUtils.isAlphanumericChar(currStr.charAt(col + 1));
            if (hasIllegalChar) {
                throw new TokenParseException("Detected illegal character after a string literal on line " + line);
            }
        }

        tList.add(new LexerToken<>(EToken.STRING, str.toString(), line));
        return true;
    }

    private char getEscaped(char c) throws TokenParseException {
        switch (c) {
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case 'f':
                return '\f';
            case 'b':
                return '\b';
            case 'u':
                StringBuilder unicode = new StringBuilder();
                for (int i = 1; i < 5; i++) {
                    col++;
                    if (col >= currStr.length() || !StringUtils.isUnicodeChar(currStr.charAt(col))) {
                        throw new TokenParseException("Malformed unicode at line " + line);
                    }
                    unicode.append(currStr.charAt(col));
                }
                return (char) Integer.parseInt(unicode.toString(), 16);
            default:
                return c;
        }
    }

    private boolean tryNumKeywordOrIdentifier() throws TokenParseException {
        char c;
        LexerToken<?> token;
        StringBuilder val = new StringBuilder();

        boolean hasNonDigit = false;
        boolean forcedDecimal = false;

        while (col < currStr.length()) {
            c = currStr.charAt(col);
            if (!StringUtils.isAlphanumericChar(c) && c != '.') {
                break;
            }
            if (StringUtils.isAlphabeticChar(c)) {
                hasNonDigit = true;
            }

            if (forcedDecimal && (hasNonDigit || c == '.')) {
                throw new TokenParseException("Illegal character in decimal on line " + line);
            }

            if (c == '.') {
                if (!hasNonDigit) {
                    forcedDecimal = true;
                }
                else {
                    break;
                }
            }

            val.append(c);
            col++;
        }

        if (val.length() == 0) {
            return false;
        }

        col--;
        String completedVal = val.toString();
        if (!hasNonDigit) {
            if (forcedDecimal) {
                token = new LexerToken<>(EToken.DECIMAL, Double.parseDouble(completedVal), line);
            }
            else {
                token = new LexerToken<>(EToken.INTEGER, Integer.parseInt(completedVal), line);
            }
        }
        else {
            switch (completedVal) {
                case "true":
                    token = new LexerToken<>(EToken.BOOLEAN, true, line);
                    break;
                case "false":
                    token = new LexerToken<>(EToken.BOOLEAN, false, line);
                    break;
                default:
                    EToken tokenEnum = KEYWORD_MAP.contains(completedVal) ? EToken.KEYWORD : EToken.IDENTIFIER;
                    token = new LexerToken<>(tokenEnum, completedVal, line);
            }
        }

        tList.add(token);
        return true;
    }

    // This method will handle tokenizing the entire header line. It should consist of a prefix and strings/characters.
    private boolean tryHeader() throws TokenParseException {
        char c;
        col++; // Ignore first character since it should always be "#"
        if (Character.isWhitespace(currStr.charAt(col))) {
            return false; // The prefix must NEVER be empty!
        }
        StringBuilder val = new StringBuilder();
        boolean hasPrefix = false;
        while (col < currStr.length()) {
            c = currStr.charAt(col);
            if (c == '\"') {
                tryString();
            }
            else if (c == '\'') {
                tryChar();
            }
            else if (Character.isWhitespace(c)) {
                if (val.length() == 0) {
                    col++;
                    continue;
                }
                String completedVal = val.toString();
                if (!hasPrefix) {
                    hasPrefix = true;
                    tList.add(new LexerToken<>(EToken.HEADER, completedVal, line));
                }
                else {
                    tList.add(new LexerToken<>(EToken.STRING, completedVal, line));
                }
                val.setLength(0);
            }
            else {
                val.append(c);
            }
            col++;
        }
        if (val.length() != 0) {
            tList.add(new LexerToken<>(EToken.STRING, val.toString(), line));
        }
        return true;
    }

    private boolean tryOperatorCommentOrLogic() throws TokenParseException {
        char c = currStr.charAt(col);
        LexerToken<?> token = null;

        if (canLookAhead()) {
            char cA = currStr.charAt(col + 1);
            if (cA == '=') {
                token = new LexerToken<>(EToken.SET_OPERATOR, c + "=", line);
                col++;
            }
            else if (c == '&' && cA == '&') {
                token = new LexerToken<>(EToken.AND, null, line);
                col++;
            }
            else if (c == '|' && cA == '|') {
                token = new LexerToken<>(EToken.OR, null, line);
                col++;
            }
            else if (c == '/') {
                if (cA == '/') {
                    commentProcessing = 1;
                    col++;
                    return true;
                }
                else if (cA == '*') {
                    commentProcessing = 2;
                    col++;
                    return true;
                }
            }
            else if (c == '*' && cA == '/') {
                throw new TokenParseException("Rogue multiline comment terminator at line " + line);
            }
        }

        token = (token != null ? token : new LexerToken<>(EToken.OPERATOR, c, line));
        tList.add(token);
        return true;
    }

    private void endFlexComment() throws TokenParseException {
        char c = currStr.charAt(col);
        if (canLookAhead()) {
            char cA = currStr.charAt(++col);
            if (c == '*' && cA == '/') {
                if (commentProcessing == 2) {
                    commentProcessing = 0;
                }
                else {
                    throw new TokenParseException("Rogue multiline comment terminator at line " + line);
                }
            }
        }
    }

    private boolean trySpecialChar() {
        char c = currStr.charAt(col);
        LexerToken<?> token = null;
        switch (c) {
            case '!':
                if (canLookAhead() && currStr.charAt(col + 1) == '=') {
                    token = new LexerToken<>(EToken.COMPARATOR, c + "=", line);
                    col++;
                }
                else {
                    token = new LexerToken<>(EToken.NOT, null, line);
                }
                break;
            case '=':
                if (canLookAhead() && currStr.charAt(col + 1) == '=') {
                    token = new LexerToken<>(EToken.COMPARATOR, c + "=", line);
                    col++;
                }
                else {
                    token = new LexerToken<>(EToken.ASSIGN, null, line);
                }
                break;
            case '(':
                token = new LexerToken<>(EToken.L_PAREN, null, line);
                newLineIgnoringToken = EToken.L_PAREN;
                break;
            case ')':
                if (newLineIgnoringToken == EToken.L_PAREN) {
                    newLineIgnoringToken = null;
                }
                token = new LexerToken<>(EToken.R_PAREN, null, line);
                break;
            case '[':
                token = new LexerToken<>(EToken.L_BRACKET, null, line);
                break;
            case ']':
                token = new LexerToken<>(EToken.R_BRACKET, null, line);
                break;
            case '{':
                token = new LexerToken<>(EToken.L_BRACE, null, line);
                break;
            case '}':
                token = new LexerToken<>(EToken.R_BRACE, null, line);
                break;
            case '.':
                token = new LexerToken<>(EToken.DOT, null, line);
                break;
            case ',':
                token = new LexerToken<>(EToken.COMMA, null, line);
                break;
            case ':':
                token = new LexerToken<>(EToken.COLON, null, line);
                break;
        }
        if (token != null) {
            tList.add(token);
            return true;
        }
        return false;
    }

    private boolean canLookAhead() {
        return col < currStr.length() - 1;
    }
}
