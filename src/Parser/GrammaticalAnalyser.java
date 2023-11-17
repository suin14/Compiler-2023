package Parser;
import Lexer.Token;
import Symbol.*;
import Error.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class GrammaticalAnalyser {
    private ArrayList<Token> tokens;
    private int index;
    private Token current;
    private ArrayList<String> grammar;

    private HashMap<Integer, SymbolTable> symboltable = new HashMap<>();
    private HashMap<String, Function> functions = new HashMap<>();
    private ArrayList<Errors> errors = new ArrayList<>();
    private int area = -1;
    private boolean needReturn = false;
    private int forFlag = 0;

    public GrammaticalAnalyser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        index = 0;
        grammar = new ArrayList<>();
        analyseCompUnit();
    }

    private void getToken() {
        current = tokens.get(index);
        grammar.add(current.toString());
        index++;
    }

    private void getTokenWithoutGrammar() {
        current = tokens.get(index);
        index++;
    }

    private Token getNext() {
        // 检查列表是否为空或者索引是否越界
        if (index >= tokens.size() || index < 0) {
            // 处理索引越界的情况，比如抛出异常或者返回默认值
            // 这里简单返回 null，你可以根据实际情况选择适当的处理方式
            return null;
        }
        return tokens.get(index);
    }

    private void addArea() {
        area++;
        symboltable.put(area, new SymbolTable());
    }

    private void removeArea() {
        symboltable.remove(area);
        area--;
    }

//    private void analyseCompUnit() { // CompUnit → {Decl} {FuncDef} MainFuncDef
//        addArea();
//        Token nextToken = getNext();
//        while (nextToken.typeIs("CONSTTK") || (nextToken.typeIs("INTTK") && tokens.get(index + 1).typeIs("IDENFR")
//                && !tokens.get(index + 2).typeIs("LPARENT"))) { // {Decl}
//            analyseDecl();
//            nextToken = getNext();
//        }
//        while (nextToken.typeIs("VOIDTK") || ((nextToken.typeIs("INTTK") && !tokens.get(index + 1).typeIs("MAINTK")))) { // {FuncDef}
//            analyseFuncDef();
//            nextToken = getNext();
//        }
//        if (nextToken.typeIs("INTTK") && tokens.get(index + 1).typeIs("MAINTK")) { // MainFuncDef
//            analyseMainFuncDef();
//        } else {
//            error();
//        }
//        removeArea();
//        grammar.add("<CompUnit>"); // 编译单元
//    }
    private void analyseCompUnit() {
        addArea();
        Token nextToken = getNext();

        // 修复边界检查问题
        while (index < tokens.size() && (nextToken.typeIs("CONSTTK") || (nextToken.typeIs("INTTK")
                && index + 1 < tokens.size() && tokens.get(index + 1).typeIs("IDENFR")
                && index + 2 < tokens.size() && !tokens.get(index + 2).typeIs("LPARENT")))) {
            analyseDecl();
            nextToken = getNext();
        }

        // 修复边界检查问题
        while (index < tokens.size() && (nextToken.typeIs("VOIDTK") || ((nextToken.typeIs("INTTK")
                && index + 1 < tokens.size() && !tokens.get(index + 1).typeIs("MAINTK"))))) {
            analyseFuncDef();
            nextToken = getNext();
        }

        // 修复边界检查问题
        if (index < tokens.size() && nextToken.typeIs("INTTK") && index + 1 < tokens.size() && tokens.get(index + 1).typeIs("MAINTK")) {
            analyseMainFuncDef();
        } else {
            error();
        }

        removeArea();
        grammar.add("<CompUnit>"); // 编译单元
    }

    private void analyseDecl() { // Decl → ConstDecl | VarDecl
        Token nextToken = getNext();
        if (nextToken.typeIs("CONSTTK")) { // ConstDecl
            analyseConstDecl();
        } else if (nextToken.typeIs("INTTK")) { // VarDecl
            analyseVarDecl();
        } else {
            error();
        }
    }

    private void analyseConstDecl() { // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        getToken(); // const
        if (getNext().typeIs("INTTK")){
            analyseBType(); // Btype
        } else {
            error();
        }
        analyseConstDef(); // ConstDef
        Token nextToken = getNext();
        while (nextToken.typeIs("COMMA")) {
            getToken(); // ,
            analyseConstDef(); // ConstDef
            nextToken = getNext();
        }
        if (getNext().typeIs("SEMICN")) {
            getToken();// ;
        } else {
            error("i"); // 缺少分号
        }
        grammar.add("<ConstDecl>");
    }

    private void analyseBType() { // BType → 'int'
        getToken();
    }

    // 函数名或者变量名在当前作用域下重复定义。
    private boolean checkSymbolInArea(Token token) {
        return symboltable.get(area).findSymbol(token);
    }

    private boolean checkSymbol(Token token) {
        for (SymbolTable s : symboltable.values()) {
            if (s.findSymbol(token)) {
                return true;
            }
        }
        return false;
    }

    private void addSymbol(Token token, String node, int intType) {
        symboltable.get(area).addSymbol(node, intType, token);
    }

    private void analyseConstDef() { // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        getToken(); // Ident
        Token ident = current;
        if (checkSymbolInArea(current)){
            error("b"); // 名字重定义
        }
        int intType = 0;
        Token nextToken = getNext();
        while (nextToken.typeIs("LBRACK")) {
            intType++; // 改变数据类型
            getToken(); // [
            analyseConstExp(getExp()); // ConstExp
            getToken(); // ]
            //check RBrack
            if (!current.typeIs("RBRACK")) {
                error("k");
            }
            nextToken = getNext();
        }
        addSymbol(ident,"const", intType);
        getToken(); // =
        analyseConstInitVal(); // ConstInitVal
        grammar.add("<ConstDef>"); // 定义数组
    }

    private void analyseConstInitVal() { // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        Token nextToken = getNext();
        if (nextToken.typeIs("LBRACE")) {
            getToken(); // {
            nextToken = getNext();
            if (!nextToken.typeIs("RBRACE")) {
                analyseConstInitVal(); // ConstInitVal
                nextToken = getNext();
                while (nextToken.typeIs("COMMA")) {
                    getToken(); // ,
                    analyseConstInitVal(); // ConstInitVal
                    nextToken = getNext();
                }
            }
            getToken(); // }
        } else {
            analyseConstExp(getExp()); // ConstExp
        }
        grammar.add("<ConstInitVal>"); // 全局变量声明
    }

    private void analyseVarDecl() { // VarDecl → BType VarDef { ',' VarDef } ';'
        getToken(); // Btype
        analyseVarDef(); // VarDef
        Token nextToken = getNext();
        while (nextToken.typeIs("COMMA")) {
            getToken(); // ,
            analyseVarDef(); // VarDef
            nextToken = getNext();
        }
        if (!getNext().typeIs("SEMICN")) {
            error("i"); // 缺少分号
        } else {
            getToken(); // ;
        }
        grammar.add("<VarDecl>");
    }

    private void analyseVarDef() { // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
        getToken(); // Ident
        Token ident = current;
        if (checkSymbolInArea(current)) {
            error("b"); // 名字重定义
        }
        int intType = 0;
        Token nextToken = getNext();
        while (nextToken.typeIs("LBRACK")) {
            intType++;
            getToken(); // [
            analyseConstExp(getExp()); // ConstExp
            getToken(); // ]
            if (!current.typeIs("RBRACK")) {
                error("k");
            }
            nextToken = getNext();
        }
        if (nextToken.typeIs("ASSIGN")) {
            getToken(); // =
            analyseInitVal(); // InitVal
        }
        addSymbol(ident,"var",intType);
        grammar.add("<VarDef>"); // 定义变量
    }

    private void analyseInitVal() { // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
        Token nextToken = getNext();
        if (nextToken.typeIs("LBRACE")) {
            getToken(); // {
            nextToken = getNext();
            if (!nextToken.typeIs("RBRACE")) {
                analyseInitVal(); // InitVal
                nextToken = getNext();
                while (nextToken.typeIs("COMMA")) {
                    getToken(); // ,
                    analyseInitVal(); // InitVal
                    nextToken = getNext();
                }
            }
            getToken(); // }
        } else {
            analyseExp(getExp()); // Exp
        }
        grammar.add("<InitVal>"); // 变量声明
    }

    private void analyseFuncDef() { // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        //analyseFuncType(); // FuncType
        Function function;
        ArrayList<Integer> params = new ArrayList<>();
        String returnType = analyseFuncType();
        getToken(); // Ident
        if (functions.containsKey(current.getContent())) {
            error("b"); //名字重定义
        }
        function = new Function(current, returnType);
        addArea();
        getToken(); // (
        Token nextToken = getNext();
        if (nextToken.typeIs("VOIDTK")||nextToken.typeIs("INTTK")) {
            params = analyseFuncFParams();
        }
        if (!getNext().typeIs("RPARENT")) {
            error("j"); // 缺少右小括号’)’
        } else {
            getToken(); //)
        }
        function.setParams(params);
        functions.put(function.getContent(), function);
        needReturn = function.getReturnType().equals("int");
        boolean isReturn = analyseBlock(true);
        if (needReturn && !isReturn) {
            error("g");
        }
        removeArea();
        grammar.add("<FuncDef>"); // 函数定义
    }

    private void analyseMainFuncDef() { // MainFuncDef → 'int' 'main' '(' ')' Block
        getToken(); // int
        getToken(); // main
        if (functions.containsKey(current.getContent())) {
            error("b"); // 名字重定义
        } else {
            Function func = new Function(current,"int");
            func.setParams(new ArrayList<>());
            functions.put("main",func);
        }
        getToken(); // (
        if (!getNext().typeIs("RPARENT")) {
            error("j"); //缺少右小括号’)’
        } else {
            getToken(); //)
        }
        needReturn = true;
        boolean isReturn = analyseBlock(false);
        if (needReturn && !isReturn) {
            error("g"); // 有返回值的函数缺少return语句
        }
        grammar.add("<MainFuncDef>"); // main函数定义
    }

    private String analyseFuncType() { // FuncType → 'void' | 'int'
        getToken(); // void | int
        grammar.add("<FuncType>"); // 覆盖两种函数类型
        return current.getContent();
    }

    private ArrayList<Integer> analyseFuncFParams() { // FuncFParams → FuncFParam { ',' FuncFParam }
        ArrayList<Integer> params = new ArrayList<>();
        int paramType = analyseFuncFParam(); // FuncFParam
        params.add(paramType);
        Token nextToken = getNext();
        while (nextToken.typeIs("COMMA")) {
            getToken(); // ,
            paramType = analyseFuncFParam(); // FuncFParam
            params.add(paramType);
            nextToken = getNext();
        }
        grammar.add("<FuncFParams>"); // 函数参数声明
        return params;
    }

    private int analyseFuncFParam() { // BType Ident ['[' ']' { '[' ConstExp ']' }]
        int paramType = 0;
        getToken(); // Btype
        getToken(); // Ident
        Token ident = current;
        if (checkSymbol(current)) {
            error("b"); // 名字重定义
        }
        Token nextToken = getNext();
        if (nextToken.typeIs("LBRACK")) {
            paramType++;
            getToken(); // [
            if (!getNext().typeIs("RBRACK")) {
                error("k"); // 缺少右中括号’]’
            } else {
                getToken(); // ]
            }
            nextToken = getNext();
            while (nextToken.typeIs("LBRACK")) {
                paramType++;
                getToken(); // [
                analyseConstExp(getExp()); // ConstExp
                if (!getNext().typeIs("RBRACK")) {
                    error("k"); // 缺少右中括号’]’
                } else {
                    getToken(); // ]
                }
                nextToken = getNext();
            }
        }
        addSymbol(ident,"param", paramType);
        grammar.add("<FuncFParam>"); // 定义函数形参
        return paramType;
    }

    private boolean analyseBlock(boolean fromFunc) { // Block → '{' { BlockItem } '}'
        getToken(); // {
        if (!fromFunc) {
            addArea();
        }
        Token nextToken = getNext();
        boolean isReturn = false;
        while (nextToken.typeIs("CONSTTK") || nextToken.typeIs("INTTK") || nextToken.typeSymbolizeStmt()) {
            if (nextToken.typeIs("CONSTTK") || nextToken.typeIs("INTTK")) {
                isReturn = analyseBlockItem(); // BlockItem
            } else {
                isReturn = analyseStmt();
            }
            nextToken = getNext();
        }
        getToken(); // }
        if (!fromFunc) {
            removeArea();
        }
        grammar.add("<Block>"); // 语句块
        return isReturn;
    }

    private boolean analyseBlockItem() { // BlockItem → Decl | Stmt
        Token nextToken = getNext();
        boolean isReturn = false;
        if (nextToken.typeIs("CONSTTK") || nextToken.typeIs("INTTK")) {
            analyseDecl(); // Decl
        } else {
            isReturn = analyseStmt(); // Stmt
        }
        return isReturn;
    }

    // 检查分号
    private void checkSemicn() {
        if (getNext().typeIs("SEMICN")) {
            getToken(); // ;
        } else {
            error("i"); //缺少分号
        }
    }

    // 检查是不是const
    private void checkConst(Token token) {
        if (isConst(token)) {
            error("h", token.getline()); // 不能改变常量的值
        }
    }

    private boolean isConst(Token token) {
        return symboltable.values().stream()
                .anyMatch(symbol -> symbol.findSymbol(token) && symbol.isConst(token));
    }

    private boolean analyseStmt() {
        boolean isReturn = false;
        Token nextToken = getNext();
        if (nextToken.typeIs("IDENFR")) { // LVal '=' Exp ';' | LVal '=' 'getint''('')'';'
            ArrayList<Token> exp = getExp();
            if (getNext().typeIs("ASSIGN")) {
                analyseLVal(exp); // LVal
                checkConst(nextToken);
                getToken(); // =
                if (getNext().typeIs("GETINTTK")) { // 'getint''('')'';'
                    getToken(); // getint
                    getToken(); // (
                    if (!getNext().typeIs("RPARENT")) {
                        error("j"); //缺少右小括号’)’
                    } else {
                        getToken(); //)
                    }
                    checkSemicn(); // ;
                } else {
                    analyseExp(getExp()); // Exp
                    checkSemicn(); // ;
                }
            } else {
                analyseExp(exp);
                checkSemicn(); // ;
            }
        } else if (nextToken.typeSymbolizeExp()) { // [Exp] ';'
            analyseExp(getExp());
            checkSemicn(); // ;
        } else if (nextToken.typeIs("LBRACE")) { // Block
            analyseBlock(false);
        } else if (nextToken.typeIs("IFTK")) { // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            getToken(); // if
            getToken(); // (
            analyseCond(); // Cond
            if (!getNext().typeIs("RPARENT")) {
                error("j"); //缺少右小括号’)’
            } else {
                getToken(); //)
            }
            analyseStmt(); // Stmt
            nextToken = getNext();
            if (nextToken.typeIs("ELSETK")) {
                getToken(); // else
                analyseStmt(); // Stmt
            }
        } else if (nextToken.typeIs("FORTK")) { // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            getToken(); // for
            forFlag++;
            getToken(); // (
            nextToken = getNext();
            if (nextToken.typeIs("IDENFR")) { // ForStmt
                analyseForStmt();
            }
            getToken(); // ;
            nextToken = getNext();
            if (nextToken.typeSymbolizeExp()) { // Cond
                analyseCond();
            }
            getToken(); // ;
            nextToken = getNext();
            if (nextToken.typeIs("IDENFR")) { // ForStmt
                analyseForStmt();
            }
            if (!getNext().typeIs("RPARENT")) {
                error("j"); // 缺少右小括号’)’
            } else {
                getToken(); //)
            }
            forFlag--;
            analyseStmt(); // Stmt
        } else if (nextToken.typeIs("BREAKTK")) { // 'break' ';'
            getToken(); // break
            //getToken(); // ;
            if (forFlag == 0) {
                error("m"); // 在非循环块中使用break和continue语句
            }
            checkSemicn(); //;
        } else if (nextToken.typeIs("CONTINUETK")) { // 'continue' ';'
            getToken(); // continue
            //getToken(); // ;
            if (forFlag == 0) {
                error("m"); // 在非循环块中使用break和continue语句
            }
            checkSemicn(); //;
        } else if (nextToken.typeIs("RETURNTK")) { // 'return' [Exp] ';'
            getToken(); // return
            isReturn = true;
            //nextToken = getNext();
            if (getNext().typeSymbolizeExp()) {
                if (!needReturn) {
                    error("f"); // 无返回值的函数存在不匹配的return语句
                }
                analyseExp(getExp());
            }
            checkSemicn(); // ;
        } else if (nextToken.typeIs("PRINTFTK")) { // 'printf' '(' FormatString { ',' Exp } ')' ';'
            getToken(); // printf
            Token printftk = current;
            getToken(); // (
            getToken(); // STRCON
            Token strcon = current;
            nextToken = getNext();
            int param = 0;
            while (nextToken.typeIs("COMMA")) {
                getToken(); // ,
                analyseExp(getExp()); // Exp
                param++;
                nextToken = getNext();
            }
            if (strcon.checkFormat()) {
                error("a", strcon.getline()); // 非法符号
            }
            if (param != strcon.cntFormat()) {
                error("l", printftk.getline()); // printf中格式字符与表达式个数不匹配
            }
            if (!getNext().typeIs("RPARENT")) {
                error("j"); //缺少右小括号’)’
            } else {
                getToken(); // )
            }
            checkSemicn(); // ;
        } else if (nextToken.typeIs("SEMICN")) { // ;
            getToken(); // ;
        }
        grammar.add("<Stmt>");
        return  isReturn;
    }

    private void analyseForStmt() { // ForStmt → LVal '=' Exp
        ArrayList<Token> exp = getExp();
        analyseLVal(exp); // LVal
        checkConst(exp.get(0)); // 检查左值是否是常量
        getToken(); // =
        analyseExp(getExp()); // Exp
        grammar.add("<ForStmt>");
    }

    private int analyseExp(ArrayList<Token> exp) { // Exp → AddExp
        int intType = analyseAddExp(exp);
        grammar.add("<Exp>");
        return intType;
    }

    private void analyseCond() { // Cond → LOrExp
        analyseLOrExp(getExp());
        grammar.add("<Cond>");
    }

    private int analyseLVal(ArrayList<Token> exp) { // LVal → Ident {'[' Exp ']'}
        int intType = 0;
        Token ident = exp.get(0);
        if (!checkSymbol(ident)) {
            error("c", ident.getline()); //未定义的名字
        }
        grammar.add(ident.toString()); // Ident
        if (exp.size() > 1) {
            ArrayList<Token> exp1 = new ArrayList<>();
            int flag = 0;
            for (int i = 1; i < exp.size(); i++) {
                Token nextToken = exp.get(i);
                if (nextToken.typeIs("LBRACK")) { // [
                    flag++;
                    intType++;
                    if (flag == 1) {
                        grammar.add(nextToken.toString());
                        exp1 = new ArrayList<>();
                    } else {
                        exp1.add(nextToken);
                    }
                } else if (nextToken.typeIs("RBRACK")) { // ]
                    flag--;
                    if (flag == 0) {
                        analyseExp(exp1);
                        grammar.add(nextToken.toString());
                    } else {
                        exp1.add(nextToken);
                    }
                } else {
                    exp1.add(nextToken);
                }
            }
            if (flag > 0) {
                analyseExp(exp1);
                error("k", exp.get(exp.size() - 1).getline()); // 缺少右中括号’]’
            }
        }
        grammar.add("<LVal>");
        if (checkSymbol(ident)) {
            return getSymbol(ident).getIntType() - intType;
        } else {
            return 0;
        }
    }

    private int analysePrimaryExp(ArrayList<Token> exp) { // PrimaryExp → '(' Exp ')' | LVal | Number
        int intType = 0;
        Token nextToken = exp.get(0);
        if (nextToken.typeIs("LPARENT")) {
            // remove ( )
            grammar.add(exp.get(0).toString());
            analyseExp(new ArrayList<>(exp.subList(1, exp.size() - 1))); // Exp
            grammar.add(exp.get(exp.size() - 1).toString());
        } else if (nextToken.typeIs("IDENFR")) { // LVal
            intType = analyseLVal(exp);
        } else if (nextToken.typeIs("INTCON")) { // Number
            analyseNumber(exp.get(0));
        } else {
            error();
        }
        grammar.add("<PrimaryExp>");
        return intType;
    }

    private void analyseNumber(Token nextToken) { // Number → IntConst
        grammar.add(nextToken.toString());
        grammar.add("<Number>");
    }

    private int analyseUnaryExp(ArrayList<Token> exp) { // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'
                                                        // | UnaryOp UnaryExp
        int intType = 0;
        if (exp.isEmpty()) {
            // 对于空列表的处理，可以根据需要抛出异常、返回默认值，或者执行其他逻辑
            return intType; // 这里假设返回默认值 0，你可以根据实际情况返回合适的值或执行逻辑
        }
        Token nextToken = exp.get(0);
        if (nextToken.typeIs("PLUS") || nextToken.typeIs("MINU") || nextToken.typeIs("NOT")) { // UnaryOp UnaryExp
            analyseUnaryOp(exp.get(0));
            analyseUnaryExp(new ArrayList<>(exp.subList(1, exp.size())));
        } else if (exp.size() == 1) {
            intType = analysePrimaryExp(exp); // PrimaryExp
        } else {
            if (exp.get(0).typeIs("IDENFR") && exp.get(1).typeIs("LPARENT")) { // Ident '(' [FuncRParams] ')'
                Token ident = exp.get(0);
                ArrayList<Integer> params = null;
                if (!checkFunction(ident)) {
                    error("c", ident.getline()); //未定义的名字
                } else {
                    params = getFunction(ident).getParams();
                }
                if (!exp.get(exp.size() - 1).typeIs("RPARENT")) {
                    exp.add(new Token(")", current.getline()));
                    error("j"); // 缺少右小括号’)’
                }
                grammar.add(exp.get(0).toString());
                grammar.add(exp.get(1).toString());
                if (exp.size() > 3) {
                    analyseFuncRParams(ident, new ArrayList<>(exp.subList(2, exp.size() - 1)), params); // FuncRParams
                } else {
                    checkParams(ident, params);
                }
                grammar.add(exp.get(exp.size() - 1).toString()); // )
                if (checkFunction(ident)) {
                    if (getFunction(ident).getReturnType().equals("void")) {
                        intType = -1;
                    }
                }
            } else {
                intType = analysePrimaryExp(exp); // PrimaryExp
            }
        }
        grammar.add("<UnaryExp>");
        return intType;
    }

    private boolean checkFunction(Token token) {
        return functions.containsKey(token.getContent());
    }

    private Function getFunction(Token token) {
        return functions.getOrDefault(token.getContent(), null);
    }


    private void analyseUnaryOp(Token nextToken) { // UnaryOp → '+' | '−' | '!'
        grammar.add(nextToken.toString());
        grammar.add("<UnaryOp>");
    }

    private void analyseFuncRParams(Token ident, ArrayList<Token> exp, ArrayList<Integer> params) { // FuncRParams → Exp { ',' Exp }
        ArrayList<Integer> rparams = new ArrayList<>();
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("COMMA")));
        for (ArrayList<Token> exp1 : exps.getTokens()) {
            int intType = analyseExp(exp1); // Exp
            rparams.add(intType);
            if (!exps.getSymbols().isEmpty()) {
                grammar.add(exps.getSymbols().remove(0).toString());
            }
        }
        checkParamsType(ident, params, rparams); // 检查参数匹配性
        grammar.add("<FuncRParams>");
    }

    private void checkParamsType(Token ident, ArrayList<Integer> params, ArrayList<Integer> rparams) {
        if (params.size() != rparams.size()) {
            error("d", ident.getline());
        } else {
            for (int i = 0; i < rparams.size(); i++) {
                if (!params.get(i).equals(rparams.get(i))) {
                    error("e", ident.getline());
                }
            }
        }
    }

    // 检查函数参数
    private void checkParams(Token ident, ArrayList<Integer> params) {
        if (params != null) {
            if (params.size() != 0) {
                error("d", ident.getline()); // 函数参数个数不匹配
            }
        }
    }

    private Exps divideExp(ArrayList<Token> exp, ArrayList<String> symbol) {
        ArrayList<ArrayList<Token>> exps = new ArrayList<>();
        ArrayList<Token> exp1 = new ArrayList<>();
        ArrayList<Token> symboltable = new ArrayList<>();
        boolean unaryFlag = false;
        int flag1 = 0;
        int flag2 = 0;
        for (int i = 0; i < exp.size(); i++) {
            Token nextToken = exp.get(i);
            if (nextToken.typeIs("LPARENT")) {
                flag1++;
            }
            if (nextToken.typeIs("RPARENT")) {
                flag1--;
            }
            if (nextToken.typeIs("LBRACK")) {
                flag2++;
            }
            if (nextToken.typeIs("RBRACK")) {
                flag2--;
            }
            if (symbol.contains(nextToken.getType()) && flag1 == 0 && flag2 == 0) {
                // UnaryOp
                if (nextToken.typeIs("PLUS")||nextToken.typeIs("MINU")||nextToken.typeIs("NOT")) {
                    if (!unaryFlag) {
                        exp1.add(nextToken);
                        continue;
                    }
                }
                exps.add(exp1);
                symboltable.add(nextToken);
                exp1 = new ArrayList<>();
            } else {
                exp1.add(nextToken);
            }
            unaryFlag = nextToken.typeIs("IDENFR") || nextToken.typeIs("RPARENT") || nextToken.typeIs("INTCON")
                    || nextToken.typeIs("RBRACK");
        }
        exps.add(exp1);
        return new Exps(exps, symboltable);
    }

    private int analyseMulExp(ArrayList<Token> exp) { // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        int intType = 0;
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("MULT", "DIV", "MOD")));
        int j = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
            intType = analyseUnaryExp(exp1); // UnaryExp
            grammar.add("<MulExp>");
            if (j < exps.getSymbols().size()) { // MulExp ('*' | '/' | '%')
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
        return intType;
    }

    private int analyseAddExp(ArrayList<Token> exp) { // AddExp → MulExp | AddExp ('+' | '−') MulExp
        int intType = 0;
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("PLUS", "MINU")));
        int j = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
            intType = analyseMulExp(exp1); // MulExp
            grammar.add("<AddExp>");
            if (j < exps.getSymbols().size()) { // AddExp ('+' | '−') MulExp
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
        return intType;
    }

    private void analyseRelExp(ArrayList<Token> exp) { // AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("LSS", "LEQ", "GRE", "GEQ")));
        int j = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
            analyseAddExp(exp1);
            grammar.add("<RelExp>");
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

    private void analyseEqExp(ArrayList<Token> exp) { // EqExp → RelExp | EqExp ('==' | '!=') RelExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("EQL", "NEQ")));
        int j = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
            analyseRelExp(exp1);
            grammar.add("<EqExp>");
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

    private void analyseLAndExp(ArrayList<Token> exp) { // LAndExp → EqExp | LAndExp '&&' EqExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("AND"))); // &&
        int j = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
            analyseEqExp(exp1); // EqExp
            grammar.add("<LAndExp>");
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

    private void analyseLOrExp(ArrayList<Token> exp) { // LOrExp → LAndExp | LOrExp '||' LAndExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("OR"))); // ||
        int j = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
            analyseLAndExp(exp1); // LAndExp
            grammar.add("<LOrExp>");
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

    private void analyseConstExp(ArrayList<Token> exp) { // ConstExp → AddExp
        analyseAddExp(exp);
        grammar.add("<ConstExp>");
    }

    private ArrayList<Token> getExp() {
        ArrayList<Token> exp = new ArrayList<>();
        boolean inFunc = false;
        int funcFlag = 0;
        int flag1 = 0;
        int flag2 = 0;
        Token preToken = null;
        Token nextToken = getNext();
        while (true) {
            if (nextToken.typeIs("SEMICN") || nextToken.typeIs("ASSIGN") || nextToken.typeIs("RBRACE")
                || nextToken.checkTypeStmt()) {
                break;
            }
            if (nextToken.typeIs("COMMA") && !inFunc) {
                break;
            }
            if (preToken != null) {
                if ((preToken.typeIs("INTCON") || preToken.typeIs("IDENFR")) && (nextToken.typeIs("INTCON") || nextToken.typeIs("IDENFR"))) {
                    break;
                }
                if ((preToken.typeIs("RPARENT") || preToken.typeIs("RBRACK")) && (nextToken.typeIs("INTCON") || nextToken.typeIs("IDENFR"))) {
                    break;
                }
                if (flag1 == 0 && flag2 == 0) {
                    if (preToken.typeIs("INTCON") && nextToken.typeIs("LBRACK")) {
                        break;
                    }
                    if (preToken.typeIs("INTCON") && nextToken.typeIs("LBRACE")) {
                        break;
                    }
                }
            }
            if (nextToken.checkNotInExp()) {
                break;
            }
            if (nextToken.typeIs("IDENFR")) {
                if (tokens.get(index + 1).typeIs("LPARENT")) {
                    inFunc = true;
                }
            }
            if (nextToken.typeIs("LPARENT")) {
                flag1++;
                if (inFunc) {
                    funcFlag++;
                }
            }
            if (nextToken.typeIs("RPARENT")) {
                flag1--;
                if (inFunc) {
                    funcFlag--;
                    if (funcFlag == 0) {
                        inFunc = false;
                    }
                }
            }
            if (nextToken.typeIs("LBRACK")) {
                flag2++;
            }
            if (nextToken.typeIs("RBRACK")) {
                flag2--;
            }
            if (flag1 < 0) {
                break;
            }
            if (flag2 < 0) {
                break;
            }
            //current = tokens.get(index);
            //index++;
            getTokenWithoutGrammar();
            exp.add(current);
            preToken = nextToken;
            nextToken = getNext();
        }
        return exp;
    }

    private void error(String type) {
        error(type, current.getline());
    }

    private void error(String type, int line) {
        errors.add(new Errors(line, type));
        System.out.println(line + " " + type);
    }

    private void error() {
        //
    }

    public void printWords(FileWriter writer) throws IOException {
        for (String str : grammar) {
            writer.write(str + "\n");
        }
        writer.flush();
        writer.close();
    }

    // 这段代码使用了流式操作。它首先将 symboltable 中的每个 SymbolTable 实例转换成流，然后使用 filter 方法找到符合条件的 SymbolTable，
    // 接着使用 map 方法得到对应的 Symbol，最后使用 findFirst 获取第一个匹配的 Symbol 或者如果没有匹配则返回 null。
    private Symbol getSymbol(Token token) {
        return symboltable.values().stream()
                .filter(s -> s.findSymbol(token))
                .map(s -> s.getSymbol(token))
                .findFirst()
                .orElse(null);
    }

    // 使用了 Comparator.comparingInt 和 Lambda 表达式来替代匿名比较器类。
    // 同时使用 try-with-resources 语句管理文件写入，自动确保资源被正确关闭，无需手动调用 writer.close()方法。
    public void printError(FileWriter writer) throws IOException {
        errors.sort(Comparator.comparingInt(Errors::getline));
        try {
            for (Errors error : errors) {
                writer.write(error + "\n");
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
