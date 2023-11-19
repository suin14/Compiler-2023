package Symbol;

import Lexer.Token;

public class Symbol {
    private final String type;
    private final int intType; // 0 -> int, 1 -> int[], 2 -> int[][]
    private final String content;

    public Symbol(String type, int intType, Token token) {
        this.type = type;
        this.intType = intType;
        this.content = token.getContent();
    }

    public String getType() {
        return type;
    }

    public int getIntType() {
        return intType;
    }

    @Override
    public String toString() {
        return content;
    }
}