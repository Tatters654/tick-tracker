package com.TickTracker;


public class RunningStats {
    // Running average/stdev code from here:
    // https://nestedsoftware.com/2018/03/27/calculating-standard-deviation-on-streaming-data-253l.23919.html
    private double mean;
    private double dSquared;
    private long count;

    public void update(double newValue) {
        count++;
        double meanDifferential = (newValue - mean) / count;
        double newMean = mean + meanDifferential;
        double dSquaredIncrement = (newValue - newMean) * (newValue - mean);
        double newDSquared = dSquared + dSquaredIncrement;
        mean = newMean;
        dSquared = newDSquared;
    }

    public double getMean() {
        return mean;
    }

    public double getVariance() {
        return count > 1 ? dSquared / (count - 1) : 0;
    }

    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }

    public void reset() {
        count = 0;
        dSquared = 0;
        mean = 0;
    }
}
