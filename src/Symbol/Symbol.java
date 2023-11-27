package Symbol;

import Lexer.Token;

public class Symbol {
    private final String type;
    private final int intType; // 0 -> int, 1 -> int[], 2 -> int[][]
    private final String content;

    private final int areaID;

    public Symbol(String type, int intType, Token token, int areaID) {
        this.type = type;
        this.intType = intType;
        this.content = token.getContent();
        this.areaID = areaID;
    }

    public String getType() {
        return type;
    }

    public int getIntType() {
        return intType;
    }

    public Integer getAreaID() {
        return areaID;
    }

    @Override
    public String toString() {
        return content;
    }

}

