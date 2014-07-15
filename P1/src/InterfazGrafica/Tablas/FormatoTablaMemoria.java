package InterfazGrafica.Tablas;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Sirve para dar formato a la tabla de memoria del Inspector
 */
@SuppressWarnings("serial")
public class FormatoTablaMemoria extends DefaultTableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value, 
												   boolean selected, boolean focused, 
												   int row, int column){
        // Si en cada fila de la tabla, la columna 2 es igual a "??" (Basura)
		 if(String.valueOf(table.getValueAt(row,column)).equals("??")){	// COLOREA SOLO LA CELDA
		//if(String.valueOf(table.getValueAt(row,1)).equals("??")){ 	// COLOREA TODA LA FILA
			setForeground(Color.DARK_GRAY);		// Color de letra: Blanco
			setBackground(Color.LIGHT_GRAY);		// Color de Fondo: Gris
			}
		else {
			setForeground(Color.BLACK);
			setBackground(Color.WHITE);
		}

		super.getTableCellRendererComponent(table, value, selected, focused, row, column);
		
		return this;
	}
}