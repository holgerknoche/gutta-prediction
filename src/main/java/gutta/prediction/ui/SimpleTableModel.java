package gutta.prediction.ui;

import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Simple table model to display a list of objects.
 * 
 * @param <T> The type of objects in the table model
 */
abstract class SimpleTableModel<T> extends AbstractTableModel {

    private static final long serialVersionUID = 5644878325338924832L;

    private final List<String> columnNames;

    private final List<T> values;

    /**
     * Creates a new table model with the given column names and values.
     * 
     * @param columnNames The column names in the table model
     * @param values      The values in the table model
     */
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

    /**
     * Returns the object in the given row.
     * 
     * @param rowIndex The index of the row
     * @return The object in the given row
     */
    public T getValue(int rowIndex) {
        return this.values.get(rowIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T object = this.values.get(rowIndex);
        return this.fieldOf(object, columnIndex);
    }

    /**
     * Returns the field of the given object in the given column.
     * 
     * @param object      The object to return the respective field of
     * @param columnIndex The column index of the desired field
     * @return The appropriate value
     */
    protected abstract Object fieldOf(T object, int columnIndex);

}
