package ds_sp2_holy.app.eventdriven.simulation;

public class WeightedStatistics {

    private int dataCount;
    private double totalWeight;
    private double sum;
    private double sumOfWeightedDataSquares; 
    private double sumOfWeightSquares;    

    private static final double[] T_TABLE_95 = {
        12.706, 4.303, 3.182, 2.776, 2.571, 2.447, 2.365, 2.306, 2.262, 2.228,
        2.201, 2.179, 2.160, 2.145, 2.131, 2.120, 2.110, 2.101, 2.093, 2.086,
        2.080, 2.074, 2.069, 2.064, 2.060, 2.056, 2.052, 2.048
    };

    private static final double[] T_TABLE_99 = {
        63.657, 9.925, 5.841, 4.604, 4.032, 3.707, 3.499, 3.355, 3.250, 3.169,
        3.106, 3.055, 3.012, 2.977, 2.947, 2.921, 2.898, 2.878, 2.861, 2.845,
        2.831, 2.819, 2.807, 2.797, 2.787, 2.779, 2.771, 2.763
    };

    public WeightedStatistics() {
        this.dataCount = 0;
        this.totalWeight = 0;
        this.sum = 0;
        this.sumOfWeightedDataSquares = 0;
        this.sumOfWeightSquares = 0;
    }

    public void addData(double data, double weight) {
        this.dataCount++;
        this.totalWeight += weight;
        this.sum += data * weight;
        this.sumOfWeightedDataSquares += (data * data) * weight;
        this.sumOfWeightSquares += weight * weight;
    }

    public int getDataCount() {
        return dataCount;
    }

    public double getMean() {
        if (this.totalWeight > 0) {
            return this.sum / this.totalWeight;
        } else {
            return 0;
        }
    }

    public void reset() {
        this.dataCount = 0;
        this.totalWeight = 0;
        this.sum = 0;
        this.sumOfWeightedDataSquares = 0;
        this.sumOfWeightSquares = 0;
    }

    
    public double getWeightedSampleStandardDeviation() {
        if (this.dataCount < 2) {
            throw new IllegalArgumentException("At least two data points are required to calculate standard deviation");
        }
    
        double mean = getMean();
    
        double varianceSum = this.sumOfWeightedDataSquares - 2 * mean * this.sum + mean * mean * this.totalWeight;
        double adjustedWeight = this.totalWeight - (this.sumOfWeightSquares / this.totalWeight);
    
        if (adjustedWeight <= 0) {
            throw new IllegalStateException("Non-positive adjusted weight in weighted sample variance");
        }
    
        return Math.sqrt(varianceSum / adjustedWeight);
    }

    public double[] getConfidenceInterval(int confidenceLevel) {
        if (this.dataCount < 2) {
            throw new IllegalArgumentException("At least two data points are required to calculate confidence interval");
        }
        if (confidenceLevel != 95 && confidenceLevel != 99) {
            throw new IllegalArgumentException("Unsupported confidence level");
        }

        double standardDeviation = getWeightedSampleStandardDeviation();
        double meanValue = getMean();

        double criticalValue;
        if (this.dataCount < 30) {
            if (confidenceLevel == 95) {
                criticalValue = T_TABLE_95[this.dataCount - 2];
            } else {
                criticalValue = T_TABLE_99[this.dataCount - 2];
            }
        } else {
            
            if (confidenceLevel == 95) {
                criticalValue = 1.9600;
            } else {
                criticalValue = 2.5760;
            }

        }

        double marginOfError = criticalValue * standardDeviation / Math.sqrt(this.dataCount);
        return new double[]{meanValue - marginOfError, meanValue, meanValue + marginOfError};
    }
}
