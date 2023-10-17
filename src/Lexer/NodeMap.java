package Lexer;

import java.util.HashMap;

public class NodeMap {
    private HashMap<String, String> nodeMap;

    public NodeMap() {
        nodeMap = new HashMap<>();
        nodeMap.put("main", "MAINTK");
        nodeMap.put("const", "CONSTTK");
        nodeMap.put("int", "INTTK");
        nodeMap.put("break", "BREAKTK");
        nodeMap.put("continue", "CONTINUETK");
        nodeMap.put("if", "IFTK");
        nodeMap.put("else", "ELSETK");
        nodeMap.put("!", "NOT");
        nodeMap.put("&&", "AND");
        nodeMap.put("||", "OR");
        nodeMap.put("for", "FORTK");
        nodeMap.put("getint", "GETINTTK");
        nodeMap.put("printf", "PRINTFTK");
        nodeMap.put("return", "RETURNTK");
        nodeMap.put("+", "PLUS");
        nodeMap.put("-", "MINU");
        nodeMap.put("void", "VOIDTK");
        nodeMap.put("*", "MULT");
        nodeMap.put("/", "DIV");
        nodeMap.put("%", "MOD");
        nodeMap.put("<", "LSS");
        nodeMap.put("<=", "LEQ");
        nodeMap.put(">", "GRE");
        nodeMap.put(">=", "GEQ");
        nodeMap.put("==", "EQL");
        nodeMap.put("!=", "NEQ");
        nodeMap.put("=", "ASSIGN");
        nodeMap.put(";", "SEMICN");
        nodeMap.put(",", "COMMA");
        nodeMap.put("(", "LPARENT");
        nodeMap.put(")", "RPARENT");
        nodeMap.put("[", "LBRACK");
        nodeMap.put("]", "RBRACK");
        nodeMap.put("{", "LBRACE");
        nodeMap.put("}", "RBRACE");
    }

    public String getType(String ident) {
        return nodeMap.get(ident);
    }

    public boolean isNode(String str) {
        return nodeMap.containsKey(str);
    }
}
