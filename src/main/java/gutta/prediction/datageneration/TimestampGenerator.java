package gutta.prediction.datageneration;

import java.util.Random;

class TimestampGenerator {

    private final Random random = new Random();

    private final long stepSize;
    
    private final long minLatency;
    
    private final long maxLatency;
    
    private long currentTimestamp;    

    public TimestampGenerator(long stepSize, long minLatency, long maxLatency) {
        this(0, stepSize, minLatency, maxLatency);
    }

    public TimestampGenerator(long startTimestamp, long stepSize, long minLatency, long maxLatency) {        
        this.currentTimestamp = startTimestamp;
        this.stepSize = stepSize;
        this.minLatency = minLatency;
        this.maxLatency = maxLatency;
    }
    
    public long nextStep() {
        return (this.currentTimestamp += this.stepSize);
    }
    
    public long nextLatency() {                
        var latency = (this.minLatency == this.maxLatency) ? this.minLatency : this.random.nextLong(this.minLatency, this.maxLatency);
        return (this.currentTimestamp += latency);
    }

}
