package Parser;
import Lexer.Token;
import Parser.Exps;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class GrammaticalAnalyser {
    private ArrayList<Token> tokens;
    private int index;
    private Token current;
    private ArrayList<String> grammar;

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

    private void analyseCompUnit() { // CompUnit → {Decl} {FuncDef} MainFuncDef
        Token nextToken = getNext();
        while (nextToken.typeIs("CONSTTK") || (nextToken.typeIs("INTTK") && tokens.get(index + 1).typeIs("IDENFR")
                && !tokens.get(index + 2).typeIs("LPARENT"))) { // {Decl}
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
        Token nextToken = getNext();
        while (nextToken.typeIs("LBRACK")) {
            getToken(); // [
            analyseConstExp(getExp()); // ConstExp
            getToken(); // ]
            nextToken = getNext();
        }
        if (nextToken.typeIs("ASSIGN")) {
            getToken(); // =
            analyseInitVal(); // InitVal
        }
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
        analyseFuncType(); // FuncType
        getToken(); // Ident
        getToken(); // (
        Token nextToken = getNext();
        if (!nextToken.typeIs("RPARENT")) {
            analyseFuncFParams();
        }
        getToken(); // )
        analyseBlock(); // Block
        grammar.add("<FuncDef>"); // 函数定义
    }

    private void analyseMainFuncDef() { // MainFuncDef → 'int' 'main' '(' ')' Block
        getToken(); // int
        getToken(); // main
        getToken(); // (
        getToken(); // )
        analyseBlock(); // Block
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
            analyseStmt(); // Stmt
        }
    }

    private void analyseStmt() {
        Token nextToken = getNext();
        if (nextToken.typeIs("IDENFR")) { // LVal '=' Exp ';' | LVal '=' 'getint''('')'';'
            ArrayList<Token> exp = getExp();
            if (!getNext().typeIs("SEMICN")) {
                analyseLVal(exp); // LVal
                getToken(); // =
                if (getNext().typeIs("GETINTTK")) { // 'getint''('')'';'
                    getToken(); // getint
                    getToken(); // (
                    getToken(); // )
                    getToken(); // ;
                } else {
                    analyseExp(getExp()); // Exp
                    getToken(); // ;
                }
            } else {
                analyseExp(exp);
                getToken(); // ;
            }
        } else if (nextToken.typeSymbolizeExp()) { // [Exp] ';'
            analyseExp(getExp());
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
            getToken(); // for
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
            getToken(); // printf
            getToken(); // (
            getToken(); // STRCON
            nextToken = getNext();
            while (nextToken.typeIs("COMMA")) {
                getToken(); // ,
                analyseExp(getExp()); // Exp
                nextToken = getNext();
            }
            getToken(); // )
            getToken(); // ;
        } else if (nextToken.typeIs("SEMICN")) { // ;
            getToken(); // ;
        } else if (nextToken.typeIs("REPEATTK")) {  // Stmt → 'repeat' Stmt 'until' '(' Cond ')' ';'
            getToken(); // repeat
            analyseStmt();  // Stmt
            nextToken = getNext();
            if (nextToken.typeIs("UNTILTK")) {
                getToken(); // until
                getToken(); // (
                analyseCond();
                getToken(); // )
                getToken(); // ;
            } else {
                error();
            }
        }
        grammar.add("<Stmt>");
    }

    private void analyseForStmt() { // ForStmt → LVal '=' Exp
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

    private void analyseCond() { // Cond → LOrExp
        analyseLOrExp(getExp());
        grammar.add("<Cond>");
    }

    private void analyseLVal(ArrayList<Token> exp) { // LVal → Ident {'[' Exp ']'}
        grammar.add(exp.get(0).toString()); // Ident
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
        } else if (nextToken.typeIs("IDENFR")) { // LVal
            analyseLVal(exp);
        } else if (nextToken.typeIs("INTCON")) { // Number
            analyseNumber(exp.get(0));
        } else {
            error();
        }
        grammar.add("<PrimaryExp>");
    }

    private void analyseNumber(Token nextToken) { // Number → IntConst
        grammar.add(nextToken.toString());
        grammar.add("<Number>");
    }

    private void analyseUnaryExp(ArrayList<Token> exp) { // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'
                                                        // | UnaryOp UnaryExp
        Token nextToken = exp.get(0);
        if (nextToken.typeIs("PLUS") || nextToken.typeIs("MINU") || nextToken.typeIs("NOT")) { // UnaryOp UnaryExp
            analyseUnaryOp(exp.get(0));
            analyseUnaryExp(new ArrayList<>(exp.subList(1, exp.size())));
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
            } else {
                analysePrimaryExp(exp); // PrimaryExp
            }
        }
        grammar.add("<UnaryExp>");
    }

    private void analyseUnaryOp(Token nextToken) { // UnaryOp → '+' | '−' | '!'
        grammar.add(nextToken.toString());
        grammar.add("<UnaryOp>");
    }

    private void analyseFuncRParams(ArrayList<Token> exp) { // FuncRParams → Exp { ',' Exp }
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("COMMA")));
        int j = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
            analyseExp(exp1); // Exp
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
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
            analyseUnaryExp(exp1); // UnaryExp
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
            analyseMulExp(exp1); // MulExp
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

    public void printWords(FileWriter writer) throws IOException {
        for (String str : grammar) {
            writer.write(str + "\n");
        }
        writer.flush();
        writer.close();
    }

}
