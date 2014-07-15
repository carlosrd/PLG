package Interprete;

import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/**
 * Objeto que representa la Máquina Pila que interpreta un lenguaje compilado (código objeto)
 * Dispone de 2 modos de ejecución: Estándar y Paso a Paso (Esta última se puede visualizar
 * gráficamente)
 */
public class MaquinaP {
	
	//Arquitectura interna. Ref Memoria 5.1.1
	
	// 	Mem. Memoria principal con celdas direccionables con datos
	private ArrayList<Elem> memoria;
	private DefaultTableModel tablaMem;
	private JTable tMem;
	
	//	Prog. Memoria de programa con celdas direccionables con	instrucciones
	ArrayList<String> programa;
	
	//	CProg. Contador de programa con un registro para la dirección de la Instrucción actualmente en ejecución
	private int cprog;
	
	//	Pila. Pila de datos con celdas direccionables con datos
	private Stack<Elem> pila;
	//	CPila. Cima de la pila de datos con un registro para la	dirección del dato situado actualmente en la cima de la pila
	//Interno a la clase Stack de java
	
	//	P. Registro con un bit de parada que detiene la ejecución
	private boolean p;
	//	RSwap1. Registro que indica si hay cambio entre el + y el -
	private boolean rswap1;
	//	RSwap2. Registro que indica si hay cambio entre el * y la /
	private boolean rswap2;
	
	//instruccion ejecutada en el momento
	private bytecode instruccion;
	
	
	/*
	 * Bytecode: formato de archivo aceptado por el interprete. Ref Memoria 10
	 */
	private enum bytecode{stop, apila, apila_dir, apila_ind, desapila, desapila_dir, desapila_ind, suma, resta, multiplica, divide,
						  modulo, mayor, mayoroigual, menor, menoroigual, distinto, igual, and_logica,
						  or_logica, niega, menos, desp_izq, desp_der, casting, swap1, swap2, in, out, 
						  ir_a, ir_f, ir_v, ir_ind, etiqueta, copia, mueve;}

	//modo de ejecución deseado
	String modoEjecucion;

	String lineaActual;
	
	// Elementos de la interfaz
	JTextArea visorTraza;
	JTextArea visorConsola;
	boolean primerOut;
	
	//Operandos
	private Elem op1, op2;
	private int heap = 500000;
	
	private boolean debug = false;
	
	/**
	 * Constructora principal. Inicializa estructuras de datos y registros de control.
	 */
	public MaquinaP(ArrayList<String> programaEntrada,JTextArea consola, JTextArea visor, JTable tablaMemoria) {
		memoria = new ArrayList<Elem>();
		if (tablaMemoria != null){
			tMem = tablaMemoria;
			tablaMem = (DefaultTableModel) tMem.getModel();
			
		}

		for(int h=0; h<heap; h++)
			memoria.add(new Elem("null","null"));
		
		programa = programaEntrada;
		cprog = 0;
		pila = new Stack<Elem>();
		rswap1 = false;
		rswap2 = false;
		p = false;
		
		// Elementos de la interfaz para mostrar la salida
		visorTraza = visor;
		visorConsola = consola;
		primerOut = true;
		
	}
	
	public MaquinaP(ArrayList<String> programaEntrada) {
		memoria = new ArrayList<Elem>();
		
		for(int h=0; h<heap; h++)
			memoria.add(new Elem("null","null"));
		
		programa = programaEntrada;
		cprog = 0;
		pila = new Stack<Elem>();
		rswap1 = false;
		rswap2 = false;
		p = false;
		debug = true;
	}
	
		// EJECUCION ESTANDAR
	/**
	 * Ejecuta el programa de forma normal visualizando la traza en Inspector
	 * @throws Exception
	 */
	public boolean ejecuta() throws Exception {
		boolean parada = false;	
		while (!p){
				parada = this.ejecutaSoloUna();
		}
		return parada == true;
	}
	
		// EJECUCION PASO A PASO
	/**
	 * Ejecuta solo una (la siguiente) instruccion del programa 
	 * @throws Exception
	 */
	public boolean ejecutaSoloUna() throws Exception {
		try{
			Elem elem = null;
			boolean paradaUsuario = false;
			
			elem = new Elem();
			lineaActual = programa.get(cprog);
			instruccion = reconoceInstruccion(elem,lineaActual);
			switch (instruccion) {
				case stop:
					stop();
					break;
				case apila:
					apila(elem);
					break;
				case apila_dir:
					apila_dir(Integer.parseInt(elem.getValor().toString()));
					break;
				case apila_ind:
					apila_ind();
					break;
				case desapila_dir:
					desapila_dir(elem.getTipo().toString(), Integer.parseInt(elem.getValor().toString()));
					break;
				case desapila_ind:
					desapila_ind(elem);
					break;
				case suma:
					suma();
					break;
				case resta:
					resta();
					break;
				case multiplica:
					multiplica();
					break;
				case divide:
					divide();
					break;
				case modulo:
					modulo();
					break;
				case mayor:
					mayor();
					break;
				case mayoroigual:
					mayoroigual();
					break;
				case menor:
					menor();
					break;
				case menoroigual:
					menoroigual();
					break;
				case mueve:
					mueve(elem);
					break;
				case distinto:
					distinto();
					break;
				case igual:
					igual();
					break;
				case and_logica:
					and();
					break;
				case or_logica:
					or();
					break;
				case niega:
					niega();
					break;
				case menos:
					menos();
					break;
				case desp_izq:
					desp_izq();
					break;
				case desp_der:
					desp_der();
					break;
				case casting:
					casting(elem.getTipo());
					break;
				case swap1:
					swap1();
					break;
				case swap2:
					swap2();
					break;
				case in:
					paradaUsuario = this.in(elem);
					break;
				case out:
					out();
					break;
				case ir_a:
					ir_a(elem);
					break;
				case ir_f:
					ir_f(elem);
					break;
				case ir_v:
					ir_v(elem);
					break;
				case ir_ind:
					ir_ind();
					break;
				case copia:
					copia();
					break;
				case desapila:
					pila.pop();
					break;
				default:
					break;
			}
				
			cprog++;
				
			if (debug) vistaInterna(); else {
				visorTraza.append("\n-------------------------------------------------------------------------");
				visorTraza.append("\nBytecode: " + lineaActual + "; Instruccion actual: "+ instruccion +"\n");
				vistaInternaEnVisor();
				
				if(p && !paradaUsuario){ // Si es una parada pero por fin de programa (no provocada por usuario)
					visorTraza.append("\n------------------------------\n \tEstado Final\n------------------------------\n");
					vistaInternaEnVisor();
				}
			}
				
			return paradaUsuario;	// Devolvemos si el usuario ha pulsado cancelar durante una peticion "in()"
		}
		catch (Exception e) {
			throw new Exception("Error en ejecución Línea: " + (cprog+1) +",  [ " + lineaActual +"  ]:  " + e.getMessage());
			//throw new Exception("Error en ejecución: '" + e.getMessage() + "'\nLinea " + cprog +": " + lineaActual);
		}
	}

	//Repertorio de instrucciones de la Maquina P. Ref. Memoria 5.1.3 --------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Detiene la ejecución
	 */
	private void stop() {
		p = true;
	}

    /**
	 * Apila un elemento en la pila
	 * @param elem Elem: elemento a apilar
     * @throws Exception Stack Overflow
	 */
	private void apila(Elem elem) throws Exception {
		pila.push(elem);
    }
	
	/**
     * Apila en la pila el contenido de memoria[iden.valor], cuyo tipo es iden.tipo.
     * @param pos direccón de memoria
     * @throws Exception 
     */
	private void apila_dir(int pos) throws Exception{
		Elem dato = memoria.get(pos);
		pila.push(new Elem(dato.getTipo(), dato.getValor()));		
    }
	
	/**
     * Desapila un dato guardandolo en memoria[iden.valor]
     * Elem: <tipo,direccion>
	 * @throws Exception Pila vacia
     */
	private void desapila_dir(String tipo, int pos) throws Exception{
    	if (pila.empty())
    		throw new Exception("No se puede guardar el elemento en memoria. La pila esta vacia.");
    	else {
	    	Elem dato = pila.pop();
	    		
//	    	si el dato es nat y se sale de rango, descomentar
//	    	if(!dato.getValor().equals("null")){
//	    		if (dato.getTipo().equals("natural") && Integer.parseInt(dato.getValor().toString()) <0) 
//	    			throw new Exception ("Valor natural fuera de rango");
//	    	}
    		
	    	while(memoria.size() <= pos)
	    		memoria.add(null);
	    	dato.setTipo(tipo);
	    	if (tipo.equals("float")){
	    		dato = new Elem(tipo, Double.parseDouble(dato.getValor().toString()));
	    	}
	    	memoria.set(pos,dato);
	    	
	    	actualizaMemUI(dato, pos);
    	}    	
    }
    
    /**
     * Suma la cima de la pila y el elemento siguiente y apila el resultado.
     * @throws Exception Fuera de rango
     */
	private void suma() throws Exception{
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	
		double f1 = op1.getTipo().equals("float")? (Double)op1.getValor() : ((Integer)op1.getValor()).doubleValue(); 
		double f2 = op2.getTipo().equals("float")? (Double)op2.getValor() : ((Integer)op2.getValor()).doubleValue(); 

		double valorResultado = rswap1 ? (double) (f1-f2) : (double) (f1+f2);
		String tipoResultado = tipoResultante(op1,op2, "+");
		
		
		Elem resultado = compruebaFueraRango(tipoResultado, valorResultado);
		pila.push(resultado);
  	}
  	
  	/**
     * Resta la cima de la pila y el elemento siguiente y apila el resultado.
     * @throws Exception Fuera de rango
     */
	private void resta() throws Exception{
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	

		double f1 = op1.getTipo().equals("float")? (Double)op1.getValor() : ((Integer)op1.getValor()).doubleValue(); 
		double f2 = op2.getTipo().equals("float")? (Double)op2.getValor() : ((Integer)op2.getValor()).doubleValue(); 

		double valorResultado = rswap1 ? (double) (f1+f2) : (double) (f1-f2);
		String tipoResultado = tipoResultante(op1,op2, "-");
		
		Elem resultado = compruebaFueraRango(tipoResultado, valorResultado);
		pila.push(resultado);
  	}
  	
  	/**
     * Resta la cima de la pila y el elemento siguiente y apila el resultado.
     * @throws Exception Fuera de rango
     */
	private void multiplica() throws Exception{
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	

		double f1 = op1.getTipo().equals("float")? (Double)op1.getValor() : ((Integer)op1.getValor()).doubleValue(); 
		double f2 = op2.getTipo().equals("float")? (Double)op2.getValor() : ((Integer)op2.getValor()).doubleValue(); 

		double valorResultado = rswap2 ? (double) (f1/f2) : (double) (f1*f2);
		String tipoResultado = tipoResultante(op1,op2, "*");
		
		Elem resultado = compruebaFueraRango(tipoResultado, valorResultado);
		pila.push(resultado);
  	}
  	
  	/**
     * Resta la cima de la pila y el elemento siguiente y apila el resultado.
     * @throws Exception División por cero, Fuera de rango
     */
	private void divide() throws Exception{
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	

		double f1 = op1.getTipo().equals("float")? (Double)op1.getValor() : ((Integer)op1.getValor()).doubleValue(); 
		double f2 = op2.getTipo().equals("float")? (Double)op2.getValor() : ((Integer)op2.getValor()).doubleValue(); 
		
		if (f2==0)
			throw new Exception ("División por cero");
		else {
			double valorResultado = rswap2 ? (double) (f1*f2) : (double) (f1/f2);
			String tipoResultado = tipoResultante(op1,op2, "/");
			
			Elem resultado = compruebaFueraRango(tipoResultado, valorResultado);
			pila.push(resultado);
  		}
  	}
  	
   /**
    * Calcula el módulo de los dos datos superiores de la pila.
    */
	private void modulo() throws Exception{
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	
		
		String tipoResultado = tipoResultante(op1,op2, "%");
		Elem resultado = new Elem(tipoResultado);
		if(tipoResultado.equals("natural"))
			resultado.setValor(Math.abs(((Integer)op1.getValor()).intValue() % ((Integer)op2.getValor()).intValue()));
		else
			resultado.setValor(((Integer)op1.getValor()).intValue() % ((Integer)op2.getValor()).intValue());
		pila.push(resultado);	
 	} 
 	
  	/**
  	 * Comparación booleana: a>b
  	 */
	private void mayor() throws Exception{
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	
		Elem resultado = new Elem("boolean");
		if(op1.getTipo().equals("boolean"))
		{
			int b1 = ((Boolean)op1.getValor()).booleanValue() ? 1 : 0;
			int b2 = ((Boolean)op2.getValor()).booleanValue() ? 1 : 0;
			resultado.setValor(b1 > b2);
		}
		else if(op1.getTipo().equals("character"))
			resultado.setValor((((Character)op1.getValor()).charValue() > ((Character)op2.getValor()).charValue()));
		else //Real, Entero o Natural
		{
			double f1 = op1.getTipo().equals("float")? (Double)op1.getValor() : ((Integer)op1.getValor()).doubleValue(); 
			double f2 = op2.getTipo().equals("float")? (Double)op2.getValor() : ((Integer)op2.getValor()).doubleValue();
			resultado.setValor(f1 > f2);			
		}
		pila.push(resultado);		
	}

	/**
  	 * Comparación booleana: a >= b
  	 */
	private void mayoroigual() throws Exception{
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	
		Elem resultado = new Elem("boolean");
		if(op1.getTipo().equals("boolean"))
		{
			int b1 = ((Boolean)op1.getValor()).booleanValue() ? 1 : 0;
			int b2 = ((Boolean)op2.getValor()).booleanValue() ? 1 : 0;
			resultado.setValor(b1 >= b2);
		}
		else if(op1.getTipo().equals("character"))
			resultado.setValor((((Character)op1.getValor()).charValue() >= ((Character)op2.getValor()).charValue()));
		else //Real, Entero o Natural
		{
			double f1 = op1.getTipo().equals("float")? (Double)op1.getValor() : ((Integer)op1.getValor()).doubleValue(); 
			double f2 = op2.getTipo().equals("float")? (Double)op2.getValor() : ((Integer)op2.getValor()).doubleValue();
			resultado.setValor(f1 >= f2);			
		}
		pila.push(resultado);	
  	}

	/**
  	 * Comparación booleana: a < b
  	 */
	private void menor() throws Exception{
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	
		Elem resultado = new Elem("boolean");
		if(op1.getTipo().equals("boolean"))
		{
			int b1 = ((Boolean)op1.getValor()).booleanValue() ? 1 : 0;
			int b2 = ((Boolean)op2.getValor()).booleanValue() ? 1 : 0;
			resultado.setValor(b1 < b2);
		}
		else if(op1.getTipo().equals("character"))
			resultado.setValor((((Character)op1.getValor()).charValue() < ((Character)op2.getValor()).charValue()));
		else //Real, Entero o Natural
		{
			double f1 = op1.getTipo().equals("float")? (Double)op1.getValor() : ((Integer)op1.getValor()).doubleValue(); 
			double f2 = op2.getTipo().equals("float")? (Double)op2.getValor() : ((Integer)op2.getValor()).doubleValue();
			resultado.setValor(f1 < f2);			
		}
		pila.push(resultado);	
  	}
  	
	/**
  	 * Comparación booleana: a <= b
  	 */
	private void menoroigual() throws Exception{
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	
		Elem resultado = new Elem("boolean");
		if(op1.getTipo().equals("boolean"))
		{
			int b1 = ((Boolean)op1.getValor()).booleanValue() ? 1 : 0;
			int b2 = ((Boolean)op2.getValor()).booleanValue() ? 1 : 0;
			resultado.setValor(b1 <= b2);
		}
		else if(op1.getTipo().equals("character"))
			resultado.setValor((((Character)op1.getValor()).charValue() <= ((Character)op2.getValor()).charValue()));
		else //Real, Entero o Natural
		{
			double f1 = op1.getTipo().equals("float")? (Double)op1.getValor() : ((Integer)op1.getValor()).doubleValue(); 
			double f2 = op2.getTipo().equals("float")? (Double)op2.getValor() : ((Integer)op2.getValor()).doubleValue();
			resultado.setValor(f1 <= f2);			
		}
		pila.push(resultado);	
  	}

	/**
  	 * Comparación booleana: a != b
  	 */
	private void distinto() throws Exception{
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	
		Elem resultado = new Elem("boolean");
		if(op1.getTipo().equals("boolean"))
		{
			int b1 = ((Boolean)op1.getValor()).booleanValue() ? 1 : 0;
			int b2 = ((Boolean)op2.getValor()).booleanValue() ? 1 : 0;
			resultado.setValor(b1 != b2);
		}
		else if(op1.getTipo().equals("character"))
			resultado.setValor((((Character)op1.getValor()).charValue() != ((Character)op2.getValor()).charValue()));
		else //Real, Entero o Natural
		{
			double f1 = op1.getTipo().equals("float")? (Double)op1.getValor() : ((Integer)op1.getValor()).doubleValue(); 
			double f2 = op2.getTipo().equals("float")? (Double)op2.getValor() : ((Integer)op2.getValor()).doubleValue();
			resultado.setValor(f1 != f2);			
		}
		pila.push(resultado);	
  	}
  	
	/**
  	 * Comparación booleana: a == b
  	 */
	private void igual() throws Exception{
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	
		Elem resultado = new Elem("boolean");
		if(op1.getTipo().equals("boolean"))
		{
			int b1 = ((Boolean)op1.getValor()).booleanValue() ? 1 : 0;
			int b2 = ((Boolean)op2.getValor()).booleanValue() ? 1 : 0;
			resultado.setValor(b1 == b2);
		}
		else if(op1.getTipo().equals("character"))
			resultado.setValor((((Character)op1.getValor()).charValue() == ((Character)op2.getValor()).charValue()));
		else //Real, Entero o Natural
		{
			double f1 = op1.getTipo().equals("float")? (Double)op1.getValor() : ((Integer)op1.getValor()).doubleValue(); 
			double f2 = op2.getTipo().equals("float")? (Double)op2.getValor() : ((Integer)op2.getValor()).doubleValue();
			resultado.setValor(f1 == f2);			
		}
		pila.push(resultado);	
  	}
  	
	/**
	 * And Lógica
	 */
	private void and() throws Exception{
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	
		Elem resultado = new Elem("boolean",((Boolean)op1.getValor()).booleanValue() && ((Boolean)op2.getValor()).booleanValue());
		pila.push(resultado);
	}
  	
	/**
	 * Or Lógica
	 */
	private void or(){
		op2 = pila.pop();     
		op1 = pila.pop();
		Elem resultado = new Elem("boolean",((Boolean)op1.getValor()).booleanValue() || ((Boolean)op2.getValor()).booleanValue());
		pila.push(resultado);
	} 
	
  	/**
  	 * Negacion logica.
  	 */
	private void niega() throws Exception{
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	
		pila.push(new Elem("boolean",!((Boolean)op1.getValor()).booleanValue()));			
  	}

	/**
	 * Calcula el complementario
	 */
	private void menos() throws Exception{
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");	
		if(op1.getTipo().equals("natural")|| (op1.getTipo().equals("integer")))		
			pila.push(new Elem("integer",-((Integer)op1.getValor()).intValue()));
		else
			pila.push(new Elem("float",-((Double)op1.getValor()).doubleValue()));
	}

  	/**
	 * Desplazamiento izquierda.
	 */
	private void desp_izq() throws Exception{
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");		
		pila.push(new Elem("natural",((Integer)op1.getValor()).intValue() << Math.abs(((Integer)op2.getValor()).intValue())));
  	}

	/**
	 * Desplazamiento derecha.
	 */
	private void desp_der() throws Exception {
		op2 = pila.pop();
		if (op2.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");
		op1 = pila.pop();
		if (op1.getValor().equals("null"))
			throw new Exception("Una de las variables esta sin inicializar!");				
		pila.push(new Elem("natural",((Integer)op1.getValor()).intValue() >> Math.abs(((Integer)op2.getValor()).intValue())));
	}

	/**
	 * Convierte la cima de la pila al tipo tipoIn
	 * @param tipoIn
	 */
	private void casting(String tipoIn) throws Exception {
		op1 = pila.pop();
		if (tipoIn.equals("natural")||tipoIn.equals("integer")||tipoIn.equals("float"))
			//si a es char -> dar unicode del char
			if (op1.getTipo().equals("character")&& !tipoIn.equals("float")){
				int valor = (int)((char)op1.getValor().toString().charAt(0));
				pila.push(new Elem(tipoIn, valor));	
			} else if (tipoIn.equals("float"))
				pila.push(new Elem(tipoIn, (double)((char)op1.getValor().toString().charAt(0))));
			else {
				pila.push(new Elem(tipoIn, (int) Double.parseDouble(op1.getValor().toString())));
			}
		else if (tipoIn.equals("boolean"))
			pila.push(new Elem(tipoIn, Boolean.parseBoolean(op1.getValor().toString())));
		else if (tipoIn.equals("character")){
			//si casting a char -> dar caracter correspondiente al natural
			if (op1.getTipo().equals("natural")){
				pila.push(new Elem(tipoIn, (char)Integer.parseInt(op1.getValor().toString())));
			} else {
				pila.push(new Elem(tipoIn, op1.getValor().toString()));
			}
		}
	}

	/**
	 * Invierte el flag rswap1
	 */
	private void swap1() {
		rswap1 = !(rswap1);
	}

	/**
	 * Invierte el flag rswap2
	 */
	private void swap2() {
		rswap2 = !(rswap2);
	}

	/**
	 * Espera a que el usuario introduzca un dato por consola. 
	 * El tipo del dato ha de coincidir con la variable donde 
	 * se va a almacenar y tiene que respetar las restricciones 
	 * en cuanto a rangos de datos 
	 * @throws Exception si el dato leido no es correcto
	 * @return TRUE si el usuario ha cancelado la entrada de datos y FALSE en otro caso
	 */
	private boolean in(Elem elem) throws Exception{
		
		String cadena = "";
		while (cadena.equals("")){
			cadena = (String) JOptionPane.showInputDialog(null, // ignorar
														"IN():\nIntroduzca un valor de tipo "+ elem.getTipo()+": ", // mensaje 
														"Funcion in() invocada", // titulo
														3,		// Icono por defecto: ?
														null, 	// custom icon
														null,	// Ignorar
														"");	// Valor inicial
			// Si pulsa cancelar, paramos ejecucion
			if (cadena == null){
				this.p = true;
				return true;	// El usuario ha parado la ejecucion
				}
			else if (cadena.equals("")) // Si pulsa aceptar e introduce un dato vacio, notificar y repetir
					JOptionPane.showMessageDialog(null,"ERROR!\nEl campo esta vacío. Debe introducir un valor");
		} // while

		// Interfaz muestra operacion in en la consola
		visorConsola.append("\n (in: " + elem.getTipo() + ") > "+ cadena + "\n");
		
		if (elem.getTipo().equals("character"))
			elem.setValor(convierteACaracter(cadena));
		else if (elem.getTipo().equals("boolean")
				&& (cadena.equals("true") || cadena.equals("false"))) {
			boolean introducido = cadena.equals("true") ? true : false;
			elem.setValor(introducido);
		} else {
			if (elem.getTipo().equals("float")) {
				try {
					double valorD = (double) Double.parseDouble(cadena);
					elem.setValor(valorD);
				} catch (Exception e){
					throw new Exception("El dato introducido no se corresponde con el tipo requerido.");
				}
				
				
			} else { //natural o integer
				try {
					int valorI = (int) Integer.parseInt(cadena);
					elem.setValor(valorI);
				} catch (Exception e) {
					throw new Exception("El dato introducido no se corresponde con el tipo requerido.");
				}
			}

			elem = compruebaFueraRango(elem.getTipo(), elem.getValor());
		}
		int direccion = (Integer)pila.pop().getValor();
		pila.push(elem);
		desapila_dir(elem.getTipo().toString(), direccion);
		
		return false;
	}	
	
	/**
	 * Muestra por pantalla la cima de la pila.
	 */
	private void out() {
        op1 = pila.pop();
        
        // Comprobamos si es el primer out de la linea para mostrar cabecera de funcion
        if (primerOut){
        	visorConsola.append("(out) > ");
        	primerOut = false;
        }
     
        // Si salta de linea, en el siguiente out, se mostrará cabecera de la función
        if (op1.getValor().equals('\n'))
        	primerOut = true;
        
        if (op1.getTipo().equals("float")){
	    	visorConsola.append(((Double)op1.getValor()).toString());
	    } else
	    	visorConsola.append(""+op1.getValor());

	    //System.out.println(op1.toString());
    }
	
	/**
	 * Duplica la cima de la pila
	 */
	public void copia(){
		op1 = pila.pop();
		op2 = new Elem(op1.getTipo(),op1.getValor());
		pila.push(op1);
		pila.push(op2);
	}
	
	/**
	 * Salta incondicionalmente a la linea señalada en elem.
	 * @param elem linea a la que saltar
	 */
	public void ir_a(Elem elem){
		cprog = Integer.parseInt((String)elem.getValor())-1;
	}
	
	/**
	 * Salta incondicionalmente a la linea señalada en la cima de la pila
	 */
	public void ir_ind(){
		op1 = pila.pop();
		cprog = (Integer)op1.getValor()-1;
	}
	
	/**
	 * Salta a la linea señalada en elem si la cima de la pila es 0 o false
	 */
	public void ir_f(Elem elem){
		Elem apilado = pila.pop();
		if (apilado.getValor().equals(false) || 
			apilado.getValor().equals(0)){
			cprog = Integer.parseInt((String)elem.getValor())-1;
		}	
	}
	
	/**
	 * Salta a la linea señalada en elem si la cima de la pila es 1 o true
	 */
	public void ir_v(Elem elem){
		Elem apilado = pila.pop();
		if (apilado.getValor().equals(true) || 
			apilado.getValor().equals(1)){
			cprog = Integer.parseInt((String)elem.getValor())-1;
		}		
	}
	
	/**
	 * Interpreta el valor d en la cima de la pila como un nºmero de celda en la memoria, y sustituye dicho valor por el almacenado en dicha celda
	 */
	public void apila_ind() throws Exception{
		op1 = pila.pop();
		int direccion = (Integer)op1.getValor();
		if (direccion<0) 
			throw new Exception("Accediendo a una posicion de memoria erronea (" + direccion+")");
		Elem dato = memoria.get(direccion);
		pila.push(dato);
	}
	
	/**
	 * Desapila el valor de la cima v y la subcima d, interpreta d como un nºmero de celda en la memoria, y almacena v en dicha celda.
	 */
	public void desapila_ind(Elem elem) throws Exception{			
		op1 = pila.pop();	//v
		op2 = pila.pop();	//d		
		int direccion = (Integer)op2.getValor();
		if (direccion<0) 
			throw new Exception("Accediendo a una posicion de memoria erronea (" + direccion+")");
		else {
			memoria.set(direccion, op1);
			actualizaMemUI(op1, direccion);	
    	}
	}
	
	/**
	 * Encuentra en la cima la dirección origen o y en la subcima la dirección destino d, y realiza el movimiento de elem.valor celdas desde o a d
	 * @param elem numero de celdas a trasladar
	 */
	public void mueve(Elem elem){
		op1 = pila.pop();	//o
		op2 = pila.pop();	//d
		int origen = (Integer) op1.getValor();
		int destino = (Integer) op2.getValor();		
		int numCeldas = (Integer) elem.getValor();
		for(int i = 0; i < numCeldas; i++){	
			memoria.set(destino+i, memoria.get(origen+i));
			actualizaMemUI(memoria.get(origen+i), destino+i);
		}
	}
	
    //------------------ Fin Repertorio de Instrucciones ----------------------------------------------------------------------------------------------------------------------------------------------------------------
	
    //Funciones auxiliares
	
	private bytecode reconoceInstruccion(Elem elem, String entrada) throws Exception {
		String instruccion, parametros;
				
		//separar tipo instruccion y parametros
		String[] aux = entrada.split("\\(|\\)");
		instruccion = aux[0];
		
		//convertir -  a _ para usar en enumerado
		if (instruccion.contains("-")) 
			instruccion = instruccion.replace("-", "_");
		
		//instrucciones con nombre etiqueta
		if (instruccion.equals("ir_a") || instruccion.equals("ir_f")|| instruccion.equals("ir_v")){
			elem.setTipo("etiqueta");
			elem.setValor(aux[1]);
		} else { 
			if (aux.length == 3) { //leimos un ( o un )
				aux[1] = aux[1]+entrada.charAt(entrada.length()-3)+"'";
			}
			if (aux.length>1) {
				parametros = aux[1];
				//separar parametros en caso de haber varios. Cinco posibilidades: (direccion), (tipo, valor), (tipo,direccion), (tipo), (etiqueta)
				if(instruccion.equals("apila")||instruccion.equals("casting")){ //||instruccion.equals("in")) { //instrucciones con tipo como primera comp
					aux = parametros.split("\\,");
					String tipo = aux[0];
					elem.setTipo(tipo);
					//crear elem de salida
					if(instruccion.equals("apila")) {//instrucciones con tipo,valor
						if (tipo.equals("boolean")) {
							boolean valorB = aux[1].equals("true") ? true : false;
							elem.setValor(valorB);
						}
						else if (tipo.equals("character")) {
							if (instruccion.equals("apila")) {
								if (aux.length == 3) //hemos metido una coma, que ha separado de más
									elem.setValor(convierteACaracter(","));
								else {
									aux[1] = aux[1].substring(1, aux[1].length()-1);
									elem.setValor(convierteACaracter(aux[1]));
								}
							}
							else if (instruccion.equals("in")) {
								int valorI = Integer.valueOf(aux[1]);
								elem.setValor(valorI);
							}
						}
						else if (tipo.equals("float")) {
							double valorF = Double.valueOf(aux[1]);
							elem.setValor(valorF);
						}
						else { //natural o integer
							double valorF = Double.valueOf(aux[1]);
							if (valorF < 2147483647 && valorF > -2147483648) {
								int valorI = Integer.valueOf(aux[1]);
								elem.setValor(valorI);
							} else { throw new Exception("Datos de entrada fuera del rango permitido"); } 
						}
						//comprueba que los valores estan en los rangos permitidos
						if (!elem.getTipo().equals("boolean"))
						elem = compruebaFueraRango(elem.getTipo(),elem.getValor());
					} 	
				}
				else if (instruccion.equals("desapila_dir")) { //instrucciones con tipo, direccion 
					aux = parametros.split("\\,");
					String tipo = aux[0];
					elem.setTipo(tipo);
					int valorI = Integer.valueOf(aux[1]);
					elem.setValor(valorI);
				} else if (instruccion.equals("in")) { //instrucciones con tipo
					elem.setTipo(aux[1]);
					elem.setValor(-1);	
				}  
				else { 
					//instrucciones con dirección: apila-dir , desapila-dir
					int direccion = Integer.valueOf(aux[1]);
					elem.setValor(direccion); 
				}
			}
		}
		try {
			return bytecode.valueOf(instruccion);
		} catch (Exception e) {
			throw new Exception("No se ha reconocido correctamente la instrucción " + instruccion + ". Revisar bytecode.");
		}
	}
    
	/**
	 * Devuelve el tipo resultante para la Operación entre los Elems opA y opB 
	 * @param opA Elem: operando 1
	 * @param opB Elem: operando 2
	 * @param operador String: operador
	 * @return String: tipo del resultado
	 */
    private String tipoResultante(Elem opA, Elem opB, String operador) {
		boolean real, entero;
    	if(operador.equals("+") || operador.equals("-") || operador.equals("*") || operador.equals("/")) {
    		real    = opA.getTipo().equals("float") || opB.getTipo().equals("float");
    		entero  = opA.getTipo().equals("integer") || opB.getTipo().equals("integer");
    		return real ? "float" : ( entero ? "integer" : "natural");
    	}	
    	else
    		return op1.getTipo();   
	}
       
    /**
     * Comprueba que el resultado de la operacion no se ha salido de rango y además le da el tipo adecuado.
     * @param valorResultado Elem resultado
     * @return Elem
     * @throws Exception Operación fuera de rango
     */
    private Elem compruebaFueraRango(String tipoResultado, Object valorResultado) throws Exception {
    	Elem resultado = new Elem(tipoResultado,valorResultado);
    	if (!tipoResultado.equals("boolean")&& !tipoResultado.equals("character")) {
    		double valorR = Double.parseDouble(valorResultado.toString()) ;
    		
	    	if(tipoResultado.equals("integer") || tipoResultado.equals("natural")){
				if (valorR > 2147483647||valorR < -2147483648)
					throw new Exception("Operación de enteros fuera de rango");
				resultado.setValor((int)valorR);
			}
			else if(tipoResultado.equals("float")){
				if (valorR > 3.40282347E+38F || valorR < -3.40282347E+38F)
					throw new Exception("Operación de reales fuera de rango");
				resultado.setValor((double)valorR);
			}
    	}
	    return resultado;
    }
    
	/**
	 * Convierte las cadenas introducidas por consola al formato válido
	 * @param cadena String de entrada 
	 * @return Caracter válido
	 * @throws Exception Caracter no soportado en nuestro lenguaje
	 */
	private char convierteACaracter(String cadena) throws Exception{		
		if(cadena.length() == 1)	//String de longitud 1
			return cadena.charAt(0);	
		else if(cadena.equals("\\r")) return'\r';				
		else if(cadena.equals("\\t")) return'\t';				
		else if(cadena.equals("\\n")) return'\n';				
		else if(cadena.equals("\\'")) return'\'';				
		else if(cadena.equals("\\\\")) return'\\';	
		else
			throw new Exception("El caracter "+cadena+" no esta contemplado en nuestro lenguaje");		
	}
	
	// METODOS PARA VISUALIZAR MAQUINA-P EN INTERFAZ
	// ******************************************************************************************
	
	private void actualizaMemUI(Elem dato, int pos){
		// Actualiza Tabla Memoria Interfaz Grafica (Solo si es ejecución paso a paso => tablaMem != null)
    	if (tablaMem != null){
    		// Guardamos la direccion de memoria de la tabla de la interfaz grafica
    		String dirMem = String.valueOf(tablaMem.getValueAt(pos, 0));
    		// Eliminamos la fila a modificar
    		tablaMem.removeRow(pos);
    		// Insertamos la fila actualizada
    		tablaMem.insertRow(pos, new String[]{dirMem,String.valueOf(dato.getValor())});
    		// La seleccionamos y hacemos scrooll hasta ella
    		tMem.changeSelection(pos, 0, false, false);
    		tMem.scrollRectToVisible(tMem.getCellRect(pos+5, 0, false));
    	}
	}
	
	/**
	 * Muestra el estado de las estructuras de datos internas
	*/ 
	private void vistaInterna() {
		Elem elem;
    	System.out.println("Instrucción nº" + cprog+": " + instruccion);    	
    	System.out.println("\tPILA: " + pila.toString());
    	System.out.println("\tMEMORIA:");
    	for(int i = 0; i < memoria.size();i++){
    		elem = memoria.get(i);
    		if(!elem.getTipo().equals("null"))   		
    			System.out.println("\t\tdir "+i+": "+elem.toString()); 
    	}
	}
	
	private void vistaInternaEnVisor() {
		Elem elem;
    	visorTraza.append("\nInstrucción " + cprog+": ");    	
    	visorTraza.append("\n\tPILA: " + pila.toString());
    	visorTraza.append("\n\tMEMORIA:");
    	for(int i = 0; i < memoria.size();i++){
    		elem = memoria.get(i);
    		if(!elem.getTipo().equals("null"))    		
    			visorTraza.append("\n\t\tdir "+i+": "+elem.toString()); 
    	}
	}
    

	/**
	 * Detiene la ejecución de la Máquina P
	 */
	public void detenerMaquinaP(){
		this.p = true;
	}
	
	
	/**
	 *  Recupera la pila para la interfaz (para mostrarla en ejecuion paso a paso)
	 * @return Pila
	 */
	public Stack<Elem> getPila(){
		return pila;
	}
	
	/**
	 *  Recupera la memoria para la interfaz (para mostrarla en ejecuion paso a paso)
	 * @return memoria
	 * @deprecated
	 */
	public ArrayList<Elem> getMemoria(){
		return memoria;
	}
	
	public DefaultTableModel getTablaMem(){
		return tablaMem;
	}
	
	/**
	 *  Recupera el registro de parada para la interfaz (para mostrarlo en ejecuion paso a paso)
	 * @return p (Registro Parada)
	 */
	public boolean getRegParada(){
		return p;
	}
	
	/**
	 *  Recupera el registro swap1 para la interfaz (para mostrarlo en ejecuion paso a paso)
	 * @return swap1 (Registro Swap1)
	 */
	public boolean getRegSwap1(){
		return rswap1;
	}
	
	/**
	 * Recupera el registro de swap2 para la interfaz (para mostrarlo en ejecuion paso a paso)
	 * @return swap2 (Registro Swap2)
	 */
	public boolean getRegSwap2(){
		return rswap2;
	}
	
	/**
	 *  Recupera el Contador de Programa para la interfaz (para mostrarlo en ejecuion paso a paso)
	 * @return cprog (Registro Contador de Programa)
	 */
	public int getCProg(){
		return cprog;
	}
	
	/**
	 *  Recupera el contador de cima de la pila para la interfaz (para mostrarlo en ejecuion paso a paso)
	 * @return
	 */
	public int getCPila(){
		return pila.size();
	}
	
}

