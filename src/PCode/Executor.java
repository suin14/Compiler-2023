package PCode;

import PCode.Operator.Operator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                HashMap<String, Var> varTableFromRetInfo = retInfo.varTable();
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
                case LABEL, END_FUNC -> {

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
                        handleControlInstruction(code, stack, labelTable);
                case FUNC -> {
                    if (!mainFlag) {
                        eip = mainAddress - 1;
                    }
                }
                case MAIN -> {
                    mainFlag = true;
                    retInfos.add(new RetInfo(codes.size(), varTable, stack.size() - 1, 0, 0, 0));
                    varTable = new HashMap<>();
                }
                case PARAM -> {
                    Var para = new Var(rparams.get(rparams.size() - callArgsNum + nowArgsNum));
                    int n = (int) code.getValue2();
                    para.setDimension(n);
                    if (n == 2) {
                        para.setDim2(pop());
                    }
                    varTable.put((String) code.getValue1(), para);
                    nowArgsNum++;
                    if (nowArgsNum == callArgsNum) {
                        rparams.subList(rparams.size() - callArgsNum, rparams.size()).clear();
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
                case RET -> {
                    int n = (int) code.getValue1();
                    RetInfo info = retInfos.remove(retInfos.size() - 1);
                    eip = info.eip();
                    varTable = info.varTable();
                    callArgsNum = info.needArgsNum();
                    nowArgsNum = info.nowArgsNum();
                    if (n == 1) {
                        stack.subList(info.stackPtr() + 1 - info.paramNum(), stack.size() - 1).clear();
                    } else {
                        stack.subList(info.stackPtr() + 1 - info.paramNum(), stack.size()).clear();
                    }
                }
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
        switch (code.getType()) {
            case ADD -> {
                int b = pop();
                int a = pop();
                push(a + b);
            }
            case SUB -> {
                int b = pop();
                int a = pop();
                push(a - b);
            }
            case MUL -> {
                int b = pop();
                int a = pop();
                push(a * b);
            }
            case DIV -> {
                int b = pop();
                int a = pop();
                push(a / b);
            }
            case MOD -> {
                int b = pop();
                int a = pop();
                push(a % b);
            }
            case NEG -> push(-pop());
            case POS -> push(pop());
        }
    }

    private void handleComparisonInstruction(PCode code) {
        int b = pop();
        int a = pop();
        int result = 0;
        switch (code.getType()) {
            case EQ -> result = a == b ? 1 : 0;
            case NE -> result = a != b ? 1 : 0;
            case LT -> result = a < b ? 1 : 0;
            case LTE -> result = a <= b ? 1 : 0;
            case GT -> result = a > b ? 1 : 0;
            case GTE -> result = a >= b ? 1 : 0;
        }
        push(result);
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

    private void handleControlInstruction(PCode code, ArrayList<Integer> stack, HashMap<String, Integer> labelTable) {
        int value = stack.get(stack.size() - 1);
        String label = (String) code.getValue1();
        switch (code.getType()) {
            case JZ -> {
                if (value == 0) {
                    eip = labelTable.get(label);
                }
            }
            case JNZ -> {
                if (value != 0) {
                    eip = labelTable.get(label);
                }
            }
            case JMP -> eip = labelTable.get(label);
        }
    }

    // 使用 Matcher 查找 %d 并逐个替换为参数列表中的值，并将结果添加到 StringBuilder 中。
    // 最后将 StringBuilder 转换为字符串，并将其添加到 prints 列表中。
    private void handlePrintInstruction(PCode code) {
        String s = (String) code.getValue1();
        int n = (int) code.getValue2();
        ArrayList<Integer> params = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            params.add(pop());
        }

        StringBuilder builder = new StringBuilder();
        int index = n - 1;
        Matcher matcher = Pattern.compile("%d").matcher(s); // 创建匹配 "%d" 的 Matcher 对象
        while (matcher.find() && index >= 0) {
            builder.append(s, 0, matcher.start()).append(params.get(index--));
            s = s.substring(matcher.end());
            matcher = Pattern.compile("%d").matcher(s);
        }
        builder.append(s);

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
                int dim = (int) code.getValue2();
                if (var != null) {
                    var.setDimension(dim);
                }
                // 一维数组
                if (dim == 1) {
                    int i = pop();
                    if (var != null) {
                        var.setDim1(i);
                    }
                }
                // 二维数组
                if (dim == 2) {
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
                int intType = (int) code.getValue2();
                if (intType == 0) {
                    push(0);
                }
                // 将第一维度的零推送到栈中
                if (intType == 1) {
                    if (var != null) {
                        for (int i = 0; i < var.getDim1(); i++) {
                            push(0);
                        }
                    }
                }
                // 将二维数组的零推送到栈中
                if (intType == 2) {
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
        // 根据不同的维度差值调用相应的方法来计算地址
        return switch (dimension) {
            case 0 -> getAddressForDimensionZero(var);
            case 1 -> getAddressForDimensionOne(var);
            case 2 -> getAddressForDimensionTwo(var);
            default -> throw new IllegalArgumentException("Invalid dimension: " + dimension);
        };
    }

    private int getAddressForDimensionZero(Var var) {
        // 返回零维度变量的索引
        return var.getIndex();
    }

    private int getAddressForDimensionOne(Var var) {
        // 获取栈顶的值
        int i = pop();
        if (var.getDimension() == 1) {
            // 如果是一维数组，返回索引+偏移量
            return var.getIndex() + i;
        } else {
            // 如果是二维数组，返回索引+偏移量
            return var.getIndex() + var.getDim2() * i;
        }
    }

    private int getAddressForDimensionTwo(Var var) {
        // 获取栈顶的两个值
        int j = pop();
        int i = pop();
        // 返回索引+偏移量
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
