package Error;

public class Errors {
    private final int line;
    private final String type;

    public Errors(int line, String type) {
        this.line = line;
        this.type = type;
    }

    public int getline() {
        return line;
    }

    @Override
    public String toString() {
        return line + " " + type;
    }
}
