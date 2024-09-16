package gutta.prediction.rewriting;

import java.util.HashSet;
import java.util.Set;

sealed abstract class Transaction permits TopLevelTransaction, SubordinateTransaction {
    
    private final String id;
    
    private final Set<SubordinateTransaction> subordinates;
    
    protected Transaction(String id) {
        this.id = id;
        this.subordinates = new HashSet<>();
    }
    
    public String id() {
        return this.id;
    }
    
    protected void registerSubordinate(SubordinateTransaction subordinate) {
        this.subordinates.add(subordinate);
    }
    
    public abstract TransactionOutcome commit();
    
    protected TransactionOutcome commitSubordinates() {
        var outcome = TransactionOutcome.COMPLETED;
        
        for (SubordinateTransaction subordinate : subordinates) {
            var currentOutcome = subordinate.commit();
            outcome = outcome.joinWith(currentOutcome);
        }
        
        return outcome;
    }
    
}
