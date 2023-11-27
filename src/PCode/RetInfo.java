package PCode;

import java.util.HashMap;

public record RetInfo(int eip, HashMap<String, Var> varTable, int stackPtr, int paramNum, int needArgsNum,
                      int nowArgsNum) {
    public RetInfo(int eip, HashMap<String, Var> varTable, int stackPtr, int paramNum, int needArgsNum, int nowArgsNum) {
        this.eip = eip;
        this.varTable = (HashMap<String, Var>) new HashMap<>(varTable);
        this.stackPtr = stackPtr;
        this.paramNum = paramNum;
        this.needArgsNum = needArgsNum;
        this.nowArgsNum = nowArgsNum;
    }

    @Override
    public HashMap<String, Var> varTable() {
        return new HashMap<>(varTable);
    }
}
