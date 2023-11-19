package PCode;

import PCode.Operator.Operator;

public class PCode<PC> {
    private final Operator type;
    private final PC value1;
    private final PC value2;

    public PCode(Operator type, PC value1, PC value2) {
        this.type = type;
        this.value1 = value1;
        this.value2 = value2;
    }

    public PCode(Operator type, PC value1) {
        this(type, value1, null);
    }

    public Operator getType() {
        return type;
    }

    public PC getValue1() {
        return value1;
    }

    public PC getValue2() {
        return value2;
    }
    
    public PCode<PC> withValue2(PC newValue2) {
        return new PCode<PC>(type, value1, newValue2);
    }

    @Override
    public String toString() {
        switch (type) {
            case LABEL:
                return value1.toString() + ": ";
            case FUNC:
                return "FUNC @" + value1.toString() + ":";
            case CALL:
                return "$" + value1.toString();
            case PRINT:
                return type + " " + value1;
            default:
                String a = (value1 != null) ? value1.toString() : "";
                String b = (value2 != null) ? ", " + value2.toString() : "";
                return type + " " + a + b;
        }
    }
}
