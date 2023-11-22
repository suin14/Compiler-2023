package PCode;

import PCode.Func;
import PCode.Operator.Operator;
import PCode.RetInfo;
import PCode.Var;

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
    private final FileWriter writer;
    private final Scanner scanner;

    public Executor(ArrayList<PCode> codes, FileWriter writer, Scanner scanner) {
        this.codes = codes;
        this.writer = writer;
        this.scanner = scanner;
        for (int i = 0; i < codes.size(); i++) {
            PCode code = codes.get(i);
            // get main function address
            if (code.getType().equals(Operator.MAIN)) {
                mainAddress = i;
            }
            // get all label
            if (code.getType().equals(Operator.LABEL)) {
                labelTable.put((String) code.getValue1(), i);
            }
            //get all function
            if (code.getType().equals(Operator.FUNC)) {
                funcTable.put((String) code.getValue1(), new Func(i, (int) code.getValue2()));
            }
        }
    }

    private void push(int i) {
        stack.add(i);
    }

    private int pop() {
        return stack.remove(stack.size() - 1);
    }

    private Var getVar(String ident) {
        if (varTable.containsKey(ident)) {
            return varTable.get(ident);
        } else {
            return retInfos.get(0).getVarTable().get(ident);
        }
    }

    public void run() {
        int callArgsNum = 0;
        int nowArgsNum = 0;
        boolean mainFlag = false;
        ArrayList<Integer> rparas = new ArrayList<>();
        for (; eip < codes.size(); eip++) {
            PCode code = codes.get(eip);
            switch (code.getType()) {
                case LABEL, END_FUNC -> {

                }
                case VAR -> {
                    Var var = new Var(stack.size());
                    varTable.put((String) code.getValue1(), var);
                }
                case PUSH -> {
                    if (code.getValue1() instanceof Integer) {
                        push((Integer) code.getValue1());
                    }
                }
                case POP -> {
                    int value = pop();
                    int address = pop();
                    stack.set(address, value);
                }
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
                case EQ -> {
                    int b = pop();
                    int a = pop();
                    push(a == b ? 1 : 0);
                }
                case NE -> {
                    int b = pop();
                    int a = pop();
                    push(a != b ? 1 : 0);
                }
                case LT -> {
                    int b = pop();
                    int a = pop();
                    push(a < b ? 1 : 0);
                }
                case LTE -> {
                    int b = pop();
                    int a = pop();
                    push(a <= b ? 1 : 0);
                }
                case GT -> {
                    int b = pop();
                    int a = pop();
                    push(a > b ? 1 : 0);
                }
                case GTE -> {
                    int b = pop();
                    int a = pop();
                    push(a >= b ? 1 : 0);
                }
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
                case NEG -> push(-pop());
                case POS -> push(pop());
                case JZ -> {
                    if (stack.get(stack.size() - 1) == 0) {
                        eip = labelTable.get((String) code.getValue1());
                    }
                }
                case JNZ -> {
                    if (stack.get(stack.size() - 1) != 0) {
                        eip = labelTable.get((String) code.getValue1());
                    }
                }
                case JMP -> eip = labelTable.get((String) code.getValue1());
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
                    Var para = new Var(rparas.get(rparas.size() - callArgsNum + nowArgsNum));
                    int n = (int) code.getValue2();
                    para.setDimension(n);
                    if (n == 2) {
                        para.setDim2(pop());
                    }
                    varTable.put((String) code.getValue1(), para);
                    nowArgsNum++;
                    if (nowArgsNum == callArgsNum) {
                        rparas.subList(rparas.size() - callArgsNum, rparas.size()).clear();
                    }
                }
                case RET -> {
                    int n = (int) code.getValue1();
                    RetInfo info = retInfos.remove(retInfos.size() - 1);
                    eip = info.getEip();
                    varTable = info.getVarTable();
                    callArgsNum = info.getNeedArgsNum();
                    nowArgsNum = info.getNowArgsNum();
                    if (n == 1) {
                        stack.subList(info.getStackPtr() + 1 - info.getParaNum(), stack.size() - 1).clear();
                    } else {
                        stack.subList(info.getStackPtr() + 1 - info.getParaNum(), stack.size()).clear();
                    }
                }
                case CALL -> {
                    Func func = funcTable.get((String) code.getValue1());
                    retInfos.add(new RetInfo(eip, varTable, stack.size() - 1, func.args(), func.args(), nowArgsNum));
                    eip = func.index();
                    varTable = new HashMap<>();
                    callArgsNum = func.args();
                    nowArgsNum = 0;
                }
                case RPARAM -> {
                    int n = (int) code.getValue1();
                    if (n == 0) {
                        rparas.add(stack.size() - 1);
                    } else {
                        rparas.add(stack.get(stack.size() - 1));
                    }
                }
                case GETINT -> {
                    int a = scanner.nextInt();
                    push(a);
                }
                case PRINT -> {
                    String s = (String) code.getValue1();
                    int n = (int) code.getValue2();
                    StringBuilder builder = new StringBuilder();
                    ArrayList<Integer> paras = new ArrayList<>();
                    int index = n - 1;
                    for (int i = 0; i < n; i++) {
                        paras.add(pop());
                    }
                    for (int i = 0; i < s.length(); i++) {
                        if (i + 1 < s.length()) {
                            if (s.charAt(i) == '%' && s.charAt(i + 1) == 'd') {
                                builder.append(paras.get(index--).toString());
                                i++;
                                continue;
                            }
                        }
                        builder.append(s.charAt(i));
                    }
                    prints.add(builder.substring(1, builder.length() - 1));
                }
                case VALUE -> {
                    Var var = getVar((String) code.getValue1());
                    int n = (int) code.getValue2();
                    int address = getAddress(var, n);
                    push(stack.get(address));
                }
                case ADDRESS -> {
                    Var var = getVar((String) code.getValue1());
                    int n = (int) code.getValue2();
                    int address = getAddress(var, n);
                    push(address);
                }
                case DIMVAR -> {
                    Var var = getVar((String) code.getValue1());
                    int n = (int) code.getValue2();
                    var.setDimension(n);
                    if (n == 1) {
                        int i = pop();
                        var.setDim1(i);
                    }
                    if (n == 2) {
                        int j = pop(), i = pop();
                        var.setDim1(i);
                        var.setDim2(j);
                    }
                }
                case PLACEHOLDER -> {
                    Var var = getVar((String) code.getValue1());
                    int n = (int) code.getValue2();
                    if (n == 0) {
                        push(0);
                    }
                    if (n == 1) {
                        for (int i = 0; i < var.getDim1(); i++) {
                            push(0);
                        }
                    }
                    if (n == 2) {
                        for (int i = 0; i < var.getDim1() * var.getDim2(); i++) {
                            push(0);
                        }
                    }
                }
                case EXIT -> {
                    return;
                }
            }
        }
    }

    private int getAddress(Var var, int intType) {
        // reminder: n is the actual type
        // for example, to int a[3][2] ,a[0][0] is 0, a[0] is 1, a is 2
        // so when search for address, the real [] num is defined dim - n
        int address = 0;
        int n = var.getDimension() - intType;
        if (n == 0) {
            address = var.getIndex();
        }
        if (n == 1) {
            int i = pop();
            if (var.getDimension() == 1) {
                address = var.getIndex() + i;
            } else {
                address = var.getIndex() + var.getDim2() * i;
            }
        }
        if (n == 2) {
            int j = pop();
            int i = pop();
            address = var.getIndex() + var.getDim2() * i + j;
        }
        return address;
    }

    public void print() throws IOException {
        //System.out.println("—————————输出———————————");
        for (String s : prints) {
            writer.write(s);
            //System.out.print(s);
        }
        writer.flush();
        writer.close();
    }
}