import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileProcessor {
    private String code;
    private FileWriter writer;

    public FileProcessor() {
        try (FileReader inputFile = new FileReader("testfile.txt", StandardCharsets.UTF_8);
                FileWriter outputFile = new FileWriter("output.txt", StandardCharsets.UTF_8)) {
            code = transferFileToCode(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String transferFileToCode(Reader reader) throws IOException {
        StringBuilder buffer = new StringBuilder();
        try (BufferedReader bf = new BufferedReader(reader)) {
            String line;
            while ((line = bf.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        }
        return buffer.toString();
    }

    public String getCode() {
        return code;
    }

    public FileWriter getWriter() {
        return writer;
    }
}
