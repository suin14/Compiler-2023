public class Word {
    private String identification;
    private String content;
    private String type;
    private int line;


    public Word(String identification, int line) {    // 标识符
        this.identification = identification;
        this.type = new KeyWordMap().getType(this.identification);  // type从KeyWordMap中获取
        this.content = this.identification; // 读取到的内容
        this.line = line;
    }

    public Word(char identification, int line) {
        this.identification = String.valueOf(identification);
        this.type = new KeyWordMap().getType(this.identification);
        this.content = this.identification;
        this.line = line;
    }

    public Word(String type, String content, int line) {  // IDENFR, INTCON, STRCON
        this.type = type;
        this.content = content;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + " " + content;
    }

    public boolean typeEquals(String str) {
        return type.equals(str);
    }

    public boolean typeSymbolizeStmt() {
        return type.equals("IDENFR")
                || type.equals("LBRACE")
                || type.equals("IFTK")
                || type.equals("ELSETK")
                || type.equals("FORTK")
                || type.equals("FORSTMT")
                || type.equals("COND")
                || type.equals("WHILETK")
                || type.equals("BREAKTK")
                || type.equals("CONTINUETK")
                || type.equals("RETURNTK")
                || type.equals("PRINTFTK")
                || type.equals("SEMICN")
                || typeSymbolizeExp();
    }

    public boolean typeSymbolizeExp() {
        return type.equals("LPARENT")
                || type.equals("IDENFR")
                || type.equals("INTCON")
                || type.equals("STRCON")
                || type.equals("NOT")
                || type.equals("PLUS")
                || type.equals("MINU");
    }

    public boolean typeOfUnary() {
        return type.equals("PLUS")
                || type.equals("MINU")
                || type.equals("NOT");
    }

    public String getType() {
        return type;
    }

    public int getline() {
        return line;
    }
}

