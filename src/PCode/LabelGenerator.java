package PCode;

import java.util.concurrent.atomic.AtomicInteger;

public class LabelGenerator {
    private final AtomicInteger labelCount = new AtomicInteger(0);

    public String generateLabel(String type) {
        int currentCount = labelCount.incrementAndGet();
        return String.format("label_%s_%d", type, currentCount);
    }
}
