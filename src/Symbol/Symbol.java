package Symbol;

import Lexer.Token;

public class Symbol {
    private String type;
    private int intType; // 0 -> int, 1 -> int[], 2 -> int[][]
    private String content;
    private int area = 0;

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

    public String getContent() {
        return content;
    }

    public int getArea() {
        return area;
    }

    @Override
    public String toString() {
        return content;
    }
}

