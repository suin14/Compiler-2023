package PCode;

import java.util.concurrent.atomic.AtomicInteger;

public class LabelGenerator {
    private final AtomicInteger count = new AtomicInteger(0);

    public String getLabel(String type) {
        int currentCount = count.incrementAndGet();
        return "label_" + type + "_" + currentCount;
    }
}
