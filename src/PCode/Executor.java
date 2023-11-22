package PCode;

import PCode.Operator.Operator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Executor {
    private final ArrayList<PCode> codes;
    private final ArrayList<RetInfo> retInfos = new ArrayList<>();
    private final ArrayList<Integer> stack = new ArrayList<>();
    private int eip = 0;
    private HashMap<String, Var> varTable = new HashMap<>();
    private final HashMap<String, Func> funcTable = new HashMap<>();
    private final HashMap<String, Integer> labelTable = new HashMap<>();

    private int mainAddress;

    private final ArrayList<String> prints = new ArrayList<>();
    private final Scanner scanner;

    public Executor(ArrayList<PCode> codes, Scanner scanner) {
        this.codes = codes;
        this.scanner = scanner;

        for (int i = 0; i < codes.size(); i++) {
            PCode code = codes.get(i);
            Operator codeType = code.getType();
            switch (codeType) {
                case MAIN -> mainAddress = i;
                case LABEL -> labelTable.put((String) code.getValue1(), i);
                case FUNC -> funcTable.put((String) code.getValue1(), new Func(i, (int) code.getValue2()));
            }
        }
    }


    private void push(int i) {
        stack.add(i);
    }

    private int pop() {
        if (stack.isEmpty()) {
            // 处理堆栈为空的情况
            return -1;
        }
        return stack.remove(stack.size() - 1);
    }

    private Var getVar(String ident) {
        Var variable = varTable.get(ident);
        if (variable != null) {
            return variable;
        } else {
            RetInfo retInfo = retInfos.get(0);
            if (retInfo != null) {
                HashMap<String, Var> varTableFromRetInfo = retInfo.getVarTable();
                if (varTableFromRetInfo != null) {
                    return varTableFromRetInfo.get(ident);
                }
            }
        }
        return null;
    }

    public void run() {
        int callArgsNum = 0;
        int nowArgsNum = 0;
        boolean mainFlag = false;
        ArrayList<Integer> rparams = new ArrayList<>();
        for (; eip < codes.size(); eip++) {
            PCode code = codes.get(eip);
            switch (code.getType()) {
                case LABEL -> {

                }
                case VAR ->
                        handleVarInstruction(code);
                case PUSH ->
                        handlePushInstruction(code);
                case POP ->
                        handlePopInstruction();
                case ADD, SUB, MUL, DIV, MOD, NEG, POS ->
                        handleArithmeticInstruction(code);
                case EQ, NE, LT, LTE, GT, GTE ->
                        handleComparisonInstruction(code);
                case AND, OR, NOT ->
                        handleLogicalInstruction(code);
                case JZ, JNZ, JMP ->
                        handleControlInstruction(code);
                case FUNC, MAIN, END_FUNC, PARAM, RPARAM, RET ->
                        handleFunctionInstruction(code, mainFlag, rparams, callArgsNum, nowArgsNum);
                case CALL -> {
                    Func func = funcTable.get((String) code.getValue1());
                    retInfos.add(new RetInfo(eip, varTable, stack.size() - 1, func.args(), func.args(), nowArgsNum));
                    eip = func.index();
                    varTable = new HashMap<>();
                    callArgsNum = func.args();
                }
                case GETINT -> {
                    int in = scanner.nextInt();
                    push(in);
                }
                case PRINT ->
                        handlePrintInstruction(code);
                case VALUE, ADDRESS, DIMVAR,PLACEHOLDER, EXIT ->
                        handleFlowControlInstruction(code);
            }
        }
    }

    private void handleVarInstruction(PCode code) {
        Var var = new Var(stack.size());
        varTable.put((String) code.getValue1(), var);
    }

    private void handlePushInstruction(PCode code) {
        if (code.getValue1() instanceof Integer) {
            push((Integer) code.getValue1());
        }
    }

    private void handlePopInstruction() {
        int value = pop();
        int address = pop();
        stack.set(address, value);
    }

    private void handleArithmeticInstruction(PCode code) {
        int b = pop();
        int a = pop();
        switch (code.getType()) {
            case ADD -> push(a + b);
            case SUB -> push(a - b);
            case MUL -> push(a * b);
            case DIV -> push(a / b);
            case MOD -> push(a % b);
            case NEG -> push(-pop());
            case POS -> push(pop());
        }
    }

    private void handleComparisonInstruction(PCode code) {
        int b = pop();
        int a = pop();
        switch (code.getType()) {
            case EQ -> push(a == b ? 1 : 0);
            case NE -> push(a != b ? 1 : 0);
            case LT -> push(a < b ? 1 : 0);
            case LTE -> push(a <= b ? 1 : 0);
            case GT -> push(a > b ? 1 : 0);
            case GTE -> push(a >= b ? 1 : 0);
        }
    }

    private void handleLogicalInstruction(PCode code) {
        switch (code.getType()) {
            case AND -> {
                boolean b = pop() != 0;
                boolean a = pop() != 0;
                push((a && b) ? 1 : 0);
            }
            case OR -> {
                boolean b = pop() != 0;
                boolean a = pop() != 0;
                push((a || b) ? 1 : 0);
            }
            case NOT -> {
                boolean a = pop() != 0;
                push((!a) ? 1 : 0);
            }
        }
    }

    private void handleControlInstruction(PCode code) {
        switch (code.getType()) {
            case JZ:
                if (stack.get(stack.size() - 1) == 0) {
                    eip = labelTable.get((String) code.getValue1());
                }
            case JNZ:
                if (stack.get(stack.size() - 1) != 0) {
                    eip = labelTable.get((String) code.getValue1());
                }
            case JMP:
                eip = labelTable.get((String) code.getValue1());

        }
    }

    private void handleFunctionInstruction(PCode code, boolean mainFlag, ArrayList<Integer> rparams, int callArgsNum, int nowArgsNum) {
        switch (code.getType()) {
            case FUNC -> {
                if (!mainFlag) {
                    eip = mainAddress - 1;
                }
            }
            case MAIN -> {
                retInfos.add(new RetInfo(codes.size(), varTable, stack.size() - 1, 0, 0, 0));
                varTable = new HashMap<>();
            }
            case PARAM -> {
                Var param = new Var(rparams.get(rparams.size() - callArgsNum + nowArgsNum));
                int n = (int) code.getValue2();
                param.setDimension(n);
                if (n == 2) {
                    param.setDim2(pop());
                }
                varTable.put((String) code.getValue1(), param);
                nowArgsNum++;
                if (nowArgsNum == callArgsNum) {
                    rparams.subList(rparams.size() - callArgsNum, rparams.size()).clear();
                }
            }
            case RET -> {
                int n = (int) code.getValue1();
                RetInfo info = retInfos.remove(retInfos.size() - 1);
                eip = info.getEip();
                varTable = info.getVarTable();
                if (n == 1) {
                    stack.subList(info.getStackPtr() + 1 - info.getParamNum(), stack.size() - 1).clear();
                } else {
                    stack.subList(info.getStackPtr() + 1 - info.getParamNum(), stack.size()).clear();
                }
            }
            case RPARAM -> {
                int n = (int) code.getValue1();
                if (n == 0) {
                    rparams.add(stack.size() - 1);
                } else {
                    rparams.add(stack.get(stack.size() - 1));
                }
            }
            case END_FUNC -> {
            }
        }
    }

    private void handlePrintInstruction(PCode code) {
        String s = (String) code.getValue1();
        int n = (int) code.getValue2();
        StringBuilder builder = new StringBuilder();
        ArrayList<Integer> params = new ArrayList<>();
        int index = n - 1;
        for (int i = 0; i < n; i++) {
            params.add(pop());
        }
        for (int i = 0; i < s.length(); i++) {
            if (i + 1 < s.length() && s.charAt(i) == '%' && s.charAt(i + 1) == 'd') {
                builder.append(params.get(index--).toString());
                i++;
                continue;
            }
            builder.append(s.charAt(i));
        }
        prints.add(builder.substring(1, builder.length() - 1));
    }

    private void handleFlowControlInstruction(PCode code) {
        switch (code.getType()) {
            case VALUE -> {
                Var var = getVar((String) code.getValue1());
                int n = (int) code.getValue2();
                int address = 0;
                if (var != null) {
                    address = getAddress(var, n);
                }
                push(stack.get(address));
            }
            case ADDRESS -> {
                Var var = getVar((String) code.getValue1());
                int n = (int) code.getValue2();
                int address = 0;
                if (var != null) {
                    address = getAddress(var, n);
                }
                push(address);
            }
            case DIMVAR -> {
                Var var = getVar((String) code.getValue1());
                int n = (int) code.getValue2();
                if (var != null) {
                    var.setDimension(n);
                }
                if (n == 1) {
                    int i = pop();
                    if (var != null) {
                        var.setDim1(i);
                    }
                }
                if (n == 2) {
                    int j = pop(), i = pop();
                    if (var != null) {
                        var.setDim1(i);
                    }
                    if (var != null) {
                        var.setDim2(j);
                    }
                }
            }
            case PLACEHOLDER -> {
                Var var = getVar((String) code.getValue1());
                int n = (int) code.getValue2();
                if (n == 0) {
                    push(0);
                }
                if (n == 1) {
                    if (var != null) {
                        for (int i = 0; i < var.getDim1(); i++) {
                            push(0);
                        }
                    }
                }
                if (n == 2) {
                    if (var != null) {
                        for (int i = 0; i < var.getDim1() * var.getDim2(); i++) {
                            push(0);
                        }
                    }
                }
            }
            case EXIT -> {
            }
        }
    }

    private int getAddress(Var var, int intType) {
        int dimension = var.getDimension() - intType;
        return switch (dimension) {
            case 0 -> getAddressForDimensionZero(var);
            case 1 -> getAddressForDimensionOne(var);
            case 2 -> getAddressForDimensionTwo(var);
            default -> throw new IllegalArgumentException("Invalid dimension: " + dimension);
        };
    }

    private int getAddressForDimensionZero(Var var) {
        return var.getIndex();
    }

    private int getAddressForDimensionOne(Var var) {
        int i = pop();
        if (var.getDimension() == 1) {
            return var.getIndex() + i;
        } else {
            return var.getIndex() + var.getDim2() * i;
        }
    }

    private int getAddressForDimensionTwo(Var var) {
        int j = pop();
        int i = pop();
        return var.getIndex() + var.getDim2() * i + j;
    }

    public void print() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("pcoderesult.txt"))) {
            for (String s : prints) {
                writer.write(s);
            }
        }
    }
}
