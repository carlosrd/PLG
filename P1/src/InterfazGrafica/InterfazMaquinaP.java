package InterfazGrafica;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import InterfazGrafica.Tablas.FormatoTablaMemoria;
import Interprete.Elem;
/**
 * Construye la pestaña Máquina P (Paso a paso) y maneja su comportamiento
 */
@SuppressWarnings("serial")
public class InterfazMaquinaP extends JPanel {

	// ATRIBUTOS
	// ****************************************************************************************

	private JTextField regCProg;
	private JTextField regCPila;
	
	private JCheckBox regSwap1;
	private JCheckBox regSwap2;
	private JCheckBox regParada;
	
	private JButton botonAvanzar;
	private JButton botonParar;
	
	private JTable tProg;
	private JTable tMem;
	
	private DefaultTableModel tablaPila;
	private DefaultTableModel tablaProg;
	private DefaultTableModel tablaMem;
	
	// CONSTRUCTORA
	// ****************************************************************************************
	
	/** Crea la pestaña del Inspector: Máquina P (Paso a Paso)
	 * @param botonAvanzar creado en la interfaz general e insertado en la pestaña
	 * @param botonParar creado en la interfaz general e insertado en la pestaña
	 */
	public InterfazMaquinaP(JButton botonAvanzar, JButton botonParar){
		
		this.setLayout(new BorderLayout(3,3));
		
		JPanel panelTablasCombinado = new JPanel();
		
		panelTablasCombinado.setLayout(new GridLayout(1,3,3,3));
		panelTablasCombinado.add(getVisorPrograma());
		panelTablasCombinado.add(getVisorPila());
		panelTablasCombinado.add(getVisorMemoria());
		
		regCProg.setToolTipText("Verde = Parada por fin # Ambar = Parada Usuario # Rojo = Parada Error");
		
		this.botonAvanzar = botonAvanzar;
		this.botonParar = botonParar;
		
		this.add(panelTablasCombinado,"Center");
		this.add(getVisorRegistros(),"South");

	}
	
	// METODOS
	// ****************************************************************************************
	
	/**
	 * Devuelve el panel inferior con los checkbox que representan los Registros de Parada
	 * Swap1, Swap2 y los botones de Avanzar y Parar
	 */
	private JPanel getVisorRegistros(){
		
		JPanel visorRegistros = new JPanel();
		
		visorRegistros.setLayout(new BorderLayout());
		
		visorRegistros.add(new JLabel("REGISTROS"),"North");
		
		// Añadir el checkbox + etiqueta para el registro Swap1
		JPanel panelCheckSwap1 = new JPanel();
		panelCheckSwap1.setLayout(new BorderLayout());
		
		regSwap1 = new JCheckBox();
		regSwap1.setEnabled(false);
		panelCheckSwap1.add(regSwap1,"West");
		panelCheckSwap1.add(new JLabel("Swap1"),"Center");
		
		// Añadir el checkbox + etiqueta para el registro Swap1
		JPanel panelCheckSwap2 = new JPanel();
		panelCheckSwap2.setLayout(new BorderLayout());
		
		regSwap2 = new JCheckBox();
		regSwap2.setEnabled(false);
		panelCheckSwap2.add(regSwap2,"West");
		panelCheckSwap2.add(new JLabel("Swap2"),"Center");
		
		// Añadir el checkbox + etiqueta para el registro Parada
		JPanel panelCheckParada = new JPanel();
		panelCheckParada.setLayout(new BorderLayout());
		
		regParada = new JCheckBox();
		regParada.setEnabled(false);
		panelCheckParada.add(regParada,"West");
		panelCheckParada.add(new JLabel("Parada"),"Center");

		//
		JPanel panelRegCombinado = new JPanel();
		
		panelRegCombinado.setLayout(new GridLayout(1,2));
		
		panelRegCombinado.add(panelCheckSwap1);
		panelRegCombinado.add(panelCheckSwap2);
		panelRegCombinado.add(panelCheckParada);
		
		visorRegistros.add(panelRegCombinado,"Center");

		// Panel con los botones de avanzar y parar
		JPanel panelBotones = new JPanel();
		
		panelBotones.setLayout(new FlowLayout()); 
		// Se heredan desde la interfaz central para tener el oyente
		panelBotones.add(botonAvanzar);
		panelBotones.add(botonParar);
		
		visorRegistros.add(panelBotones,"South");
		//visorRegistros.add(botonAvanzar,"South");
		
		return visorRegistros;
	}	
	
	/**
	 * Devuelve la tabla de programa para insertar en la pestaña Máquina P (Paso a Paso)
	 * @return Panel Tabla Programa
	 */
	private JPanel getVisorPrograma(){
		
		// Preparamos el contenedor inicial
		JPanel visorPrograma = new JPanel();
		
		// Seteamos el layout al contenedor
		visorPrograma.setLayout(new BorderLayout());
		
		// Añadimos etiquetas 
		JPanel cabecera = new JPanel();
		cabecera.setLayout(new GridLayout(0,1));
	
		cabecera.add(new JLabel("PROGRAMA"));
		cabecera.add(new JLabel("CProg:"));
		
		// Añadimos el registro del contador de programa
		regCProg = new JTextField("0");
		regCProg.setBackground(Color.white);
		regCProg.setEditable(false);
		cabecera.add(regCProg);
		
		visorPrograma.add(cabecera,"North");
		//visorPrograma.add(new JSeparator(SwingConstants.HORIZONTAL));
		// Creamos la tabla para visualizar el programa
		tablaProg = new DefaultTableModel(0,2);
		tablaProg.setColumnIdentifiers(new String[]{"#", "Prog"});

		tProg = new JTable();//tablaProg);
		tProg.setEnabled(false);
		
		tProg.setModel(tablaProg);
		tProg.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		tProg.getColumnModel().getColumn(0).setMaxWidth(35);	
		
		// Creamos un contenedor con scroll para la tabla
		JScrollPane scrollPane = new JScrollPane(tProg);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		visorPrograma.add(scrollPane,"Center");
		
		return visorPrograma;
		
	}
	
	/**
	 *  Devuelve la tabla de la pila para insertar en la pestaña Máquina P (Paso a Paso)
	 * @return Panel Tabla Pila
	 */
	private JPanel getVisorPila(){
		
		// Preparamos el contenedor inicial
		JPanel visorPila = new JPanel();

		// Seteamos el layout al contenedor
		visorPila.setLayout(new BorderLayout());
		
		// Añadimos etiquetas 
		
		JPanel cabecera = new JPanel();
		cabecera.setLayout(new GridLayout(0,1));
	
		cabecera.add(new JLabel("PILA"));
		cabecera.add(new JLabel("CPila:"));
		
		// Añadimos el registro del contador de programa
		regCPila = new JTextField("0");
		regCPila.setBackground(Color.white);
		regCPila.setEditable(false);
		cabecera.add(regCPila);
		
		visorPila.add(cabecera,"North");
		
		// Creamos la tabla para visualizar el programa
		tablaPila = new DefaultTableModel(0,1);
		tablaPila.setColumnIdentifiers(new String[]{"Pila"});
		
		JTable tPila = new JTable(tablaPila);
		tPila.setEnabled(false);
		
		// Creamos un contenedor con scroll para la tabla
		JScrollPane scrollPane = new JScrollPane(tPila);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		visorPila.add(scrollPane,"Center");
		
		return visorPila;
		
	}
	
	/**
	 * Devuelve la tabla de memoria para insertar en la pestaña Máquina P (Paso a Paso)
	 * @return Panel Tabla Memoria
	 */
	private JPanel getVisorMemoria(){
		
		// Preparamos el contenedor inicial
		JPanel visorMemoria = new JPanel();
		
		// Seteamos el layout al contenedor
		visorMemoria.setLayout(new BorderLayout());
		
		// Añadimos etiquetas 
		
		JPanel cabecera = new JPanel();
		cabecera.setLayout(new GridLayout(0,1));
	
		visorMemoria.add(new JLabel("MEMORIA"),"North");

		// Creamos la tabla para visualizar el programa
		tablaMem = new DefaultTableModel(0,2);
		tablaMem.setColumnIdentifiers(new String[]{"@","Mem"});
		
		for (int i = 0; i < 150; i++)
			tablaMem.addRow(new String[]{String.valueOf(i),"??"});
		
		tMem = new JTable();
		tMem.setEnabled(false);
		
		tMem.setDefaultRenderer(Object.class, new FormatoTablaMemoria());
		
		tMem.setModel(tablaMem);
		tMem.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		//tMem.getColumnModel().getColumn(0).setPreferredWidth(20);
		tMem.getColumnModel().getColumn(0).setMaxWidth(35);
		//tMem.getColumnModel().getColumn(1).setPreferredWidth(85);


		// Creamos un contenedor con scroll para la tabla
		JScrollPane scrollPane = new JScrollPane(tMem);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		visorMemoria.add(scrollPane,"Center");
		
		return visorMemoria;
		
	}

	
	// METODOS AUXILIARES PARA ACTUALIZAR LA INTERFAZ
	// ****************************************************************************************
	
	/**
	 * Vacia la interfaz y rellena la tabla Program con el codigo dado por parametro
	 * @param cod : Codigo objeto para el que preparar la interfaz
	 */
	public void preparaInterfazPara(ArrayList<String> cod){
		
		// Vaciar tabla prog
		while(tablaProg.getRowCount() > 0)
			tablaProg.removeRow(0);
		 
		// Inicializar Visor prog
		regCProg.setText("0");
		regCProg.setBackground(Color.WHITE);
		regCProg.setForeground(Color.BLACK);
		
		for(int i = 0; i < cod.size(); i++)
			tablaProg.addRow(new String[]{""+(i+1), cod.get(i)});
		
		// Vaciar tabla pila
		while (tablaPila.getRowCount()> 0)
			tablaPila.removeRow(0);
		 
		// Vaciar tabla memoria
		while(tablaMem.getRowCount() > 0)
			tablaMem.removeRow(0);
		 
		// Inicializar la memoria con basura
		for(int i = 0; i < 10000; i++)
			tablaMem.addRow(new String[]{String.valueOf(i),"??"});
		 
		// Inicializar los registros
		regParada.setSelected(false);
		regSwap1.setSelected(false);
		regSwap2.setSelected(false);
	}
	
	/**
	 * Actualiza el TextFiled que contiene el valor del Contador de Programa
	 * @param tipoParada Sirve para determinar el color que adoptará el TextField en función del
	 * tipo de parada
	 */
	public void actualizaCProgParada(int tipoParada){
		
		// Marcamos el contador de programa en verde
		switch (tipoParada){
					// Parada Natural
			case 0: regCProg.setBackground(new Color(118,238,0));
					regCProg.setForeground(Color.DARK_GRAY);
					//regCProg.
					break;
					
					// Parada Provocada
			case 1: regCProg.setBackground(new Color(255,215,0));
					regCProg.setForeground(Color.BLACK);
					break;
					
					// Parada por Error
			case 2: regCProg.setBackground(Color.RED);
					regCProg.setForeground(Color.WHITE);
					break;
		
		}
	}
	
		// PROGRAMA
	
	/**
	 * Pasa a la siguiente instrucción en la tabla Programa
	 */
	public void pullInstruccion(){
		// Seleccionamos (para marcarla) la instruccion actual
		tProg.changeSelection(Integer.parseInt(regCProg.getText()), 0, false, false);
		// Provoca el scroll automatico. De esta manera, siempre que sea posible,
		// la instruccion actual esta en el centro de la lista y se ven las anteriores
		// y las siguientes (si la ventana esta maximizada)
		tProg.scrollRectToVisible(tProg.getCellRect(Integer.parseInt(regCProg.getText())+5, 0, false));
	}
	
	/**
	 * Actualiza el contador de programa de la interfaz con el parametro dado
	 * @param cp Valor a setear en el Contador de Programa
	 */
	public void setCProg(int cp){
		regCProg.setText(String.valueOf(cp));
	}
	
		// PILA
	/**
	 * Actualiza la tabla de la pila en la interfaz con la pila dada por parámetro
	 * @param pila que recibe de la máquina P para representar gráficamente
	 */
	public void actualizaPila(Stack<Elem> pila){
		
		// Vaciar tabla pila
		while (tablaPila.getRowCount()> 0)
			tablaPila.removeRow(0);
		 
		// Rellenar
		//int i = 0;
		for (int i = 0; i < pila.size(); i++){
			Elem e = pila.elementAt(i);
			if (e.getValor().equals('\n'))
				tablaPila.addRow(new String [] {"\\"+"n"});
			else if (e.getValor().equals('\r'))
				tablaPila.addRow(new String [] {"\\"+"r"});
			else if (e.getValor().equals('\t'))
				tablaPila.addRow(new String [] {"\\"+"t"});
			else
				tablaPila.addRow(new String [] {e.getValor().toString()});
		}
		
	}
	
	/**
	 * Actualiza el contador de cima de la pila de la interfaz con el parametro dado
	 * @param cp Valor a setear la Cima de la Pila 
	 */
	public void setCPila(int cp){
		regCPila.setText(String.valueOf(cp));
	}
	
		// MEMORIA
	
	/**
	 * Actualiza la tabla de memoria en la interfaz con la memoria dada por parámetro
	 * @param memoria devuelta por la Máquina P que se representará gráficamente
	 * @deprecated
	 */
	public void actualizaMemoria(ArrayList<Elem> memoria){
		
		// FIXME: Cambiar para que solo actualice las filas modificadas y no todas
		//		  Demasiada carga para el procesador borrar y reimprimir
		
		// Vaciar tabla memoria
		while (tablaMem.getRowCount()> 0)
			tablaMem.removeRow(0);
			
		// Rellenar
		for (int i = 0; i < memoria.size(); i++){
			Elem e = memoria.get(i);
			if (e.getTipo().equals("null") && e.getValor().equals("null"))
				tablaMem.addRow(new String [] {String.valueOf(i),"??"});						// Rellenamos con basura si no tiene valor
			else if (e.getValor().equals('\n'))
				tablaMem.addRow(new String [] {String.valueOf(i),"\\"+"n"});
			else if (e.getValor().equals('\r'))
				tablaMem.addRow(new String [] {String.valueOf(i),"\\"+"r"});
			else if (e.getValor().equals('\t'))
				tablaMem.addRow(new String [] {String.valueOf(i),"\\"+"t"});
			else
				tablaMem.addRow(new String [] {String.valueOf(i),e.getValor().toString()});		// Rellenamos con valor si tiene
		} // for
		
	}
	
	public JTable getTablaMem(){
		return tMem;
	}
	
	public void setTablaMem(DefaultTableModel tMem){
		tablaMem = tMem;
	}
	
	
		// REGISTROS CHECKBOX
	
	/**
	 * Muestra el Reg de Parada activado
	 */
	public void setRegParadaChecked(){
		regParada.setSelected(true);
	}

	/**
	 * Muestra el Reg Swap1 activado
	 */
	public void setRSwap1Checked(){
		regSwap1.setSelected(true);
	}
	
	/**
	 * Muestra el Reg Swap1 desactivado
	 */
	public void setRSwap1Unchecked(){
		regSwap1.setSelected(false);
	}

	/**
	 * Muestra el Reg Swap2 activado
	 */
	public void setRSwap2Checked(){
		regSwap2.setSelected(true);
	}
	
	/**
	 * Muestra el Reg Swap2 desactivado
	 */
	public void setRSwap2Unchecked(){
		regSwap2.setSelected(false);
	}

}
