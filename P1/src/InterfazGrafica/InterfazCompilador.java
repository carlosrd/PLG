package InterfazGrafica;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import AnalizadorSintactico.AnalizadorSintactico;
import InterfazGrafica.Tablas.TableCellLongTextRenderer;
import Interprete.MaquinaP;


/**
 * Construye la Interfaz Gráfica general y maneja su comportamiento
 */
@SuppressWarnings("serial")
public class InterfazCompilador extends JFrame {


	// ATRIBUTOS
	// ****************************************************************************************
	private JTextArea areaTextoCodigo;
	private JTextArea areaTextoConsola;
	private JTextArea areaTextoCodObj;
	private JTextArea areaTextoTrazaMP;
	
	private InterfazMaquinaP guiMP;
	private JButton botonAvanzar;
	private JButton botonParar;
	private JButton botonAvanzarToolBar;
	private JButton botonPararToolBar;
	private JButton botonSigPos;
	private JButton botonSigNeg;
	private JMenuItem opcionAvanzar;
	private JMenuItem opcionParar;
	private JCheckBoxMenuItem opcionLimpiarAutoConsola;
	private MaquinaP interpreteP;
	
	private InterfazAyuda guiAyuda;
	
	private JTextArea visorNumerosLineas;
	private JTextArea visorNumerosLineasCodObj;
	
	private JTabbedPane panelTabulado;
	private JTabbedPane panelTabuladoTS;
	private DefaultTableModel dtmSimbolos;
	
	private String rutaProgActual;			// Ruta del archivo con el que estamos trabajando actualmente
	private String rutaCodObjActual;		// Ruta del archivo que contiene el codigo objeto actual compilado
	
	private int pruebaPositiva;
	private final int maxPos = 30;
	private final String pathPositivo = "Pruebas/Parte 2/Positivas/prueba";
	
	private int pruebaNegativa;
	private final int maxNeg = 79;
	private final String pathNegativo = "Pruebas/Parte 2/Negativas/prueba";
	
	private boolean codigoModificado;		// Indica si hay cambios en el archivo de codigo actual
	private boolean ejecucionSBS;			// Indica si esta activa una ejecucion Paso a Paso
	private boolean autoLimpieza;
	
	final Object[] possibilitiesPos = {"01","02","03","04","05","06","07","08","09",
			  "10","11","12","13","14","15","16","17","18","19",
			  "20","21","22","23","24","25","26","27","28","29",
			  "30"};
	
	final Object[] possibilitiesNeg = {"01","02","03","04","05","06","07","08","09",
			  "10","11","12","13","14","15","16","17","18","19",
			  "20","21","22","23","24","25","26","27","28","29",
			  "30","31","32","33","34","35","36","37","38","39",
			  "40","41","42","43","44","45","46","47","48","49",
			  "50","51","52","53","54","55","56","57","58","59",
			  "60","61","62","63","64","65","66","67","68","69",
			  "70","71","72","73","74","75","76","77","78","79"};
	
	// CONSTRUCTORA
	// ****************************************************************************************
	
	/**
	 * Inicia el proceso de inicialización de la Interfaz del Compilador
	 */
	public InterfazCompilador(){
		
		inicializarInterfaz();
		//JOptionPane.showMessageDialog(null,"PLG Grupo 05"); //TODO Comentado de momento
		
	}
		
	// METODOS
	// *****************************************************************************************
	
	/**
	 * MAIN: Setea el Look and Feel y crea el objeto Interfaz para lanzar la aplicación
	 * @param args
	 */
	public static void main(String[] args) {
			
		// obj representa el frame
		InterfazCompilador obj = new InterfazCompilador();
		obj.setVisible(true);
		obj.setEnabled(true);
		obj.setSize(800,600);
	}

	/**
	 * Rellena la interfaz con los paneles y barras de herramientas definidos
	 */
	private void inicializarInterfaz(){
		
		this.setJMenuBar(getMenuPrincipal());
		
		// Oyente boton Avanzar Interfaz Maquina P
		botonAvanzar = new JButton("Avanzar", new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconNextMenu.png")));
		botonAvanzar.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e){
								accionBotonAvanzar();
							}
		});
		
		// Oyente boton Parar Interfaz Maquina P
		botonAvanzar.setEnabled(false);
		
		botonParar = new JButton("Parar", new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconStopMenu.png")));
		botonParar.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent arg0) {
							accionParar(true);
				
						}
		});
		botonParar.setEnabled(false);
		
		this.setContentPane(getPanelPrincipal());
		
		this.setTitle("Compilador PLG (G05) - Nuevo Programa");
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Sirve para que se termine el proceso cuando se pulsa sobre la X de cerrar
		
		// Seteamos oyente al cierre de la aplicación
		this.addWindowListener(new WindowAdapter()
		{public void windowClosing(WindowEvent e)
			{
			comprobarCambios("Antes de salir...",
							 "¿Desea guardar los cambios hechos en el programa actual?",
							 "Guardar",
							 "Descartar y Salir");
			System.exit(0);
			}
		}
		);
		
		ImageIcon iconoVentana = new ImageIcon((this.getClass().getResource("/resources/icons/toolbar/iconCompile.png")));
		this.setIconImage(iconoVentana.getImage());
		
		rutaProgActual = "";	
		rutaCodObjActual = "";
		codigoModificado = false;
		ejecucionSBS = false;
		autoLimpieza = true;
		
		pruebaPositiva = 0;
		botonSigPos.setEnabled(false);
		pruebaNegativa = 0;
		botonSigNeg.setEnabled(false);
		
		// Crear (pero no mostrar) la Interfaz de Ayuda
		guiAyuda = new InterfazAyuda();
	
	}

	/**
	 * Obtiene la barra de menus: Archivo, Codigo...
	 * @return El objeto "Barra de Menu"
	 */
	private JMenuBar getMenuPrincipal(){
		
		JMenuBar barraMenu = new JMenuBar();
		
		barraMenu.add(getMenuArchivo());
		barraMenu.add(getMenuCodigo());
		barraMenu.add(getMenuConsola());
		barraMenu.add(getMenuTesting());
		barraMenu.add(getMenuAyuda());
		barraMenu.setVisible(true);
		
		return barraMenu;
	}
	
	/**
	 * Obtiene el panel principal (que a su vez, se divide en subpaneles) de la aplicación
	 * @return Panel Principal de la aplicacion (Paneles: Codigo, Inspector y Consola)
	 */
	private JPanel getPanelPrincipal() {
		
		JPanel panelPrincipal = new JPanel();
				
		panelPrincipal.setLayout(new BorderLayout(2,2));
		
		// SPLIT PANELS (Paneles que se pueden redimensionar)
		// -----------------------------------------
			// Paneles Codigo y Tabla de Simbolos en horizontal
		JSplitPane panelComb_CyD = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getPanelCentral(),getPanelDerecho());
		panelComb_CyD.setResizeWeight(0.5);
		panelComb_CyD.setOneTouchExpandable(true);
			// Panel anterior y Consola en vertical
		JSplitPane panelComb_CyI = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelComb_CyD,getPanelInferior());
		panelComb_CyI.setResizeWeight(0.5);
		panelComb_CyI.setOneTouchExpandable(true);
		
		panelPrincipal.add(getBarraHerramientas(),"North");
		panelPrincipal.add(panelComb_CyI,"Center");

		panelPrincipal.validate();

		return panelPrincipal;
	}
	
	/**
	 * Resetea la zona de codigo (previa comprobación para salvar el contenido actual) para comenzar
	 * a escribir un nuevo programa
	 */
	private void iniciarNuevoPrograma(){

		// Si estamos en medio de una ejecución paso a paso, finalizarla
		if (ejecucionSBS)
			accionParar(true);
		
		// Comprobamos cambios en el archivo actual antes de iniciar un nuevo programa
		comprobarCambios("Antes de iniciar un nuevo programa...",
						 "¿Desea guardar los cambios hechos en el programa actual?",
						 "Guardar",
						 "Descartar");
		
		// Ajustes para el nuevo programa
		this.setTitle("Compilador PLG (G05) - Nuevo Programa");
		areaTextoCodigo.setText("@ Nuevo programa");
		rutaProgActual = "";
		codigoModificado = false;
		
	}

	/**
	 * Añade nuevos numeros de linea en la zona de escritura de código si esta crece en número de líneas
	 */
	private void actualizaNumLineas(){
		for (int i = visorNumerosLineas.getLineCount(); i <= areaTextoCodigo.getLineCount(); i++)
			visorNumerosLineas.append( i +"\n");
	}
	
	/**
	 * Añade nuevos numeros de linea en la zona de código objeto si esta crece en número de líneas
	 */
	private void actualizaNumLineasCodObj(){
		for (int i = visorNumerosLineasCodObj.getLineCount(); i <= areaTextoCodObj.getLineCount(); i++)
			visorNumerosLineasCodObj.append( i +"\n");
	}
	
	/**
	 * Vacía la tabla de símbolos de la interfaz gráfica
	 */
	private void vaciarTablaSimbolos(){
		while (dtmSimbolos.getRowCount() > 0)
			dtmSimbolos.removeRow(0);
	}
	
	/**
	 * Vacía el contenido de las pestañas del Inspector
	 */
	private void vaciarInspector(){
		
		areaTextoCodObj.setText("> Codigo Objeto:\n");
		areaTextoTrazaMP.setText("> Traza de ejecución Máquina P:\n");
		vaciarTablaSimbolos();
	}
	
	/** 
	 * Actualiza las tablas y registros de la interfaz según el estado actual de los mismos en la Máquina P
	 * en una Ejecución Paso a Paso
	 */
	private void actualizarInterfazMP(){
		
		// Actualizar Programa
		guiMP.pullInstruccion();					// Pasar a la instruccion siguiente en la tabla prog		
		guiMP.setCProg(interpreteP.getCProg());		// Actualizar el contador de programa
		
		// Actualizar Pila
		guiMP.actualizaPila(interpreteP.getPila());	// Actualizar la tabla pila
		guiMP.setCPila(interpreteP.getCPila());		// Actualizar el registro con la cima de la pila
		
		// Actualizar Memoria 
		//guiMP.actualizaMemoria(interpreteP.getMemoria());
		guiMP.setTablaMem(interpreteP.getTablaMem());
		
		// Actualiza Registros
		if (interpreteP.getRegParada()){			// Si hemos acabado
			
			botonAvanzar.setEnabled(false);			// Desactivar boton de avanzar 1
			botonAvanzarToolBar.setEnabled(false); 	// Desactivar boton de avanzar del tool bar
			opcionAvanzar.setEnabled(false);		// Desactivar boton de avanzar del menu
			
			botonParar.setEnabled(false);			// Desactivar boton de parar 1
			botonPararToolBar.setEnabled(false);	// Desactivar boton de parar del tool bar
			opcionParar.setEnabled(false);			// Desactivar boton de parar del menu
			
			guiMP.setRegParadaChecked();			// Marcamos el checkbox de Parada
			
			guiMP.actualizaCProgParada(0);
		}
		
		// Actualizar el registro Swap1
		if (interpreteP.getRegSwap1())				
			guiMP.setRSwap1Checked();
		else
			guiMP.setRSwap1Unchecked();
		
		// Actualizar el registro Swap2
		if (interpreteP.getRegSwap2())				
			guiMP.setRSwap2Checked();
		else
			guiMP.setRSwap2Unchecked();

	}

		// ACCIONES (Fragmentos de acciones para botones que se repiten)
		// *****************************************************************************************
	
	/**
	 * Acción a realizar cuando se pulsa alguno de los 3 botones de Avanzar de la interfaz
	 */
	private void accionBotonAvanzar(){
		
		if (interpreteP != null)
			if (!interpreteP.getRegParada()){
				try {
					boolean paradaUsuario = interpreteP.ejecutaSoloUna();
					actualizarInterfazMP();
					if (paradaUsuario)
						accionParar(true);		// Accion Parar por el usuario		
				} catch (Exception e1) {
					accionParar(false);			// Accion Parar por error ejecucion
					areaTextoConsola.append("\n"+e1.getMessage());
					}		
			} // if
	}
	
	/**
	 * Acción a realizar cuando se pulsa alguno de los 2 botones de Compilar de la interfaz
	 */
	private void accionBotonCompilar(){
		
		// Limpiar panel tabulado TS
		int i = 1;
		while (i < panelTabuladoTS.getTabCount())
			panelTabuladoTS.remove(i);
		
		// INICIAR COMPILADOR
		if (comprobarCambios("Antes de continuar...",
							 "Para compilar es necesario confirmar los cambios\n¿Guardar ahora y compilar?",
							 "Guardar",
							 "Cancelar")){ // Comprueba si el archivo esta guardado o si hay cambios sin guardar
			
			// Iniciar el analizador sintactico
			ArrayList<String> codigoDevuelto = iniciarAnalisis();
			
			// Si se genero codigo, actualizamos el visor de num de lineas del codigo objeto
			if (codigoDevuelto != null)
				actualizaNumLineasCodObj();
		
			panelTabulado.setSelectedIndex(1);			// Traemos al frente la pestaña de traza
		}
	}
	
	/**
	 * Acción a realizar cuando se pulsa alguno de los 2 botones de Ejecutar de la interfaz
	 */
	private void accionBotonEjecutar(){
		
		// Limpiar panel tabulado TS
		int i = 1;
		while (i < panelTabuladoTS.getTabCount())
			panelTabuladoTS.remove(i);
		
		// INICIAR COMPILADOR Y EJECUTAR
		ArrayList<String> codigoDevuelto = null;

		if (comprobarCambios("Antes de continuar...",
				 "Para compilar y ejecutar es necesario confirmar los cambios\n¿Guardar ahora y compilar?",
				 "Guardar",
				 "Cancelar")){ // Comprueba si el archivo esta guardado o si hay cambios sin guardar
			
			// Iniciamos la compilacion (devuelve el Cod Obj)
			codigoDevuelto = iniciarAnalisis();
				
		}
		
		// Si ya estabamos en medio de una ejecución paso a paso, finalizarla
		if (ejecucionSBS)
			accionParar(true);
		
		// Si se ha devuelto codigo, ejecutamos
		if (codigoDevuelto != null){
			try {
				areaTextoConsola.append("\n\n> Ejecución iniciada:\n");
				actualizaNumLineasCodObj();					// Actualizar el visor de num de lineas del codigo objeto
				panelTabulado.setSelectedIndex(2);			// Traemos al frente la pestaña de traza
				iniciarEjecucion(codigoDevuelto);
			} catch (Exception e1) {
				// Capturamos y mostramos errores de ejecucion
				areaTextoConsola.append("\n\n"+e1.getMessage());
			}
		}	

	}
	
	/**
	 * Acción a realizar cuando se pulsa alguno de los 2 botones de Ejecutar Paso a Paso de la interfaz
	 */
	private void accionBotonEjecutarSBS(){
		
		// Limpiar panel tabulado TS
		int i = 1;
		while (i < panelTabuladoTS.getTabCount())
			panelTabuladoTS.remove(i);
		
		// COMPILAR y EJECUTAR PASO A PASO
		ArrayList<String> codigoDevuelto = null;

		// Comprueba si el archivo esta guardado o si hay cambios sin guardar
		if (comprobarCambios("Antes de continuar...",
				 "Para compilar y ejecutar paso a paso es necesario confirmar los cambios\n¿Guardar ahora y compilar?",
				 "Guardar",
				 "Cancelar")){ 
			codigoDevuelto = iniciarAnalisis();
		}
		
		// Si el analisis (compilacion) ha devuelto codigo, ejecutamos
		if (codigoDevuelto != null){
			try {
				// Habilitar botones de avance
				botonAvanzar.setEnabled(true);
				botonAvanzarToolBar.setEnabled(true);
				opcionAvanzar.setEnabled(true);
				
				// Habilitar botones de parada
				botonParar.setEnabled(true);
				botonPararToolBar.setEnabled(true);
				opcionParar.setEnabled(true);
				
				// Notificamos por consola
				areaTextoConsola.append("\n\n> Ejecución Paso a Paso iniciada:" +
										"\n -> Use botones 'Avanzar' y 'Parar' para controlar el flujo\n");
				
				panelTabulado.setSelectedIndex(3);			// Traemos al frente la pestaña de paso a paso
				
				// Iniciar ejecucion
				iniciarEjecucionSBS(codigoDevuelto);
			} catch (Exception e1) {
				// Capturamos y mostramos errores de ejecución
				areaTextoConsola.append("\n\n" + e1.getMessage());
			}
		}	
	}
	
	/**
	 * Acción a realizar si se produce una parada en la Máquina P o se pulsa algun boton Parar de la interfaz
	 * El parametro determina si la parada es provocada por el usuario o por un error en ejecución
	 * @param paradaUsuario
	 */
	private void accionParar(boolean paradaUsuario){
		
		// NOTA: Este codigo tb se ejecuta cuando se produce un error de ejecucion durante
		//		 una ejecucion paso a paso por que el codigo es el mismo salvo el final.
		//		 Lo distinguimos por el booleano de entrada
		
		// Detener la maquina P
		if (interpreteP != null){
			interpreteP.detenerMaquinaP();		
			interpreteP = null;
			ejecucionSBS = false;
			
			// Bloquear botones de avanzar
			botonAvanzar.setEnabled(false);
			botonAvanzarToolBar.setEnabled(false);
			opcionAvanzar.setEnabled(false);
			
			// Bloquear botones de Parar
			botonParar.setEnabled(false);
			botonPararToolBar.setEnabled(false);
			opcionParar.setEnabled(false);
			
			guiMP.setRegParadaChecked();
			
			// Si es una parada provocada por el usuario
			if (paradaUsuario){
				areaTextoConsola.append("\n> Ejecución paso a paso detenida por el usuario!");
				guiMP.actualizaCProgParada(1);
			}
			else // sino, es error de ejecucucion
				guiMP.actualizaCProgParada(2);
		}
	}

	// *****************************************************************************************
	
	/**
	 * Comprueba antes de compilar si el codigo esta guardado en un archivo. Si ya lo está, comprobará si
	 * existen cambios en el archivo que no hayan sido guardados.
	 * @return Devuelve "true" si consigue guardar el código en un archivo o guardar los cambios. "false" en otro caso
	 */
	private boolean comprobarCambios(String titulo, String mensaje, String etiqBoton1,String etiqBoton2){
		
		boolean exito = false;	// True si conseguimos guardar en un fichero el codigo. False en otro caso
		
		if (codigoModificado){ // Si hay cambios sin guardar
		    
			Object[] opciones = {etiqBoton1,etiqBoton2};
		    int n = JOptionPane.showOptionDialog(null,
		    		mensaje,
		    		titulo,
			        JOptionPane.YES_NO_OPTION,
			        JOptionPane.QUESTION_MESSAGE,
			        null,
			        opciones,
			        opciones[1]);

		    // YES = 0 # NO = 1 # X(Cerrar) = (-1)
		    if (n == 0){
		    	if (rutaProgActual.equals(""))
		    		exito = guardarProgramaEnFichero(true);	
		    	else
		    		exito = guardarProgramaEnFichero(false);
		    	}
			}
		else
			if (rutaProgActual.equals(""))
				exito = false;
			else
				exito = true;
		
		return exito;
	}
	
	/**
	 * Inicia el analizador léxico y sintáctico para procesar el programa salvado en el fichero
	 * @return Si el analisis es correcto, devolverá un ArrayList<String> con el Código Objeto generado. NULL en otro caso
	 */
	private ArrayList<String> iniciarAnalisis(){
		
		File f = new File(rutaProgActual);
		AnalizadorSintactico as = null;
		try {
			as = new AnalizadorSintactico(new FileInputStream(f));
			if (autoLimpieza)
				areaTextoConsola.setText("");		// Vaciar consola antes de compilar
			vaciarInspector();
		} catch (FileNotFoundException e1){
			JOptionPane.showMessageDialog(null,"ERROR!\nNo se ha podido compilar el código actual");
		} 
			
		return as.run(areaTextoConsola,dtmSimbolos,panelTabuladoTS,areaTextoCodObj,rutaCodObjActual);

	}
	
	/**
	 * Inicia la Máquina P y ejecuta el código objeto proporcionado por parámetro
	 * @param codObj devuelto por los analizadores léxico/sintáctico
	 * @throws Exception en caso de error de ejecución
	 */
	private void iniciarEjecucion(ArrayList<String> codObj) throws Exception {
		
		// Iniciar nueva ejecución estandar
		interpreteP = new MaquinaP(codObj,areaTextoConsola,areaTextoTrazaMP,null);
		interpreteP.ejecuta();
		interpreteP = null;
	}
	
	/**
	 * Inicia la Máquina P y la prepara para una ejecución paso a paso con el código objeto 
	 * proporcionado por parámetro
	 * @param codObj devuelto por los analizadores léxico/sintáctico
	 * @throws Exception en caso de error de ejecución
	 */
	private void iniciarEjecucionSBS(ArrayList<String> codObj) throws Exception{
		
		// Prepara la maquina para una ejecucion paso a paso
		guiMP.preparaInterfazPara(codObj);
		interpreteP = new MaquinaP(codObj,areaTextoConsola,areaTextoTrazaMP,guiMP.getTablaMem());
		ejecucionSBS = true;
	}
	
	/**
	 * Carga en el área de escritura de código el programa de ejemplo que viene como muestra
	 * en el enunciado de la práctica. Será necesario salvarlo en un fichero antes de compilarlo
	 */
	private void cargaProgramaEjemplo(){
		
		if (ejecucionSBS)
			accionParar(true);
		
		areaTextoCodigo.setText("");	// Vaciar area de codigo

		try {
		    InputStream is = getClass().getResourceAsStream("/resources/strings/Program.txt");
		    BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String linea;
			
		    while ((linea = br.readLine()) != null) 
		    	areaTextoCodigo.append(linea+"\n");        
		      
        
            br.close();
            areaTextoCodigo.requestFocus();            
	     	}
	    catch(Exception e2) { 
	    	JOptionPane.showMessageDialog(null, "ERROR!\nNo se puede cargar archivo de prueba");
           }	
		
		rutaProgActual = "";
		rutaCodObjActual = "";		// Guardar ruta del codigo objeto generado
		codigoModificado = true;
		this.setTitle("Compilador PLG (G05) - *Programa de Ejemplo");		// Cambiar la barra de titulo
		actualizaNumLineas();
	}
	
	/**
	 * Carga en el área de escritura de código una prueba cargada desde fichero
	 * @param path Ruta del fichero que contiene la prueba
	 */
	private void cargarPrueba(String path){	
		if (ejecucionSBS)
			accionParar(true);
		
		areaTextoCodigo.setText("");	// Vaciar area de codigo

	    try {
	    	FileReader reader = new FileReader(path);
	    	BufferedReader br = new BufferedReader(reader);
	    	areaTextoCodigo.read( br, null );
            br.close();
            areaTextoCodigo.requestFocus();            
	     	}
	    catch(Exception e2) { 
	    	JOptionPane.showMessageDialog(this, "No se puede cargar archivo de prueba:\n"+path+"\nEl archivo no es válido","Error!", JOptionPane.ERROR_MESSAGE);
           }	
		
		rutaProgActual = path;
		rutaCodObjActual = path.replace(".lpp", ".co");		// Guardar ruta del codigo objeto generado
		codigoModificado = false;
		this.setTitle("Compilador PLG (G05) - " + path);		// Cambiar la barra de titulo
		actualizaNumLineas();
		
	}
	
	/**
	 * Lanza un diálogo para elegir un fichero ".lpp" o ".txt" para cargarlo en la zona de área
	 * de código
	 */
	private void cargarProgramaDesdeFichero(){

		// Si estamos en medio de una ejecución paso a paso, finalizarla
		if (ejecucionSBS)
			accionParar(true);
		
		// Comprobamos los cambios en el fichero actual antes de cargar uno nuevo
		comprobarCambios("Antes de cargar otro programa...",
						 "¿Desea guardar los cambios hechos en el programa actual?",
						 "Guardar",
						 "Descartar");

		JFileChooser chooser = new JFileChooser(".");			// Creamos objeto OpenDialog
		
		// Creamos el filtro para el OpenDialog
		FileFilter filter = new FileNameExtensionFilter("Lenguaje de Programación Propio (*.lpp)","lpp","LPP");
		FileFilter filter2 = new FileNameExtensionFilter("Código en texto plano (*.txt)","txt","TXT");
		chooser.setFileFilter(filter);								// Setear ".lpp" como la extension principal
		chooser.addChoosableFileFilter(filter2);					// Añadir ".txt" como extension tb usable
		chooser.setAcceptAllFileFilterUsed(false);					// No incluir "Todos los archivos" en el filtro de extensiones 	
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);		// Solo aceptar ficheros

		
		if (chooser.showOpenDialog(InterfazCompilador.this) == JFileChooser.APPROVE_OPTION){		// Mostrar OpenDialog y si pulsó Aceptar
			rutaProgActual = chooser.getSelectedFile().getAbsolutePath(); // Guardar ruta del programa		
			if (rutaProgActual.matches(".*.lpp"))
				rutaCodObjActual = rutaProgActual.replace(".lpp", ".co");		// Guardar ruta del codigo objeto generado
			if (rutaProgActual.matches(".*.LPP"))
				rutaCodObjActual = rutaProgActual.replace(".LPP", ".co");		// Guardar ruta del codigo objeto generado
			if (rutaProgActual.matches(".*.txt"))
				rutaCodObjActual = rutaProgActual.replace(".txt", ".co");		// Va a aceptar codigo en txt tb
			if (rutaProgActual.matches(".*.TXT"))
				rutaCodObjActual = rutaProgActual.replace(".TXT", ".co");		// Guardar ruta del codigo objeto generado si es .TXT'

			this.setTitle("Compilador PLG (G05) - " + rutaProgActual );		// Cambiar la barra de titulo
		
			// Pasar fichero al JTextArea
			// --------------------------
		    try {
		    	FileReader reader = new FileReader(rutaProgActual);
		    	BufferedReader br = new BufferedReader(reader);
		    	areaTextoCodigo.read( br, null );
	            br.close();
	            areaTextoCodigo.requestFocus();            
		     	}
		    catch(Exception e2) { 
		    	JOptionPane.showMessageDialog(null, "ERROR!\nNo se puede cargar el fichero: "+rutaProgActual);
	           }
		    
		    codigoModificado = false;
			actualizaNumLineas();
		}
		
		
		
	}
	
	/**
	 * Guarda el contenido del área de escritura de código en fichero (en uno ya guardado o en uno
	 * elegido mediante un diálogo de Guardar archivo... 
	 * @param mostrarDialog Si es "true", mostrará un diálogo para elegir o crear un nuevo fichero 
	 * donde almacenar el contenido. Si "false", es que ya hay un archivo cargado y sobreescribir
	 * (actualizar) los cambios
	 * @return "true" si guardó con éxito. "false" en otro caso
	 */
	private boolean guardarProgramaEnFichero(boolean mostrarDialog){
		
		boolean exito = false;	// True si conseguimos guardar, false sino
		
		// Si estamos en medio de una ejecución paso a paso, finalizarla
		if (ejecucionSBS)
			accionParar(true);
		
		if (mostrarDialog){		// Si es "Guardar como..."
		
			// Creamos objeto SaveDialog que se abra en el directorio actual
			
			JFileChooser chooser = new JFileChooser("."){
				// Sobrecargamos el metodo para preguntar en caso de sobrescritura de archivo    
				@Override
			    public void approveSelection(){
			        File f = getSelectedFile();
			        if(f.exists() && getDialogType() == SAVE_DIALOG){
			            int result = JOptionPane.showConfirmDialog(this,"El archivo seleccionado ya existe. ¿Deseas sobrescribirlo?","Archivo existente",JOptionPane.YES_NO_CANCEL_OPTION);
			            switch(result){
			                case JOptionPane.YES_OPTION:
			                    super.approveSelection();
			                    return;
			                case JOptionPane.NO_OPTION:
			                    return;
			                case JOptionPane.CLOSED_OPTION:
			                    return;
			                case JOptionPane.CANCEL_OPTION:
			                    cancelSelection();
			                    return;
			            }
			        }
			    
			    super.approveSelection();
			    }
			};
			
			FileFilter filter = new FileNameExtensionFilter("Lenguaje de Programación Propio (*.lpp)","lpp","LPP");
			chooser.setFileFilter(filter);								// Setear ".lpp" como la extension principal
			chooser.setAcceptAllFileFilterUsed(false);					// No incluir "Todos los archivos" en el filtro de extensiones 	
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);		// Solo aceptar ficheros

			
			if (chooser.showSaveDialog(InterfazCompilador.this) == JFileChooser.APPROVE_OPTION){	// Mostrar OpenDialog y si hay exito
				
				if (chooser.getSelectedFile().getAbsolutePath().contains(".lpp") ||
					chooser.getSelectedFile().getAbsolutePath().contains(".LPP"))			// Si ya contiene la extension LPP, dejar como esta
					rutaProgActual = chooser.getSelectedFile().getAbsolutePath();
				else
					rutaProgActual = chooser.getSelectedFile().getAbsolutePath()+".lpp";	// Sino, añadimos la extension
				
				if (rutaProgActual.matches(".*.lpp"))
					rutaCodObjActual = rutaProgActual.replace(".lpp", ".co");		// Guardar ruta del codigo objeto generado si es .lpp
				if (rutaProgActual.matches(".*.LPP"))
					rutaCodObjActual = rutaProgActual.replace(".LPP", ".co");		// Guardar ruta del codigo objeto generado si es .LPP
				if (rutaProgActual.matches(".*.txt"))
					rutaCodObjActual = rutaProgActual.replace(".txt", ".co");		// Guardar ruta del codigo objeto generado si es .txt
				if (rutaProgActual.matches(".*.TXT"))
					rutaCodObjActual = rutaProgActual.replace(".TXT", ".co");		// Guardar ruta del codigo objeto generado si es .TXT'
				
				// Pasar JTextArea al Fichero
				// --------------------------
				try {
					FileWriter writer = new FileWriter(rutaProgActual);
					BufferedWriter bw = new BufferedWriter( writer );
					areaTextoCodigo.write( bw );
					bw.close();         
					}
				catch(Exception e2) { 
					JOptionPane.showMessageDialog(null, "ERROR!\nNo se puede guardar en el fichero: "+rutaProgActual);
					}
				
				exito = true;
				
				} // showDialog
			}
		else {	// Sino, es Guardar (Ya hay fichero)
			
			// Pasar JTextArea al Fichero
			// --------------------------
			try {
				FileWriter writer = new FileWriter(rutaProgActual);
				BufferedWriter bw = new BufferedWriter( writer );
				areaTextoCodigo.write( bw );
				bw.close();         
				}
			catch(Exception e2) { 
				JOptionPane.showMessageDialog(null, "ERROR!\nNo se puede guardar en el fichero: "+rutaProgActual);
				}
			
			exito = true;
			
		} // else
		
		codigoModificado = false;
		
		this.setTitle("Compilador PLG (G05) - "+ rutaProgActual);
		
		return exito;
		
		}
	
	
	// BARRA DE HERRAMIENTAS (Tool Bar)
	// *****************************************************************************************
	
	/**
	 * Devuelve la barra de herramientas que se encuentra entre la barra de menú y el panel principal
	 * Esta barra se puede anclar a la zona izquierda o derecha de la app o extraer en una venta aparte
	 * arrastrándola desde su posición inicial
	 * @return Barra de Herramientas Rápidas
	 */
	private JToolBar getBarraHerramientas(){
		
		JToolBar toolBar = new JToolBar("Herramientas Rápidas");
		
	    // 1er boton: Nuevo programa
		// ------------
		Icon iconoNuevo = new ImageIcon(this.getClass().getResource("/resources/icons/toolbar/iconNewProg.png"));
	    JButton botonNuevo = new JButton(iconoNuevo);
		botonNuevo.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								iniciarNuevoPrograma();
							}
					}
					);
	    botonNuevo.setToolTipText("Crear nuevo programa");
	    toolBar.add(botonNuevo);

	    // 2º boton: Cargar programa
	    // -------------
		Icon iconoAbrir = new ImageIcon(this.getClass().getResource("/resources/icons/toolbar/iconOpen.png"));
	    JButton botonAbrir = new JButton(iconoAbrir);
		botonAbrir.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								cargarProgramaDesdeFichero();
							}
					}
					);
	    botonAbrir.setToolTipText("Cargar un programa desde fichero");
	    toolBar.add(botonAbrir);

		
	    // 3er boton: Guardar programa
	    // -------------
		Icon iconoGuardar= new ImageIcon(this.getClass().getResource("/resources/icons/toolbar/iconSave.png"));
	    JButton botonGuardar = new JButton(iconoGuardar);
		botonGuardar.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								if (rutaProgActual != "") // Si ya estamos trabajando sobre un archivo
									guardarProgramaEnFichero(false);
								else
									guardarProgramaEnFichero(true);
							}
					}
					);
	    botonGuardar.setToolTipText("Guardar programa en un fichero");
	    toolBar.add(botonGuardar);

	    toolBar.addSeparator();
	    
	    // 4º boton: Compilar programa
	    // -------------
		Icon iconoCompilar = new ImageIcon(this.getClass().getResource("/resources/icons/toolbar/iconCompile.png"));
	    JButton botonCompilar = new JButton(iconoCompilar);
		botonCompilar.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){	
								// INICIAR COMPILADOR
								accionBotonCompilar();
							}
					}
					);
	    botonCompilar.setToolTipText("Compila el código actual");
	    toolBar.add(botonCompilar);
		
	    // 5º boton: Compilar y Ejecutar
	    // -------------
		Icon iconoEjecutar = new ImageIcon(this.getClass().getResource("/resources/icons/toolbar/iconRun.png"));
	    JButton botonEjecutar = new JButton(iconoEjecutar);
		botonEjecutar.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								// INICIAR COMPILADOR Y EJECUTAR
								accionBotonEjecutar();
							}
					}
					);
	    botonEjecutar.setToolTipText("Compila y ejecuta el código actual");
	    toolBar.add(botonEjecutar);
	    
	    toolBar.addSeparator();
	    
	    // 6º boton: Compilar y Ejecutar Paso a Paso
	    // -------------
		Icon iconoEjecutarSBS = new ImageIcon(this.getClass().getResource("/resources/icons/toolbar/iconRunSBS.png"));
	    JButton botonEjecutarSBS = new JButton(iconoEjecutarSBS);
		botonEjecutarSBS.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								// COMPILAR y EJECUTAR PASO A PASO
								accionBotonEjecutarSBS();
								
							}
					}
					);
	    botonEjecutarSBS.setToolTipText("Compila y ejecuta paso a paso el código actual");
	    toolBar.add(botonEjecutarSBS);
	    
	    // 7º boton: Avanzar
	    // -------------
		Icon iconoAvanzar= new ImageIcon(this.getClass().getResource("/resources/icons/toolbar/iconNext.png"));
	    botonAvanzarToolBar = new JButton(iconoAvanzar);
	    botonAvanzarToolBar.setEnabled(false);
	    

		botonAvanzarToolBar.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								accionBotonAvanzar();
							}
					}
					);
	    botonAvanzarToolBar.setToolTipText("Avanza a la sig instrucción");
	    toolBar.add(botonAvanzarToolBar);
	    
	    // 8º boton: Parar
	    // ------------
	    Icon iconoParar = new ImageIcon(this.getClass().getResource("/resources/icons/toolbar/iconStop.png"));
	    botonPararToolBar = new JButton(iconoParar);
	    botonPararToolBar.setEnabled(false);
	    botonPararToolBar.addActionListener(new ActionListener(){
	    					public void actionPerformed(ActionEvent e){
	    						accionParar(true);
	    					}
	    			}
	    			);
	    botonPararToolBar.setToolTipText("Detiene la ejecución paso a paso");
	    toolBar.add(botonPararToolBar);
	    
	    
	    toolBar.addSeparator();
	    
	    // 9º boton: Limpiar consola
	    // -------------
		Icon iconoLimpiar = new ImageIcon(this.getClass().getResource("/resources/icons/toolbar/iconCleanConsole.png"));
	    JButton botonLimpiar = new JButton(iconoLimpiar);
		botonLimpiar.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								areaTextoConsola.setText("> ");
							}
					}
					);
	    botonLimpiar.setToolTipText("Limpiar la consola");
	    toolBar.add(botonLimpiar);
	    
	    toolBar.addSeparator();
	    
	    // 10º boton: Siguiente prueba Pos
	    // -------------
		Icon iconoSigPos = new ImageIcon(this.getClass().getResource("/resources/icons/toolbar/iconNextOK.png"));
	    botonSigPos = new JButton(iconoSigPos);
		botonSigPos.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								
								if (pruebaPositiva < maxPos){
									
									pruebaPositiva++;
									
									String num;
									if (pruebaPositiva < 10)
										num = "0" + pruebaPositiva;
									else
										num = String.valueOf(pruebaPositiva);
									
									File prueba = new File(pathPositivo  + num + ".lpp");
									if (prueba.isFile())
										cargarPrueba(prueba.getAbsolutePath());
									else 
								    	JOptionPane.showMessageDialog(InterfazCompilador.this, 
								    								  "No se puede cargar archivo de prueba:\n" +
								    								  prueba.getAbsolutePath() + "\nEl archivo no existe",
								    								  "Error!",
								    								  JOptionPane.ERROR_MESSAGE);
								}
								else
									botonSigPos.setEnabled(false);
							}
					}
					);
	    botonSigPos.setToolTipText("Carga la siguiente prueba positiva");
	    toolBar.add(botonSigPos);
	    
	    // 11º boton: Siguiente prueba Negativa
	    // -------------
		Icon iconoSigNeg = new ImageIcon(this.getClass().getResource("/resources/icons/toolbar/iconNextFAIL.png"));
	    botonSigNeg = new JButton(iconoSigNeg);
		botonSigNeg.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								
								if (pruebaNegativa < maxNeg){
									
									pruebaNegativa++;
									
									String num;
									if (pruebaNegativa < 10)
										num = "0" + pruebaNegativa;
									else
										num = String.valueOf(pruebaNegativa);
									
									File prueba = new File(pathNegativo  + num + ".lpp");
									if (prueba.isFile())
										cargarPrueba(prueba.getAbsolutePath());
									else 
								    	JOptionPane.showMessageDialog(InterfazCompilador.this, 
								    								  "No se puede cargar archivo de prueba:\n" +
								    								  prueba.getAbsolutePath() + "\nEl archivo no existe",
								    								  "Error!",
								    								  JOptionPane.ERROR_MESSAGE);
								}	
								else
									botonSigNeg.setEnabled(false);
							}
					}
					);
	    botonSigNeg.setToolTipText("Carga la siguiente prueba negativa");
	    toolBar.add(botonSigNeg);
	    
	    return toolBar;
		
	}
	
	// PANEL CENTRAL (Panel escritura de código)
	// *****************************************************************************************
	
	/**
	 * Devuelve el panel central, el cual es una combinación de 2 paneles redimensionables:
	 * (Código con Inspector) y estos 2 con la consola 
	 * @return Panel Central
	 */
	private JScrollPane getPanelCentral(){
		
		TitledBorder tituloSeccion;
		tituloSeccion = BorderFactory.createTitledBorder("Código");
		tituloSeccion.setTitleJustification(TitledBorder.LEFT);
		
		// Preparar el area para escritura de codigo:
		// -----------------------------------------------------------------------------
		areaTextoCodigo = new JTextArea("@ Escribir aquí el código del programa",1,25);
		areaTextoCodigo.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
		areaTextoCodigo.addKeyListener(new KeyListener(){
			
            @Override 
            public void keyPressed(KeyEvent e){
            	codigoModificado = true;		// Si se pulsa una tecla, el codigo se ha modificado
            	if (rutaProgActual.equals(""))
            		setTitle("Compilador PLG (G05) - *Nuevo Programa");
            	else
            		setTitle("Compilador PLG (G05) - *" + rutaProgActual);
        		actualizaNumLineas();
            }
            
			@Override
			public void keyReleased(KeyEvent e) {}

			@Override
			public void keyTyped(KeyEvent e) {}

		});
		
		// Preparar el area visor de numero de linea
		// -----------------------------------------------------------------------------
		visorNumerosLineas = new JTextArea("");
		visorNumerosLineas.setColumns(4);
		visorNumerosLineas.setEditable(false);
		visorNumerosLineas.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
		visorNumerosLineas.setBackground(new Color(237,237,237));
		visorNumerosLineas.setMaximumSize(new Dimension(0,10));
		actualizaNumLineas();
		
		// Preparar el panel conjunto
		// -----------------------------------------------------------------------------
		JPanel panelTotal = new JPanel();
		panelTotal.setLayout(new BorderLayout());
		
		panelTotal.add(visorNumerosLineas,"West");
		panelTotal.add(areaTextoCodigo,"Center");
		
		JScrollPane scrollPane = new JScrollPane(panelTotal);
		scrollPane.setBorder(tituloSeccion);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setMinimumSize(new Dimension(350,300));	// Ancho: 350 - Alto: 300
		
		return scrollPane;
	}
	
	// PANEL INFERIOR (Vista Consola)
	// *****************************************************************************************
		
	/**
	 * Devuelve el panel con la Consola
	 * @return Panel consola
	 */
	private JScrollPane getPanelInferior(){
		
		TitledBorder tituloSeccion;
		tituloSeccion = BorderFactory.createTitledBorder("Consola");
		tituloSeccion.setTitleJustification(TitledBorder.LEFT);

		areaTextoConsola = new JTextArea("> Compilador iniciado! Escribe o carga un programa para comenzar...",8,1);
		areaTextoConsola.setEditable(false);
		
		JScrollPane scrollPane = new JScrollPane(areaTextoConsola);
		scrollPane.setBorder(tituloSeccion);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setMinimumSize(new Dimension(1,75));	// Ancho: X - Alto: 75
		return scrollPane;
		
	}
	
	
	// PANEL Derecho (Vista Tabla de Simbolos)
	// *****************************************************************************************
	
	/**
	 * Devuelve el panel tabulado con las 4 pestañas del Inspector
	 * @return Panel Tabulado
	 */
	private JTabbedPane getPanelDerecho(){
		
		// Borde del panel izquierdo
		TitledBorder tituloSeccion;
		tituloSeccion = BorderFactory.createTitledBorder("Inspector");
		tituloSeccion.setTitleJustification(TitledBorder.LEFT);
		
		panelTabulado = new JTabbedPane();
		panelTabulado.setBorder(tituloSeccion);					
		panelTabulado.setMinimumSize(new Dimension(375,300)); // Ancho: 375 - Alto: 300
		
		panelTabulado.addTab("Tablas de Símbolos", new ImageIcon(this.getClass().getResource("/resources/icons/tabs/iconTabTS.png")), getPanelTS(), "Muestra la tabla de símbolos para el programa actual"); 
		panelTabulado.addTab("Código Objeto", new ImageIcon(this.getClass().getResource("/resources/icons/tabs/iconTabCO.png")),getPanelCodObj(), "Muestra el código objeto generado");
		panelTabulado.addTab("Máquina P (Traza)", new ImageIcon(this.getClass().getResource("/resources/icons/tabs/iconTabTrace.png")),getPanelTrazaMaquinaP(), "Muestra la traza del programa actual que se este ejecutando");

		guiMP = new InterfazMaquinaP(botonAvanzar,botonParar);
		panelTabulado.addTab("Máquina P (Paso a Paso)", new ImageIcon(this.getClass().getResource("/resources/icons/tabs/iconTabMPGraphic.png")), guiMP, "Muestra el estado de la Máquina P en una ejecución paso por paso");
		
		return panelTabulado;

	}

	/**
	 * Devuelve el panel que contiene la pestaña Tabla de símbolos de la interfaz gráfica
	 * @return Panel
	 */
	private JTabbedPane getPanelTS(){
		
		// Tabla de simbolos
		dtmSimbolos = new DefaultTableModel(0,5);
		dtmSimbolos.setColumnIdentifiers(new String[]{"Id.","Nivel","Clase","@","ExprTipo","Ini"});
		
		JTable tSimbolos = new JTable(dtmSimbolos);
		tSimbolos.setEnabled(false);
		
		// Habilitar ordenar por columnas
		TableRowSorter<TableModel> ordenaTabla = new TableRowSorter<TableModel>(dtmSimbolos);
		tSimbolos.setRowSorter(ordenaTabla);
		
		// Setear tamaños de las columnas de la tabla
		tSimbolos.getColumnModel().getColumn(0).setMaxWidth(150);
		tSimbolos.getColumnModel().getColumn(1).setMaxWidth(30);
		tSimbolos.getColumnModel().getColumn(2).setMaxWidth(40);
		tSimbolos.getColumnModel().getColumn(3).setMaxWidth(30);
		tSimbolos.getColumnModel().getColumn(4).setCellRenderer(new TableCellLongTextRenderer());
		tSimbolos.getColumnModel().getColumn(5).setMaxWidth(30);
		
		JScrollPane scrollPane = new JScrollPane(tSimbolos);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//scrollPane.setMinimumSize(new Dimension(100,300)); // Ancho: 100 - Alto: 300
		
		panelTabuladoTS = new JTabbedPane();
		panelTabuladoTS.addTab("Global", scrollPane);

		return panelTabuladoTS;
		
	}
	
	/**
	 * Devuelve el panel que contiene la pestaña Código Objeto de la interfaz gráfica
	 * @return Panel Código Objeto
	 */
	private JScrollPane getPanelCodObj(){
		
		// Preparar el area para escritura de codigo:
		// -----------------------------------------------------------------------------
		areaTextoCodObj = new JTextArea("> Codigo Objeto:\n",8,1);
		areaTextoCodObj.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
		areaTextoCodObj.setEditable(false);
					
		// Preparar el area visor de numero de linea
		// -----------------------------------------------------------------------------
		visorNumerosLineasCodObj = new JTextArea("");
		visorNumerosLineasCodObj.setColumns(4);
		visorNumerosLineasCodObj.setEditable(false);

		visorNumerosLineasCodObj.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
		visorNumerosLineasCodObj.setBackground(new Color(237,237,237));
		visorNumerosLineasCodObj.setMaximumSize(new Dimension(0,10));
		actualizaNumLineasCodObj();
		
		// Preparar el panel conjunto
		// -----------------------------------------------------------------------------
		JPanel panelTotal = new JPanel();
		panelTotal.setLayout(new BorderLayout());
		
		panelTotal.add(visorNumerosLineasCodObj,"West");
		panelTotal.add(areaTextoCodObj,"Center");
		
		JScrollPane scrollPane = new JScrollPane(panelTotal);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		return scrollPane;
	}
	
	/**
	 * Devuelve el panel que contiene la pestaña Máquina P (Traza) de la interfaz gráfica
	 * @return Panel Máquina P (Traza)
	 */
	private JScrollPane getPanelTrazaMaquinaP(){
		
		areaTextoTrazaMP = new JTextArea("> Traza de ejecución Maquina P:\n",8,1);
		areaTextoTrazaMP.setEditable(false);
		
		JScrollPane scrollPane = new JScrollPane(areaTextoTrazaMP);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		return scrollPane;
	}
	
	// BARRAS DE MENUS (Archivo, Ejecutar.... etc)
	// *****************************************************************************************
		
	/**
	 * Devuelve el menú archivo de la barra de menú
	 * @return Menú Archivo
	 */
	private JMenu getMenuArchivo(){
		
		JMenu archivoMenu = new JMenu("Archivo");
		
		// OPCION: Nuevo programa
		// ----------------------
		JMenuItem opcionNuevo = new JMenuItem("Nuevo", new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconNewProgMenu.png")));
		opcionNuevo.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								iniciarNuevoPrograma();
							}
					}
					);
		
		archivoMenu.add(opcionNuevo);
		
		// OPCION: Cargar programa
		// ----------------------
		JMenuItem opcionEjecutar = new JMenuItem("Cargar programa...",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconOpenMenu.png")));
		opcionEjecutar.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								cargarProgramaDesdeFichero();
							}
					}
					);
		
		archivoMenu.add(opcionEjecutar);
		
		// OPCION: Guardar programa
		// ----------------------
		JMenuItem opcionGuardar = new JMenuItem("Guardar",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconSaveMenu.png")));
		opcionGuardar.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								if (rutaProgActual != "") // Si ya estamos trabajando sobre un archivo
									guardarProgramaEnFichero(false);
								else
									guardarProgramaEnFichero(true);
							}
					}
					);
		
		archivoMenu.add(opcionGuardar);
		
		// OPCION: Guardar como...
		// ----------------------
		JMenuItem opcionGuardarComo = new JMenuItem("Guardar como...",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconSaveMenu.png")));
		opcionGuardarComo.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								guardarProgramaEnFichero(true);
							}
					}
					);
		
		archivoMenu.add(opcionGuardarComo);
		archivoMenu.addSeparator();
		
		// OPCION: Cargar programa de ejemplo (Para pruebas del compilador)
		// ----------------------
		JMenuItem opcionProgEjemplo = new JMenuItem("Cargar programa de Ejemplo",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconExampleMenu.png")));
		opcionProgEjemplo.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
							    cargaProgramaEjemplo();
							}
					}
					);
		
		opcionProgEjemplo.setToolTipText("Carga el programa de ejemplo que se incluye en el enunciado de la memoria");
		archivoMenu.add(opcionProgEjemplo);
		
		archivoMenu.addSeparator();
		
		// OPCION: Salir.
		// ----------------------
		JMenuItem opcionSalir = new JMenuItem("Salir",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconExit.png")));
		opcionSalir.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								System.exit(0);
							}
					}
					);
		
		archivoMenu.add(opcionSalir);
		archivoMenu.addSeparator();
		 
		return archivoMenu;
	}
	
	/**
	 * Devuelve el menú Código de la barra de menú
	 * @return Menú Código
	 */
	private JMenu getMenuCodigo(){
		
		JMenu ejecutarMenu = new JMenu("Código");
		
		// OPCION: Compilar programa
		// ----------------------
		JMenuItem opcionCompilar = new JMenuItem("Compilar",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconCompileMenu.png")));
		opcionCompilar.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								// COMPILAR PROGRAMA
								accionBotonCompilar();
							}
					}
					);
		
		opcionCompilar.setToolTipText("Compila el código actual");
		ejecutarMenu.add(opcionCompilar);
		
		// OPCION: Ejecutar
		// ----------------------
		JMenuItem opcionEjecutar = new JMenuItem("Ejecutar",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconRunMenu.png")));
		opcionEjecutar.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								// COMPILAR y EJECUTAR PROGRAMA
								accionBotonEjecutar();
							}
					}
					);
		
		opcionEjecutar.setToolTipText("Compila y ejecuta el código actual");
		ejecutarMenu.add(opcionEjecutar);
		ejecutarMenu.addSeparator();
		
		// OPCION: Ejecutar Paso a paso
		// ----------------------
		JMenuItem opcionEjecutarSBS = new JMenuItem("Ejecutar (Paso a Paso)",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconRunSBSMenu.png")));
		opcionEjecutarSBS.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								// COMPILAR y EJECUTAR PASO A PASO
								accionBotonEjecutarSBS();
							}
					}
					);
		opcionEjecutarSBS.setToolTipText("Compila y ejecuta paso a paso el código actual");
		ejecutarMenu.add(opcionEjecutarSBS);
		
		// OPCION: Avanzar
		// ----------------------
		opcionAvanzar = new JMenuItem("Avanzar Instrucción",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconNextMenu.png")));
		opcionAvanzar.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								accionBotonAvanzar();
							}
					}
					);
		opcionAvanzar.setEnabled(false);
		opcionAvanzar.setToolTipText("En una ejecución paso a paso: Avanzar una instrucción");
		ejecutarMenu.add(opcionAvanzar);
		
		// OPCION: Stop
		// --------------------
		opcionParar = new JMenuItem("Detener Ejecución", new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconStopMenu.png")));
		opcionParar.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e) {
									accionParar(true);
				
							}
			
					}
					);
		opcionParar.setEnabled(false);
		opcionParar.setToolTipText("En una ejecución paso a paso: Detener la ejecución");
		ejecutarMenu.add(opcionParar);
		
		return ejecutarMenu;
	}

	/**
	 * Devuelve el menú Consola de la barra de menú
	 * @return Menú Consola
	 */
	private JMenu getMenuConsola(){
		
		JMenu consolaMenu = new JMenu("Consola");
		
		// OPCION: Limpiar Consola
		// ----------------------
		JMenuItem opcionLimpiarConsola = new JMenuItem("Limpiar consola",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconCleanConsoleMenu.png")));
		opcionLimpiarConsola.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								areaTextoConsola.setText("> ");
							}
					}
					);
		
		consolaMenu.add(opcionLimpiarConsola);
		
		// OPCION: Limpieza Automatica Consola
		// ----------------------
		opcionLimpiarAutoConsola = new JCheckBoxMenuItem("Autolimpieza de consola",new ImageIcon(this.getClass().getResource("/resources/icons/menu/noIcon.png")),true);

		opcionLimpiarAutoConsola.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								if (autoLimpieza)
									autoLimpieza = false;
								else
									autoLimpieza = true;
								
								opcionLimpiarAutoConsola.setState(autoLimpieza);
									
							}
					}
					);
		
		opcionLimpiarAutoConsola.setToolTipText("Si está activa, la consola se limpiará cada vez que se compile o ejecute un programa");
		consolaMenu.add(opcionLimpiarAutoConsola);
		
		
		return consolaMenu;
	}

	
	private JMenu getMenuTesting(){
		
		JMenu testingMenu = new JMenu("Testing");
		
		// OPCION: Cargar tests (+) (Para pruebas del compilador)
		// ----------------------		
		JMenuItem opcionTestPositivo = new JMenuItem("Cargar prueba positiva...",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconNextOK.png")));	

		opcionTestPositivo.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String numPrueba = (String)JOptionPane.showInputDialog(
	                    InterfazCompilador.this,
	                    "PRUEBAS POSITIVAS:\nConjunto de pruebas que compilan y \nse ejecutan de manera satisfactoria\n\nSelecciona la prueba a cargar:",
	                    "Cargar test...",
	                    JOptionPane.QUESTION_MESSAGE,
	                    new ImageIcon(this.getClass().getResource("/resources/icons/toolbar/iconNextOK.png")),
	                    possibilitiesPos,
	                    possibilitiesPos[0]);

				File prueba = new File(pathPositivo+numPrueba+".lpp");
				
				if (numPrueba != null && numPrueba.length() > 0) {
					if (prueba.isFile()){
						cargarPrueba(prueba.getAbsolutePath());
						pruebaPositiva = Integer.parseInt(numPrueba);
						botonSigPos.setEnabled(true);
						}
					else 
				    	JOptionPane.showMessageDialog(InterfazCompilador.this, 
				    								  "No se puede cargar archivo de prueba:\n" +
				    								  prueba.getAbsolutePath() + "\nEl archivo no existe",
				    								  "Error!",
				    								  JOptionPane.ERROR_MESSAGE);	
				}
		
			}
			
		});
		opcionTestPositivo.setToolTipText("Muestra un cuadro de diálogo para seleccionar que fichero de prueba positiva se cargará en el compilador");
		
		testingMenu.add(opcionTestPositivo);
		
		// OPCION: Cargar tests (-) (Para pruebas del compilador)
		// ----------------------		
		JMenuItem opcionTestNegativo = new JMenuItem("Cargar prueba negativa...",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconNextFAIL.png")));	
		
		opcionTestNegativo.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String numPrueba = (String)JOptionPane.showInputDialog(
	                    InterfazCompilador.this,
	                    "PRUEBAS NEGATIVAS:\nConjunto de pruebas que no compilan y \ndeben para el analizadory mostrar algún \nmensaje de error\n\nSelecciona la prueba a cargar:",
	                    "Cargar test...",
	                    JOptionPane.QUESTION_MESSAGE,
	                    new ImageIcon(this.getClass().getResource("/resources/icons/toolbar/iconNextFAIL.png")),
	                    possibilitiesNeg,
	                    possibilitiesNeg[0]);

				File prueba = new File(pathNegativo+numPrueba+".lpp");
				
				if (numPrueba != null && numPrueba.length() > 0) {
					if (prueba.isFile()){
						cargarPrueba(prueba.getAbsolutePath());
						pruebaNegativa = Integer.parseInt(numPrueba);
						botonSigNeg.setEnabled(true);
						}
					else 
				    	JOptionPane.showMessageDialog(InterfazCompilador.this, 
				    								  "No se puede cargar archivo de prueba:\n" +
				    								  prueba.getAbsolutePath() + "\nEl archivo no existe",
				    								  "Error!",
				    								  JOptionPane.ERROR_MESSAGE);	
				}
		
			}
			
		});
		opcionTestNegativo.setToolTipText("Muestra un cuadro de diálogo para seleccionar que fichero de prueba negativa se cargará en el compilador");
		
		testingMenu.add(opcionTestNegativo);
		
		
		return testingMenu;
	}
	
	/**
	 * Devuelve el menú Ayuda de la barra de menú
	 * @return Menú Ayuda
	 */
	private JMenu getMenuAyuda(){
		
		JMenu ayudaMenu = new JMenu("Ayuda");
		
		// OPCION: Mostrar Ayuda
		// ----------------------
		JMenuItem opcionMostrarAyuda = new JMenuItem("Mostrar Ayuda",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconHelpMenu.png")));
		opcionMostrarAyuda.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								guiAyuda.mostrarAyuda();
							}
					}
					);
		
		ayudaMenu.add(opcionMostrarAyuda);
		ayudaMenu.addSeparator();
		
		// OPCION: Mostrar Acerca de...
		// ----------------------
		JMenuItem opcionAcercaDe = new JMenuItem("Acerca de...",new ImageIcon(this.getClass().getResource("/resources/icons/menu/iconInfoMenu.png")));
		opcionAcercaDe.addActionListener(new ActionListener(){
							public void actionPerformed (ActionEvent e){
								JOptionPane.showMessageDialog(InterfazCompilador.this,
										"\nCompilador PLG Grupo 05 (2012-2013)\n" +
										"\n Version Final (Septiembre 2013)\n"+
										"\nAutores:"+
										"\n---------------------------------------------------\n"+
										"* Raúl Bueno Sevilla\n"+
										"* Álvaro Pérez Liaño\n"+
										"* Pablo Pizarro Moleón\n"+
										"* Carlos Rodríguez Díaz\n\n",
										"Acerca de...",
										JOptionPane.INFORMATION_MESSAGE);
							}
					}
					);
		
		ayudaMenu.add(opcionAcercaDe);


		return ayudaMenu;
	}
	
} // Interfaz

