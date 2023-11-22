package PCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Var {
    private final int index;
    private int dimension = 0;
    private List<Integer> dimensions = new ArrayList<>();

    public Var(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public int getDimensionValue(int dimension) {
        if (dimension > 0 && dimension <= this.dimension) {
            return dimensions.get(dimension - 1);
        }
        throw new IllegalArgumentException("Invalid dimension index: " + dimension);
    }

    public void setDimensionValue(int dimension, int value) {
        if (dimension > 0 && dimension <= this.dimension) {
            dimensions.set(dimension - 1, value);
        } else {
            throw new IllegalArgumentException("Invalid dimension index: " + dimension);
        }
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        if (dimension >= 0) {
            this.dimension = dimension;
            dimensions = new ArrayList<>(Arrays.asList(new Integer[dimension]));
        } else {
            throw new IllegalArgumentException("Dimension cannot be negative: " + dimension);
        }
    }
}
