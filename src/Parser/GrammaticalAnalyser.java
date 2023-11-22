package Parser;
import Lexer.Token;
<<<<<<< Updated upstream
import Parser.Exps;
=======
import PCode.LabelGenerator;
import PCode.Operator.Operator;
import PCode.PCode;
import Symbol.*;
import Error.*;
>>>>>>> Stashed changes

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class GrammaticalAnalyser {
    private ArrayList<Token> tokens;
    private int index;
    private Token current;
    private ArrayList<String> grammar;

    // pcode
    private int areaID = -1;

    private final ArrayList<PCode> codes = new ArrayList<>();
    private final LabelGenerator labelGenerator = new LabelGenerator();
    private final ArrayList<HashMap<String, String>> ifLabels = new ArrayList<>();
    private final ArrayList<HashMap<String, String>> forLabels = new ArrayList<>();

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

    private Token getNext() {
        return tokens.get(index);
    }

<<<<<<< Updated upstream
    private void analyseCompUnit() { // CompUnit → {Decl} {FuncDef} MainFuncDef
        Token nextToken = getNext();
        while (nextToken.typeIs("CONSTTK") || (nextToken.typeIs("INTTK") && tokens.get(index + 1).typeIs("IDENFR")
                && !tokens.get(index + 2).typeIs("LPARENT"))) { // {Decl}
=======
    private void addArea() {
        areaID++;
        area++;
        symboltable.put(area, new SymbolTable());
    }

    private void removeArea() {
        symboltable.remove(area);
        area--;
    }

    private void analyseCompUnit() {
        addArea();

        Token nextToken = getNext();
        while (index < tokens.size() && (nextToken.typeIs(String.valueOf(Word.CONSTTK)) || (nextToken.typeIs(String.valueOf(Word.INTTK))
                && index + 1 < tokens.size() && tokens.get(index + 1).typeIs(String.valueOf(Word.IDENFR))
                && index + 2 < tokens.size() && !tokens.get(index + 2).typeIs(String.valueOf(Word.LPARENT))))) {
>>>>>>> Stashed changes
            analyseDecl();
            nextToken = getNext();
        }
        while (nextToken.typeIs("VOIDTK") || ((nextToken.typeIs("INTTK") && !tokens.get(index + 1).typeIs("MAINTK")))) { // {FuncDef}
            analyseFuncDef();
            nextToken = getNext();
        }
        if (nextToken.typeIs("INTTK") && tokens.get(index + 1).typeIs("MAINTK")) { // MainFuncDef
            analyseMainFuncDef();
        } else {
            error();
        }
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
        analyseBType(); // Btype
        analyseConstDef(); // ConstDef
        Token nextToken = getNext();
        while (nextToken.typeIs("COMMA")) {
            getToken(); // ,
            analyseConstDef(); // ConstDef
            nextToken = getNext();
        }
        getToken();// ;
        grammar.add("<ConstDecl>");
    }

    private void analyseBType() { // BType → 'int'
        getToken();
    }

    private void analyseConstDef() { // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        getToken(); // Ident
<<<<<<< Updated upstream
=======
        Token ident = current;
        if (checkSymbol(current)){
            error("b"); // 名字重定义
        }
        codes.add(new PCode(Operator.VAR, areaID + "_" + current.getContent()));
        int intType = 0;
>>>>>>> Stashed changes
        Token nextToken = getNext();
        while (nextToken.typeIs("LBRACK")) {
            getToken(); // [
            analyseConstExp(getExp()); // ConstExp
            getToken(); // ]
            if (!current.typeIs("RBRACK")) {
                error();
            }
            nextToken = getNext();
        }
<<<<<<< Updated upstream
=======
        // todo
        if (intType != 0) { // 数组类型
            codes.add(new PCode(Operator.DIMVAR, areaID + "_" + ident.getContent(), intType));
        }
        addSymbol(ident,"const", intType, areaID);
>>>>>>> Stashed changes
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
        getToken(); // ;
        grammar.add("<VarDecl>");
    }

    private void analyseVarDef() { // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
        getToken(); // Ident
<<<<<<< Updated upstream
=======
        Token ident = current;
        if (checkSymbol(current)){
            error("b"); // 名字重定义
        }
        codes.add(new PCode(Operator.VAR, areaID + "_" + current.getContent()));
        int intType = 0;
>>>>>>> Stashed changes
        Token nextToken = getNext();
        while (nextToken.typeIs("LBRACK")) {
            getToken(); // [
            analyseConstExp(getExp()); // ConstExp
            getToken(); // ]
            nextToken = getNext();
        }
<<<<<<< Updated upstream
        if (nextToken.typeIs("ASSIGN")) {
=======
        // todo
        if (intType != 0) {
            codes.add(new PCode(Operator.DIMVAR, areaID + "_" + ident.getContent(), intType));
        }
        if (nextToken.typeIs(String.valueOf(Word.ASSIGN))) {
>>>>>>> Stashed changes
            getToken(); // =
            analyseInitVal(); // InitVal
        } else {
            codes.add(new PCode(Operator.PLACEHOLDER, areaID + "_" + ident.getContent(), intType));
        }
<<<<<<< Updated upstream
=======
        addSymbol(ident,"var",intType, areaID);
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
        analyseFuncType(); // FuncType
        getToken(); // Ident
=======
        //analyseFuncType(); // FuncType
        int startIdx = index;
        Function function;
        ArrayList<Integer> params = new ArrayList<>();
        String returnType = analyseFuncType();
        getToken(); // Ident
        if (functions.containsKey(current.getContent())) {
            error("b"); //名字重定义
        }
        codes.add(new PCode(Operator.FUNC, current.getContent()));
        function = new Function(current, returnType);
        addArea();
>>>>>>> Stashed changes
        getToken(); // (
        Token nextToken = getNext();
        if (!nextToken.typeIs("RPARENT")) {
            analyseFuncFParams();
        }
<<<<<<< Updated upstream
        getToken(); // )
        analyseBlock(); // Block
=======
        if (!getNext().typeIs(String.valueOf(Word.RPARENT))) {
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
        //todo
        codes.add(new PCode(Operator.RET, 0));
        codes.add(new PCode(Operator.END_FUNC));

>>>>>>> Stashed changes
        grammar.add("<FuncDef>"); // 函数定义
    }

    private void analyseMainFuncDef() { // MainFuncDef → 'int' 'main' '(' ')' Block
        getToken(); // int
        getToken(); // main
<<<<<<< Updated upstream
        getToken(); // (
        getToken(); // )
        analyseBlock(); // Block
=======
        if (functions.containsKey(current.getContent())) {
            error("b"); // 名字重定义
        } else {
            Function func = new Function(current,"int");
            func.setParams(new ArrayList<>());
            functions.put("main",func);
        }
        codes.add(new PCode(Operator.MAIN, current.getContent()));
        getToken(); // (
        if (!getNext().typeIs(String.valueOf(Word.RPARENT))) {
            error("j"); //缺少右小括号’)’
        } else {
            getToken(); // )
        }
        needReturn = true;
        boolean isReturn = analyseBlock(false);
        if (needReturn && !isReturn) {
            error("g"); // 有返回值的函数缺少return语句
        }
        codes.add(new PCode(Operator.EXIT));
>>>>>>> Stashed changes
        grammar.add("<MainFuncDef>"); // main函数定义
    }

    private void analyseFuncType() { // FuncType → 'void' | 'int'
        getToken(); // void | int
        grammar.add("<FuncType>"); // 覆盖两种函数类型
    }

    private void analyseFuncFParams() { // FuncFParams → FuncFParam { ',' FuncFParam }
        analyseFuncFParam(); // FuncFParam
        Token nextToken = getNext();
        while (nextToken.typeIs("COMMA")) {
            getToken(); // ,
            analyseFuncFParam(); // FuncFParam
            nextToken = getNext();
        }
        grammar.add("<FuncFParams>"); // 函数参数声明
    }

    private void analyseFuncFParam() { // BType Ident ['[' ']' { '[' ConstExp ']' }]
        getToken(); // Btype
        getToken(); // Ident
        Token nextToken = getNext();
        if (nextToken.typeIs("LBRACK")) {
            getToken(); // [
            getToken(); // ]
            nextToken = getNext();
            while (nextToken.typeIs("LBRACK")) {
                getToken(); // [
                analyseConstExp(getExp()); // ConstExp
                getToken(); // ]
                nextToken = getNext();
            }
        }
<<<<<<< Updated upstream
=======
        codes.add(new PCode(Operator.PARAM, areaID + "_" + ident.getContent(), paramType));
        addSymbol(ident,"param", paramType, areaID);
>>>>>>> Stashed changes
        grammar.add("<FuncFParam>"); // 定义函数形参
    }

    private void analyseBlock() { // Block → '{' { BlockItem } '}'
        getToken(); // {
        Token nextToken = getNext();
        while (nextToken.typeIs("CONSTTK") || nextToken.typeIs("INTTK") || nextToken.typeSymbolizeStmt()) {
            if (nextToken.typeIs("CONSTTK") || nextToken.typeIs("INTTK")) {
                analyseBlockItem(); // BlockItem
            } else {
                analyseStmt();
            }
            nextToken = getNext();
        }
        getToken(); // }
        grammar.add("<Block>"); // 语句块
    }

    private void analyseBlockItem() { // BlockItem → Decl | Stmt
        Token nextToken = getNext();
        if (nextToken.typeIs("CONSTTK") || nextToken.typeIs("INTTK")) {
            analyseDecl(); // Decl
        } else {
<<<<<<< Updated upstream
            analyseStmt(); // Stmt
        }
    }

    private void analyseStmt() {
=======
            isReturn = analyseStmt(); // Stmt
        }
        return isReturn;
    }

    private boolean analyseStmt() {
        boolean isReturn = false;
>>>>>>> Stashed changes
        Token nextToken = getNext();
        if (nextToken.typeIs("IDENFR")) { // LVal '=' Exp ';' | LVal '=' 'getint''('')'';'
            ArrayList<Token> exp = getExp();
<<<<<<< Updated upstream
            if (!getNext().typeIs("SEMICN")) {
                analyseLVal(exp); // LVal
=======
            if (getNext().typeIs(String.valueOf(Word.ASSIGN))) {
                Token ident = exp.get(0);
                int intType = analyseLVal(exp); // LVal
                checkConst(nextToken);
                codes.add(new PCode<>(Operator.ADDRESS, getSymbol(ident).getAreaID() + "_" + ident.getContent(), intType));
                // todo
//                if (isConst(nextToken)) {
//                    error("h", nextToken.getline());
//
//                }
>>>>>>> Stashed changes
                getToken(); // =
                if (getNext().typeIs("GETINTTK")) { // 'getint''('')'';'
                    getToken(); // getint
                    getToken(); // (
<<<<<<< Updated upstream
                    getToken(); // )
                    getToken(); // ;
=======
                    if (!getNext().typeIs(String.valueOf(Word.RPARENT))) {
                        error("j"); //缺少右小括号’)’
                    } else {
                        getToken(); //)
                    }
                    checkSemicn(); // ;
                    codes.add(new PCode<>(Operator.GETINT));
>>>>>>> Stashed changes
                } else {
                    analyseExp(getExp()); // Exp
                    getToken(); // ;
                }
                codes.add(new PCode<>(Operator.POP, getSymbol(ident).getAreaID() + "_" + ident.getContent()));
            } else {
                analyseExp(exp);
                getToken(); // ;
            }
        } else if (nextToken.typeSymbolizeExp()) { // [Exp] ';'
            analyseExp(getExp());
<<<<<<< Updated upstream
            getToken(); // ;
        } else if (nextToken.typeIs("LBRACE")) { // Block
            analyseBlock();
        } else if (nextToken.typeIs("IFTK")) { // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            getToken(); // if
            getToken(); // (
            analyseCond(); // Cond
            getToken(); // )
            analyseStmt(); // Stmt
            nextToken = getNext();
            if (nextToken.typeIs("ELSETK")) {
                getToken(); // else
                analyseStmt(); // Stmt
            }
        } else if (nextToken.typeIs("FORTK")) { // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
=======
            checkSemicn(); // ;
        } else if (nextToken.typeIs(String.valueOf(Word.LBRACE))) { // Block
            analyseBlock(false);
        } else if (nextToken.typeIs(String.valueOf(Word.IFTK))) { // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            ifLabels.add(new HashMap<>());
            ifLabels.get(ifLabels.size() - 1).put("if", labelGenerator.getLabel("if"));
            ifLabels.get(ifLabels.size() - 1).put("else", labelGenerator.getLabel("else"));
            ifLabels.get(ifLabels.size() - 1).put("if_end", labelGenerator.getLabel("if_end"));
            ifLabels.get(ifLabels.size() - 1).put("if_block", labelGenerator.getLabel("if_block"));
            codes.add(new PCode(Operator.LABEL, ifLabels.get(ifLabels.size() - 1).get("if")));

            //addIfLabelCode("if");
            getToken(); // if
            getToken(); // (
            analyseCond(String.valueOf(Word.IFTK)); // Cond
            if (!getNext().typeIs(String.valueOf(Word.RPARENT))) {
                error("j"); //缺少右小括号’)’
            } else {
                getToken(); //)
            }
            codes.add(new PCode(Operator.JZ, ifLabels.get(ifLabels.size() - 1).get("else")));
            codes.add(new PCode(Operator.LABEL, ifLabels.get(ifLabels.size() - 1).get("if_block")));
            analyseStmt(); // Stmt
            nextToken = getNext();
            codes.add(new PCode(Operator.JMP, ifLabels.get(ifLabels.size() - 1).get("if_end")));
            codes.add(new PCode(Operator.LABEL, ifLabels.get(ifLabels.size() - 1).get("else")));
            if (nextToken.typeIs(String.valueOf(Word.ELSETK))) {
                getToken(); // else
                analyseStmt(); // Stmt
            }
            codes.add(new PCode(Operator.LABEL, ifLabels.get(ifLabels.size() - 1).get("if_end")));
            ifLabels.remove(ifLabels.size() - 1);
        } else if (nextToken.typeIs(String.valueOf(Word.FORTK))) { // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            // todo
            forLabels.add(new HashMap<>());
            forLabels.get(forLabels.size() - 1).put("if", labelGenerator.getLabel("if"));
            forLabels.get(forLabels.size() - 1).put("else", labelGenerator.getLabel("else"));
            forLabels.get(forLabels.size() - 1).put("if_end", labelGenerator.getLabel("if_end"));
            forLabels.get(forLabels.size() - 1).put("if_block", labelGenerator.getLabel("if_block"));
            codes.add(new PCode(Operator.LABEL, forLabels.get(forLabels.size() - 1).get("if")));
            //addForLabelCode("for");

>>>>>>> Stashed changes
            getToken(); // for
            getToken(); // (
            nextToken = getNext();
            if (nextToken.typeIs("IDENFR")) { // ForStmt
                analyseForStmt();
            }
            getToken(); // ;
            nextToken = getNext();
            if (nextToken.typeSymbolizeExp()) { // Cond
                analyseCond(String.valueOf(Word.FORTK));
            }
            getToken(); // ;
            nextToken = getNext();
            if (nextToken.typeIs("IDENFR")) { // ForStmt
                analyseForStmt();
            }
<<<<<<< Updated upstream
            getToken(); // )
            analyseStmt(); // Stmt
        } else if (nextToken.typeIs("BREAKTK")) { // 'break' ';'
            getToken(); // break
            getToken(); // ;
        } else if (nextToken.typeIs("CONTINUETK")) { // 'continue' ';'
            getToken(); // continue
            getToken(); // ;
        } else if (nextToken.typeIs("RETURNTK")) { // 'return' [Exp] ';'
            getToken(); // return
            nextToken = getNext();
            if (nextToken.typeSymbolizeExp()) {
                analyseExp(getExp()); // Exp
            }
            getToken(); // ;
        } else if (nextToken.typeIs("PRINTFTK")) { // 'printf' '(' FormatString { ',' Exp } ')' ';'
=======
            if (!getNext().typeIs(String.valueOf(Word.RPARENT))) {
                error("j"); // 缺少右小括号’)’
            } else {
                getToken(); //)
            }
            codes.add(new PCode(Operator.JZ, forLabels.get(forLabels.size() - 1).get("for_end")));
            codes.add(new PCode(Operator.LABEL, forLabels.get(forLabels.size() - 1).get("for_block")));

            forFlag--;
            analyseStmt(); // Stmt
            codes.add(new PCode(Operator.JMP, forLabels.get(forLabels.size() - 1).get("for")));
            codes.add(new PCode(Operator.LABEL, forLabels.get(forLabels.size() - 1).get("for_end")));
            forLabels.remove(forLabels.size() - 1);
        } else if (nextToken.typeIs(String.valueOf(Word.BREAKTK))) { // 'break' ';'
            getToken(); // break
            codes.add(new PCode(Operator.JMP, forLabels.get(forLabels.size() - 1).get("for_end")));
            if (forFlag == 0) {
                error("m"); // 在非循环块中使用break和continue语句
            }
            checkSemicn(); //;
        } else if (nextToken.typeIs(String.valueOf(Word.CONTINUETK))) { // 'continue' ';'
            getToken(); // continue
            codes.add(new PCode(Operator.JMP, forLabels.get(forLabels.size() - 1).get("for")));
            if (forFlag == 0) {
                error("m"); // 在非循环块中使用break和continue语句
            }
            checkSemicn(); //;
        } else if (nextToken.typeIs(String.valueOf(Word.RETURNTK))) { // 'return' [Exp] ';'
            getToken(); // return
            isReturn = true;
            boolean ret = false;
            if (getNext().typeSymbolizeExp()) {
                if (!needReturn) {
                    error("f"); // 无返回值的函数存在不匹配的return语句
                }
                analyseExp(getExp());
                ret = true;
            }
            checkSemicn(); // ;
            codes.add(new PCode(Operator.RET, (ret? 1 : 0)));
        } else if (nextToken.typeIs(String.valueOf(Word.PRINTFTK))) { // 'printf' '(' FormatString { ',' Exp } ')' ';'
>>>>>>> Stashed changes
            getToken(); // printf
            getToken(); // (
            getToken(); // STRCON
            nextToken = getNext();
            while (nextToken.typeIs("COMMA")) {
                getToken(); // ,
                analyseExp(getExp()); // Exp
                nextToken = getNext();
            }
<<<<<<< Updated upstream
            getToken(); // )
            getToken(); // ;
        } else if (nextToken.typeIs("SEMICN")) { // ;
=======
            if (strcon.checkFormat()) {
                error("a", strcon.getline()); // 非法符号
            }
            if (param != strcon.cntFormat()) {
                error("l", printftk.getline()); // printf中格式字符与表达式个数不匹配
            }
            if (!getNext().typeIs(String.valueOf(Word.RPARENT))) {
                error("j"); //缺少右小括号’)’
            } else {
                getToken(); // )
            }
            checkSemicn(); // ;
            codes.add(new PCode(Operator.PRINT, strcon.getContent(), param));
        } else if (nextToken.typeIs(String.valueOf(Word.SEMICN))) { // ;
>>>>>>> Stashed changes
            getToken(); // ;
        }
        grammar.add("<Stmt>");
    }

    private void analyseForStmt() { // ForStmt → LVal '=' Exp
        // todo
        ArrayList<Token> exp = getExp();
        analyseLVal(exp); // LVal
        getToken(); // =
        analyseExp(getExp()); // Exp
        grammar.add("<ForStmt>");
    }

    private void analyseExp(ArrayList<Token> exp) { // Exp → AddExp
        analyseAddExp(exp);
        grammar.add("<Exp>");
    }

    private void analyseCond(String from) { // Cond → LOrExp
        analyseLOrExp(getExp(), from);
        grammar.add("<Cond>");
    }
<<<<<<< Updated upstream

    private void analyseLVal(ArrayList<Token> exp) { // LVal → Ident {'[' Exp ']'}
        grammar.add(exp.get(0).toString()); // Ident
=======
    private int analyseLVal(ArrayList<Token> exp) { // LVal → Ident {'[' Exp ']'}
        int intType = 0;
        Token ident = exp.get(0);
        if (!checkSymbol(ident)) {
            error("c", ident.getline()); //未定义的名字
        }
        codes.add(new PCode(Operator.PUSH, getSymbol(ident).getAreaID() + "_" + ident.getContent()));
        grammar.add(ident.toString()); // Ident
>>>>>>> Stashed changes
        if (exp.size() > 1) {
            ArrayList<Token> exp1 = new ArrayList<>();
            int flag = 0;
            for (int i = 1; i < exp.size(); i++) {
                Token nextToken = exp.get(i);
                if (nextToken.typeIs("LBRACK")) { // [
                    flag++;
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
        }
        grammar.add("<LVal>");
    }

    private void analysePrimaryExp(ArrayList<Token> exp) { // PrimaryExp → '(' Exp ')' | LVal | Number
        Token nextToken = exp.get(0);
        if (nextToken.typeIs("LPARENT")) {
            // remove ( )
            grammar.add(exp.get(0).toString());
            analyseExp(new ArrayList<>(exp.subList(1, exp.size() - 1))); // Exp
            grammar.add(exp.get(exp.size() - 1).toString());
<<<<<<< Updated upstream
        } else if (nextToken.typeIs("IDENFR")) { // LVal
            analyseLVal(exp);
        } else if (nextToken.typeIs("INTCON")) { // Number
=======
        } else if (nextToken.typeIs(String.valueOf(Word.IDENFR))) { // LVal
            intType = analyseLVal(exp);
            Token ident = exp.get(0);
            if (intType != 0) {
                codes.add(new PCode(Operator.ADDRESS, getSymbol(ident).getAreaID() + "_" + ident.getContent(), intType));
            } else {
                codes.add(new PCode(Operator.VALUE, getSymbol(ident).getAreaID() + "_" + ident.getContent(), intType));
            }
        } else if (nextToken.typeIs(String.valueOf(Word.INTCON))) { // Number
>>>>>>> Stashed changes
            analyseNumber(exp.get(0));
        } else {
            error();
        }
        grammar.add("<PrimaryExp>");
    }

    private void analyseNumber(Token token) { // Number → IntConst
        codes.add(new PCode(Operator.PUSH, Integer.parseInt(token.getContent())));
        grammar.add(token.toString());
        grammar.add("<Number>");
    }

    private void analyseUnaryExp(ArrayList<Token> exp) { // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'
                                                        // | UnaryOp UnaryExp
        Token nextToken = exp.get(0);
        if (nextToken.typeIs("PLUS") || nextToken.typeIs("MINU") || nextToken.typeIs("NOT")) { // UnaryOp UnaryExp
            analyseUnaryOp(exp.get(0));
            analyseUnaryExp(new ArrayList<>(exp.subList(1, exp.size())));
            if (nextToken.typeIs(String.valueOf(Word.PLUS))) {
                codes.add(new PCode(Operator.POS));
            } else if (nextToken.typeIs(String.valueOf(Word.MINU))) {
                codes.add(new PCode(Operator.NEG));
            } else if (nextToken.typeIs(String.valueOf(Word.NOT))) {
                codes.add(new PCode(Operator.NOT));
            }
        } else if (exp.size() == 1) {
            analysePrimaryExp(exp); // PrimaryExp
        } else {
            if (exp.get(0).typeIs("IDENFR") && exp.get(1).typeIs("LPARENT")) { // Ident (
                grammar.add(exp.get(0).toString());
                grammar.add(exp.get(1).toString());
                if (exp.size() > 3) {
                    analyseFuncRParams(new ArrayList<>(exp.subList(2, exp.size() - 1))); // FuncRParams
                }
                grammar.add(exp.get(exp.size() - 1).toString()); // )
<<<<<<< Updated upstream
=======
                codes.add(new PCode(Operator.CALL, ident.getContent()));
                if (checkFunction(ident)) {
                    if (getFunction(ident).getReturnType().equals("void")) {
                        intType = -1;
                    }
                }
>>>>>>> Stashed changes
            } else {
                analysePrimaryExp(exp); // PrimaryExp
            }
        }
        grammar.add("<UnaryExp>");
    }

<<<<<<< Updated upstream
    private void analyseUnaryOp(Token nextToken) { // UnaryOp → '+' | '−' | '!'
        grammar.add(nextToken.toString());
=======
    private void analyseUnaryOp(Token token) { // UnaryOp → '+' | '−' | '!'
        grammar.add(token.toString());
>>>>>>> Stashed changes
        grammar.add("<UnaryOp>");
    }

    private void analyseFuncRParams(ArrayList<Token> exp) { // FuncRParams → Exp { ',' Exp }
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("COMMA")));
        int j = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
<<<<<<< Updated upstream
            analyseExp(exp1); // Exp
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
=======
            int intType = analyseExp(exp1); // Exp
            rparams.add(intType);
            codes.add(new PCode(Operator.RPARAM, intType));
            if (!exps.getSymbols().isEmpty()) {
                grammar.add(exps.getSymbols().remove(0).toString());
>>>>>>> Stashed changes
            }
        }
        grammar.add("<FuncRParams>");
    }

    private Exps divideExp(ArrayList<Token> exp, ArrayList<String> symbol) {
        ArrayList<ArrayList<Token>> exps = new ArrayList<>();
        ArrayList<Token> exp1 = new ArrayList<>();
        ArrayList<Token> symbols = new ArrayList<>();
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
                symbols.add(nextToken);
                exp1 = new ArrayList<>();
            } else {
                exp1.add(nextToken);
            }
            unaryFlag = nextToken.typeIs("IDENFR") || nextToken.typeIs("RPARENT") || nextToken.typeIs("INTCON")
                    || nextToken.typeIs("RBRACK");
        }
        exps.add(exp1);
        return new Exps(exps, symbols);
    }

    private void analyseMulExp(ArrayList<Token> exp) { // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("MULT", "DIV", "MOD")));
        int j = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
<<<<<<< Updated upstream
            analyseUnaryExp(exp1); // UnaryExp
=======
            intType = analyseUnaryExp(exp1); // UnaryExp
            if (j > 0) {
                if (exps.getSymbols().get(j - 1).typeIs(String.valueOf(Word.MULT))) {
                    codes.add(new PCode(Operator.MUL));
                } else if (exps.getSymbols().get(j - 1).typeIs(String.valueOf(Word.DIV))) {
                    codes.add(new PCode(Operator.DIV));
                } else if (exps.getSymbols().get(j - 1).typeIs(String.valueOf(Word.MOD))){
                    codes.add(new PCode(Operator.MOD));
                }
            }
>>>>>>> Stashed changes
            grammar.add("<MulExp>");
            if (j < exps.getSymbols().size()) { // MulExp ('*' | '/' | '%')
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

    private void analyseAddExp(ArrayList<Token> exp) { // AddExp → MulExp | AddExp ('+' | '−') MulExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("PLUS", "MINU")));
        int j = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
<<<<<<< Updated upstream
            analyseMulExp(exp1); // MulExp
=======
            intType = analyseMulExp(exp1); // MulExp
            if (j > 0) {
                if (exps.getSymbols().get(j - 1).typeIs(String.valueOf(Word.PLUS))) {
                    codes.add(new PCode(Operator.ADD));
                } else if (exps.getSymbols().get(j - 1).typeIs(String.valueOf(Word.MINU))){
                    codes.add(new PCode(Operator.SUB));
                }
            }
>>>>>>> Stashed changes
            grammar.add("<AddExp>");
            if (j < exps.getSymbols().size()) { // AddExp ('+' | '−') MulExp
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

    private void analyseRelExp(ArrayList<Token> exp) { // AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("LSS", "LEQ", "GRE", "GEQ")));
        int j = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
            analyseAddExp(exp1);
            if (j > 0) {
                if (exps.getSymbols().get(j - 1).typeIs(String.valueOf(Word.LSS))) {
                    codes.add(new PCode(Operator.LT));
                } else if (exps.getSymbols().get(j - 1).typeIs(String.valueOf(Word.LEQ))) {
                    codes.add(new PCode(Operator.LTE));
                } else if (exps.getSymbols().get(j - 1).typeIs(String.valueOf(Word.GRE))) {
                    codes.add(new PCode(Operator.GT));
                } else if (exps.getSymbols().get(j - 1).typeIs(String.valueOf(Word.GEQ))){
                    codes.add(new PCode(Operator.GTE));
                }
            }
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
            if (j > 0) {
                if (exps.getSymbols().get(j - 1).typeIs(String.valueOf(Word.EQL))) {
                    codes.add(new PCode(Operator.EQ));
                } else if (exps.getSymbols().get(j - 1).typeIs(String.valueOf(Word.NEQ))) {
                    codes.add(new PCode(Operator.NE));
                }
            }
            grammar.add("<EqExp>");
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

<<<<<<< Updated upstream
    private void analyseLAndExp(ArrayList<Token> exp) { // LAndExp → EqExp | LAndExp '&&' EqExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("AND"))); // &&
=======
    private void analyseLAndExp(ArrayList<Token> exp, String from, String label) { // LAndExp → EqExp | LAndExp '&&' EqExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList(String.valueOf(Word.AND)))); // &&
>>>>>>> Stashed changes
        int j = 0;
        for (int i = 0; i < exps.getTokens().size(); i++) {
            ArrayList<Token> exp1 = exps.getTokens().get(i);
            analyseEqExp(exp1); // EqExp
            if (j > 0) {
                codes.add(new PCode(Operator.AND));
            }
            if (exps.getTokens().size() > 1 && i != exps.getTokens().size() - 1) {
                if (from.equals(Word.IFTK)) {
                    codes.add(new PCode(Operator.JZ, label));
                } else {
                    codes.add(new PCode(Operator.JZ, label));
                }
            }
            // todo
            grammar.add("<LAndExp>");
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

<<<<<<< Updated upstream
    private void analyseLOrExp(ArrayList<Token> exp) { // LOrExp → LAndExp | LOrExp '||' LAndExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("OR"))); // ||
=======
    private void analyseLOrExp(ArrayList<Token> exp, String from) { // LOrExp → LAndExp | LOrExp '||' LAndExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList(String.valueOf(Word.OR)))); // ||
>>>>>>> Stashed changes
        int j = 0;
        for (int i = 0; i < exps.getTokens().size(); i++) {
            ArrayList<Token> exp1 = exps.getTokens().get(i);
            String label = labelGenerator.getLabel("cond_" + i);
            analyseLAndExp(exp1, from, label); // LAndExp
            codes.add(new PCode(Operator.LABEL, label));
            if (j > 0) {
                codes.add(new PCode(Operator.OR));
            }
            if (exps.getTokens().size() > 1 && i != exps.getTokens().size() - 1) {
                if (from.equals(Word.IFTK)) {
                    codes.add(new PCode(Operator.JNZ, ifLabels.get(ifLabels.size() - 1).get("if_block")));
                } else if(from.equals(Word.FORTK)){
                    codes.add(new PCode(Operator.JNZ, forLabels.get(forLabels.size() - 1).get("for_block")));
                }
            }
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
        Token nextToken = getNext();
        while (true) {
            if (nextToken.typeIs("SEMICN") || nextToken.typeIs("ASSIGN") || nextToken.typeIs("RBRACE")) {
                break;
            }
            if (nextToken.typeIs("COMMA") && !inFunc) {
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
            current = tokens.get(index);
            index++;
            exp.add(current);
            nextToken = getNext();
        }
        return exp;
    }

    private void error() {
        //
    }

<<<<<<< Updated upstream
=======
    private void addSymbol(Token token, String node, int intType, int areaID) {
        symboltable.get(area).addSymbol(node, intType, token, areaID);
    }

    private boolean checkSymbol(Token token) {
        for (SymbolTable s : symboltable.values()) {
            if (s.findSymbol(token)) {
                return true;
            }
        }
        return false;
    }

    // 检查分号
    private void checkSemicn() {
        if (getNext().typeIs(String.valueOf(Word.SEMICN))) {
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

    private boolean checkFunction(Token token) {
        return functions.containsKey(token.getContent());
    }

    private Function getFunction(Token token) {
        return functions.getOrDefault(token.getContent(), null);
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

    // 这段代码使用了流式操作。它首先将 symboltable 中的每个 SymbolTable 实例转换成流，然后使用 filter 方法找到符合条件的 SymbolTable，
    // 接着使用 map 方法得到对应的 Symbol，最后使用 findFirst 获取第一个匹配的 Symbol 或者如果没有匹配则返回 null。
    private Symbol getSymbol(Token token) {
        return symboltable.values().stream()
                .filter(s -> s.findSymbol(token))
                .map(s -> s.getSymbol(token))
                .findFirst()
                .orElse(null);
    }

>>>>>>> Stashed changes
    public void printWords(FileWriter writer) throws IOException {
        for (String str : grammar) {
            writer.write(str + "\n");
        }
        writer.flush();
        writer.close();
    }

<<<<<<< Updated upstream
=======
    //     使用了 Comparator.comparingInt 和 Lambda 表达式来替代匿名比较器类。
//     同时使用 try-with-resources 语句管理文件写入，自动确保资源被正确关闭，无需手动调用 writer.close()方法。
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

    public void printPCode() {
        for (PCode code : codes) {
            System.out.println(code);
        }
    }

    public ArrayList<PCode> getCodes() {
        return codes;
    }
>>>>>>> Stashed changes
}
