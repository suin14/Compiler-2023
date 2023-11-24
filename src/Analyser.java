import Lexer.FileProcessor;
import Lexer.LexicalAnalyser;
import Parser.GrammaticalAnalyser;
import PCode.Executor;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Analyser {

    public Analyser() throws IOException {
        //File inputFile = new File("input.txt");Scanner input = new Scanner(inputFile);
        Scanner input = new Scanner(System.in);
        LexicalAnalyser lexicalAnalyser = new LexicalAnalyser();
        GrammaticalAnalyser grammaticalAnalyser = new GrammaticalAnalyser(lexicalAnalyser.getTokens());

        // handleErrors(grammaticalAnalyser); // 错误处理

        executePCode(grammaticalAnalyser, input); // 执行 PCode
    }

    private void handleErrors(GrammaticalAnalyser grammaticalAnalyser) throws IOException {
        grammaticalAnalyser.printError(new FileProcessor("testfile.txt","error.txt").getWriter());
    }

    private void executePCode(GrammaticalAnalyser grammaticalAnalyser, Scanner input) throws IOException {
        Executor pCodeExecutor = new Executor(grammaticalAnalyser.getCodes(), input);
        pCodeExecutor.run();
        pCodeExecutor.print();
    }
}
