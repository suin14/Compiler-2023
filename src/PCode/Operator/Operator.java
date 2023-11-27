package PCode.Operator;

public enum Operator {
    LABEL(OperationCategory.FLOW_CONTROL),    // 标签
    VAR(OperationCategory.FLOW_CONTROL),      // 变量

    PUSH(OperationCategory.ARITHMETIC),       // 入栈
    POP(OperationCategory.ARITHMETIC),       // 出栈

    ADD(OperationCategory.ARITHMETIC),       // 加法
    SUB(OperationCategory.ARITHMETIC),       // 减法
    MUL(OperationCategory.ARITHMETIC),       // 乘法
    DIV(OperationCategory.ARITHMETIC),       // 除法
    MOD(OperationCategory.ARITHMETIC),       // 取模

    EQ(OperationCategory.LOGICAL),       // 比较相等
    NE(OperationCategory.LOGICAL),       // 比较不等
    GT(OperationCategory.LOGICAL),       // 比较大于
    LT(OperationCategory.LOGICAL),       // 比较小于
    GTE(OperationCategory.LOGICAL),       // 比较大于等于
    LTE(OperationCategory.LOGICAL),       // 比较小于等于
    AND(OperationCategory.LOGICAL),          // 逻辑与
    OR(OperationCategory.LOGICAL),           // 逻辑或
    NOT(OperationCategory.LOGICAL),          // 逻辑非

    NEG(OperationCategory.ARITHMETIC),      // 取负
    POS(OperationCategory.ARITHMETIC),      // 取正

    JZ(OperationCategory.FLOW_CONTROL),      // 跳转为零
    JNZ(OperationCategory.FLOW_CONTROL),     // 跳转非零
    JMP(OperationCategory.FLOW_CONTROL),     // 无条件跳转

    FUNC(OperationCategory.FUNCTION),        // 函数
    MAIN(OperationCategory.FUNCTION),        // 主函数
    END_FUNC(OperationCategory.FUNCTION),    // 结束函数
    PARAM(OperationCategory.FUNCTION),        // 参数
    RET(OperationCategory.FUNCTION),         // 返回
    CALL(OperationCategory.FUNCTION),        // 调用
    RPARAM(OperationCategory.FUNCTION),      // 右参数

    GETINT(OperationCategory.I_O),           // 获取整数输入
    PRINT(OperationCategory.I_O),            // 输出打印

    DIMVAR(OperationCategory.FLOW_CONTROL),  // 维度变量
    VALUE(OperationCategory.FLOW_CONTROL),   // 值
    ADDRESS(OperationCategory.FLOW_CONTROL), // 地址
    PLACEHOLDER(OperationCategory.FLOW_CONTROL), // 占位符
    EXIT(OperationCategory.FLOW_CONTROL);    // 退出

    private final OperationCategory category;  // 操作类别

    Operator(OperationCategory category) {
        this.category = category;
    }

    public OperationCategory getCategory() {
        return category;
    }
}
