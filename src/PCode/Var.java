package PCode;

public class Var {
    private final int index;
    private int dimension = 0;
    private final int[] dimensions = new int[2];

    public Var(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    /**
     * 获取维度值数组。
     *
     * @return 维度值数组
     */
    public int[] getDimensions() {
        return dimensions;
    }

    /**
     * 获取指定维度的值。
     *
     * @param dimension 维度索引（从 1 开始）
     * @return 指定维度的值
     */
    public int getDimensionValue(int dimension) {
        assertValidDimension(dimension);
        return dimensions[dimension - 1];
    }
    private void assertValidDimension(int dimension) {
        if (dimension <= 0 || dimension > this.dimension) {
            throw new IllegalArgumentException("Invalid dimension index: " + dimension);
        }
    }

    /**
     * 设置指定维度的值。
     *
     * @param dimension 维度索引（从 1 开始）
     * @param value     维度值
     */
    public void setDimensionValue(int dimension, int value) {
        if (dimension > 0 && dimension <= this.dimension) {
            dimensions[dimension - 1] = value;
        } else {
            // 处理维度索引越界的情况
            throw new IllegalArgumentException("Invalid dimension index: " + dimension);
        }
    }

    /**
     * 获取维度数。
     *
     * @return 维度数
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * 设置维度数。
     *
     * @param dimension 维度数
     */
    public void setDimension(int dimension) {
        this.dimension = dimension;
    }
}