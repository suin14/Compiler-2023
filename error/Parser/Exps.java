package Parser;
import Lexer.Token;

import java.util.ArrayList;

public class Exps {
    private final ArrayList<ArrayList<Token>> tokens;
    private final ArrayList<Token> symboltable;

    public Exps(ArrayList<ArrayList<Token>> tokens, ArrayList<Token> symboltable) {
        this.tokens = tokens;
        this.symboltable = symboltable;
    }

    public ArrayList<ArrayList<Token>> getTokens() {
        return tokens;
    }

    public ArrayList<Token> getSymbols() {
        return symboltable;
    }

}
