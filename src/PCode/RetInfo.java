package PCode;

import java.util.HashMap;

public class RetInfo {
    private final int eip;
    private final HashMap<String, Var> varTable;
    private final int stackPtr;
    private final int paramNum;
    private final int needArgsNum;
    private final int nowArgsNum;

    public RetInfo(Integer eip, HashMap<String, Var> varTable, Integer stackPtr, Integer paramNum, Integer needArgsNum, Integer nowArgsNum) {
        this.eip = eip;
        this.varTable = varTable;
        this.stackPtr = stackPtr;
        this.paramNum = paramNum;
        this.needArgsNum = needArgsNum;
        this.nowArgsNum = nowArgsNum;
    }

    public Integer getEip() {
        return eip;
    }

    public HashMap<String, Var> getVarTable() {
        return varTable;
    }

    public Integer getStackPtr() {
        return stackPtr;
    }

    public Integer getParaNum() {
        return paramNum;
    }

    public Integer getNeedArgsNum() {
        return needArgsNum;
    }

    public Integer getNowArgsNum() {
        return nowArgsNum;
    }
}
