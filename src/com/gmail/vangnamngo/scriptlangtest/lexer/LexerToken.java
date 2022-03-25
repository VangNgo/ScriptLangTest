package com.gmail.vangnamngo.scriptlangtest.lexer;

public class LexerToken<E> {

    public final Token tokenType;
    public final E token;
    public final int line;
    public final int col;

    public LexerToken(Token tokenType, E token, int line, int col) {
        this.tokenType = tokenType;
        this.token = token;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        return "LexerToken{" + tokenType + " >> ( " + token + " ) at " + line + "," + col + "}";
    }
}
