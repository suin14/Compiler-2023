public class Word {
    private String identification;
    private String content;
    private String type;

    public Word(String identification) {    // 标识符
        this.identification = identification;
        this.type = new KeyWordMap().getType(this.identification);  // type从KeyWordMap中获取
        this.content = this.identification; // 读取到的内容
    }

    public Word(char identification) {
        this.identification = String.valueOf(identification);
        this.type = new KeyWordMap().getType(this.identification);
        this.content = this.identification;
    }

    public Word(String type, String content) {  // IDENFR, INTCON, STRCON
        this.type = type;
        this.content = content;
    }

    @Override
    public String toString() {
        return type + " " + content;
    }
}
