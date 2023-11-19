package Symbol;

import java.util.HashMap;
import Lexer.Token;

public class SymbolTable {
    private final HashMap<String, Symbol> symbolHashMap;

    public SymbolTable() {
        symbolHashMap = new HashMap<>();
    }

    public void addSymbol(String type, int intType, Token token) {
        symbolHashMap.put(token.getContent(), new Symbol(type, intType, token));
    }

    public boolean findSymbol(Token token) {
        return symbolHashMap.containsKey(token.getContent());
    }

    public Symbol getSymbol(Token token) {
        return symbolHashMap.get(token.getContent());
    }

    public boolean isConst(Token token) {
        return symbolHashMap.get(token.getContent()).getType().equals("const");
    }

    @Override
    public String toString() {
        return symbolHashMap.toString();
    }
}