package Error;

public class Errors {
    private int line;
    private String type;

    public Errors(int line, String type) {
        this.line = line;
        this.type = type;
    }

    public int getline() {
        return line;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return line + " " + type;
    }
}
