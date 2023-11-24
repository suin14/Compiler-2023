package PCode;

import PCode.Operator.Operator;

public class PCode {
    private final Operator type;
    private Object value1;
    private Object value2;

    public PCode(Operator type) {
        this(type, null);
    }

    public PCode(Operator type, Object value1) {
        this(type, value1, null);
    }

    public PCode(Operator type, Object value1, Object value2) {
        this.type = type;
        this.value1 = value1;
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

    public void setValue1(Object value1) {
        this.value1 = value2;
    }

    public void setValue2(Object value2) {
        this.value2 = value2;
    }

    @Override
    public String toString() {
        String result = switch (type) {
            case LABEL -> value1 + ": ";
            case FUNC -> "FUNC @" + value1 + ":";
            case CALL -> "$" + value1;
            case PRINT -> type + " " + value1;
            default -> {
                String values = value1 != null ? value1.toString() : "";
                if (value2 != null) {
                    values += ", " + value2;
                }
                yield type + " " + values;
            }
        };
        return result;
    }
}
