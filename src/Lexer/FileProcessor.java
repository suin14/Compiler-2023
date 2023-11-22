package Lexer;

import java.io.*;
import java.util.Scanner;
import java.util.StringJoiner;

public class FileProcessor {
    private final String code;
    private final FileWriter writer;

    public FileProcessor(String inputFile, String outputFile) throws IOException {
        code = transferToCode(inputFile);
        writer = new FileWriter(outputFile);
        //errorWriter = new FileWriter(new File("error.txt"));
    }

    public static String transferToCode (String filename) throws IOException {
        InputStream stream = new FileInputStream(filename);
        //读取一个文件，并按行转化为带有'\n'的长字符串
        Scanner scanner = new Scanner(stream);
        StringJoiner stringJoiner = new StringJoiner("\n");
        //如果scanner的位置不是被读取文件的最后一行
        while (scanner.hasNextLine()) {
            stringJoiner.add(scanner.nextLine()); //读取下一行
        }
        scanner.close();
        stream.close();
        return stringJoiner.toString();
    }

    public String getCode() {
        return code;
    }

    public FileWriter getWriter() {
        return writer;
    }
}
