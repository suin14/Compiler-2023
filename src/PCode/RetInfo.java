package PCode;

import java.util.HashMap;

public class RetInfo {
    private final int eip;
    private final HashMap<String, Var> varTable;
    private final int stackPtr;
    private final int paramNum;
    private final int needArgsNum;
    private final int nowArgsNum;

    public RetInfo(int eip, HashMap<String, Var> varTable, int stackPtr, int paramNum, int needArgsNum, int nowArgsNum) {
        this.eip = eip;
        this.varTable = new HashMap<>(varTable);
        this.stackPtr = stackPtr;
        this.paramNum = paramNum;
        this.needArgsNum = needArgsNum;
        this.nowArgsNum = nowArgsNum;
    }

    public int getEip() {
        return eip;
    }

    public HashMap<String, Var> getVarTable() {
        return new HashMap<>(varTable);
    }

    public int getStackPtr() {
        return stackPtr;
    }

    public int getParamNum() {
        return paramNum;
    }

    public int getNeedArgsNum() {
        return needArgsNum;
    }

    public int getNowArgsNum() {
        return nowArgsNum;
    }
}
