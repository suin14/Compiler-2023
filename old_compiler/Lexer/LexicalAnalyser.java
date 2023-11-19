package Lexer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class LexicalAnalyser {
    private String code;
    private int line = 0;    // 行数，方便以后输出报错信息
    private int index = 0;
    private ArrayList<Token> token = new ArrayList<>();

    public LexicalAnalyser() throws IOException {
        code = new FileProcessor("testfile.txt","output.txt").getCode();
        analyse();
    }


    private Character getChar() {   // 读取一个字符
        if (index < code.length()) {
            char current = code.charAt(index);
            if (current == '\n') {
                line++;
            }
            index++;
            return current;
        } else {
            return null;
        }
    }

    private void backup() {  // 回退，避免漏读
        index--;
        char current = code.charAt(index);
        if (current == '\n') {
            line--;
        }
    }

    private void analyse() throws IOException {
        Character current = null;
        while ((current = getChar()) != null) {
            switch (current) {
                case ' ':
                case '\r':
                case '\t':
                    continue;
                case '+':
                case '-':
                case '*':
                case '%':
                case '(':
                case ')':
                case '[':
                case ']':
                case '{':
                case '}':
                case ',':
                case ';':
                    token.add(new Token(current, line));
                    break;
                case '/':
                    analyseSlash();
                    break;
                case '=':
                case '<':
                case '>':
                case '!':
                    analyseRelation(current);
                    break;
                case '"':
                    analyseQuot();
                    break;
                case '&':
                case '|':
                    analyseLogic(current);
                    break;
                default:
                    if (Character.isDigit(current)) {
                        analyseDigit(current);
                    } else if (Character.isLetter(current) || current == '_') {
                        analyseLetter(current);
                    }
                    break;
            }
        }
    }

    private void analyseSlash() {
        Character current = getChar();

        if (current == '/') {
            // 单行注释
            while (current != null && current != '\n') {
                current = getChar();
            }
        } else if (current == '*') {
            // 多行注释
            while (true) {
                while (current != null && current != '*') {
                    current = getChar();
                }
                if (current == null) {
                    return;
                }
                current = getChar();
                if (current == '/') {
                    return;
                }
            }
        } else {
            token.add(new Token("/", line)); // 字符'/'
            backup();
        }
    }

    private void analyseRelation(char current) {
        Character next = getChar();

        if (next == '=') {
            // 处理关系运算符：==, <=, >=, !=
            token.add(new Token(String.valueOf(current) + "=", line));
        } else {
            // 处理单目运算符：=, <, >, !
            token.add(new Token(String.valueOf(current), line));
            backup();
        }
    }


    private void analyseQuot() {
        Character current = null;
        StringBuffer buffer = new StringBuffer(""); //保存读取到的内容
        while ((current = getChar()) != null) {
            if (current == '"') { // 寻找到结束的‘"’
                token.add(new Token("STRCON", "\"" + buffer + "\"",line));
                return;
            } else {
                buffer.append(current);
            }
        }
    }


    private void analyseLogic(char pre) {
        Character current = getChar();
        if (current == null) {
            return;
        }

        String operator = String.valueOf(pre) + current;

        if (operator.equals("&&") || operator.equals("||")) {
            token.add(new Token(operator, line));
        } else {
            backup();
            token.add(new Token(String.valueOf(pre), line));
        }
    }


    private void analyseDigit(char pre) {
        StringBuilder builder = new StringBuilder(String.valueOf(pre));
        Character current;

        while (Character.isDigit(current = getChar())) {
            builder.append(current);
        }

        backup();
        token.add(new Token("INTCON", builder.toString(), line));
    }

    private void analyseLetter(char pre) {
        StringBuilder builder = new StringBuilder("" + pre);
        Character current = null;
        while ((current = getChar()) != null) {
            if (Character.isLetter(current) || current == '_' || Character.isDigit(current)) {
                builder.append(current);
            } else {
                backup();
                if (new NodeMap().isNode(builder.toString())) {   // 先比对是不是关键字
                    token.add(new Token(builder.toString(),line));
                } else {
                    token.add(new Token("IDENFR", builder.toString(),line));  // 变量名
                }
                return;
            }
        }
    }

    public void printWords(FileWriter writer) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            for (Token word : token) {
                bufferedWriter.write(word.toString());
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Token> getWords() {
        return token;
    }
}
