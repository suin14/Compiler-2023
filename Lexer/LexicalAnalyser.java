import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class LexicalAnalyser {
    private String code;
    private int lineNum = 0;    // 行数，方便以后输出报错信息
    private int index = 0;
    private ArrayList<Word> words = new ArrayList<>();

    public LexicalAnalyser() throws IOException {
        code = new FileProcessor().getCode();
        analyse();
    }


    private Character getChar() {   // 读取一个字符
        if (index < code.length()) {
            char c = code.charAt(index);
            if (c == '\n') {
                lineNum++;
            }
            index++;
            return c;
        } else {
            return null;
        }
    }

    private void unGetChar() {  // 回退，避免漏读
        index--;
        char c = code.charAt(index);
        if (c == '\n') {
            lineNum--;
        }
    }

    private void analyse() throws IOException {
        Character c = null;
        while ((c = getChar()) != null) {
            if (c == ' ' || c == '\r' || c == '\t') {
                continue;
            } else if (c == '+' || c == '-' || c == '*' || c == '%') {
                words.add(new Word(c));
            } else if (c == '/') {
                analyseSlash();
            } else if (c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}') {
                words.add(new Word(c));
            } else if (c == '=' || c == '<' || c == '>' || c == '!') {
                analyseRelation(c);
            } else if (c == ',' || c == ';') {
                words.add(new Word(c));
            } else if (c == '"') {  //读取到字符串
                analyseCitation();
            } else if (c == '&' || c == '|') {
                analyseLogic(c);
            } else if (Character.isDigit(c)) {  //读取到数字
                analyseDigit(c);
            } else if (Character.isLetter(c) || c == '_') { //读取到变量名或关键字
                analyseLetter(c);
            }
        }
    }

    private void analyseSlash() {
        Character c = getChar();
        if (c == '/') {     // 单行注释‘//’
            do {
                c = getChar();
                if (c == null || c == '\n') {
                    return;
                }
            } while (true);
        } else if (c == '*') { // 多行注释‘/* */’
            do {
                c = getChar();
                if (c == null) {
                    return;
                }
                if (c == '*') {    // 寻找结束的‘*/’
                    c = getChar();
                    if (c == '/') {
                        return;
                    } else {
                        unGetChar();
                    }
                }
            } while (true);
        } else {
            words.add(new Word("/"));   // 只是'/'字符，非注释
            unGetChar();
        }
    }

    private void analyseRelation(char c) {
        if (c == '=') {
            c = getChar();
            if (c == '=') {
                words.add(new Word("=="));
            } else {
                unGetChar();
                words.add(new Word("="));
                return;
            }
        } else if (c == '<') {
            c = getChar();
            if (c == '=') {
                words.add(new Word("<="));
            } else {
                unGetChar();
                words.add(new Word("<"));
            }
        } else if (c == '>') {
            c = getChar();
            if (c == '=') {
                words.add(new Word(">="));
            } else {
                unGetChar();
                words.add(new Word(">"));
            }
        } else {
            c = getChar();
            if (c == '=') {
                words.add(new Word("!="));
            } else {
                unGetChar();
                words.add(new Word("!"));
            }
        }
    }

    private void analyseCitation() {
        Character c = null; // 用来读取字符（类比指针？）
        StringBuffer buffer = new StringBuffer(""); //保存读取到的内容
        while ((c = getChar()) != null) {
            if (c == '"') { // 寻找到结束的‘"’
                words.add(new Word("STRCON", "\"" + buffer + "\""));
                return;
            } else {
                buffer.append(c);
            }
        }
    }

    private void analyseLogic(char pre) {
        Character c = null;
        if ((c = getChar()) != null) {
            if (pre == '&') {
                if (c == '&') {
                    words.add(new Word("&&"));
                } else {
                    unGetChar();
                    words.add(new Word("&"));
                }
            } else {
                if (c == '|') {
                    words.add(new Word("||"));
                } else {
                    unGetChar();
                    words.add(new Word("|"));
                }
            }
        }
    }

    private void analyseDigit(char pre) {
        StringBuilder builder = new StringBuilder("" + pre);
        Character c = null;
        while ((c = getChar()) != null) {
            if (Character.isDigit(c)) {
                builder.append(c);
            } else {
                unGetChar();
                words.add(new Word("INTCON", builder.toString()));
                return;
            }
        }
    }

    private void analyseLetter(char pre) {
        StringBuilder builder = new StringBuilder("" + pre);
        Character c = null;
        while ((c = getChar()) != null) {
            if (Character.isLetter(c) || c == '_' || Character.isDigit(c)) {
                builder.append(c);
            } else {
                unGetChar();
                if (new KeyWordMap().isKey(builder.toString())) {   // 先比对是不是关键字
                    words.add(new Word(builder.toString()));
                } else {
                    words.add(new Word("IDENFR", builder.toString()));  // 变量名
                }
                return;
            }
        }
    }

    public void printWords(FileWriter writer) throws IOException {
        for (Word word : words) {
            writer.write(word.toString() + "\n");
        }
        writer.flush();
        writer.close();
    }
}
