package ca.odell.glazedlists.swing;

import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

public class CalculationTableModel extends AbstractTableModel {

    private final TableModel delegate;
    private final TableModelListener delegateListener = new DelegateTableModelListener();

    public CalculationTableModel(TableModel delegate) {
        this.delegate = delegate;
        this.delegate.addTableModelListener(delegateListener);
    }

    public int getRowCount() { return delegate.getRowCount(); }
    public int getColumnCount() { return delegate.getColumnCount(); }
    public String getColumnName(int columnIndex) { return delegate.getColumnName(columnIndex); }
    public Class<?> getColumnClass(int columnIndex) { return delegate.getColumnClass(columnIndex); }
    public boolean isCellEditable(int rowIndex, int columnIndex) { return delegate.isCellEditable(rowIndex, columnIndex); }
    public Object getValueAt(int rowIndex, int columnIndex) { return delegate.getValueAt(rowIndex, columnIndex); }
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) { delegate.setValueAt(aValue, rowIndex, columnIndex); }

    private class DelegateTableModelListener implements TableModelListener {
        public void tableChanged(TableModelEvent e) {
            final TableModelEvent newEvent = new TableModelEvent(CalculationTableModel.this, e.getFirstRow(), e.getLastRow(), e.getColumn(), e.getType());
            fireTableChanged(newEvent);
        }
    }
}