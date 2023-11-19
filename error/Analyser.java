import Lexer.*;
import Parser.GrammaticalAnalyser;

import java.io.IOException;

public class Analyser {
    private LexicalAnalyser lexicalAnalyser;
    private GrammaticalAnalyser grammaticalAnalyser;

    public Analyser() throws IOException {
        lexicalAnalyser = new LexicalAnalyser();
        grammaticalAnalyser = new GrammaticalAnalyser(lexicalAnalyser.getTokens());
        grammaticalAnalyser.printError(new FileProcessor("testfile.txt","error.txt").getWriter());

    }
}
