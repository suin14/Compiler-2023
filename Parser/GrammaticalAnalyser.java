import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class GrammaticalAnalyser {
    private ArrayList<Word> words;
    private int index;
    private Word currentWord;
    private ArrayList<String> grammar;

    public GrammaticalAnalyser(ArrayList<Word> words) {
        this.words = words;
        index = 0;
        grammar = new ArrayList<>();
        analyseCompUnit();
    }

    private void getWord() {
        currentWord = words.get(index);
        grammar.add(currentWord.toString());
        index++;
    }

    private void getWordWithoutAddToGrammar() {
        currentWord = words.get(index);
        index++;
    }

    private Word getNextWord() {
        return words.get(index);
    }

    private Word getNext2Word() {
        return words.get(index + 1);
    }

    private Word getNext3Word() {
        return words.get(index + 2);
    }

    private void analyseCompUnit() { // CompUnit → {Decl} {FuncDef} MainFuncDef
        Word word = getNextWord();
        while (word.typeEquals("CONSTTK") || (word.typeEquals("INTTK") && getNext2Word().typeEquals("IDENFR")
                && !getNext3Word().typeEquals("LPARENT"))) { // {Decl}
            analyseDecl();
            word = getNextWord();
        }
        while (word.typeEquals("VOIDTK") || ((word.typeEquals("INTTK") && !getNext2Word().typeEquals("MAINTK")))) { // {FuncDef}
            analyseFuncDef();
            word = getNextWord();
        }
        if (word.typeEquals("INTTK") && getNext2Word().typeEquals("MAINTK")) { // MainFuncDef
            analyseMainFuncDef();
        } else {
            error();
        }
        grammar.add("<CompUnit>"); // 编译单元
    }

    private void analyseDecl() { // Decl → ConstDecl | VarDecl
        Word word = getNextWord();
        if (word.typeEquals("CONSTTK")) { // ConstDecl
            analyseConstDecl();
        } else if (word.typeEquals("INTTK")) { // VarDecl
            analyseVarDecl();
        } else {
            error();
        }
    }

    private void analyseConstDecl() { // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        getWord(); // const
        analyseBType(); // Btype
        analyseConstDef(); // ConstDef
        Word word = getNextWord();
        while (word.typeEquals("COMMA")) {
            getWord(); // ,
            analyseConstDef(); // ConstDef
            word = getNextWord();
        }
        getWord();// ;
        grammar.add("<ConstDecl>");
    }

    private void analyseBType() { // BType → 'int'
        getWord();
    }

    private void analyseConstDef() { // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        getWord(); // Ident
        Word word = getNextWord();
        while (word.typeEquals("LBRACK")) {
            getWord(); // [
            analyseConstExp(getExp()); // ConstExp
            getWord(); // ]
            if (!currentWord.typeEquals("RBRACK")) {
                error();
            }
            word = getNextWord();
        }
        getWord(); // =
        analyseConstInitVal(); // ConstInitVal
        grammar.add("<ConstDef>"); // 定义数组
    }

    private void analyseConstInitVal() { // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        Word word = getNextWord();
        if (word.typeEquals("LBRACE")) {
            getWord(); // {
            word = getNextWord();
            if (!word.typeEquals("RBRACE")) {
                analyseConstInitVal(); // ConstInitVal
                word = getNextWord();
                while (word.typeEquals("COMMA")) {
                    getWord(); // ,
                    analyseConstInitVal(); // ConstInitVal
                    word = getNextWord();
                }
            }
            getWord(); // }
        } else {
            analyseConstExp(getExp()); // ConstExp
        }
        grammar.add("<ConstInitVal>"); // 全局变量声明
    }

    private void analyseVarDecl() { // VarDecl → BType VarDef { ',' VarDef } ';'
        getWord(); // Btype
        analyseVarDef(); // VarDef
        Word word = getNextWord();
        while (word.typeEquals("COMMA")) {
            getWord(); // ,
            analyseVarDef(); // VarDef
            word = getNextWord();
        }
        getWord(); // ;
        grammar.add("<VarDecl>");
    }

    private void analyseVarDef() { // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
        getWord(); // Ident
        Word word = getNextWord();
        while (word.typeEquals("LBRACK")) {
            getWord(); // [
            analyseConstExp(getExp()); // ConstExp
            getWord(); // ]
            word = getNextWord();
        }
        if (word.typeEquals("ASSIGN")) {
            getWord(); // =
            analyseInitVal(); // InitVal
        }
        grammar.add("<VarDef>"); // 定义变量
    }

    private void analyseInitVal() { // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
        Word word = getNextWord();
        if (word.typeEquals("LBRACE")) {
            getWord(); // {
            word = getNextWord();
            if (!word.typeEquals("RBRACK")) {
                analyseInitVal(); // InitVal
                word = getNextWord();
                while (word.typeEquals("COMMA")) {
                    getWord(); // ,
                    analyseInitVal(); // InitVal
                    word = getNextWord();
                }
            }
            getWord(); // }
        } else {
            analyseExp(getExp()); // Exp
        }
        grammar.add("<InitVal>"); // 变量声明
    }

    private void analyseFuncDef() { // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        analyseFuncType(); // FuncType
        getWord(); // Ident
        getWord(); // (
        Word word = getNextWord();
        if (!word.typeEquals("RPARENT")) {
            analyseFuncFParams();
        }
        getWord(); // )
        analyseBlock(); // Block
        grammar.add("<FuncDef>"); // 函数定义
    }

    private void analyseMainFuncDef() { // MainFuncDef → 'int' 'main' '(' ')' Block
        getWord(); // int
        getWord(); // main
        getWord(); // (
        getWord(); // )
        analyseBlock(); // Block
        grammar.add("<MainFuncDef>"); // main函数定义
    }

    private void analyseFuncType() { // FuncType → 'void' | 'int'
        getWord(); // void | int
        grammar.add("<FuncType>"); // 覆盖两种函数类型
    }

    private void analyseFuncFParams() { // FuncFParams → FuncFParam { ',' FuncFParam }
        analyseFuncFParam(); // FuncFParam
        Word word = getNextWord();
        while (word.typeEquals("COMMA")) {
            getWord(); // ,
            analyseFuncFParam(); // FuncFParam
            word = getNextWord();
        }
        grammar.add("<FuncFParams>"); // 函数参数声明
    }

    private void analyseFuncFParam() { // BType Ident ['[' ']' { '[' ConstExp ']' }]
        getWord(); // Btype
        getWord(); // Ident
        Word word = getNextWord();
        if (word.typeEquals("LBRACK")) {
            getWord(); // [
            getWord(); // ]
            word = getNextWord();
            while (word.typeEquals("LBRACK")) {
                getWord(); // [
                analyseConstExp(getExp()); // ConstExp
                getWord(); // ]
                word = getNextWord();
            }
        }
        grammar.add("<FuncFParam>"); // 定义函数形参
    }

    private void analyseBlock() { // Block → '{' { BlockItem } '}'
        getWord(); // {
        Word word = getNextWord();
        while (word.typeEquals("CONSTTK") || word.typeEquals("INTTK") || word.typeSymbolizeStmt()) {
            if (word.typeEquals("CONSTTK") || word.typeEquals("INTTK")) {
                analyseBlockItem(); // BlockItem
            } else {
                analyseStmt();
            }
            word = getNextWord();
        }
        getWord(); // }
        grammar.add("<Block>"); // 语句块
    }

    private void analyseBlockItem() { // BlockItem → Decl | Stmt
        Word word = getNextWord();
        if (word.typeEquals("CONSTTK") || word.typeEquals("INTTK")) {
            analyseDecl(); // Decl
        } else {
            analyseStmt(); // Stmt
        }
    }

    private void analyseStmt() {
        Word word = getNextWord();
        if (word.typeEquals("IDENFR")) { // LVal '=' Exp ';' | LVal '=' 'getint''('')'';'
            ArrayList<Word> exp = getExp();
            if (!getNextWord().typeEquals("SEMICN")) {
                analyseLVal(exp); // LVal
                getWord(); // =
                if (getNextWord().typeEquals("GETINTTK")) { // 'getint''('')'';'
                    getWord(); // getint
                    getWord(); // (
                    getWord(); // )
                    getWord(); // ;
                } else {
                    analyseExp(getExp()); // Exp
                    getWord(); // ;
                }
            } else {
                analyseExp(exp);
                getWord(); // ;
            }
        } else if (word.typeSymbolizeExp()) { // [Exp] ';'
            analyseExp(getExp());
            getWord(); // ;
        } else if (word.typeEquals("LBRACE")) { // Block
            analyseBlock();
        } else if (word.typeEquals("IFTK")) { // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            getWord(); // if
            getWord(); // (
            analyseCond(); // Cond
            getWord(); // )
            analyseStmt(); // Stmt
            word = getNextWord();
            if (word.typeEquals("ELSETK")) {
                getWord(); // else
                analyseStmt(); // Stmt
            }
        } else if (word.typeEquals("FORTK")) { // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            getWord(); // for
            getWord(); // (
            word = getNextWord();
            if (word.typeEquals("IDENFR")) { // ForStmt
                analyseForStmt();
            }
            getWord(); // ;
            word = getNextWord();
            if (word.typeSymbolizeExp()) { // Cond
                analyseCond();
            }
            getWord(); // ;
            word = getNextWord();
            if (word.typeEquals("IDENFR")) { // ForStmt
                analyseForStmt();
            }
            getWord(); // )
            analyseStmt(); // Stmt
        } else if (word.typeEquals("BREAKTK")) { // 'break' ';'
            getWord(); // break
            getWord(); // ;
        } else if (word.typeEquals("CONTINUETK")) { // 'continue' ';'
            getWord(); // continue
            getWord(); // ;
        } else if (word.typeEquals("RETURNTK")) { // 'return' [Exp] ';'
            getWord(); // return
            word = getNextWord();
            if (word.typeSymbolizeExp()) {
                analyseExp(getExp()); // Exp
            }
            getWord(); // ;
        } else if (word.typeEquals("PRINTFTK")) { // 'printf' '(' FormatString { ',' Exp } ')' ';'
            getWord(); // printf
            getWord(); // (
            getWord(); // STRCON
            word = getNextWord();
            while (word.typeEquals("COMMA")) {
                getWord(); // ,
                analyseExp(getExp()); // Exp
                word = getNextWord();
            }
            getWord(); // )
            getWord(); // ;
        } else if (word.typeEquals("SEMICN")) { // ;
            getWord(); // ;
        }
        grammar.add("<Stmt>");
    }

    private void analyseForStmt() { // ForStmt → LVal '=' Exp
        ArrayList<Word> exp = getExp();
        analyseLVal(exp); // LVal
        getWord(); // =
        analyseExp(getExp()); // Exp
        grammar.add("<ForStmt>");
    }

    private void analyseExp(ArrayList<Word> exp) { // Exp → AddExp
        analyseAddExp(exp);
        grammar.add("<Exp>");
    }

    private void analyseCond() { // Cond → LOrExp
        analyseLOrExp(getExp());
        grammar.add("<Cond>");
    }

    private void analyseLVal(ArrayList<Word> exp) { // LVal → Ident {'[' Exp ']'}
        grammar.add(exp.get(0).toString()); // Ident
        if (exp.size() > 1) {
            ArrayList<Word> exp1 = new ArrayList<>();
            int flag = 0;
            for (int i = 1; i < exp.size(); i++) {
                Word word = exp.get(i);
                if (word.typeEquals("LBRACK")) { // [
                    flag++;
                    if (flag == 1) {
                        grammar.add(word.toString());
                        exp1 = new ArrayList<>();
                    } else {
                        exp1.add(word);
                    }
                } else if (word.typeEquals("RBRACK")) { // ]
                    flag--;
                    if (flag == 0) {
                        analyseExp(exp1);
                        grammar.add(word.toString());
                    } else {
                        exp1.add(word);
                    }
                } else {
                    exp1.add(word);
                }
            }
        }
        grammar.add("<LVal>");
    }

    private void analysePrimaryExp(ArrayList<Word> exp) { // PrimaryExp → '(' Exp ')' | LVal | Number
        Word word = exp.get(0);
        if (word.typeEquals("LPARENT")) {
            // remove ( )
            grammar.add(exp.get(0).toString());
            analyseExp(new ArrayList<>(exp.subList(1, exp.size() - 1))); // Exp
            grammar.add(exp.get(exp.size() - 1).toString());
        } else if (word.typeEquals("IDENFR")) { // LVal
            analyseLVal(exp);
        } else if (word.typeEquals("INTCON")) { // Number
            analyseNumber(exp.get(0));
        } else {
            error();
        }
        grammar.add("<PrimaryExp>");
    }

    private void analyseNumber(Word word) { // Number → IntConst
        grammar.add(word.toString());
        grammar.add("<Number>");
    }

    private void analyseUnaryExp(ArrayList<Word> exp) { // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'
                                                        // | UnaryOp UnaryExp
        Word word = exp.get(0);
        if (word.typeEquals("PLUS") || word.typeEquals("MINU") || word.typeEquals("NOT")) { // UnaryOp UnaryExp
            analyseUnaryOp(exp.get(0));
            analyseUnaryExp(new ArrayList<>(exp.subList(1, exp.size())));
        } else if (exp.size() == 1) {
            analysePrimaryExp(exp); // PrimaryExp
        } else {
            if (exp.get(0).typeEquals("IDENFR") && exp.get(1).typeEquals("LPARENT")) { // Ident (
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

    private void analyseUnaryOp(Word word) { // UnaryOp → '+' | '−' | '!'
        grammar.add(word.toString());
        grammar.add("<UnaryOp>");
    }

    private void analyseFuncRParams(ArrayList<Word> exp) { // FuncRParams → Exp { ',' Exp }
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("COMMA")));
        int j = 0;
        for (ArrayList<Word> exp1 : exps.getWords()) {
            analyseExp(exp1); // Exp
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
        grammar.add("<FuncRParams>");
    }

    private Exps divideExp(ArrayList<Word> exp, ArrayList<String> symbol) {
        ArrayList<ArrayList<Word>> exps = new ArrayList<>();
        ArrayList<Word> exp1 = new ArrayList<>();
        ArrayList<Word> symbols = new ArrayList<>();
        boolean unaryFlag = false;
        int flag1 = 0;
        int flag2 = 0;
        for (int i = 0; i < exp.size(); i++) {
            Word word = exp.get(i);
            if (word.typeEquals("LPARENT")) {
                flag1++;
            }
            if (word.typeEquals("RPARENT")) {
                flag1--;
            }
            if (word.typeEquals("LBRACK")) {
                flag2++;
            }
            if (word.typeEquals("RBRACK")) {
                flag2--;
            }
            if (symbol.contains(word.getType()) && flag1 == 0 && flag2 == 0) {
                // UnaryOp
                if (word.typeOfUnary()) {
                    if (!unaryFlag) {
                        exp1.add(word);
                        continue;
                    }
                }
                exps.add(exp1);
                symbols.add(word);
                exp1 = new ArrayList<>();
            } else {
                exp1.add(word);
            }
            unaryFlag = word.typeEquals("IDENFR") || word.typeEquals("RPARENT") || word.typeEquals("INTCON")
                    || word.typeEquals("RBRACK");
        }
        exps.add(exp1);
        return new Exps(exps, symbols);
    }

    private void analyseMulExp(ArrayList<Word> exp) { // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("MULT", "DIV", "MOD")));
        int j = 0;
        for (ArrayList<Word> exp1 : exps.getWords()) {
            analyseUnaryExp(exp1); // UnaryExp
            grammar.add("<MulExp>");
            if (j < exps.getSymbols().size()) { // MulExp ('*' | '/' | '%')
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

    private void analyseAddExp(ArrayList<Word> exp) { // AddExp → MulExp | AddExp ('+' | '−') MulExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("PLUS", "MINU")));
        int j = 0;
        for (ArrayList<Word> exp1 : exps.getWords()) {
            analyseMulExp(exp1); // MulExp
            grammar.add("<AddExp>");
            if (j < exps.getSymbols().size()) { // AddExp ('+' | '−') MulExp
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

    private void analyseRelExp(ArrayList<Word> exp) { // AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("LSS", "LEQ", "GRE", "GEQ")));
        int j = 0;
        for (ArrayList<Word> exp1 : exps.getWords()) {
            analyseAddExp(exp1);
            grammar.add("<RelExp>");
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

    private void analyseEqExp(ArrayList<Word> exp) { // EqExp → RelExp | EqExp ('==' | '!=') RelExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("EQL", "NEQ")));
        int j = 0;
        for (ArrayList<Word> exp1 : exps.getWords()) {
            analyseRelExp(exp1);
            grammar.add("<EqExp>");
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

    private void analyseLAndExp(ArrayList<Word> exp) { // LAndExp → EqExp | LAndExp '&&' EqExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("AND"))); // &&
        int j = 0;
        for (ArrayList<Word> exp1 : exps.getWords()) {
            analyseEqExp(exp1); // EqExp
            grammar.add("<LAndExp>");
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

    private void analyseLOrExp(ArrayList<Word> exp) { // LOrExp → LAndExp | LOrExp '||' LAndExp
        Exps exps = divideExp(exp, new ArrayList<>(Arrays.asList("OR"))); // ||
        int j = 0;
        for (ArrayList<Word> exp1 : exps.getWords()) {
            analyseLAndExp(exp1); // LAndExp
            grammar.add("<LOrExp>");
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }

    private void analyseConstExp(ArrayList<Word> exp) { // ConstExp → AddExp
        analyseAddExp(exp);
        grammar.add("<ConstExp>");
    }

    private ArrayList<Word> getExp() {
        ArrayList<Word> exp = new ArrayList<>();
        boolean inFunc = false;
        int funcFlag = 0;
        int flag1 = 0;
        int flag2 = 0;
        Word word = getNextWord();
        while (true) {
            if (word.typeEquals("SEMICN") || word.typeEquals("ASSIGN") || word.typeEquals("RBRACE")) {
                break;
            }
            if (word.typeEquals("COMMA") && !inFunc) {
                break;
            }
            if (word.typeEquals("IDENFR")) {
                if (getNext2Word().typeEquals("LPARENT")) {
                    inFunc = true;
                }
            }
            if (word.typeEquals("LPARENT")) {
                flag1++;
                if (inFunc) {
                    funcFlag++;
                }
            }
            if (word.typeEquals("RPARENT")) {
                flag1--;
                if (inFunc) {
                    funcFlag--;
                    if (funcFlag == 0) {
                        inFunc = false;
                    }
                }
            }
            if (word.typeEquals("LBRACK")) {
                flag2++;
            }
            if (word.typeEquals("RBRACK")) {
                flag2--;
            }
            if (flag1 < 0) {
                break;
            }
            if (flag2 < 0) {
                break;
            }
            getWordWithoutAddToGrammar();
            exp.add(currentWord);
            word = getNextWord();
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
