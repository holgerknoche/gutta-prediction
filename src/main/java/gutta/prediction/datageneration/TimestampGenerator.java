package gutta.prediction.datageneration;

import java.util.Random;

class TimestampGenerator {

    private final Random random = new Random();

    private final long stepSize;
    
    private final long minOverhead;
    
    private final long maxOverhead;
    
    private long currentTimestamp;    

    public TimestampGenerator(long stepSize, long minOverhead, long maxOverhead) {
        this(0, stepSize, minOverhead, maxOverhead);
    }

    public TimestampGenerator(long startTimestamp, long stepSize, long minOverhead, long maxOverhead) {        
        this.currentTimestamp = startTimestamp;
        this.stepSize = stepSize;
        this.minOverhead = minOverhead;
        this.maxOverhead = maxOverhead;
    }
    
    public long noStep() {
        return this.currentTimestamp;
    }
    
    public long nextStep() {
        return (this.currentTimestamp += this.stepSize);
    }
    
    public long nextOverhead() {                
        var overhead = (this.minOverhead == this.maxOverhead) ? this.minOverhead : this.random.nextLong(this.minOverhead, this.maxOverhead);
        return (this.currentTimestamp += overhead);
    }

}
