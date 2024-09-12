package gutta.prediction.domain;

public record ComponentConnectionProperties(long latency, ConnectionType type, boolean modified) {

    public enum ConnectionType {
        LOCAL {
            
            @Override
            public boolean isRemote() {
                return false;
            }
            
        },
        
        REMOTE_WITH_TRANSACTION_PROPAGATION {
            
            @Override
            public boolean isRemote() {
                return true;
            }
            
        },
        
        REMOTE_WITHOUT_TRANSACTION_PROPAGATION {
            
            @Override
            public boolean isRemote() {
                return true;
            }
            
        };
        
        public abstract boolean isRemote();
        
    }

}
