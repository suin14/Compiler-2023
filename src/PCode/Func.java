package PCode;

public class Func {
    private int index;
    private int args;

    public Func(int index, int args) {
        this.index = index;
        this.args = args;
    }

    public int index() {
        return index;
    }

    public int args() {
        return args;
    }

}
