import Lexer.*;
import Parser.GrammaticalAnalyser;
import PCode.Executor;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Analyser {
    private final LexicalAnalyser lexicalAnalyser;
    private final GrammaticalAnalyser grammaticalAnalyser;
    private final Executor PCodeExecutor;
    private final FileProcessor fileProcessor;
    private final Scanner input;

    public Analyser() throws IOException {
        File inputFile = new File("input.txt");
        input = new Scanner(inputFile);
        fileProcessor = new FileProcessor("testfile.txt", "pcoderesult.txt");
        lexicalAnalyser = new LexicalAnalyser();
        grammaticalAnalyser = new GrammaticalAnalyser(lexicalAnalyser.getTokens());
        // grammaticalAnalyser.printError(new FileProcessor("testfile.txt","error.txt").getWriter());
        PCodeExecutor = new Executor(grammaticalAnalyser.getCodes(), fileProcessor.getWriter(), input);
        PCodeExecutor.run();
        PCodeExecutor.print();
    }
}
