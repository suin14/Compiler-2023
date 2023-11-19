package Lexer;

public class Token {
    private String identification;
    private final String content;
    private final String type;
    private final int line;


    public Token(String identification, int line) {    // 标识符
        this.identification = identification;
        this.type = new NodeMap().getType(this.identification);  // type从NodeWordMap中获取
        this.content = this.identification; // 读取到的内容
        this.line = line;
    }

    public Token(char identification, int line) {
        this.identification = String.valueOf(identification);
        this.type = new NodeMap().getType(this.identification);
        this.content = this.identification;
        this.line = line;
    }

    public Token(String type, String content, int line) {  // IDENFR, INTCON, STRCON
        this.type = type;
        this.content = content;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + " " + content;
    }

    public boolean typeIs(String str) {
        return type.equals(str);
    }

    public boolean typeSymbolizeStmt() {
        return type.equals(String.valueOf(Word.IDENFR))
                || type.equals(String.valueOf(Word.LBRACE))
                || type.equals(String.valueOf(Word.IFTK))
                || type.equals(String.valueOf(Word.ELSETK))
                || type.equals(String.valueOf(Word.FORTK))
                || type.equals(String.valueOf(Word.BREAKTK))
                || type.equals(String.valueOf(Word.CONTINUETK))
                || type.equals(String.valueOf(Word.RETURNTK))
                || type.equals(String.valueOf(Word.PRINTFTK))
                || type.equals(String.valueOf(Word.SEMICN))
                || typeSymbolizeExp();
    }

    public boolean typeSymbolizeExp() {
        return type.equals(String.valueOf(Word.LPARENT))
                || type.equals(String.valueOf(Word.IDENFR))
                || type.equals(String.valueOf(Word.INTCON))
                || type.equals(String.valueOf(Word.PLUS))
                || type.equals(String.valueOf(Word.MINU))
                || type.equals((String.valueOf(Word.NOT)));
    }

    public boolean checkTypeStmt() {
        return type.equals(String.valueOf(Word.IFTK))
                || type.equals(String.valueOf(Word.ELSETK))
                || type.equals(String.valueOf(Word.FORTK))
                || type.equals(String.valueOf(Word.BREAKTK))
                || type.equals(String.valueOf(Word.CONTINUETK))
                || type.equals(String.valueOf(Word.RETURNTK))
                || type.equals(String.valueOf(Word.PRINTFTK))
                || type.equals(String.valueOf(Word.SEMICN));
    }

    public boolean checkNotInExp() {
        return type.equals(String.valueOf(Word.CONSTTK))
                || type.equals(String.valueOf(Word.INTTK))
                || type.equals(String.valueOf(Word.BREAKTK))
                || type.equals(String.valueOf(Word.CONTINUETK))
                || type.equals(String.valueOf(Word.IFTK))
                || type.equals(String.valueOf(Word.ELSETK))
                || type.equals(String.valueOf(Word.FORTK))
                || type.equals(String.valueOf(Word.GETINTTK))
                || type.equals(String.valueOf(Word.PRINTFTK))
                || type.equals(String.valueOf(Word.RETURNTK));
    }

    public String getType() {
        return type;
    }

    public int getline() {
        return line;
    }

    public String getContent() {
        return content;
    }

    public int cntFormat() {
        int cnt = 0;
        for (int i = 0; i < content.length(); i++) {
            if (i + 1 < content.length()) {
                char current = content.charAt(i);
                char currentNext = content.charAt(i + 1);
                if (isPercentD(current, currentNext)) {
                    cnt++;
                }
            }
        }
        return cnt;
    }

    public boolean checkFormat() {
        int len = content.length();
        for (int i = 0; i < len - 1; i++) {
            char current = content.charAt(i);
            char currentNext = content.charAt(i + 1);
            if (!isValidCharacter(current)) {
                if (isPercentD(current, currentNext)) {
                    continue;
                }
                return true;
            } else if (isBackslashWithoutNewline(current, currentNext)) {
                return true;
            }
        }
        return false;
    }

    // 检查字符是否为有效字符
    private boolean isValidCharacter(char c) {
        return (c == 32 || c == 33 || (c >= 40 && c <= 126));
    }

    // 检查是否是 "%d"
    private boolean isPercentD(char c1, char c2) {
        return (c1 == '%' && c2 == 'd');
    }

    // 检查反斜杠是否后面不是换行符
    private boolean isBackslashWithoutNewline(char c1, char c2) {
        return (c1 == '\\' && c2 != 'n');
    }

}

