package gutta.prediction.rewriting;

final class SubordinateTransaction extends Transaction {
    
    private final Transaction parent;
    
    public SubordinateTransaction(Transaction parent) {
        this.parent = parent;
    }

}
