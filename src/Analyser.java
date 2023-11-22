import Lexer.*;
import Parser.GrammaticalAnalyser;
import PCode.Executor;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Analyser {
    private LexicalAnalyser lexicalAnalyser;
    private GrammaticalAnalyser grammaticalAnalyser;
    private Executor PCodeExecutor;
    private FileProcessor fileProcessor;
    private Scanner input;

    public Analyser() throws IOException {
        File inputFile = new File("input.txt");
        input = new Scanner(inputFile);
        fileProcessor = new FileProcessor("testfile.txt", "pcoderesult.txt");
        lexicalAnalyser = new LexicalAnalyser();
<<<<<<< Updated upstream
        grammaticalAnalyser = new GrammaticalAnalyser(lexicalAnalyser.getWords());
        grammaticalAnalyser.printWords(new FileProcessor("testfile.txt","output.txt").getWriter());
=======
        grammaticalAnalyser = new GrammaticalAnalyser(lexicalAnalyser.getTokens());
        //grammaticalAnalyser.printError(new FileProcessor("testfile.txt","error.txt").getWriter());
        PCodeExecutor = new Executor(grammaticalAnalyser.getCodes(), fileProcessor.getWriter(), input);
        PCodeExecutor.run();
        PCodeExecutor.print();
>>>>>>> Stashed changes
    }
}
