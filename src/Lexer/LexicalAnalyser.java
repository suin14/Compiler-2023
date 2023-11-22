package Lexer;

import java.io.IOException;
import java.util.ArrayList;

public class LexicalAnalyser {
    private final String code;
    private int line = 1; // 用于报错行数
    private int index = 0;
    private final ArrayList<Token> token = new ArrayList<>();

    public LexicalAnalyser() throws IOException {
        code = new FileProcessor("testfile.txt","error.txt").getCode();
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

    private void analyse() {
        Character current;
        while ((current = getChar()) != null) {
            switch (current) {
                case ' ', '\r', '\t' -> {
                }
                case '+', '-', '*', '%', '(', ')', '[', ']', '{', '}', ',', ';' -> token.add(new Token(current, line));
                case '/' -> analyseSlash();
                case '=', '<', '>', '!' -> analyseRelation(current);
                case '"' -> analyseQuot();
                case '&', '|' -> analyseLogic(current);
                default -> {
                    if (Character.isDigit(current)) {
                        analyseDigit(current);
                    } else if (Character.isLetter(current) || current == '_') {
                        analyseLetter(current);
                    }
                }
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
            token.add(new Token(current + "=", line));
        } else {
            // 处理单目运算符：=, <, >, !
            token.add(new Token(String.valueOf(current), line));
            backup();
        }
    }


    private void analyseQuot() {
        Character current;
        int flag = 0;
        StringBuilder buffer = new StringBuilder(); //保存读取到的内容
        while ((current = getChar()) != null) {
            if (current == '"') { // 寻找到结束的‘"’
                token.add(new Token(String.valueOf(Word.STRCON), "\"" + String.valueOf(buffer) + "\"", line));
                return;
            } else {
                if (current == '\\') {
                    flag = 1;
                } else {
                    if (flag == 1 && current == 'n') {
                        buffer.append("\n");
                    } else {
                        buffer.append(current);
                    }
                    flag = 0;
                }
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
        token.add(new Token(String.valueOf(Word.INTCON), builder.toString(), line));
    }

    private void analyseLetter(char pre) {
        StringBuilder builder = new StringBuilder(String.valueOf(pre));
        Character current;
        while ((current = getChar()) != null) {
            if (Character.isLetter(current) || current == '_' || Character.isDigit(current)) {
                builder.append(current);
            } else {
                backup();
                if (new NodeMap().isNode(builder.toString())) {   // 先比对是不是关键字
                    token.add(new Token(builder.toString(),line));
                } else {
                    token.add(new Token((String.valueOf(Word.IDENFR)), builder.toString(),line));  // 变量名
                }
                return;
            }
        }
    }

    public ArrayList<Token> getTokens() {
        return token;
    }
}