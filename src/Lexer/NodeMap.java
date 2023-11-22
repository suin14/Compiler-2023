package Lexer;

import java.util.HashMap;

public class NodeMap {
    private final HashMap<String, String> nodeMap;

    public NodeMap() {
        nodeMap = new HashMap<>();
        nodeMap.put("main", String.valueOf(Word.MAINTK));
        nodeMap.put("const", String.valueOf(Word.CONSTTK));
        nodeMap.put("int", String.valueOf(Word.INTTK));
        nodeMap.put("break", String.valueOf(Word.BREAKTK));
        nodeMap.put("continue", String.valueOf(Word.CONTINUETK));
        nodeMap.put("if", String.valueOf(Word.IFTK));
        nodeMap.put("else", String.valueOf(Word.ELSETK));
        nodeMap.put("!", String.valueOf(Word.NOT));
        nodeMap.put("&&", String.valueOf(Word.AND));
        nodeMap.put("||", String.valueOf(Word.OR));
        nodeMap.put("for", String.valueOf(Word.FORTK));
        nodeMap.put("getint", String.valueOf(Word.GETINTTK));
        nodeMap.put("printf", String.valueOf(Word.PRINTFTK));
        nodeMap.put("return", String.valueOf(Word.RETURNTK));
        nodeMap.put("+", String.valueOf(Word.PLUS));
        nodeMap.put("-", String.valueOf(Word.MINU));
        nodeMap.put("void", String.valueOf(Word.VOIDTK));
        nodeMap.put("*", String.valueOf(Word.MULT));
        nodeMap.put("/", String.valueOf(Word.DIV));
        nodeMap.put("%", String.valueOf(Word.MOD));
        nodeMap.put("<", String.valueOf(Word.LSS));
        nodeMap.put("<=", String.valueOf(Word.LEQ));
        nodeMap.put(">", String.valueOf(Word.GRE));
        nodeMap.put(">=", String.valueOf(Word.GEQ));
        nodeMap.put("==", String.valueOf(Word.EQL));
        nodeMap.put("!=", String.valueOf(Word.NEQ));
        nodeMap.put("=", String.valueOf(Word.ASSIGN));
        nodeMap.put(";", String.valueOf(Word.SEMICN));
        nodeMap.put(",", String.valueOf(Word.COMMA));
        nodeMap.put("(", String.valueOf(Word.LPARENT));
        nodeMap.put(")", String.valueOf(Word.RPARENT));
        nodeMap.put("[", String.valueOf(Word.LBRACK));
        nodeMap.put("]", String.valueOf(Word.RBRACK));
        nodeMap.put("{", String.valueOf(Word.LBRACE));
        nodeMap.put("}", String.valueOf(Word.RBRACE));
    }

    public String getType(String ident) {
        return nodeMap.get(ident);
    }

    public boolean isNode(String str) {
        return nodeMap.containsKey(str);
    }
}
