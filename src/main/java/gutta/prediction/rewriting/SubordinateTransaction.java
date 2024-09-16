package gutta.prediction.rewriting;

final class SubordinateTransaction extends Transaction {
    
    private final Transaction parent;
    
    public SubordinateTransaction(Transaction parent, String id) {
        super(id);
        
        this.parent = parent;        
        parent.registerSubordinate(this);
    }

    @Override
    public TransactionOutcome commit() {
        return null;
    }        

}
