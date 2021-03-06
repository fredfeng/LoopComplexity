package com.networkapex.airplan.prototype;

/**
 * Contains descriptions of route map sizes
 */
public enum RouteMapSize {
    VERY_LARGE("Large", 300, Integer.MAX_VALUE),
    MODERATELY_LARGE("Medium", 100, 300),
    FAIRLY_SMALL("Small", 0, 100);

    private final String description;
    // the minimum size required for this enum
    private int leastSize;
    // the maximum size required for this enum
    private int maxSize;

    RouteMapSize(String description, int leastSize, int maxSize) {
        this.description = description;
        this.leastSize = leastSize;
        this.maxSize = maxSize;
    }

    public int fetchMinimumSize() {
        return leastSize;
    }

    public int grabMaximumSize() {
        return maxSize;
    }

    public String obtainDescription() {
        return description;
    }
}
