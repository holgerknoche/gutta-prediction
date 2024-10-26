package gutta.prediction.ui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

abstract class SimpleTableModel<T> extends AbstractTableModel {

    private static final long serialVersionUID = 5644878325338924832L;
    
    private final List<String> columnNames;
    
    private final List<T> values;
    
    protected SimpleTableModel(List<String> columnNames, List<T> values) {
        this.columnNames = columnNames;
        this.values = values;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    @Override
    public String getColumnName(int column) {
        return this.columnNames.get(column);
    }
    
    @Override
    public int getColumnCount() {
        return this.columnNames.size();
    }
    
    @Override
    public int getRowCount() {
        return this.values.size();
    }
    
    public T getValue(int rowIndex) {
        return this.values.get(rowIndex);
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T object = this.values.get(rowIndex);
        return this.fieldOf(object, columnIndex);
    }
    
    protected abstract Object fieldOf(T object, int columnIndex);

}
