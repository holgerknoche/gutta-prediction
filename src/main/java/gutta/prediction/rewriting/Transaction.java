package gutta.prediction.rewriting;

sealed abstract class Transaction permits TopLevelTransaction, SubordinateTransaction {

}
