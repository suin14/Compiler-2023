package PCode;

import PCode.Operator.Operator;

public class PCode {
    private Operator type;
    private Object value1 = null;
    private Object value2 = null;

    public PCode(Operator type) {
        this.type = type;
    }

    public PCode(Operator type, Object value1) {
        this.type = type;
        this.value1 = value1;
    }

    public PCode(Operator type, Object value1, Object value2) {
        this.type = type;
        this.value1 = value1;
        this.value2 = value2;
    }

    public void setValue2(Object value2) {
        this.value2 = value2;
    }

    public Operator getType() {
        return type;
    }

    public Object getValue1() {
        return value1;
    }

    public Object getValue2() {
        return value2;
    }

    @Override
    public String toString() {
        if (type.equals(Operator.LABEL)) {
            return value1.toString() + ": ";
        }
        if (type.equals(Operator.FUNC)) {
            return "FUNC @" + value1.toString() + ":";
        }
        if (type.equals(Operator.CALL)) {
            return "$" + value1.toString();
        }
        if (type.equals(Operator.PRINT)) {
            return type + " " + value1;
        }
        String a = value1 != null ? value1.toString() : "";
        String b = value2 != null ? ", " + value2.toString() : "";
        return type + " " + a + b;
    }
}
