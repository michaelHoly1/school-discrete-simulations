package ds_sp2_holy.app.eventdriven.simulation;

public class Statistics {

    private int dataCount;
    private double sum;
    private double sumOfDataSquares;

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


    public Statistics() {
        this.dataCount = 0;
        this.sum = 0;
        this.sumOfDataSquares = 0;
    }

    public void addData(double data) {
        this.dataCount++;
        this.sum += data;
        this.sumOfDataSquares += data * data;
    }

    public double getSampleStandardDeviation() {
        if (this.dataCount < 2) {
            throw new IllegalArgumentException("At least two data points are required to calculate the standard deviation");
        }
        return Math.sqrt((this.sumOfDataSquares - (this.sum * this.sum) / this.dataCount) / (this.dataCount - 1));
    }

    public double getMean() {
        if (this.dataCount == 0) {
            throw new IllegalStateException("No data points to calculate the mean");
        }
        return this.sum / this.dataCount;
    }

    public int getDataCount() {
        return this.dataCount;
    }

    public void reset() {
        this.dataCount = 0;
        this.sum = 0;
        this.sumOfDataSquares = 0;
    }

    

    public double[] getConfidenceInterval(int confidenceLevel) {
        double standardDeviation;
        double meanValue;
        double criticalValue;
        double marginOfError;
        
        if (this.dataCount < 2) {
            throw new IllegalArgumentException("At least two data points are required to calculate the confidence interval");
        } else if (confidenceLevel != 95 && confidenceLevel != 99) {
            throw new IllegalArgumentException("Unsupported confidence level");
        } else {
            standardDeviation = this.getSampleStandardDeviation();
            meanValue = this.getMean();

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

            marginOfError = criticalValue * standardDeviation / Math.sqrt(this.dataCount);

            return new double[] {meanValue - marginOfError, meanValue, meanValue + marginOfError};

        }

        
    }

    

}
