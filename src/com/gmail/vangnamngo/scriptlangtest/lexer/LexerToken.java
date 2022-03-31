package com.gmail.vangnamngo.scriptlangtest.lexer;

public class LexerToken<E> {

    public final EToken tokenType;
    public final E token;
    public final int line;

    public LexerToken(EToken tokenType, E token, int line) {
        this.tokenType = tokenType;
        this.token = token;
        this.line = line;
    }

    @Override
    public String toString() {
        return "LexerToken{" + tokenType + " >> ( " + token + " ), " + line + "}";
    }
}
