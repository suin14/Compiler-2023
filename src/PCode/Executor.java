package PCode;

import PCode.Operator.Operator;

import java.io.FileWriter;
import java.util.*;

public class Executor {
    private final ArrayList<PCode> codes;
    private final ArrayList<RetInfo> retInfos = new ArrayList<>();
    private final ArrayList<Integer> stack = new ArrayList<>();
    private int eip = 0; // 当前正在执行的指令位置
    private HashMap<String, Var> varTable = new HashMap<>();
    private final HashMap<String, Func> funcTable = new HashMap<>();
    private final HashMap<String, Integer> labelTable = new HashMap<>();

    private int mainAddress;

    private final ArrayList<String> prints = new ArrayList<>();
    private FileWriter writer;
    private Scanner scanner;

    public Executor(ArrayList<PCode> codes, FileWriter writer, Scanner scanner) {
        this.codes = codes;
        this.writer = writer;
        this.scanner = scanner;
        buildExecutionStructure();
    }

    private void buildExecutionStructure() {
        for (int i = 0; i < codes.size(); i++) {
            PCode code = codes.get(i);
            Operator codeType = code.getType();
            // 是主函数
            if (codeType.equals(Operator.MAIN)) {
                mainAddress = i;
            }
            if (codeType.equals(Operator.LABEL)) {
                String labelValue = (String) code.getValue1();
                if (!labelTable.containsKey(labelValue)) {
                    labelTable.put(labelValue, i);
                }
            }
            if (codeType.equals(Operator.FUNC)) {
                String funcName = (String) code.getValue1();
                if (!funcTable.containsKey(funcName)) {
                    funcTable.put(funcName, new Func(i, (int) code.getValue2()));
                }
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
        int codesSize = codes.size();
        for (; eip < codesSize; eip++) {
            PCode code = codes.get(eip);
            executeCode(code);
        }
    }

    private void executeCode(PCode code) {
        int needArgs = 0;
        int nowArgs = 0;
        boolean mainFlag = false;
        ArrayList<Integer> rparams = new ArrayList<>();
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
            // 进栈时是先a后b
            // 所以先出栈的是b，然后才是a
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
            case GT -> {
                int b = pop();
                int a = pop();
                push(a > b ? 1 : 0);
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
                if (!stack.isEmpty()) {
                    int stackTop = stack.get(stack.size() - 1);
                    if (stackTop == 0) {
                        String labelName = (String) code.getValue1();
                        if (labelTable.containsKey(labelName)) {
                            int labelAddress = labelTable.get(labelName);
                            eip = labelAddress;
                        }
                    }
                }
            }
            case JNZ -> {
                if (!stack.isEmpty()) {
                    int stackTop = stack.get(stack.size() - 1);
                    if (stackTop != 0) {
                        String labelName = (String) code.getValue1();
                        if (labelTable.containsKey(labelName)) {
                            int labelAddress = labelTable.get(labelName);
                            eip = labelAddress;
                        }
                    }
                }
            }
            case JMP -> {
                String labelName = (String) code.getValue1();
                if (labelTable.containsKey(labelName)) {
                    int labelAddress = labelTable.get(labelName);
                    eip = labelAddress;
                }
            }
            case FUNC -> {
                if (!mainFlag) {
                    // 边界情况: 确保 mainAddress 的值合法
                    if (mainAddress >= 1) {
                        eip = mainAddress - 1;
                    }
                }
            }
            case MAIN -> {
                mainFlag = true;
                retInfos.add(new RetInfo(codes.size(), varTable, stack.size() - 1, 0, 0, 0));
                varTable = new HashMap<>();
            }
            case PARAM -> {
                Var param = new Var(rparams.get(rparams.size() - needArgs + nowArgs));
                // 设置参数的维度
                int dimension = (int) code.getValue2();
                param.setDimension(dimension);
                // 如果参数维度为 2，则设置第二维度的值为堆栈顶部的值
                if (dimension == 2) {
                    param.setDimensionValue(2, pop());
                }
                // 将参数放入 varTable 中，并增加参数计数器 nowArgs
                varTable.put((String) code.getValue1(), param);
                nowArgs++;
                // 如果已经收集了所有需要的参数，从 rparams 中移除这些参数
                if (nowArgs == needArgs) {
                    rparams.subList(rparams.size() - needArgs, rparams.size()).clear();
                }
            }
            case RET -> {
                int n = (int) code.getValue1();
                // 从 retInfos 中获取最后一个 RetInfo 对象，并提取信息
                RetInfo infoToRemove = retInfos.remove(retInfos.size() - 1);
                // 恢复执行指针、变量表和参数数
                eip = infoToRemove.getEip();
                varTable = infoToRemove.getVarTable();
                needArgs = infoToRemove.getNeedArgsNum();
                nowArgs = infoToRemove.getNowArgsNum();
                // 根据 n 的值，从堆栈中移除参数
                if (!stack.isEmpty()) {
                    int startIndex = infoToRemove.getStackPtr() + 1 - infoToRemove.getParaNum();
                    int endIndex = (n == 1) ? stack.size() - 1 : stack.size();
                    if (startIndex >= 0 && startIndex < endIndex) {
                        stack.subList(startIndex, endIndex).clear();
                    }
                }
            }
            case CALL -> {
                Func calledFunction = funcTable.get((String) code.getValue1());
                RetInfo returnInfo = new RetInfo(eip, varTable, stack.size() - 1, calledFunction.args(), calledFunction.args(), nowArgs);
                retInfos.add(returnInfo);
                eip = calledFunction.index();
                varTable = new HashMap<>();
                needArgs = calledFunction.args();
                nowArgs = 0;
            }
            case RPARAM -> {
                int n = (int) code.getValue1();
                // 如果 n 为 0，则将 stack.size() - 1 添加到 rparams 列表中；否则，将堆栈顶部元素添加到 rparams 列表中
                if (!stack.isEmpty()) {
                    int paramToAdd = (n == 0) ? stack.size() - 1 : stack.get(stack.size() - 1);
                    rparams.add(paramToAdd);
                }
            }
            case GETINT -> {
                try {
                    int a = scanner.nextInt();
                    push(a);
                } catch (InputMismatchException e) {
                    // 处理输入不匹配的异常情况
                    System.err.println("Invalid input: Please enter an integer.");
                } catch (NoSuchElementException e) {
                    // 处理未找到元素的异常情况
                    System.err.println("No such element found.");
                }
            }
            case PRINT -> {
                String s = (String) code.getValue1();
                int n = (int) code.getValue2();
                StringBuilder builder = new StringBuilder();
                ArrayList<Integer> paras = new ArrayList<>();
                if (stack.size() >= n) {
                    int index = n - 1;
                    for (int i = 0; i < n; i++) {
                        paras.add(pop());
                    }
                    for (int i = 0; i < s.length(); i++) {
                        if (i + 1 < s.length()) {
                            if (s.charAt(i) == '%' && s.charAt(i + 1) == 'd') {
                                if (index >= 0 && index < n) {
                                    builder.append(paras.get(index--).toString());
                                    i++;
                                    continue;
                                } else {
                                    // 处理参数数量与格式化字符串不匹配的异常情况
                                    System.err.println("Invalid format: Not enough parameters for formatting.");
                                    return;
                                }
                            }
                        }
                        builder.append(s.charAt(i));
                    }
                    prints.add(builder.toString());
                } else {
                    // 处理堆栈中参数不足的异常情况
                    System.err.println("Not enough parameters on the stack for printing.");
                }
            }
            case VALUE -> {
                String varName = (String) code.getValue1();
                int n = (int) code.getValue2();
                Var var = getVar(varName);
                int address = getAddress(var, n);
                // 确保堆栈不为空，并且地址有效
                if (!stack.isEmpty() && address >= 0 && address < stack.size()) {
                    push(stack.get(address));
                } else {
                    // 处理地址越界或堆栈为空的异常情况
                    System.err.println("Invalid stack address or empty stack.");
                }
            }
            case ADDRESS -> {
                String varName = (String) code.getValue1();
                int n = (int) code.getValue2();
                Var var = getVar(varName);
                int address = getAddress(var, n);
                if (address >= 0) {
                    push(address);
                } else {
                    // 处理变量不存在或地址越界的异常情况
                    System.err.println("Variable not found or invalid address.");
                }
            }
            case DIMVAR -> {
                String varName = (String) code.getValue1();
                int n = (int) code.getValue2();
                Var var = getVar(varName);
                var.setDimension(n);
                if (n == 1) {
                    if (!stack.isEmpty()) {
                        int i = pop();
                        var.setDimensionValue(1, i);
                    } else {
                        // 处理堆栈中没有足够参数的异常情况
                        System.err.println("Not enough parameters on the stack.");
                    }
                } else if (n == 2) {
                    if (stack.size() >= 2) {
                        int j = pop();
                        int i = pop();
                        var.setDimensionValue(1, i);
                        var.setDimensionValue(2, j);
                    } else {
                        // 处理堆栈中没有足够参数的异常情况
                        System.err.println("Not enough parameters on the stack.");
                    }
                }
            }
            case PLACEHOLDER -> {
                String varName = (String) code.getValue1();
                int n = (int) code.getValue2();
                Var var = getVar(varName);
                if (var != null) {
                    if (n == 0) {
                        push(0);
                    } else if (n == 1) {
                        for (int i = 0; i < var.getDimensionValue(1); i++) {
                            push(0);
                        }
                    } else if (n == 2) {
                        for (int i = 0; i < var.getDimensionValue(1) * var.getDimensionValue(2); i++) {
                            push(0);
                        }
                    } else {
                        // 处理无效的维度数
                        System.err.println("Invalid dimension: " + n);
                    }
                } else {
                    // 处理变量不存在的异常情况
                    System.err.println("Variable not found: " + varName);
                }
            }
            case EXIT -> {
            }
        }
    }


    private int getAddress(Var var, int intType) {
        int address = 0;
        int dimensionsDiff = var.getDimension() - intType;

        if (dimensionsDiff == 0) {
            address = var.getIndex();
        } else if (dimensionsDiff == 1) {
            if (!stack.isEmpty()) {
                int i = pop();
                if (var.getDimension() == 1) {
                    address = var.getIndex() + i;
                } else {
                    address = var.getIndex() + var.getDimensionValue(2) * i;
                }
            } else {
                // 处理堆栈中没有足够参数的异常情况
                System.err.println("Not enough parameters on the stack.");
            }
        } else if (dimensionsDiff == 2) {
            if (stack.size() >= 2) {
                int j = pop();
                int i = pop();
                address = var.getIndex() + var.getDimensionValue(2) * i + j;
            } else {
                // 处理堆栈中没有足够参数的异常情况
                System.err.println("Not enough parameters on the stack.");
            }
        } else {
            // 处理无效的维度数
            System.err.println("Invalid dimensions difference: " + dimensionsDiff);
        }
        return address;
    }

//    public void print() throws IOException {
//        //System.out.println("—————————输出———————————");
//        for (String s : prints) {
//            writer.write(s);
//            //System.out.print(s);
//        }
//        writer.flush();
//        writer.close();
//    }
}
