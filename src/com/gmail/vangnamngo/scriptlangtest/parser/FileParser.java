package com.gmail.vangnamngo.scriptlangtest.parser;

import com.gmail.vangnamngo.scriptlangtest.lexer.LexerToken;
import com.gmail.vangnamngo.scriptlangtest.lexer.Token;
import com.gmail.vangnamngo.scriptlangtest.script.Script;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FileParser {
    private final static List<Character> SPECIAL_CHARS = Arrays.asList(
            '!', '=', '+', '-', '*', '/', '%', '<', '>', '(', ')', '[', ']', '{', '}', '#', '.', ',', ':', '\\'
    );

    public static Script compileFile(File file) throws FileNotFoundException {
        ArrayList<LexerToken> tokenList = new ArrayList<>();
        Scanner scan = new Scanner(file);
        StringBuilder builder = new StringBuilder();
        int lineNum = 1;
        boolean hadDQuote = false;;
        boolean hadSQuote = false;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            // Quickly scan for special characters
            for (int colNum = 0; colNum < line.length(); colNum++) {
                char c = line.charAt(colNum);
                if (SPECIAL_CHARS.contains(c)) {
                    tokenList.add(getToken(c, lineNum, colNum));
                }
            }
        }
        return null;
    }

    public static LexerToken<Boolean> getToken(boolean token, int line, int col) {
        return new LexerToken<>(Token.BOOLEAN, token, line, col);
    }

    public static LexerToken<Long> getToken(long token, int line, int col) {
        return new LexerToken<>(Token.NUMBER, token, line, col);
    }

    public static LexerToken<Double> getToken(double token, int line, int col) {
        return new LexerToken<>(Token.NUMBER, token, line, col);
    }
    
    public static LexerToken<String> getToken(char token, int line, int col) {
        String sToken = String.valueOf(token);
        switch (sToken) {
            // Operators
            case "!":
                return new LexerToken<>(Token.NOT, sToken, line, col);
            case "=":
                return new LexerToken<>(Token.ASSIGN, sToken, line, col);
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
                return new LexerToken<>(Token.OPERATOR, sToken, line, col);
            // Comparators
            case "<":
            case ">":
                return new LexerToken<>(Token.COMPARATOR, sToken, line, col);
            // Flow
            case "(":
                return new LexerToken<>(Token.L_PAREN, sToken, line, col);
            case ")":
                return new LexerToken<>(Token.R_PAREN, sToken, line, col);
            case "[":
                return new LexerToken<>(Token.L_BRACKET, sToken, line, col);
            case "]":
                return new LexerToken<>(Token.R_BRACKET, sToken, line, col);
            case "{":
                return new LexerToken<>(Token.BLOCK_START, sToken, line, col);
            case "}":
                return new LexerToken<>(Token.BLOCK_END, sToken, line, col);
            // Header
            case "#":
                return new LexerToken<>(Token.HEADER_MARKER, sToken, line, col);
            // Misc
            case ".":
                return new LexerToken<>(Token.DOT, sToken, line, col);
            case ",":
                return new LexerToken<>(Token.COMMA, sToken, line, col);
            case ":":
                return new LexerToken<>(Token.COLON, sToken, line, col);
            case "\\":
                return new LexerToken<>(Token.BACKSLASH, sToken, line, col);
            default:
                // TODO: Error
        }
        return null;
    }

    public static LexerToken<String> getToken(String token, int line, int col) {
        switch (token) {
            // Comments
            case "//":
                return new LexerToken<>(Token.SINGLE_COMMENT, token, line, col);
            case "/*":
                return new LexerToken<>(Token.START_FLEX_COMMENT, token, line, col);
            case "*/":
                return new LexerToken<>(Token.END_FLEX_COMMENT, token, line, col);
            // Set operators
            case "+=":
            case "-=":
            case "*=":
            case "/=":
            case "%=":
                return new LexerToken<>(Token.SET_OPERATOR, token, line, col);
            // Comparators
            case "==":
            case "!=":
            case ">=":
            case "<=":
            case ">":
            case "<":
                return new LexerToken<>(Token.COMPARATOR, token, line, col);
            // Conditionals
            case "&&":
                return new LexerToken<>(Token.AND, token, line, col);
            case "||":
                return new LexerToken<>(Token.OR, token, line, col);
            // Protection modifiers
            case "public":
            case "group":
            case "directory":
            case "private":
            // Flow
            case "if":
            case "else if":
            case "else":
            case "while":
            case "for":
            case "continue":
            case "break":
                return new LexerToken<>(Token.KEYWORD, token, line, col);
            // Default
            default:
                return null;
                // TODO: Handle STRING vs IDENTIFIER differentiation in the actual lexing.
        }
    }
}
