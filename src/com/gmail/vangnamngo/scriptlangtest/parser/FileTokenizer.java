package com.gmail.vangnamngo.scriptlangtest.parser;

import com.gmail.vangnamngo.scriptlangtest.lexer.LexerToken;
import com.gmail.vangnamngo.scriptlangtest.lexer.EToken;
import com.gmail.vangnamngo.scriptlangtest.exception.TokenParseException;
import com.gmail.vangnamngo.scriptlangtest.utility.StringUtils;

import java.io.File;
import java.util.*;

public class FileTokenizer {
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
                col = 0;
                currStr = scan.nextLine();

                // If the very last symbol was an assignment, operator, set operator, or logic operator, skip adding
                // a new line.
                if (line > 1) {
                    if (!shouldSkipNewline()) {
                        tList.add(new LexerToken<>(EToken.NEWLINE, null, line));
                        calculateIndent();
                    }
                }

                boolean matchedToken;
                line++;

                while (col < currStr.length()) {
                    ignoreWhitespace();
                    char c = currStr.charAt(col);
                    if (StringUtils.isAlphanumeric(c) || c == '.') {
                        matchedToken = tryNumKeywordOrIdentifier();
                    }
                    else {
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
                                matchedToken = trySpecialChar();
                                if (!matchedToken) {
                                    matchedToken = tryExtraToken();
                                }
                        }
                    }
                    if (!matchedToken) {
                        throw new TokenParseException("Illegal character on line " + line + ": \"" + c + "\"");
                    }

                    col++;
                }
            }
            tList.remove(tList.size() - 1);
            tList.add(new LexerToken<>(EToken.EOF, null, line));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        wasTokenized = true;
        return tList;
    }

    private boolean shouldSkipNewline() {
        boolean hadIgnoringToken = newLineIgnoringToken != null;
        if (newLineIgnoringTokenIsVolatile) {
            newLineIgnoringToken = null;
            newLineIgnoringTokenIsVolatile = false;
        }

        EToken lastToken = tList.get(tList.size() - 1).tokenType;
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

    private void ignoreWhitespace() {
        char c = currStr.charAt(col);
        while (col < currStr.length() && Character.isWhitespace(c)) {
            col++;
        }
    }

    private boolean tryChar() throws TokenParseException {
        // We can ignore the "first" character in the string sequence since it'll just be the first quotation mark.
        char c = currStr.charAt(++col);
        if (c == '\\') {
            c = getEscaped(currStr.charAt(++col));
        }
        if (currStr.charAt(++col) != '\'') {
            throw new TokenParseException("Illegal character specified on line " + line);
        }
        tList.add(new LexerToken<>(EToken.CHARACTER, c, line));
        return true;
    }

    private boolean tryString() throws TokenParseException {
        char c = 0;
        StringBuilder str = new StringBuilder();
        // We can ignore the "first" character in the string sequence since it'll just be the first quotation mark.
        col++;
        while (col < currStr.length()) {
            c = currStr.charAt(col);
            if (c == '\\') {
                str.append(getEscaped(currStr.charAt(++col)));
                continue;
            }
            if (c == '\"') {
                break;
            }
            str.append(c);
            col++;
        }
        if (col >= currStr.length()) {
            throw new TokenParseException("String not terminated on line " + line + ".");
        }

        if (col < currStr.length() - 1) {
            boolean hasIllegalChar = StringUtils.isAlphanumeric(currStr.charAt(col + 1));
            if (hasIllegalChar) {
                throw new TokenParseException("Detected illegal character after a string on line " + line + ".");
            }
        }

        tList.add(new LexerToken<>(EToken.STRING, str.toString(), line));
        return true;
    }

    private char getEscaped(char c) {
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
                String unicode = currStr.substring(col + 1, col + 5);
                col += 5;
                return (char) Integer.parseInt(unicode, 16);
            default:
                return c;
        }
    }

    private boolean tryNumKeywordOrIdentifier() throws TokenParseException {
        char c = currStr.charAt(col);
        LexerToken<?> token = null;
        StringBuilder val = new StringBuilder();

        boolean hasNonDigit = false;
        boolean forcedDecimal = false;

        while (StringUtils.isAlphanumeric(c) || c == '.') {
            if (StringUtils.isAlphabetic(c)) {
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
                    String completedVal = val.toString();
                    EToken tokenEnum = KEYWORD_MAP.contains(completedVal) ? EToken.KEYWORD : EToken.IDENTIFIER;
                    token = new LexerToken<>(tokenEnum, completedVal, line);
                    break;
                }
            }

            val.append(c);
            c = currStr.charAt(++col);
        }

        if (!hasNonDigit) {
            if (forcedDecimal) {
                token = new LexerToken<>(EToken.DECIMAL, Double.parseDouble(val.toString()), line);
            }
            else {
                token = new LexerToken<>(EToken.INTEGER, Integer.parseInt(val.toString()), line);
            }
        }

        if (token != null) {
            tList.add(token);
            return true;
        }
        return false;
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
        return true;
    }

    private boolean tryOperatorCommentOrLogic() throws TokenParseException {
        char c = currStr.charAt(col);
        char cA = currStr.charAt(col + 1);
        LexerToken<?> token = null;
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
                token = new LexerToken<>(EToken.SINGLE_COMMENT, null, line);
                col++;
            }
            else if (cA == '*') {
                token = new LexerToken<>(EToken.FLEX_COMMENT_START, null, line);
                newLineIgnoringToken = EToken.FLEX_COMMENT_START;
                col++;
            }
        }
        else if (c == '*' && cA == '/') {
            if (newLineIgnoringToken != EToken.FLEX_COMMENT_START) {
                throw new TokenParseException("Rogue multiline comment terminator at line " + line);
            }
            newLineIgnoringToken = null;
            token = new LexerToken<>(EToken.FLEX_COMMENT_END, null, line);
        }

        token = (token != null ? token : new LexerToken<>(EToken.OPERATOR, c, line));
        tList.add(token);
        return true;
    }

    private boolean trySpecialChar() {
        char c = currStr.charAt(col);
        LexerToken<?> token = null;
        switch (c) {
            case '!':
                if (currStr.charAt(col + 1) == '=') {
                    token = new LexerToken<>(EToken.COMPARATOR, c + "=", line);
                    col++;
                }
                else {
                    token = new LexerToken<>(EToken.NOT, null, line);
                }
                break;
            case '=':
                if (currStr.charAt(col + 1) == '=') {
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
}
