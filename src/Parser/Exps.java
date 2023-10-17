package Parser;
import Lexer.Token;

import java.util.ArrayList;

public class Exps {
    private ArrayList<ArrayList<Token>> tokens;
    private ArrayList<Token> symbols;

    public Exps(ArrayList<ArrayList<Token>> tokens, ArrayList<Token> symbols) {
        this.tokens = tokens;
        this.symbols = symbols;
    }

    public ArrayList<ArrayList<Token>> getTokens() {
        return tokens;
    }

    public ArrayList<Token> getSymbols() {
        return symbols;
    }

}
