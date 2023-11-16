package Error;

import Lexer.*;
import java.util.ArrayList;

public class Function {
    private String type;
    private String content;
    private String returnType;
    private ArrayList<Integer> params;

    public Function(Token token, String returnType) {
        this.type = token.getType();
        this.content = token.getContent();
        this.returnType = returnType;
    }

    public ArrayList<Integer> getParams() {
        return params;
    }

    public void setParams(ArrayList<Integer> params) {
        this.params = params;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getReturnType() {
        return returnType;
    }
}
