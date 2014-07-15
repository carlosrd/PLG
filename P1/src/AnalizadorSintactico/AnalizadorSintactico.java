package AnalizadorSintactico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.*;

import AnalizadorLexico.AnalizadorLexico;
import AnalizadorSintactico.ExpTipos.*;
import AnalizadorSintactico.TOperaciones.TablaOperaciones;
import AnalizadorSintactico.TSimbolos.*;
import Atributos.CategoriaLexica;
import Atributos.Token;
import InterfazGrafica.Tablas.TableCellLongTextRenderer;

/**
 * Analizador Sintáctico del compilador. Encargada de procesar lo que va recibiendo del Analizador Léxico y 
 * finalmente construye el código P si el procesamiento ha sido correcto
 *  */
public class AnalizadorSintactico {

	// ATRIBUTOS
	// ******************************************************************************
	private ArrayList<String> listaErr; 
	private TablaOperaciones tOperaciones;
	private TablaSimbolos ts;
	private Codigo cod;
	private int nivelH;
	private int etq;
	private boolean desig;
	private ArrayList<Integer> lirv = new ArrayList<Integer>();
	private ArrayList<Integer> lirf = new ArrayList<Integer>();
	
	// Constantes
	private final int longPrologo = 13;
	private final int longEpilogo = 14;
	private final int longInicio = 2;
	private final int longRetorno = 5;
	
	private AnalizadorLexico scanner;
	private Token tActual;
	
	// Atributos referentes a la Interfaz Grafica
	JTextArea consola;					// Consola para mostrar los errores
	DefaultTableModel tsInterfaz;		// Tabla para mostrar la tabla de simbolos
	JTabbedPane panelTabuladoTS;
	private boolean parh;

	
	// RETORNOS COMPUESTOS
	// ******************************************************************************	
	// Clases privadas para almacenar los retornos
	
	/**
	 * Clase que permite gestionar la información de retorno asociada a los variables
	 */
	private class retVar {
		public boolean err;
		public int dir;
		public retVar(boolean err, int dir) {
			this.err = err;
			this.dir = dir;
		}	
	}
	
	/**
	 * Clase que permite gestionar la información de retorno asociada a los paramétros
	 */
	private class retPF {
		public boolean err;
		public int dir;
		// Para hacerlo mas general, exprTipo es un array (en algunos casos solo tenemos un valor)
		public ArrayList<ExpTipo> exprTipo;
		// La lista de parámetros no la metemos en la constructora
		// porque no la usan todas las producciones que devuelve esta clase
		public ArrayList<String> listaParams = new ArrayList<String>();
		
		public retPF(boolean err, int dir, ArrayList<ExpTipo> exprTipo) {
			this.err = err;
			this.dir = dir;
			this.exprTipo = exprTipo;
		}		
	}
	
	/**
	 * Clase que permite gestionar las llamadas a parámetros
	 */
	private class retParam {
		public boolean err;
		public ArrayList<String> listaParams;
		public retParam(boolean err, ArrayList<String> listaParams) {
			this.err = err;
			this.listaParams = listaParams;
		}	
	}
	
	/**
	 * Clase que permite gestionar el retorno de constantes
	 */
	private class retValor {
		public String lex;
		public CategoriaLexica tipoDevuelto;
		public retValor(String valor, CategoriaLexica catLex){
			this.lex = valor;
			this.tipoDevuelto = catLex;
		}
	}
	

	// CONSTRUCTORA
	// ******************************************************************************
	
	/**
	 * Constructora del Sintactico, inicializa el Léxico para que procese un programa que se le pasa como
	 * parámetro. Inicializa la lista de errores, la tabla de operaciones y el codigo.
	 * @param program Programa que procesará el Léxico e irá devolviendo Tokens
	 */
	public AnalizadorSintactico(InputStream program)
	{
		scanner = new AnalizadorLexico(program);
		listaErr = new ArrayList<String>();
		tOperaciones = new TablaOperaciones(ts);
		cod = new Codigo();
	}
	
	/**
	 * Método que empieza a analizar el programa.
	 * @param consola rea de texto de la consola (para mostrar errores y otros mensajes)
	 * @param tsInterfaz La parte de la interfaz que muestra la TS
	 * @param visorCodObj La parte de la interfaz que muestra el codigo objeto
	 * @param rutaCodObj Ruta de dónde está el código objeto
	 * @return devuelve el código preparado para ser procesado por la máquina pila
	 */
	public ArrayList<String> run(JTextArea consola,DefaultTableModel tsInterfaz,JTabbedPane panelTabuladoTS, JTextArea visorCodObj, String rutaCodObj)
	{
		this.consola = consola;
		this.tsInterfaz = tsInterfaz;
		this.panelTabuladoTS = panelTabuladoTS;
		
		try {	// Probamos a parsear. En caso de error sintáctico, se lanzará una excepción que recogeremos aquí

			if (rProgFin()){
				consola.append("\n> Se han encontrado errores:");
				mostrarListaErrores();
			}
			else
			{
				consola.append("\n> Se ha generado el código objeto correctamente!");
				cod.emite(InstruccionesPila.stop, null);
				cod.imprimeCodigo(rutaCodObj);
				rellenaVisorCodigoObjeto(visorCodObj,rutaCodObj);
				return cod.preparaCodigoParaMaquinaP();
			}
			
			
		} catch (Exception e) {
			mostrarListaErrores();
			e.printStackTrace();
			System.out.println(e.getMessage());
			consola.append(e.getMessage());
			consola.append("\n> [DEBUG]: Parser STOP!");			
		}
		return null;
	}
	
	/**
	 * <dd>ProgFin (out err) ::= Prog (out err1)
	 * @return boolean indica si ha ocurrido algún error 
	 * @throws Exception 
	 */
	private boolean rProgFin() throws Exception
	{
		boolean err;
		err = rProg();
		reconocer(CategoriaLexica.TFin,"<eof>");
		consola.append("\n> Todos los tokens procesados!");
		return err;
	}
	
	/**
	 * <dd>Prog (out err) ::= 
	 * <dl>
	 * 		<dd><b>program:</b> iden <b>{</b>
	 * 	  	<dd>SecConst (out errConst)
	 * 	  	<dd>SecTipos (out errTipos)
	 *	  	<dd>SecVars (in dirh ; out errVars, tamVars)
	 *	  	<dd>SecSubprogs (out errSubprogs)
	 *	<b>}</b>
	 * <dl>
	 * @return boolean para indicar si ocurrido algún error
	 * @throws Exception para indicar errores sintácticos
	 */
	private boolean rProg() throws Exception {
				
		boolean errSecConsts;
		boolean errSecTipos; 
		boolean errSecVars; 
		boolean errSecSubprogs;
		boolean errIns;

		// Reconocer "program:"
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.PalabraReservada,"program:"))
			throw new Exception(errorSintactico("Se esperaba palabra reservada 'program:'"));

		// Reconocer "identificador prog"
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.Identificador))
			throw new Exception(errorSintactico("Se esperaba identificador de programa válido"));
		
		// Reconocer "{"
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.AbreLlave))
			throw new Exception(errorSintactico("Se esperaba apertura de ámbito '{' de programa después de identificador de programa válido"));
		
		// nivel -> global
		nivelH = 0;
		// ultima direccion ocupada (B esta en la pos 1)
		int dirh = 1;
		// Creamos la TS()
		ts = creaTS(); 		
		
		// Seccion de constantes
		errSecConsts = rSecConsts(); 
		
		// Seccion de tipos
		errSecTipos = rSecTipos(); 	
		
		// Seccion de variables
		retVar outSecVars = rSecVars(dirh);
		// Para recoger los dos atributos de salida		
		errSecVars = outSecVars.err;
		int tamVars = outSecVars.dir;
			
		// Codigo de inicio y su longitud
		cod.inicio(tamVars);
		etq = longInicio;
		// Direccion de futuro parcheo
		ArrayList<Integer> listaParchear = new ArrayList<Integer>();
		listaParchear.add(etq);
		// ir-a(?)
		ArrayList<String> aux = new ArrayList<String>();
		aux.add("?");
		cod.emite(InstruccionesPila.ir_a, aux);
		// Avanzamos etq 
		etq = etq +1;
		
		// Seccion de subprogramas
		tOperaciones = new TablaOperaciones(ts);
		errSecSubprogs = rSecSubprogs();		
		
		// Parcheamos
		cod.parchea(listaParchear, etq);
		
		rellenaTSInterfaz(ts);		
		tOperaciones = new TablaOperaciones(ts);
		
		// Reconocer "instructions"
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		
		if (!reconocer(CategoriaLexica.PalabraReservada,"instructions"))
			throw new Exception(errorSintactico("Se esperaba palabra reservada 'instructions'"));
		
		// Reconocer "{" 
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.AbreLlave))
			throw new Exception(errorSintactico("Se esperaba apertura de ámbito '{' de instrucciones"));
				
		errIns = rIs(); 
		
		// Reconocer "}" (del bloque de Instrucciones)
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.CierraLlave))
			throw new Exception(errorSintactico("Se esperaba cierre de ámbito '}' de instrucciones"));
			
		// Reconocer "}" (final de programa)
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.CierraLlave))
			throw new Exception(errorSintactico("Se esperaba cierre de ámbito '}' de programa"));
		
		// INTERFAZ GRAFICA
		// -------------------------------------
	
		return errSecConsts || errSecTipos || errSecVars || errSecSubprogs || errIns;
		
	} // rProg();
	
	/**
	 * <dd>SecConst (out errConst)::= 
	 * <dl>
	 * 		<dd><b>consts {</b>DecConsts(out errDecConsts)<b>}</b> 
	 * </dl>
	 * <dd>SecConst (out errConst) ::= λ
	 * @return boolean para saber si ha ocurrido algún error
	 * @throws Exception
	 */
	private boolean rSecConsts() throws Exception{
		
		boolean errSecConsts = false;
		// Si reconoce consts -> Seccion de consts
		// --------------------------------------------------------------
		tActual = scanner.checkNextToken(); 
		if (reconocer(CategoriaLexica.PalabraReservada, "consts")){ 	
			tActual = scanner.getNextToken();
			// Reconocer "{"
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.AbreLlave))
				throw new Exception(errorSintactico("Se esperaba '{' (Inicio sección declaración de constantes)"));
			// Vamos a DecConsts
			errSecConsts = rDecConsts();			
			// Reconocer "}"
			tActual = scanner.getNextToken();		
			if (!reconocer(CategoriaLexica.CierraLlave))
				throw new Exception(errorSintactico("Se esperaba 'const' (Inicio de declaración de constante) o '}' (Fin sección declaración de constantes)"));
		}
		// Si no reconocemos consts, estaremos en lambda
		return errSecConsts;		
	}
	
	/**
	 * <dd>SecTipos (out errTipos) ::= 
	 * <dl>
	 * 		<dd><b>tipos {</b>DecTipos(out errDecTipos)<b>}</b>
	 * </dl>
	 * <dd>SecTipos (out errTipos) ::= λ
	 * @return boolean para saber si ha ocurrido algún error
	 * @throws Exception para indicar errores sintácticos
	 */
	private boolean rSecTipos() throws Exception{
		
		boolean errSecTipos = false;		
		// Si reconoce tipos -> Seccion de tipos
		// --------------------------------------------------------------
		tActual = scanner.checkNextToken();
		if (reconocer(CategoriaLexica.PalabraReservada, "tipos")){
			tActual = scanner.getNextToken();
			// Reconocer "{"
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.AbreLlave))
				throw new Exception(errorSintactico("Se esperaba '{' (Inicio sección declaración de nuevos tipos)"));	
			
			// Vamos a DecTipos
			errSecTipos = rDecTipos();
			
			// Reconocer "}"
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.CierraLlave))
				throw new Exception(errorSintactico("Se esperaba 'tipo' (Inicio declaración de nuevo tipo) o '}' (Fin sección declaración de nuevos tipos)"));			
		}
		// Si no reconocemos tipos, estaremos en lambda		
		return errSecTipos;		
	}
	
	/**
	 * <dd>SecVars (in dirh ; out errVars, tamVars) ::=
	 * <dl> 
	 *    	<dd><b>vars {</b> DecVars(in dirh ; out errDecVars , dir)<b> }</b> 
	 * </dl>
	 * <dd>SecVars (in dirh ; out errVars, tamVars) ::= λ
	 * @param dir_h para saber en qué dirección estará la variable
	 * @return retVar el cual contiene información sobre la dirección siguiente y si hay errores
	 * @throws Exception para indicar errores sintácticos
	 */
	private retVar rSecVars(int dir_h) throws Exception{		
		retVar devolucion = new retVar(false, dir_h); // (out errVars, out tamVars)
		// Si reconoce vars -> Seccion de vars
		// --------------------------------------------------------------
		tActual = scanner.checkNextToken();
		if (reconocer(CategoriaLexica.PalabraReservada, "vars")){
			tActual = scanner.getNextToken();
			// Reconocer "{"
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.AbreLlave))
				throw new Exception(errorSintactico("Se esperaba '{' (Inicio sección declaración de variables)"));						
			
			// Vamos a DecVars
			devolucion = rDecVars(dir_h);
			// Ponemos el valor correcto de tamVars
			// devolucion.dir = devolucion.dir-1;		
			
			// Reconocer "}"
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.CierraLlave))
				throw new Exception(errorSintactico("Se esperaba 'var' (Inicio declaración de variable) o '}' (Fin sección declaración de variables)"));					
		}
		// Si no reconocemos vars, estaremos en lambda		
		return devolucion;		
	}
	
	/**
	 * <dd>SecSubprogs (out errSubprogs) ::=
	 * <dl> 
	 *    	<dd><b>subprograms {</b> DecSubprogs (out errDecSubprog)<b> }</b> 
	 * </dl>
	 * <dd>SecSubprogs (out errSubprogs) ::= λ
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception para indicar errores sintácticos
	 */
	private boolean rSecSubprogs() throws Exception{
		boolean errSecSubprog = false;		
		// Si reconoce subprograms -> Seccion de subprograms
		// --------------------------------------------------------------
		tActual = scanner.checkNextToken();
		if (reconocer(CategoriaLexica.PalabraReservada, "subprograms")){
			tActual = scanner.getNextToken();
			// Aumentamos el nivel
			nivelH++;
			// Reconocer "{"
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.AbreLlave))
				throw new Exception(errorSintactico("Se esperaba '{' (Inicio sección declaración de subprogramas)"));
						
			// Vamos a DecSubprogs
			errSecSubprog = rDecSubProgs();
						
			// Reconocer "}"
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.CierraLlave))
				throw new Exception(errorSintactico("Se esperaba 'subprogram:' (Inicio declaración de subprograma) o '}' (Fin sección declaración de subprogramas)"));
			// Bajamos el nivel
			nivelH--;
		} 
		// Si no reconocemos subprograms, estaremos en lambda
		return errSecSubprog;
	}
	

	// DECLARACIONES: CONSTANTES
	// *****************************************************************************************
	
	/**
	 * <dd>DecConsts (out errDecConst) ::= 
	 * <dl>
	 * 		<dd>DecConst (out errDecConst) 
	 *    	<dd>RDecConsts (in errhRDecConsts ; out errRDecConsts)
	 * </dl>
	 * @return boolean que indica si se ha producido algún error
	 * @throws Exception para indicar errores sintácticos
	 */
	private boolean rDecConsts() throws Exception {
		
		// Reconocimiento y tratamiento de una declaracion
		boolean errRDecConst_h = rDecConst();	
		
		// Reconocemos ;
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.PuntoComa,";"))
			throw new Exception(errorSintactico("Se esperaba ; después de declaración de constante"));
		
		// Recursion para el resto de declaraciones (si existen)
		boolean errRDecConsts = rRDecConsts(errRDecConst_h);	
		
		return errRDecConsts;								
	}
	
	/**
	 * <dd>RDecConsts (in errhRDecConsts ; out errRDecConsts) ::= 
	 * <dl>
	 * 		<dd>DecConst (out errDecConst) 
	 *    	<dd>RDecConsts (in errhRDecConsts1 ; out errRDecConsts1)
	 * </dl>
	 * <dd>RDecConsts (in errhRDecConsts ; out errRDecConsts) ::= λ
	 * @param errRDecConst0_h parametro para saber si se ha producido un error anteriormente
	 * @return boolean que indica si se ha producido algún error
	 * @throws Exception para indicar errores sintácticos
	 */
	private boolean rRDecConsts(boolean errRDecConst0_h) throws Exception{
		
		boolean errDecConst = false;
		boolean errRDecConst1_h;
		boolean errRDecConst0  = errRDecConst0_h;
		
		// Comprobamos el siguiente token a ver si tiene "const" 
		tActual = scanner.checkNextToken();			
		// Reconocer "const"
		// ----------------------------------------------
		if (reconocer(CategoriaLexica.PalabraReservada,"const")) {

			// Si el siguiente token es "const", vamos a procesar una declaracion de constante
			errDecConst = rDecConst();
			
			// Reconocemos ;
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.PuntoComa))
				throw new Exception(errorSintactico("Se esperaba ; después de declaración de constante"));
						
			// Probamos a ver si hay mas declaraciones (Volver a llamar a rRDecConsts()
			errRDecConst1_h = errDecConst || errRDecConst0_h;
			errRDecConst0 = rRDecConsts(errRDecConst1_h);
			
		} 
		// Si no reconocemos const, estaremos en lambda
		return errRDecConst0;
		
	}
	
	/**
	 * <dd>DecConst (out errDecConst) ::= 
	 * <dl>
	 * 		<dd><b>const</b> tipoBasico (out lexTipo) iden (out lexIden) 
	 * 		<b>:=</b> Valor (out lexValor , tipoValor) <b>;</b>
	 * </dl>
	 * @return boolean para saber si se ha producido un error
	 * @throws Exception para indicar errores sintácticos
	 */
	private boolean rDecConst() throws Exception{
		
		String tipoConst;
		String idConst;
		String valorConst;
		
		// Llamadas implicitas
		boolean existeID = false;
		boolean compatibles;
		
		// Avanzamos y leemos el siguiente token
		tActual = scanner.getNextToken();		
		// Reconocer "const"
		// ----------------------------------------------
		if (reconocer(CategoriaLexica.PalabraReservada,"const"))
		{
			// Reconocer "tipo"
			// ----------------------------------------------
			tActual = scanner.getNextToken();
			CategoriaLexica tipoEsperado = reconocerTipo();
			if (tipoEsperado == null)
				throw new Exception(errorSintactico("Se esperaba definicion de tipo básico válido"));
						
			tipoConst = tActual.get_lexema();		// Propiedad tipo de datos
						
			// Reconocer "identificador"
			// ----------------------------------------------
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.Identificador))
				throw new Exception(errorSintactico("Se esperaba identificador de constante válido"));

			idConst = tActual.get_lexema();		// Propiedad identificador de constante

			// Reconocer ":="
			// ----------------------------------------------
			tActual = scanner.getNextToken();
				if (!reconocer(CategoriaLexica.OpAsignConst))
					throw new Exception(errorSintactico("Se esperaba ':=' (Instruccion asignacion de constante) después de identificador"));
						
			// Reconocer "valor"
			// ----------------------------------------------
				
			retValor valor = rValor(tipoConst);
			
			compatibles = !(valor.tipoDevuelto == null || 
						!tOperaciones.existeOperacion(CategoriaLexica.OpAsignConst,null,tipoEsperado,valor.tipoDevuelto));

			
			// Si leemos un ";" antes de avanzar, es que el valor no se ha declarado			
			if (reconocer(CategoriaLexica.PuntoComa)) 
				throw new Exception(errorSintactico("Se esperaba un valor de inicialización para la constante '"+idConst+"'"));
			else
				valorConst = valor.lex;	// Propiedad valor de la constante
						
			if (!compatibles)
				añadirError(tActual.get_linea(),"El tipo esperado no coincide con el tipo declarado '" +tipoConst+"'.");
			else {
				//Si no se han producido errores, entonces se anade a la TS
				ExpTipo expTipo = new ExpTipoConstante(tipoConst,ts,valorConst); 	// ExpTipoConstante(String t, TablaSimbolos ts,String valor)
				Propiedades p = new Propiedades(-1, ClaseDec.Constante,expTipo); 	// Propiedades(int d, ClaseDec c, int nivel, ExpTipo expTipo)
				// existeID si no pudimos añdirlo a la TS
				
				existeID = !(ts.añadeID(idConst, 0, p));
				
				if (existeID)
					añadirError(tActual.get_linea(),"El identificador '"+idConst+"' ya ha sido declarado.");	
			}
		}
		else
			throw new Exception(errorSintactico("Se esperaba 'const' (Inicio declaración de constante) o '}' (Fin sección declaración de constantes) "));

		return existeID || !compatibles;				 
	}
	
	/**
	 * Método para tratar la devolución de información sobre constantes
	 * @param tipoEsperado tipo que se espera que tenga la constante
	 * @return información asociada que se recoge, asi como el tipo devuelto, etc.
	 * @throws Exception para indicar errores sintácticos
	 */
	private retValor rValor(String tipoEsperado) throws Exception{
		
		String valor = null;
		CategoriaLexica catLex = null;
		
		// Avanzamos tokenizer para leer el valor.
		tActual = scanner.getNextToken();
		
		// Si el menos esta separado, juntarlo (Solo para constantes con valores integer)
		if (reconocer(CategoriaLexica.Menos)){
			
			// Copiamos el "-" al valor
			valor = tActual.get_lexema();
			
			// Avanzamos para leer el numero despues del "-" y lo concatenamos
			tActual = scanner.getNextToken();
			valor += tActual.get_lexema();
			
			// Si dice que es un natural, actualizamos informacion a entero
			if (tActual.getCatLexica() == CategoriaLexica.LitNatural)
				catLex = CategoriaLexica.LitEntero;
			else
				catLex = tActual.getCatLexica();
		}
		else {
			// Sino, es cualquier otro tipo de valor
			valor = tActual.get_lexema();
			catLex = tActual.getCatLexica();
		}
				
		CategoriaLexica tipoDevuelto = reconocerValor(catLex, tipoEsperado, valor);
		
		return new retValor(valor,tipoDevuelto);
		
	}
	
	
	// DECLARACIONES: TIPOS
	// *****************************************************************************************

	/**
	 * <dd>DecTipos (out errDecTipos) ::= 
	 * <dl>
	 * 		<dd>DecTipo (out errDecTipo) <b>;</b>
	 *    	<dd>RDecTipos (in errhRDecTipos ; out errRDecTipos)
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception si se ha producido un error sintáctico
	 */
	private boolean rDecTipos() throws Exception{
		
		// Reconocimiento y tratamiento de una declaracion
		boolean errRDecTipos_h = rDecTipo();

		// Reconocemos el ;
		tActual = scanner.getNextToken();		
		if (!reconocer(CategoriaLexica.PuntoComa,";")){ 
			throw new Exception(errorSintactico("Se esperaba ';' después de declaración de nuevo tipo"));
		}
		
		// Recursion para el resto de declaraciones (si existen)
		boolean errRDecTipos = rRDecTipos(errRDecTipos_h);
		
		return errRDecTipos;
	}

	/**
	 * <dd>RDecTipos (in errhRDecTipos ; out errRDecTipos) ::= 
	 * <dl>
	 * 		<dd>DecTipo (out errDecTipo) 
	 *    	<dd>RDecTipos (in errhRDecTipos ; out errRDecTipos)
	 * </dl>
	 * <dd>RDecTipos (in errhRDecTipos ; out errRDecTipos) ::= λ
	 * @param errRDecTipos0_h para indicar si se ha producido un error anteriormente
	 * @return booleano para indicar si se ha producido un error
	 * @throws Exception para indicar errores sintácticos
	 */
	private boolean rRDecTipos(boolean errRDecTipos0_h) throws Exception{
		
		boolean errDecTipo = false;
		boolean errRDecTipos1_h;
		boolean errRDecTipos0 = errRDecTipos0_h;
		
		// Comprobamos el siguiente token a ver si tiene "tipo" o "}"
		tActual = scanner.checkNextToken();			
		// Reconocer "tipo"
		// ----------------------------------------------
		if (reconocer(CategoriaLexica.PalabraReservada,"tipo")) {			
			// Vamos a procesar una declaracion de tipo
			errDecTipo = rDecTipo();
				
			// Reconocemos el ;
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.PuntoComa,";")){
				throw new Exception(errorSintactico("Se esperaba ';' después de declaración de nuevo tipo "));
			}
			
			// Probamos a ver si hay mas declaraciones (Volver a llamar a rRDecTiposs()
			errRDecTipos1_h = errDecTipo || errRDecTipos0_h;
			errRDecTipos0 = rRDecTipos(errRDecTipos1_h);
			
		}
		//  Si lo siguiente no es una declaracion de tipo, estaremos en lambda
		return errRDecTipos0;		
	}
	
	/**
	 * <dd>DecTipo (out errDecTipo) ::= 
	 * <dl>
	 * 		<dd><b>tipo</b> DTipo (out errDTipo, exprTipo) iden (out lexIden)<b> ;</b>
	 * </dl>
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception para indicar errores sintácticos
	 */
	private boolean rDecTipo() throws Exception{
		
		String idNuevoTipo;
		
		boolean existeID = true;
		boolean errDTipo = false;	
		
		// Reconocer "tipo"
		// ---------------------------------------------------
		tActual = scanner.getNextToken();
		if (reconocer(CategoriaLexica.PalabraReservada, "tipo")){
			
			// Reconocer una definicion de tipo DTipo
			// -------------------------------------------------------------
			ExpTipo expTipo = rDTipo();
				
			// Reconocer un identificador
			// -------------------------------------------------------------
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.Identificador))
				throw new Exception(errorSintactico("Se esperaba un identificador válido para el nuevo tipo"));
			
			idNuevoTipo = tActual.get_lexema();
							
			existeID = ts.existeID(idNuevoTipo, 0);
			
			/* Si se hace alusion a otro tipo (En expTipo hay una t:ref), comprobar que exista ese tipo, si no
			 * detener ejecución
			 */
			
			if (expTipo.getT().equals(TablaSimbolos.REF)){
				if (!ts.existeID(expTipo.getIdTipo(), 0))
					throw new Exception(errorSintactico("El tipo "+expTipo.getIdTipo()+" no ha sido declarado"));
			}
			
			if (existeID)
				añadirError(tActual.get_linea(),"El identificador '"+idNuevoTipo+"' ya ha sido declarado.");
			else {
				//Si no se han producido errores, entonces se añade a la TS					
				errDTipo = expTipo.getError();				
				Propiedades p = new Propiedades(-1, ClaseDec.Tipo, expTipo); 	// Propiedades(int d, ClaseDec c, int nivel, ExpTipo expTipo)
				
				// La llamada a existeID() esta implicita al anadirID()
				//existeID = !(ts.añadeID(idNuevoTipo, 0, p));	
				ts.añadeID(idNuevoTipo, 0, p);
			}					
		}
		else
			throw new Exception(errorSintactico("Se esperaba 'tipo' (declaracion de tipo) o '}' (Fin sección declaración de tipos) "));

		return existeID || errDTipo;		
	}
	
	/**
	 * <dd>DTipo (out errDTipo, exprTipo) ::= tipoBasico (out lexTipo)
	 * <dd>DTipo (out errDTipo, exprTipo) ::= 
	 * <dl>
	 *    	<dd>iden (out lexIden)
	 *    	<dd>RDTipo (in exprTipoh,errhRDTipo ; out errRDTipo, exprTipo)
	 * </dl>
	 * <dd>DTipo (out errDTipo, exprTipo) ::= 
	 * <dl>
	 * 		<dd><b>(</b> DTipos (out errDTipos, exprTipo1) <b>)</b>
	 *     	<dd>RDTipo (in exprTipoh, errhRDTipo ; out errRDTipo, exprTipo)
	 * </dl>
	 * @return boolean para indicar si se ha producido un error
	 * @throws Exception para indicar errores sintácticos
	 */
	private ExpTipo rDTipo() throws Exception{
		
		boolean errDTipo_h = false;
		
		// Creamos la expTipo, que luego se usara para anadir el valor a la TS
		ExpTipo expTipo;
		// expTipo tambien lleva informacion de los errores
		ExpTipo expTipo_h;
		
		// Chequeamos el siguiente token para ver el comienzo de DTipo: tipoBasico, iden, (tupla), [array]
		tActual = scanner.checkNextToken();		
		// Reconocer "tipoBasico"
		// ----------------------------------------------
		CategoriaLexica tipoBasico = reconocerTipo();
		if (tipoBasico != null) {
			
			// Avanzamos tokenizer
			tActual = scanner.getNextToken();
			
			// Obtenemos su ExpTipo
			expTipo_h = new ExpTipo(tActual.get_lexema(), ts); //String t, TablaSimbolos ts
			
			// "Comprobamos" restricciones contextuales
			errDTipo_h = false;
			expTipo_h.setError(errDTipo_h);
			
			// Se lo pasamos a RDTipo
			expTipo = rRDTipo(expTipo_h);			
		}
		
		// Reconocer tipo ya creado
		// ----------------------------------------------
		else if (reconocer(CategoriaLexica.Identificador)){ //DTipo ::= iden RDTipo
			
			// Avanzamos tokenizer
			tActual = scanner.getNextToken();
			
			// Obtenemos el identificador
			String id = tActual.get_lexema();
			
			// Obtenemos su ExpTipo
			expTipo_h = new ExpTipo(id, ts); // String t, TablaSimbolos ts
			
			// Comprobamos restricciones contextuales
			if (!ts.existeID(id, 0))
				throw new Exception(errorSintactico("El identificador '"+id+"' no existe o no es válido"));

			expTipo_h.setError(errDTipo_h);
			
			// Se lo pasamos a RDTipo (dentro de expTipo_h ya esta puesto si da error o no)
			expTipo = rRDTipo(expTipo_h);			
		} 
		
		// Reconocer Tupla (a,b,...,z)
		// ----------------------------------------------
		else if (reconocer(CategoriaLexica.AbreParentesis)){ //DTipo ::= (DTipos) RDTipo
			
			// Avanzamos tokenizer
			tActual = scanner.getNextToken();
			
			// Vamos DTipos
			expTipo_h = rDTipos();
						
			// Reconocer ')'
			// ----------------------------------------------
			tActual = scanner.getNextToken();
			
			if ((reconocer(CategoriaLexica.Identificador) && ts.existeID(tActual.get_lexema(), 0)) || 
				 reconocer(CategoriaLexica.PalabraReservada))
				throw new Exception(errorSintactico("Se esperaba ',' para separar los tipos de la tupla"));
			
			if (!reconocer(CategoriaLexica.CierraParentesis))
				throw new Exception(errorSintactico("Se esperaba ')' (Símbolo de fin de tupla)"));
	
			// Se lo pasamos a RDTipo
			expTipo = rRDTipo(expTipo_h);			
		}
		
		// Comprobar si es ')' -> (La tupla esta vacia)
		// ----------------------------------------------
		else
			throw new Exception(errorSintactico("Se esperaba un tipo basico, un identificador de tipo o '(' (inicio declaracion de tupla)"));
	
		return expTipo;
	}
	
	/**
	 * <dd>RDTipo(in exprTipoh, errhRDTipo ; out errRDTipo, exprTipo) ::= 
	 * <dl>
	 * 		<dd><b>[</b> iden (out lexIden) <b>]</b> 
	 *    	<dd>RDTipo(in exprTipoh, errhRDTipo ; out errRDTipo, exprTipo)
	 * </dl>
	 * <dd>RDTipo (in exprTipoh, errhRDTipo ; out errRDTipo, exprTipo) ::= 
	 * <dl>
	 * 		<dd><b>[</b>lit-natural (out lexTam)<b>]</b> 
	 *    	<dd>RDTipo (in exprTipoh, errhRDTipo ; out errRDTipo, exprTipo)
	 * </dl>
	 * <dd>RDTipo (in exprTipoh, errhRDTipo ; out errRDTipo, exprTipo) ::= λ
	 * @param expTipo_h para saber si se ha producido un error anteriormente
	 * @return boolean indicando si se ha producido error
	 * @throws Exception para indicar errores sintácticos
	 */
	private ExpTipo rRDTipo(ExpTipo expTipo_h) throws Exception{

		int numElems = -1;
		
		boolean errRDTipo = false;
		
		ExpTipo expTipo;
		
		// Avanzamos para ver si existe el comienzo de RDTipo: '['
		tActual = scanner.checkNextToken();
		
		// Reconocer Array
		// ----------------------------------------------
		if (reconocer(CategoriaLexica.AbreCorchete)){
			tActual = scanner.getNextToken();
			// Comprobamos si es un iden, o un valor (natural o entero positivo)
			tActual = scanner.getNextToken();
			// Reconocer identificador
			// ----------------------------------------------
			if (reconocer(CategoriaLexica.Identificador)){
				
				// Obtener lexema "identificador"
				String id = tActual.get_lexema();
				
				// Obtenemos la ExpTipo de la cte
				ExpTipo expTipoCte = ts.getExprTipo(id, 0);
				
				// Comprobamos que la constante existe
				if (!ts.existeID(id, 0)) 
					throw new Exception(errorSintactico("El identificador "+id+" no está declarado previamente como constante"));
				
				// Comprobamos que el tipo de la cte es el correcto
				if (!expTipoCte.getT().equals("natural") && !expTipoCte.getT().equals("integer"))
					throw new Exception(errorSintactico("Se esperaba un natural o un entero positivo para tamaño de array"));
				
				// Obtenemos el valor de la cte
				String valor = ((ExpTipoConstante) expTipoCte).getValor();
				
				// Comprobamos que es natural o entera
				if (expTipoCte.getT().equals("natural") || expTipoCte.getT().equals("integer"))
					numElems = Integer.parseInt(valor);
				else
					throw new Exception(errorSintactico("Se esperaba una constante natural o entera positiva para tamaño de array"));
				
				// Si fue un entero, comprobar que es positivo
				if (numElems < 1)	// 
					throw new Exception(errorSintactico("Se esperaba una constante entera positiva tamaño de array"));	
			}
			
			// Reconocer Valor Natural
			// ----------------------------------------------
			else if (reconocer(CategoriaLexica.LitNatural)) {
				numElems = Integer.parseInt(tActual.get_lexema());
				if (numElems < 1)
					throw new Exception(errorSintactico("Se esperaba valor natural para tamaño de array"));
			}
			else
				throw new Exception(errorSintactico("Se esperaba valor natural o constante natural/entera positiva para tamaño de array"));
			
			// Reconocer']'
			// ----------------------------------------------
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.CierraCorchete))
				throw new Exception(errorSintactico("Se esperaba ']' después de la definición de tamaño del array"));
			
			// Construimos ExpTipo (con su error) y se lo pasamos a la nueva llamada a RDTipo
			expTipo_h = new ExpTipoArray(numElems, ts, expTipo_h);
			// Anadir el error para subirlo junto a la ExpTipo
			expTipo_h.setError(errRDTipo);
			
			expTipo = rRDTipo(expTipo_h);		
		}
		// Si no reconocemos [, entonces estaremos en lambda
		// ----------------------------------------------
		else 
			expTipo = expTipo_h;
		
		return expTipo;
		
	}
	
	/**
	 * <dd>DTipos (out errDTipos, exprTipo) ::= 
	 * <dl>
	 * 		<dd>DTTipos (in errhDTTipos ; out errDTTipos, exprTipo, desp)
	 * </dl>
	 * <dd>DTipos (out errDTipos, exprTipo) ::= λ
	 * @return boolean indicando si se ha producido error
	 * @throws Exception para indicar errores sintácticos
	 */
	private ExpTipo rDTipos() throws Exception{
		ExpTipo expTiposTupla;
		
		// Comprobamos si la tupla esta vacia (Lambda)
		tActual = scanner.checkNextToken();
		if (reconocer(CategoriaLexica.CierraParentesis)) {			
			expTiposTupla = new ExpTipoTupla(new ArrayList<ElemTupla>(), ts);
		} else {			
			// Obtenemos la lista de tipos que compone la tupla
			ArrayList<ElemTupla> elems = rDTTipos(false);		
			expTiposTupla = new ExpTipoTupla(elems,ts);
		}
		
		// Lambda:
		// Seria la tupla vacia. Consideramos que en lugar de dejar vacio ExpTipo, se queda solo vacio el
		// arrayList q contiene los tipos. Se puede saber entonces si esta vacia con la funcion esTuplaVacia()
		
		return expTiposTupla;
	}
	
	/**
	 * <dd>DTTipos (in errhDTTipos ; out errDTTipos, exprTipo, desp) ::= 
	 * <dl>
	 * 		<dd>DTipo (out errDTipo1, exprTipo)
	 *    	<dd>RDTTipos (in exprTipoh, desph, errhRDTTipos ; out exprTipo, desp, errRDTTipos)
	 * </dl>
	 * @param errhDTTipos
	 * @return lista con los tipos que contiene la tupla
	 * @throws Exception para indicar errores sintácticos
	 */
	private ArrayList<ElemTupla> rDTTipos(boolean errhDTTipos) throws Exception{
		
		// Obtenemos la expresion de tipo del primer elemento (si es que hay)
		ExpTipo expTipo_elem0 = rDTipo();
		
		// Creamos el arrayList con los tipos de los elementos de la tupla que se ira heredando
		ArrayList<ElemTupla> elems = new ArrayList<ElemTupla>();
		
		// Si no es un elemento nulo, es decir, no se ha leido ')' en DTipo(), lo anadimos a la tupla
		if (expTipo_elem0 != null)
			elems.add(new ElemTupla(expTipo_elem0));	// ElemTupla(ExpTipo, desp)

		elems = rRDTTipos(elems);
		
		return elems;
	}
	
	/**
	 * <dd>RDTTipos (in exprTipoh, desph, errhRDTTipos ; out exprTipo, desp, errRDTTipos) := 
	 * <dl>
	 * 		<dd><b>,</b> DTipo (out errDTipo, exprTipo1)
	 *   	<dd>RDTTipos (in exprTipoh, desph, errhRDTTipos ; out exprTipo, desp, errRDTTipos)
	 * </dl>
	 * <dd>RDTTipos (in exprTipoh, desph, errhRDTTipos ; out exprTipo, desp, errRDTTipos) := λ
	 * @param elems_h la lista de los tipos que ya tiene la tupla
	 * @return lista de los tipos de la tupla si se ha añadido alguno más (si no es la vacía)
	 * @throws Exception para indicar errores sintácticos
	 */
	private ArrayList<ElemTupla> rRDTTipos(ArrayList<ElemTupla> elems_h) throws Exception{
		
		ArrayList<ElemTupla> elems = null;
		
		// Consultamos el siguiente token a ver si es ',' (mas elems en tupla) o ')' (fin de tupla)
		tActual = scanner.checkNextToken();
		
		// Reconocer ')' -> (La tupla esta vacia)
		// ----------------------------------------------
		if (reconocer(CategoriaLexica.Coma)){
			tActual = scanner.getNextToken();
					
			// Obtenemos la expresion de tipo del siguiente elemento (si es que hay)
			ExpTipo expTipo_elem = rDTipo();
			
			// Si no es un elemento nulo, es decir, no se ha leido ')' en DTipo() lo añdimos a la tupla
			if (expTipo_elem != null)
				elems_h.add(new ElemTupla( expTipo_elem));	// ElemTupla(ExpTipo, desp)
			
			// Le pasamos el arrayList con el nuevo elemento otra vez a RDTTipos a ver si 
			// hay mas elems y recogemos el array actualizado
			elems = rRDTTipos(elems_h);
		}
		else {
			// No avanzamos.
			elems = elems_h;
		}
		return elems;		
	}
	
	
	// DECLARACIONES: VARIABLES
	// *****************************************************************************************
	
	/**
	 * <dd>DecVars (in dirh ; out errDecVars, dir) ::= 
	 * <dl>
	 * 		<dd>DecVar (in dirh ; out errDecVar, dir1)
	 *    	<dd>RDecVars (in dirh, errhRDecVars ; out errRDecVars, dir)
	 * </dl>
	 * @param dir_h indica en que posición de memoria debe ir la siguiente variable
	 * @return retVar en el que se guarda la dirección después de tratar todas las variables y si se ha producido error
	 * @throws Exception si se ha producido un error sintáctico
	 */
	private retVar rDecVars(int dir_h) throws Exception {

		// Reconocimiento y tratamiento de una declaracion
		retVar devolucion = rDecVar(dir_h);	
		
		// Reconocemos el ;
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.PuntoComa,";")){
			throw new Exception(errorSintactico("Se esperaba ';' después de declaración de variable "));
		}
		
		// Recursion para el resto de declaraciones (si existen)
		devolucion = rRDecVars(devolucion.dir, devolucion.err);	
		
		return devolucion;								
	}
	
	/**
	 * <dd>RDecVars (in dirh, errhRDecVars ; out errRDecVars, dir) ::= 
	 * <dl>
	 *    	<dd>DecVar (in dirh ; out errDecVar , dir1)
	 *    	<dd>RDecVars (in dirh, errhRDecVars ; out errRDecVars, dir)
	 * </dl>
	 * <dd>RDecVars (in dirh, errhRDecVars ; out errRDecVars, dir) ::= λ
	 * @param dir_h indica en que posición de memoria debe ir la siguiente variable
	 * @param errRDecVars0_h indica si ya se produjo error anteriormente
	 * @return retVar en el que se guarda la dirección después de tratar todas las variables y si se ha producido error
	 * @throws Exception si se ha producido un error sintáctico
	 */
	private retVar rRDecVars(int dir_h, boolean errRDecVars0_h) throws Exception {
		
		retVar devolucion;
		boolean errRDecVars1_h;
		
		// Comprobamos el siguiente token a ver si tiene "var"
		tActual = scanner.checkNextToken();	
		// ----------------------------------------------
		if (reconocer(CategoriaLexica.PalabraReservada, "var")){
			
			// Si el siguiente token es "var", vamos a procesar una declaracion de variable
			devolucion = rDecVar(dir_h);
			
			// Reconocemos el ;
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.PuntoComa,";")){
				throw new Exception(errorSintactico("Se esperaba ';' después de declaración de variable"));
			}
						
			errRDecVars1_h = devolucion.err || errRDecVars0_h;
			// Probamos a ver si hay mas declaraciones (Volver a llamar a rRDecVars())
			devolucion = rRDecVars(devolucion.dir, errRDecVars1_h);
			
		} else {
			// Si no reconocemos "var" entonces podemos estar en lambda
			devolucion = new retVar(errRDecVars0_h, dir_h);
		}
	
		return devolucion;			
	}
	
	/**
	 * <dd>DecVar (in dirh ; out dir, errDecVar) ::= 
	 * <dl>
	 * 		<dd><b>var</b> DTipo (out errDTipo, exprTipo) iden (out lexIden)<b> ;</b>
	 * </dl>
	 * @param dir_h indica en que posición de memoria debe ir la siguiente variable
	 * @return en el que se guarda la dirección después de tratar todas las variables y si se ha producido error
	 * @throws Exception si se ha producido un error sintáctico 
	 */
	private retVar rDecVar(int dir_h) throws Exception {
		
		String idVar;
		ExpTipo expTipo;
		
		// Llamadas implicitas
		boolean existeID = false;
		boolean errDTipo = false;
		
		// Avanzamos y leemos el siguiente token
		tActual = scanner.getNextToken();
		
		// Reconocer "var"
		// ----------------------------------------------
		if (reconocer(CategoriaLexica.PalabraReservada,"var")){
			
			// Reconocer una definicion de tipo DTipo
			// -------------------------------------------------------------
			expTipo = rDTipo();
			
			// Reconocer "identificador"
			// ----------------------------------------------
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.Identificador))
				throw new Exception(errorSintactico("Se esperaba identificador de variable válido"));
			
			idVar = tActual.get_lexema();	// Propiedad identificador de variable
			
			if (ts.existeID(idVar,nivelH)) {
				añadirError(tActual.get_linea(),"El identificador '"+idVar+"' ya ha sido declarado.");
				existeID = true;
			} else {
				
				//Si no se han producido errores, entonces se anade a la TS	
				// Sumamos 1 a dirh para coger la siguiente direccion libre
				dir_h = dir_h + 1;
				errDTipo = expTipo.getError();
								
				Propiedades p = new Propiedades(dir_h, ClaseDec.Variable, expTipo); 	// Propiedades(int d, ClaseDec c, int nivel, ExpTipo expTipo)
				
				ts.añadeID(idVar, nivelH, p);		
			}			
		}
		else 
			throw new Exception(errorSintactico("Se esperaba 'var' (declaracion de variable) o '}' (Fin sección declaración de variables)"));

		return new retVar(existeID || errDTipo, dir_h + expTipo.getTam() - 1);
	}
	
	
	// DECLARACIONES: SUBPROGRAMAS
	// *****************************************************************************************
	
	/**
	 * <dd>DecSubprogs (errDecSubprogs) ::= 
	 * <dl>
	 * 		<dd>DecSubprog (out errDecSubprog)
	 *    	<dd>RDecSubprogs (in errhRDecSubprogs ; out errRDecSubprogs)
	 * </dl>
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception si se ha producido un error sintáctico 
	 */
	private boolean rDecSubProgs() throws Exception {
	
		// Reconocimiento y tratamiento de un subprograma
		boolean errDecSubProgs = rDecSubProg();	
		
		// Recursion para el resto de subprogramas (si existen)
		errDecSubProgs = rRDecSubprogs(errDecSubProgs);	
		
		return errDecSubProgs;								
	}
	
	/**
	 * <dd>RDecSubprogs (errhRDecSubprogs ; out errRDecSubprogs) ::= 
	 * <dl>
	 * 		<dd>DecSubprog (out errDecSubprog)
	 *    	<dd>RDecSubprogs (errhRDecSubprogs ; out errRDecSubprogs)
	 * </dl>
	 * <dd>RDecSubprogs (errhRDecSubprogs ; out errRDecSubprogs) ::= λ
	 * @param errhDecSubProgs para saber si se ha producido algún error anteriormente
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception si se ha producido un error sintáctico 
	 */
	private boolean rRDecSubprogs(boolean errhDecSubProgs) throws Exception {
		
		boolean errRSubprogs1_h;
		boolean errRDecSubprogs = errhDecSubProgs;
		
		// Comprobamos el siguiente token a ver si tiene "subprogram:"
		tActual = scanner.checkNextToken();	
		// ----------------------------------------------
		if (reconocer(CategoriaLexica.PalabraReservada, "subprogram:")){
			
			// Si el siguiente token es "subprogram", vamos a procesar una declaracion de subprograma
			errRSubprogs1_h = rDecSubProg();
			
			errRSubprogs1_h = errRSubprogs1_h || errhDecSubProgs;
			// Probamos a ver si hay mas declaraciones (Volver a llamar a RDecSubprogs())
			errRDecSubprogs = rRDecSubprogs(errRSubprogs1_h);			
		} 
		// Si no hemos reconocido "subprogram:" puede que estemos en lambda
	
		return errRDecSubprogs;
	}
	
	/**
	 * <dd>DecSubprog (out errDecSubprog) ::= 
	 * <dl>
	 *    	<dd><b>subprogram:</b> iden (out lexIden)
	 *    	<dd>( PFs (out listaExprTipo, dir, errPFs) 
	 *    	<dd><b>{</b> CS (in dirh ; out errCS) <b>}</b>
	 * </dl>
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception si se ha producido un error sintáctico 
	 */
	private boolean rDecSubProg() throws Exception {
				
		String idSubprog;
		retPF devolucionPF;
		boolean errCs;
		
		// Llamadas implicitas
		boolean existeID = true;
		
		// Avanzamos y leemos el siguiente token
		tActual = scanner.getNextToken();
		
		// Reconocer "subprogram:"
		// ----------------------------------------------
		if (reconocer(CategoriaLexica.PalabraReservada,"subprogram:")){
			
			// GUARDAMOS LA TS GLOBAL
			TablaSimbolos tsAux = ts.clone();
			
			// Reconocer "identificador"
			// ----------------------------------------------
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.Identificador))
				throw new Exception(errorSintactico("Se esperaba identificador de subprograma válido"));
			
			idSubprog = tActual.get_lexema();	// Propiedad identificador de variable
			
			// Reconocer (
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.AbreParentesis))
				throw new Exception(errorSintactico("Se esperaba una apertura de paréntesis '('"));
			
			// Vamos a PFs
			devolucionPF = rPFs();	
					
			// Reconocer )
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.CierraParentesis))
				throw new Exception(errorSintactico("Se esperaba un cierre de paréntesis ')'"));
					
			// Se comprueba que no exista ya el procedimiento
			existeID = ts.existeID(idSubprog, 0);
			
			if (existeID)
				añadirError(tActual.get_linea(),"El identificador '"+idSubprog+"' ya ha sido declarado.");
			else {
				HashMap<String, Param> listaParam = new HashMap<String,Param>();
				// Preparamos la lista de parámetros segun la devolucion de la produccion de PF
				preparaListaparámetros(listaParam,devolucionPF);
				// Creamos la exptipo del procedimiento con la lista de parámetros y lo agregamos a la ts
				ExpTipo etProc = new ExpTipoProcedimiento(devolucionPF.listaParams,listaParam,etq,ts);				
				Propiedades pProc = new Propiedades(-1, ClaseDec.Procedimiento, etProc);
				ts.añadeID(idSubprog, 0, pProc);
				
				// El procedimiento lo guardamos en la global
				tsAux.añadeID(idSubprog, 0, pProc);
			}				
			
			// Reconocer {
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.AbreLlave))
				throw new Exception(errorSintactico("Se esperaba '{' apertura de ámbito del subprograma '"+idSubprog+"'"));
			
			// Vamos a CS
			errCs = rCS(devolucionPF.dir);
			
			// Reconocer }
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.CierraLlave))
				throw new Exception(errorSintactico("Se esperaba '}' cierre de ámbito de subprograma '"+idSubprog+"'"));
			
			// Mostramos tabla local en interfaz
			rellenaTSInterfazLocal(ts,idSubprog);
			
			// Recuperamos la TS Global que estaba almacenada en tsAux
			ts = tsAux;
			tOperaciones = new TablaOperaciones(tsAux);
		}
		else 
			throw new Exception(errorSintactico("Se esperaba 'subprogram:' (Inicio declaración subprograma) o '}' (Fin sección declaración de subprogramas)"));
		
		return existeID || devolucionPF.err || errCs;
	}
	
	/**
	 * Metodo que rellena la lista de parámetros de forma que quede indexado su ID, tambien
	 * incluyendo la ID en una lista para que mantenga su indexacion
	 * @param listaParam I/O: lista de los parámetros
	 * @param devolucionPF
	 */
	private void preparaListaparámetros(HashMap<String, Param> listaParam, retPF devolucionPF){
		// asociar el indice del parámetro a un ID -> mantiene el orden para calcular bien el desplazamiento
		/*
		 *  Se debe crear cada parámetro y añadirlo al HashMap indexado con su ID
		 *  Además, se debe introducir el ID en la lista de ID de parámetros para
		 *  que mantenga el mismo orden que en el proc
		 */
		Param p; // Auxiliar para crear los parámetros apropidados
		for (int i = 0; i < devolucionPF.listaParams.size(); i++ ){
			Modo m = Modo.Valor;
			ClaseDec d = ts.getClase(devolucionPF.listaParams.get(i), 1);
			
			if (d.equals(ClaseDec.ParamVar))
				m = Modo.Variable;
			// Creamos el parámetro, el cual tiene ademas el modo, (el id tambien lo tenia para cuando hacia pruebas, se puede eliminar)
			p = new Param((String) devolucionPF.listaParams.get(i),m,(ExpTipo) devolucionPF.exprTipo.get(i));
			// aniadimos el parámetro con su id a la lista de parámetros indexados
			listaParam.put((String) devolucionPF.listaParams.get(i), p);
		}
	}
		
	// PARAMETROS FORMALES
	// *****************************************************************************************
	
	/**
	 * <dd>PFs (out listaExprTipo, dir, errPFs) ::= PFFs (in dirh ; out listaExprTipo, dir, errPFFs)
	 * <dd>PFs (out listaExprTipo, dir, errPFs) ::= λ
	 * @return parámetros y si se ha producido algún error
	 * @throws Exception si se ha producido un error sintáctico 
	 */
	private retPF rPFs() throws Exception {
		int dirh = -1;
		// Si lo siguiente es un cierre de parentesis, entonces estamos en lambda
		tActual = scanner.checkNextToken();
		if (!reconocer(CategoriaLexica.CierraParentesis))
			return rPFFs(dirh);
		else 
			return new retPF(false, -1, new ArrayList<ExpTipo>()); // A null no puede estar porque las comprobaciones dan una excepcion de null
	}
	
	/**
	 * <dd>PFFs (in dirh ; out listaExprTipo, dir, errPFFs) ::= 
	 * <dl>
	 * 		<dd>PF (in dirh, listaParamsh ; out dir, listaExprTipo1, errPF, listaParams)
	 *    	<dd>RPFFs (in dirh, errhRPFFs, listaExprTipoh, listaParamsh ; out dir, listaExprTipo, errRPFFs)
	 * </dl>
	 * @param dirh para saber en qué dirección continúan los parámetros
	 * @return parámetros y si se ha producido algún error
	 * @throws Exception si se ha producido un error sintáctico
	 */
	private retPF rPFFs(int dirh) throws Exception {		
		ArrayList<String> listaParamsh = new ArrayList<String>();
		// Vamos a PF
		retPF devolucion = rPF(dirh, listaParamsh);
		// Vamos a RPFFs		
		devolucion = rRPFFs(devolucion.dir, devolucion.err, devolucion.exprTipo, devolucion.listaParams);
		return devolucion;
	}
	
	/**
	 * <dd>RPFFs  (in dirh, errhRPFFs, listaExprTipoh, listaParamsh ; out dir, listaExprTipo, errRPFFs) ::= 
	 * <dl>
	 *    	<dd><b>,</b> PF (in dirh, listaParamsh ; out dir, exprTipo, errPF, listaParams)
	 *    	<dd>RPFFs (in dirh, errhRPFFs, listaExprTipoh, listaParamsh ; out dir, listaExprTipo, errRPFFs)
	 * </dl>
	 * <dd>RPFFs (in dirh, errhRPFFs, listaExprTipoh, listaParamsh ; out dir, listaExprTipo, errRPFFs) ::= λ
	 * @param dirh para saber en qué dirección continúan los parámetros
	 * @param errhRPFFs para saber si se ha producido algún error anteriormente
	 * @param exprTipo lista de expresiones de tipo de los parámetros
	 * @param listaParamsh lista de parámetros ya procesados
	 * @return devuelve la lista de parámetros y si se ha producido algún error
	 * @throws Exception si se ha producido un error sintáctico
	 */
	private retPF rRPFFs(int dirh, boolean errhRPFFs, ArrayList<ExpTipo> exprTipo, ArrayList<String> listaParamsh) throws Exception {
		retPF devolucion = new retPF(errhRPFFs, dirh, exprTipo);
		devolucion.listaParams = listaParamsh;
		// Reconocemos la ,
		tActual = scanner.checkNextToken();
		if (reconocer(CategoriaLexica.Coma)) {
			tActual = scanner.getNextToken();			
			// Vamos a PF
			devolucion = rPF(dirh, listaParamsh);
			// Damos valor a los atributos heredados
			boolean errhRPFFs1 = devolucion.err || errhRPFFs;
			// Añadimos a la lista la exp de tipo
			exprTipo.addAll(devolucion.exprTipo);
			
			// Vamos a RPFFs
			devolucion = rRPFFs(devolucion.dir, errhRPFFs1, exprTipo, devolucion.listaParams);
		} 
		// Si no se reconoce una , podemos estar en lambda			
		return devolucion;				
	}
	
	/**
	 * <dd>PF (in dirh, listaParamsh ; out dir, exprTipo, errPF, listaParams) ::= DTipo (out errDTipo, exprTipo)
	 * @param dirh para saber en qué dirección continúan los parámetros
	 * @param listParamsh lista de parámetros ya procesados
	 * @return devuelve la lista de parámetros y si se ha producido algún error
	 * @throws Exception si se ha producido un error sintáctico
	 */
	private retPF rPF(int dirh, ArrayList<String> listParamsh) throws Exception {
		retPF devolucion;
		// Reconocemos DTipo
		ExpTipo exprTipo = rDTipo();	
		// Vamos a FPF
		devolucion = rFPF(dirh, exprTipo.getError(), exprTipo, listParamsh);
		devolucion.exprTipo.add(exprTipo);
		return devolucion;
	}
	
	/**
	 * <dd>FPF (in dirh, in errhFPF, listaParams, exprTipoh ; out dir, errFPF) ::= iden (out lexIden)
	 * <dd>FPF (in dirh, in errhFPF, listaParams, exprTipoh ; out dir, errFPF) ::= <b>*</b> iden (out lexIden)
	 * @param dirh para saber en qué dirección continúan los parámetros
	 * @param errhFPF si se ha producido un error anteriormente
	 * @param exprTipo lista de expresiones de tipo de los parámetros
	 * @param listaParams lista de parámetros ya procesados
	 * @return devuelve la lista de parámetros y si se ha producido algún error
	 * @throws Exception
	 */
	private retPF rFPF(int dirh, boolean errhFPF, ExpTipo exprTipo, ArrayList<String> listaParams) throws Exception {
		
		String idParam;
		boolean paramPorVariable = false;
		boolean errorTipo = false;
		retPF devolucion = new retPF(errhFPF, dirh, new ArrayList<ExpTipo>());
		devolucion.listaParams = listaParams;
		Propiedades p;
		
		// Comprobamos que haya o no un *, si estamos en un parámetro por var
		tActual = scanner.checkNextToken();
		if (reconocer(CategoriaLexica.Por)) {
			// Consumimos el *
			tActual = scanner.getNextToken();
			// Entonces es un parámetro por variable
			paramPorVariable = true;
		}
		
		// Leemos un iden
		tActual = scanner.getNextToken();
		// Comprobamos que no es el iden de un tipo
		if (ts.existeID(tActual.get_lexema(), 0)){
			if (ts.getClase(tActual.get_lexema(), 0).equals(ClaseDec.Tipo)){
				añadirError(tActual.get_linea(),"El nombre del identificador debe ser distinto a un nombre de tipo");
				errorTipo = true;
			}
		}
		// Comprobamos que no este repetido
		boolean repetido = false;
		idParam = tActual.get_lexema();
		for (String idens: listaParams) {
			if (idens.equals(idParam)) {
				repetido = true;
				break;
			}
		}
		// Si no esta repetido lo añadimos, si lo esta ponemos el error
		if (!repetido) {
			// Añadimos el iden a la lista de parámetros usados
			devolucion.listaParams.add(idParam);
			// Asignamos los valores bien
			if (paramPorVariable) {
				dirh = dirh + 1;
				p = new Propiedades(dirh, ClaseDec.ParamVar, exprTipo); 
				devolucion.dir = dirh;
			} else {
				dirh = dirh + 1;
				p = new Propiedades(dirh, ClaseDec.Variable, exprTipo); 
				devolucion.dir = dirh + exprTipo.getTam() - 1;
			}
			// Añadimos en la ts
			ts.añadeID(idParam, 1, p);
		} else {
			añadirError(tActual.get_linea(),"El parámetro '"+idParam+"' ya ha sido declarado");
		}
		// Ponemos el valor del error
		devolucion.err = errhFPF || repetido || errorTipo;
		
		return devolucion;
	}
	
	// CUERPO SUBPROGRAMA
	// *****************************************************************************************
	
	/**
	 * <dd>CS (in dirh ; out errCS) ::= 
	 * <dl>
	 * 		<dd>SecVars (in dirh ; out errVars, tamVars)
	 * </dl>
	 * @param dirh para saber la dirección de comienzo de las variables del cuerpo del subprograma
	 * @return boolean para indicar si se ha producido error
	 * @throws Exception si se ha producido un error sintáctico
	 */
	private boolean rCS (int dirh) throws Exception {
		boolean errIs = false;
		boolean errCs = false;
		retVar devolucion;
		
		// Vamos a secVars
		devolucion = rSecVars(dirh);
		
		// Emite cod prologo
		cod.codPrologo(devolucion.dir + 1);
		// Avanzamos etq longPrologo
		etq = etq + longPrologo;
		
		// Procesamos la palabra instructions
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.PalabraReservada, "instructions"))
			throw new Exception(errorSintactico("Se esperaba la palabra 'vars' (opcional) o 'instructions' (obligatoria)"));
		
		// Leemos {
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.AbreLlave))
			throw new Exception(errorSintactico("Se esperaba '{' apertura de ámbito de instrucciones de subprograma"));
		
		// Vamos a Is
		errIs = rIs();
		
		// Leemos }
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.CierraLlave))
			throw new Exception(errorSintactico("Se esperaba '}' cierre de ámbito de instrucciones de subprograma"));
		
		// Emite cod epilogo
		cod.codEpilogo(devolucion.dir + 1);
		etq = etq + longEpilogo;
		
		// Devolver el error
		errCs = errIs || devolucion.err;
		return errCs;
	}
	
	
	// INSTRUCCIONES
	// *****************************************************************************************
	/**
	 * <dd>Is (out errIs) ::=
	 * <dl>  
	 * 		<dd>I (out errI) <b>;</b>
	 * 		<dd>RIs (in errhRIS ; out errRIs)
	 * </dl>
	 * @return boolean para saber si hay error
	 * @throws Exception para indicar un error sintáctico
	 */
	private boolean rIs() throws Exception {		
		
		boolean errI = rI();	// I.err		

		boolean errIs;

		errIs = rRIs(errI);		// RIs.errh = I.err  => rRIS(errI)
		
		return errIs;			// Is.err = RIs.err
	}
	
	/**
	 * <dd>RIs (in errhRIS ; out errRIs) ::=
	 * <dl> 
	 *    	<dd>I (out errI) <b>;</b>
	 *    	<dd>RIs (in errhRIS ; out errRIs)
	 * </dl>
	 * <dd>RIs (in errhRIS, out errRIs) ::= λ
	 * @param errRIs_h para saber si se ha producido un error anteriormente
	 * @return boolean para saber si hay error
	 * @throws Exception para indicar un error sintáctico
	 */
	private boolean rRIs(boolean errRIs_h) throws Exception{
		
		boolean errI = false;
		boolean errRIs = false;								
		
		//Comprobamos el token de forma local sin mover al siguiente
		tActual = scanner.checkNextToken();		
		
		if (!reconocer(CategoriaLexica.CierraLlave) &&
			!reconocer(CategoriaLexica.PalabraReservada,"endwhile") &&
			!reconocer(CategoriaLexica.PalabraReservada,"else") &&
			!reconocer(CategoriaLexica.PalabraReservada,"endif")){ // Si hay mas instrucciones
			
			// Vamos a I
			errI = rI();				

			// Vamos a RIs
			errRIs = rRIs(errI);
			
			errRIs = errRIs_h || errRIs;
					
		} else {	// Lambda
			//Se acaban las instrucciones y volvemos, ademas subimos el error.			
			errRIs = errRIs_h;
		}		
		return errRIs;		
	}
	
	/**
	 * <dd>I (out errI) ::= IAsig (out errI)
	 * <dd>I (out errI) ::= ILect (out errI)
	 * <dd>I (out errI) ::= IEsc (out errI)
	 * <dd>I (out errI) ::= swap1() <b>;</b>
	 * <dd>I (out errI) ::= swap2() <b>;</b>
	 * <dd>I (out errI) ::= If (out errI)
	 * <dd>I (out errI) ::= IWhile (out errI)  
	 * <dd>I (out errI) ::= ICall (out errI)
	 * @return boolean que indica si se ha producido algún error
	 * @throws Exception si se ha producido un error sintáctico
	 */
	private boolean rI() throws Exception
	{
		boolean errI = false;
		tActual = scanner.checkNextToken(); // Comprobamos el siguiente token sin avanzar
		boolean reconocerPuntoComa = true;
		
		// Reconocer tipo de instruccion
		if (reconocer(CategoriaLexica.PalabraReservada, "in"))
		{
			errI = rILect();
		}
		else if (reconocer(CategoriaLexica.PalabraReservada, "out"))
		{
				errI = rIEscrit();
		}
		else if (reconocer(CategoriaLexica.Identificador))
		{
				errI = rIAsig();
		}
		else if (reconocer(CategoriaLexica.PalabraReservada, "swap1"))
		{
				reconocerSwap(1);
				etq = etq + 1;		
		}
		else if (reconocer(CategoriaLexica.PalabraReservada, "swap2"))
		{
				reconocerSwap(2);
				etq = etq + 1;
		}
		else if (reconocer(CategoriaLexica.PalabraReservada, "if"))
		{
				errI = rIIf();	
				reconocerPuntoComa = false;
		}
		else if (reconocer(CategoriaLexica.PalabraReservada, "while"))		
		{
				errI = rIWhile();	
				reconocerPuntoComa = false;
		}
		else if (reconocer(CategoriaLexica.PalabraReservada, "call"))		
		{
				errI = rICall();
		}
		else //si no es ninguna de las anteriores opciones, se produce un error sintactico.
			throw new Exception(errorSintactico("Se esperaba una instruccion válida"));			
		
		// Reconocer ";"
		// ----------------------------------------------
		if (reconocerPuntoComa){
			// Reconocer ";"
			// ----------------------------------------------
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.PuntoComa))
				throw new Exception(errorSintactico("Se esperaba operador válido (después de identificador o valor) o ';' al final de la instrucción"));		
		}
		
		return errI;

	}
	
	/**
	 * <dd>DesigTipo (out tipo, errDesigTipo) ::=
	 * <dl>
	 * 		<dd>iden (out lexIden)
	 *     	<dd>RDesigTipo (in errhRDesigTipo, tipoh ; out errRDesigTipo, tipo)
	 * </dl>
	 * @return ExpTipo asociado al designador de tipo
	 * @throws Exception si se ha producido un error sintáctico
	 */
	private ExpTipo rDesigTipo() throws Exception {
		desig = true;
		String idenLex;
		boolean errhRDesigTipo = false;
		ExpTipo tipo = new ExpTipo("vacio", ts);
		int nivel = 0;
		ArrayList<String> a1 = new ArrayList<String>();
		
		// Reconocer "identificador"
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.Identificador))
			throw new Exception(errorSintactico("El identificador "+ tActual.get_lexema() + "no es válido o no se ha declarado"));
		
		idenLex = tActual.get_lexema();
		
		// Comprobamos el nivel del identificador		
		if (ts.getNivel(idenLex) > 0) {
			nivel = 1;
			
			//emite codigo
			a1.add("1");
			cod.emite(InstruccionesPila.apila_dir, a1);
			a1.clear();
			
			a1.add("natural");
			a1.add(Integer.toString(ts.getDir(idenLex, nivel)));
			cod.emite(InstruccionesPila.apila, a1);
			
			cod.emite(InstruccionesPila.suma, null);
			
			if (ts.getClase(idenLex, 1) == ClaseDec.ParamVar) {
				// emite codigo
				cod.emite(InstruccionesPila.apila_ind, null);				
				etq = etq + 4;
				} 
			else 
				etq = etq + 3;
			
			} 
		else {		
			// Apilamos la direccion si NO es una constante			
			if (ts.getClase(idenLex, nivel) != ClaseDec.Constante){
				nivel = 0;
				
				//emite codigo
				a1.clear();
				a1.add("natural");
				a1.add(Integer.toString(ts.getDir(idenLex, nivel)));
				cod.emite(InstruccionesPila.apila, a1);
				etq = etq + 1;
			}
		}
		
		// Ponemos el error
		if (!ts.existeID(idenLex, nivel))
			throw new Exception(errorSintactico("El identificador '"+ idenLex +"' no existe o no es válido"));	
		
		if (ts.existeID(idenLex, nivel)){
			if (ts.getClase(idenLex, nivel).equals(ClaseDec.Tipo)){
				tipo = new ExpTipoError();
				return tipo;
			}
		}
		
		if (!(ts.getClase(idenLex, nivel).equals(ClaseDec.ParamVar) || ts.getClase(idenLex, nivel).equals(ClaseDec.Variable)))
			 errhRDesigTipo = true;
		
		// Buscamos su referencia
		if (ts.existeID(idenLex, nivel)) {
			tipo = ts.ref(ts.getExprTipo(idenLex, nivel));
		} else {
			añadirError(tActual.get_linea(),"El tipo '"+idenLex+"' no existe.");
		}
		
		// Vamos a RDesigTipo
		tipo = rRDesigTipo(errhRDesigTipo, tipo);		
		
		// Comprobamos si el designador completo es efectivamente un designador (NO una constante)
		if (ts.getClase(idenLex, nivel) == ClaseDec.Constante){
			desig = false;
		} else {
			desig = true;
		}
		
		return tipo;
	}
	
	/**
	 * <dd>RDesigTipo (in errhRDesigTipo, tipoh ; out errRDesigTipo, tipo) ::= 
	 * <dl>
	 *    <dd>[Exp0 (out tipo)]
	 *    <dd>RDesigTipo (in errhRDesigTipo, tipoh ; out errRDesigTipo, tipo)
	 * </dl>
	 * <dd>RDesigTipo (in errhRDesigTipo, tipoh ; outerrRDesigTipo, tipo) ::=
	 * <dl> 
	 *    <dd>_ valor (out lexValor, tipoValor)
	 *    <dd>RDesigTipo (in errhRDesigTipo, tipoh ; out errRDesigTipo, tipo)
	 * </dl>
	 * <dd>RDesigTipo (in errhRDesigTipo, tipoh ; out errRDesigTipo, tipo) ::= λ
	 * @return ExpTipo Expresión de tipo asociado a la devolución del designador de tipo
	 * @throws Exception si se ha producido un error sintáctico
	 */
	private ExpTipo rRDesigTipo(boolean errhRDesigTipo, ExpTipo tipoh) throws Exception {
		
		ExpTipo tipo = null;;
		String lexVal;
		boolean err = false;
		ArrayList<String> a1 = new ArrayList<String>();
		
		// Chequeamos el token (necesitamos un [ o _ )		
		tActual = scanner.checkNextToken();
		
		// Desig Arrays
		if (reconocer(CategoriaLexica.AbreCorchete)) {
			// Consumimos el corchete
			tActual = scanner.getNextToken();
			// Ejecutamos Exp0
			tipo = rExp0();	
			
			// Comprobamos el error

			errhRDesigTipo = (!(tipo.getT().equals("integer") || tipo.getT().equals("natural")) || errhRDesigTipo || !tipoh.getT().equals("array"));
			
			if (!errhRDesigTipo) {
				// emite codigo
				a1.add("natural");
				a1.add(Integer.toString(((ExpTipoArray) tipoh).getTBase().getTam()));
				cod.emite(InstruccionesPila.apila, a1);				
				cod.emite(InstruccionesPila.multiplica, null);
				cod.emite(InstruccionesPila.suma, null);
				
				etq = etq + 3;						
			}
			else{
				añadirError(tActual.get_linea(),"El valor para acceder a una posición del array no es válido.");
				err = true;
			}
			// Ponemos la referencia bien
			if (tipoh.getT().equals("array") && tipo.getT().equals("natural")) {
				tipo = ts.ref(((ExpTipoArray) tipoh).getTBase());
			}
			
			// Consumimos el corchete de cierre
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.CierraCorchete))
				throw new Exception(errorSintactico("Se esperaba ']' después de expresión de acceso a array"));
			
			// LLamamos a RDesigTipo
			tipo = rRDesigTipo(errhRDesigTipo, tipo);	
			
			if (err)
				tipo.setError(true);
			
		// Desig Tuplas	
		} else if (reconocer(CategoriaLexica.BarraBaja)) {
			// Consumimos la barra
			tActual = scanner.getNextToken();
			// Procesamos el valor
			tActual = scanner.getNextToken();
			
			lexVal = tActual.get_lexema();	// Propiedad identificador de variable
			
			// Comprobamos que el valor se un entero positivo 
			if (!(tActual.getCatLexica() == CategoriaLexica.LitNatural)) {
				añadirError(tActual.get_linea(),"El valor '"+lexVal+"' tiene que ser un entero positivo.");
				err = true;
			} 		
			
			
			// Comprobamos el error
			errhRDesigTipo = !(tActual.getCatLexica() == CategoriaLexica.LitNatural) || errhRDesigTipo || !tipoh.getT().equals("tupla");
			
			if(!errhRDesigTipo) {
				// emite codigo
				a1.add("natural");
				try{ 
					//TIENE QUE CALCULAR EL DESPLAZAMIENTO
					a1.add(Integer.toString(((ExpTipoTupla)tipoh).getDespElementoTupla(Integer.valueOf(lexVal))));
				} catch (Exception e){
					añadirError(tActual.get_linea(),"El acceso a posición de tupla está fuera de rango.");
					err = true;
				}
				
				if (!err){
				cod.emite(InstruccionesPila.apila, a1);
				cod.emite(InstruccionesPila.suma, null);
				etq = etq + 2;
				}
			}
			
			// Añadimos el tipo
			if (tipoh.getT().equals("tupla") && (tActual.getCatLexica() == CategoriaLexica.LitNatural)) {
				if (!err){
				tipo = ts.ref(((ExpTipoTupla)tipoh).getTipoElementoTupla(Integer.valueOf(lexVal)));	
				// Llamamos a RDesigTIpo
				tipo = rRDesigTipo(errhRDesigTipo, tipo);
				}

			} else {
				añadirError(tActual.get_linea(),"Error de tipos.");
			}
		
		// Si no es nada de lo anterior entonces estamos en lambda
		} else {
			tipo = tipoh;
			tipo.setError(tipoh.getError());
		}
		
		if (err)
			tipo = new ExpTipoError();

		
		return tipo;
	}
	
	/** 
	 * <dd>IAsig (out errI) ::= 
	 * <dl>
	 * 		<dd>DesigTipo (out tipoD, errDesigTipo) = 
	 * 		<dd>Exp0 (out tipoE) ;
	 * </dl>
	 * @return boolean que se ha producido algún error
	 * @throws Exception
	 */
	private boolean rIAsig() throws Exception {

		boolean errAsig = false;
		parh = false;
		ExpTipo tipoDesigTipo;
		ExpTipo tipoExp0;
		ArrayList<String> a1 = new ArrayList<String>();
		
		// Reconocer Designador de tipo
		// ----------------------------------------------
		//tActual = scanner.getNextToken();
		
		tipoDesigTipo = rDesigTipo();

		if (tipoDesigTipo.getT().equals("proc"))
			throw new Exception(errorSintactico("Se esperaba 'call' antes de identificador de subprograma"));
					
		// Reconocer "=" 
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.OpAsignVar))
			throw new Exception(errorSintactico("Se esperaba '=' despues de un designador"));
		
		// Reconocer "Exp0" 
		// ----------------------------------------------
		tipoExp0 = rExp0();
		lirv.addAll(lirf);
		cod.parchea(lirv, etq);   
		lirv.clear();
		lirf.clear();
		
		// Comprobamos si el tipo de la expresion es erroneo => si (tipo0 = terr)
		// ----------------------------------------------
		if (tipoExp0.getT().equals("err")){	
			// errAsig <- true;
			errAsig = true;
			añadirError(tActual.get_linea(), "El tipo de la expresión es erróneo.");
		}
		
		// Comprobamos que no haya error con el designador
		errAsig = tipoDesigTipo.getError();
		
		// Comprobamos el tipo de la expresion => (funcion compatibles)
		// ---------------------------------------------- 
		if (!ts.compatibles(tipoDesigTipo, tipoExp0)){
			// errAsig <- true;
			errAsig = true;
			añadirError(tActual.get_linea(), "El tipo de la expresión no coincide con el tipo del designador en la asignación.");
		}
		
		// si no
		// ----------------------------------------------
		if (!errAsig) //si no ha habido nigun error, generamos el codigo
		{
			// Avanzamos etq
			etq = etq + 1;

			// emite codigo 
			if (tipoDesigTipo.getT().equals("natural") || tipoDesigTipo.getT().equals("float") || tipoDesigTipo.getT().equals("character")
					|| tipoDesigTipo.getT().equals("boolean") || tipoDesigTipo.getT().equals("integer"))
				cod.emite(InstruccionesPila.desapila_ind, null);
			else {
				a1.clear();
				a1.add(Integer.toString(tipoDesigTipo.getTam()));
				cod.emite(InstruccionesPila.mueve, a1);
			}
		}
		return errAsig;
	}
	
	/**
	 * <dd>ILect (out errI) ::= 
	 * <dl>
	 * 		<dd>in ( DesigTipo (out tipo, errDesigTipo) ) ;
	 * </dl>
	 * @return boolean si se ha producido algún error
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private boolean rILect() throws Exception {
		
		boolean errILect = false;
		ExpTipo tipoDesigTipo;
		ArrayList<String> a1 = new ArrayList<String>();
		
		// Reconocido "in"
		// ----------------------------------------------
		scanner.getNextToken();
		if (!reconocer(CategoriaLexica.PalabraReservada, "in"))
			throw new Exception(errorSintactico("Se esperaba la palabra reservada 'in'"));
				
		// Reconocer "("
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.AbreParentesis))
			throw new Exception(errorSintactico("Se esperaba apertura de parentesis '(' despues de 'in'"));
		
		// Vamos a DesigTipo
		tipoDesigTipo = rDesigTipo();
			
		// Reconocer ")"
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.CierraParentesis))
			throw new Exception(errorSintactico("Se esperaba cierre de parentesis ')' despues de identificador"));
			
		// Comprobamos si el designador devuelve error (Si es algo que no sea variable dará error)
		// ----------------------------------------------
		errILect = tipoDesigTipo.getError();

		// sino
		// ----------------------------------------------
		if (!errILect)
		{
			// Avanzamos etq
			etq = etq + 1;
			
			// emite codigo
			a1.clear();
			a1.add(ts.ref(tipoDesigTipo).getT());	
			cod.emite(InstruccionesPila.in,a1);
		}		
		return errILect;
	}
	
	/**
	 * <dd>IEsc (out errI) ::= 
	 * <dl>
	 * 		<dd>out( Exp0 (out tipo) ) ;
	 * </dl>
	 * @return boolean para indicar que se ha producido algún error
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private boolean rIEscrit() throws Exception {
		
		boolean errIEscrit = false;
		ExpTipo tipoExp0;
		parh = false;
		
		// Reconocido "out"
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.PalabraReservada, "out"))
			throw new Exception(errorSintactico("Se esperaba la palabra reservada 'out'"));
		
		// Reconocer "("
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.AbreParentesis))
			throw new Exception(errorSintactico("Se esperaba apertura de paréntesis '(' después de 'in'"));
		
		// Reconocer "Exp0"
		// ----------------------------------------------
		tipoExp0 = rExp0();
		lirv.addAll(lirf);
		cod.parchea(lirv, etq);   
		lirv.clear();
		lirf.clear();
		// Sacamos su tipo basico (con ref)
		ExpTipo tipoBasico = ts.ref(tipoExp0);
		
		// Reconocer ")"
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.CierraParentesis))
			throw new Exception(errorSintactico("Se esperaba cierre de paréntesis ')' después de identificador"));
		
		// Comprobamos si el tipo de la expresion es erroneo => si (tipo0 = terr)
		// ----------------------------------------------
		if (tipoExp0.getT().equals("err")) {
			errIEscrit = true;
			añadirError(tActual.get_linea(), "La expresion tiene un tipo erróneo.");		
		}		
		// Comprobamos si el tipo de la expresion es basico
		// ----------------------------------------------		
		else if (!(tipoBasico.getT().equals("natural") || tipoBasico.getT().equals("integer") || tipoBasico.getT().equals("float") || 
				tipoBasico.getT().equals("character") || tipoBasico.getT().equals("boolean"))) {
			errIEscrit = true;			
			listaErr.add("Error en linea "+tActual.get_linea()+". La expresion no tiene un tipo básico");
		}
		else // sino
			// Avanzamos etq
			etq = etq + 1;
			// emite codigo
			cod.emite(InstruccionesPila.out,null);
				
		return errIEscrit;
	}
	
	/**
	 * <dd>IIf (out errI) ::= 
	 * <dl>
	 * 		<dd><b>if</b> Exp0 (out tipo)
	 * 		<dd><b>then</b> Is (out errIs)
	 * 		<dd>IElse (out errIElse) <b>endif</b>
	 * </dl>	  		
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private boolean rIIf() throws Exception{
		
		boolean errIIf = false;
		boolean errIs = false;
		boolean errIElse = false;
		boolean errCondicion = false;
		ExpTipo tipoExp0;
		ArrayList<Integer> lAuxf = new ArrayList<Integer>();
		ArrayList<Integer> lAux = new ArrayList<Integer>();
		ArrayList<String> a1 = new ArrayList<String>();
		parh = false;
		
		// Reconocemos 'if'
		tActual = scanner.getNextToken();		
		if (!reconocer(CategoriaLexica.PalabraReservada,"if")){
			throw new Exception(errorSintactico("Se esperaba la palabra reservada 'if' "));
		}
			
		// Vamos a Exp0
		// ----------------------------------------------
		tipoExp0 = rExp0();
		
		errCondicion = !ts.ref(tipoExp0).getT().equals("boolean");
		
		if (errCondicion)
			añadirError(tActual.get_linea(),"La expresión no tiene un tipo booleano");
		
		
		lirv.addAll(lirf);
		cod.parchea(lirv, etq);   
		lirv.clear();
		lirf.clear();
		// emite codigo
		a1.clear();
		a1.add("?");
		cod.emite(InstruccionesPila.ir_f, a1);
		// Guardamos la direccion para parchear
		lAuxf.add(etq);
		// Avanzamos etq
		etq = etq + 1;
		
		// Reconocemos 'then'
		tActual = scanner.getNextToken();		
		if (!reconocer(CategoriaLexica.PalabraReservada,"then")){
			throw new Exception(errorSintactico("Se esperaba la palabra reservada 'then' "));
		}
		
		// Vamos a Is
		// ----------------------------------------------
		errIs = rIs();
		
		// parcheamos
		
		
		lAux.clear();
		// emite codigo
		a1.clear();
		a1.add("?");
		cod.emite(InstruccionesPila.ir_a, a1);
		lAux.add(etq);
		// Avanzamos etq
		etq = etq + 1;
		
		cod.parchea(lAuxf, etq);
		errIElse = rIElse();
		
		// Reconocemos 'endif'
		tActual = scanner.getNextToken();		
		if (!reconocer(CategoriaLexica.PalabraReservada,"endif")) {
			throw new Exception(errorSintactico("Se esperaba la palabra reservada 'endif' "));
		}
		
		// parcheamos
		cod.parchea(lAux, etq);
		
		// Ponemos los errores
		errIIf = errIs || errIElse || errCondicion;	
		return errIIf;
	}
	
	/**
	 * <dd>IElse (out errIElse) ::= 
	 * <dl>
	 * 		<dd><b>else</b>
	 * 		<dd>Is (out errIs)
	 * </dl>
	 * <dd>	IElse (out errIElse) ::= λ	 
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private boolean rIElse() throws Exception{
		boolean errIs = false;
		tActual = scanner.checkNextToken();
		// Si lo siguiente no es "else" entonces es la produccion lambda
		if (reconocer(CategoriaLexica.PalabraReservada,"else")) {
			// Consumimos el "else"
			tActual = scanner.getNextToken();
			errIs = rIs();
		} 	
		return errIs;
	}
	
	/**
	 * <dd>IWhile (out errIWhile) ::= 
	 * <dl>
	 * 		<dd><b>while</b>
	 * 		<dd>Exp0 (out tipo)
	 * 		<dd><b>do</b> Is  (out errIs)
	 * 		<dd><b>endwhile;</b>
	 * </dl>	
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private boolean rIWhile() throws Exception{
		
		boolean errIs = false;
		boolean errIWhile = false;
		boolean errCondicion = false;
		ExpTipo tipoExp0;
		ArrayList<Integer> lAux = new ArrayList<Integer>();
		ArrayList<Integer> lAuxf = new ArrayList<Integer>();
		ArrayList<String> a1 = new ArrayList<String>();
		int etqAux;
		
		tActual = scanner.getNextToken();
		// Reconocemos el "while"
		if (!reconocer(CategoriaLexica.PalabraReservada,"while")) {
			throw new Exception(errorSintactico("Se esperaba la palabra reservada 'while' "));
		}
		
		parh = false;
		etqAux = etq;
		
		// Vamos a Exp0
		tipoExp0 = rExp0();
		
		//comprobacion Error
		errCondicion = !ts.ref(tipoExp0).getT().equals("boolean");
		
		if (errCondicion)
			añadirError(tActual.get_linea(),"La expresión no tiene un tipo booleano");
		
		lirv.addAll(lirf);
		cod.parchea(lirv, etq);
		lirv.clear();
		lirf.clear();
		
		// emite codigo
		a1.clear();
		a1.add("?");
		cod.emite(InstruccionesPila.ir_f, a1);
		
		// Guardamos la etq para parchear
		lAuxf.add(etq);
		// Avanzamos etq
		etq = etq + 1;
		
		tActual = scanner.getNextToken();
		// Reconocemos el "do"
		if (!reconocer(CategoriaLexica.PalabraReservada,"do"))
			throw new Exception(errorSintactico("Se esperaba la palabra reservada 'do' "));
		
		// Vamos a Is
		// ----------------------------------------------
		errIs = rIs();
		
		// Reconocer "endwhile"
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.PalabraReservada,"endwhile"))
			throw new Exception(errorSintactico("Se esperaba la palabra reservada 'endwhile'"));
		
		// Reconocer ";"
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.PuntoComa))
			throw new Exception(errorSintactico("Se esperaba ';' después de 'endwhile'"));	
		
		cod.parchea(lAux, etq);
		// emite codigo
		a1.clear();
		a1.add(""+etqAux);
		cod.emite(InstruccionesPila.ir_a, a1);
		// Avanzmos etq
		etq = etq + 1;
		cod.parchea(lAuxf, etq);

		// Ponemos los errores
		errIWhile = errIs || errCondicion;
		
		return errIWhile;
	}
			
	/**
	 * <dd>ICall (out errICall) ::= 
	 * <dl>
	 * 		<dd><b>call</b> iden (out lexIden)
	 * 	 	<dd>(FParams (in nombreSubprogh, listaParamsh ; out errFParams))
	 * </dl>
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private boolean rICall() throws Exception {
		
		boolean errICall = false;
		boolean errFParams = false;
		String idSubProg;
		ArrayList<String> listaParams;
		
		ArrayList<String> a1 = new ArrayList<String>();
		ArrayList<Integer> lAux = new ArrayList<Integer>();
		
		tActual = scanner.getNextToken();
		// Reconocemos el "call"
		if (!reconocer(CategoriaLexica.PalabraReservada,"call")) {
			throw new Exception(errorSintactico("Se esperaba la palabra reservada 'call' "));
		}
	
		// Reconocemos el identificador
		tActual = scanner.getNextToken();
		idSubProg = tActual.get_lexema();
		// Comprobamos que es un identificador
		if (reconocer(CategoriaLexica.Identificador)){
			// Ahora comprobamos que existe en la TS
			if (ts.existeID(idSubProg, 0)){
				//Si existe, hay que comprobar que sea un procedimiento
				if ( !ts.getClase(idSubProg, 0).equals(ClaseDec.Procedimiento))
					throw new Exception(errorSintactico("Se esperaba identificador de subprograma válido"));
			}
			else
				throw new Exception(errorSintactico("El subprograma con id: '"+ idSubProg+"' no ha sido declarado"));
		}
		else
			throw new Exception(errorSintactico("Se esperaba identificador de subprograma válido"));
		
		tActual = scanner.getNextToken();
		// Reconocemos el (
		if (!reconocer(CategoriaLexica.AbreParentesis)) {
			throw new Exception(errorSintactico("Se esperaba '(' (Inicio declaración de parámetros formales)"));
		}
		
		//emite codigo
		etq = etq + longRetorno;
		
		cod.retorno("?");
		lAux.add(etq-2); 
		// Avanzamos etq 
		//etq = etq +1;
		
		// Vamos FParams
		listaParams = new ArrayList<String>();
		errFParams = rFParams(idSubProg, listaParams);
		
		tActual = scanner.getNextToken();
		// Reconocemos el )
		if (!reconocer(CategoriaLexica.CierraParentesis)) {
			throw new Exception(errorSintactico("Se esperaba ')' (Fin paso de parámetros) "));
		}
		
		//  emite
		a1.clear();
		a1.add(Integer.toString(((ExpTipoProcedimiento)ts.getExprTipo(idSubProg, 0)).getParamsIni()));
		cod.emite(InstruccionesPila.ir_a, a1);
		etq++; 
		
		cod.parchea(lAux, etq);
		
		boolean lsParamOK = ts.listaParamsCompleta(listaParams,idSubProg);
		
		if (!lsParamOK)
			añadirError(tActual.get_linea(),"La llamada a '"+idSubProg+"' no es correcta. Comprobar parámetros.");
		
		// Ponemos correctamente el error
		errICall = !ts.existeID(idSubProg, 0) || !ts.getClase(idSubProg, 0).equals(ClaseDec.Procedimiento) || errFParams 
				|| !lsParamOK;
	
		return errICall;
	}
		
	/** 
	 * <dd>FParams (in nombreSubprogh, listaParamsh ; out errFParams) ::=  
	 * <dl>
	 * 		<dd>Params (in nombreSubprogh, listaParamsh ; out errParams) 
	 * </dl>
	 * <dd>FParams (in nombreSubprogh, listaParamsh ; out errFParams) ::= λ
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private boolean rFParams(String idSubProg, ArrayList<String> listaParams) throws Exception {
		
		boolean errParams = false;
		
		tActual = scanner.checkNextToken();
		// Si lo siguiente es un ')' entonces estamos en lambda
		if (!reconocer(CategoriaLexica.CierraParentesis))
			errParams = rParams(idSubProg, listaParams);

		return errParams;
	}
	
	/** 
	 * <dd>Params (in nombreSubprogh, listaParamsh ; out errParams) ::=	
	 * <dl>
	 * 		<dd>Param (in nombreSubprogh, listaParamsh ; out errParam, listaParams)
	 * 		<dd>RParams (in nombreSubprogh, errhRParams, listaParamsh ; out errRParams)
	 * </dl>
	 * @param idSubProg indica el nombre del subprograma
	 * @param listaParams indica la lista de parámetros
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private boolean rParams(String idSubProg, ArrayList<String> listaParams) throws Exception {
		
		retParam devolucion;		
		boolean errParams = false;
		
		// Vamos a Param
		devolucion = rParam(idSubProg, listaParams);
		
		// Vamos a RParams
		errParams = rRParams(idSubProg, listaParams, devolucion.err);
		
		return errParams;
	}

	/**
	 * <dd>RParams (in nombreSubprogh, errhRParams, listaParamsh ;out errRParams, listaParams) ::= 
	 * <dl>
	 * 		<dd><b>,</b> Param (in nombreSubprogh, listaParamsh ; out errParam, listaParams1)
	 * 		<dd>RParams (in nombreSubprogh, errhRParams, listaParamsh ; out errRParams)
	 * </dl>
	 * <dd> RParams (in nombreSubprogh, errhRParams, listaParamsh ;	out errRParams, listaParams) ::= λ
	 * @param idSubProg indica el nombre del subprograma
	 * @param listaParams indica la lista de parámetros
	 * @param errhRParams indica si se ha producido un error anteriormente
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private boolean rRParams(String idSubProg, ArrayList<String> listaParams, boolean errhRParams) throws Exception {
		
		retParam devolucion;		
		boolean errParams = errhRParams;
		
		tActual = scanner.checkNextToken();
		// Si lo siguiente es una ',' no entonces estamos en lambda
		if (reconocer(CategoriaLexica.Coma)) {
			// Consumimos ','
			tActual = scanner.getNextToken();
			
			// Vamos a Param
			devolucion = rParam(idSubProg, listaParams);
			
			// Ponemos los errores y la lista de parámetros
			errhRParams = errhRParams || devolucion.err;
			
			// Vamos a RParams
			errParams = rRParams(idSubProg, listaParams, errhRParams);
		}		
		return errParams;
	}
	
	/**	
	 * <dd>Param (in nombreSubprogh, listaParamsh ; out errParam, listaParams)::= 
	 * <dl>
	 * 		<dd><b>iden</b> (out lexIden) <b>=</b>
	 * 		<dd>Exp0 (out tipo)
	 * </dl>
	 * @param idSubProg indica el nombre del subprograma
	 * @param listaParams indica la lista de parámetros
	 * @return boolean para indicar si se ha producido algún error
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private retParam rParam(String idSubProg, ArrayList<String> listaParams) throws Exception {
		
		String idParam;
		ExpTipo tipoExp0;
		retParam devolucion = new retParam(false, listaParams);
		ArrayList<String> a1 = new ArrayList<String>();
		ExpTipoProcedimiento expSub = (ExpTipoProcedimiento)ts.getExprTipo(idSubProg, 0);
		
		// Errores a devolver
		boolean tipoErroneo = false;
		boolean existeParam = false;
		boolean compatibles = false;
		boolean valorEnPVar = false;
		boolean repetido = false;
		
		// Reconocer "identificador"
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.Identificador))
			throw new Exception(errorSintactico("Se esperaba identificador de parámetro válido"));

		idParam = tActual.get_lexema();

		// Reconocer "="
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.OpAsignVar))
			throw new Exception(errorSintactico("Se esperaba '=' (Instruccion asignacion de parámetro) despues de identificador"));
				
		//emite apila-dir(0)
		a1.clear();
		a1.add("0");
		cod.emite(InstruccionesPila.apila_dir, a1);
		//emite apila(natural,3)
		a1.clear();
		a1.add("natural");
		a1.add("3");
		cod.emite(InstruccionesPila.apila, a1);
		//emite suma
		cod.emite(InstruccionesPila.suma, null);
		
		a1.clear();
		a1.add("natural");
		
		//Comprobacion si existe param
		existeParam = ts.existeParam(idSubProg, idParam);
		
		if (!existeParam)
			añadirError(tActual.get_linea(),"El parámetro '" + idParam + "' no existe o es erróneo." );
		else {
			a1.add(Integer.toString(((ExpTipoProcedimiento)ts.getExprTipo(idSubProg,0)).getParamDesp(idParam)));
		    //emite apila(natural,getParam(ts, nombreSubprogh, lexIden).desp)
			cod.emite(InstruccionesPila.apila, a1);
		    //emite suma
			cod.emite(InstruccionesPila.suma, null);		
			
			/*if (expSub.getParamModo(idParam).equals(Modo.Variable)){
				cod.emite(InstruccionesPila.apila_ind, null);
				etq++;
			}*/
			
			if (expSub.getParamModo(idParam).equals(Modo.Variable))
				parh = true;
			else
				parh = false;
			
			// Vamos a Exp0
			tipoExp0 = rExp0();
			
			lirv.addAll(lirf);
			cod.parchea(lirv, etq);   
			lirv.clear();
			lirf.clear();
		
			if (expSub.getParamModo(idParam).equals(Modo.Valor)){
				if (tipoExp0.esTipoBasico())
					cod.emite(InstruccionesPila.desapila_ind, null);
				else {
					a1.clear();
					a1.add(""+tipoExp0.getTam());
					cod.emite(InstruccionesPila.mueve, a1);
				}
			}
			else if (expSub.getParamModo(idParam).equals(Modo.Variable))
				cod.emite(InstruccionesPila.desapila_ind, null);
				
			// Avanzamos etq
			etq = etq + 6;
			
			// Comprobamos que el parámetro no este repetido
			repetido = paramRepetido(listaParams,idParam);
			tipoErroneo = tipoExp0.getT().equals("err"); 
			compatibles = ts.compatibles(ts.getParam(0, idSubProg, idParam), tipoExp0);
			valorEnPVar = expSub.getParamModo(idParam).equals(Modo.Variable) && !desig; // Solo admite designadores, no valores
					
			if (tipoErroneo)
				añadirError(tActual.get_linea(), "La expresión del parámetro '" + idParam + "' tiene un tipo erróneo");
			if (repetido)
				añadirError(tActual.get_linea(), "El parámetro '" + idParam + "' ya ha sido previamente usado");
			if (!compatibles)
				añadirError(tActual.get_linea(),"El parámetro '" + idParam + "' y la expresion no son compatibles");
			if (valorEnPVar)
				añadirError(tActual.get_linea(), "El parámetro '" + idParam + "' está pasado por variable y no es un designador");
		}
		
		devolucion.err = tipoErroneo || !existeParam || !compatibles || valorEnPVar || repetido;
		
		// Si no tenemos error añadimos el identificador
		if (!devolucion.err) {
			devolucion.listaParams.add(idParam);
		}
		return devolucion;
	}	
	
	/**
	 * <dd>Exp0 (out tipo0) ::= 
	 * <dl>
	 * 		<dd>Exp1(out tipo1)
	 * 		<dd>FExp0 	(in tipo1; out tipoF0)
	 * </dl>
	 * @return Devuelve el tipo correspondiente a la expresión
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private ExpTipo rExp0() throws Exception {
		
		ExpTipo tipo1 = rExp1();					// Exp1 	(out tipo1) 
		
		ExpTipo tipoF0 = rFExp0(tipo1);				// FExp0 	(in tipo1; out tipoF0)
		return tipoF0;										// { tipo0 <- tipoF0 }		
	}
		
	/**
	 * <dd>FExp0 (in tipoF0h; out tipoF0) ::= 
	 * <dl>
	 * 		<dd>Op0(out op)
	 * 		<dd>Exp1 (out tipo1)
	 * </dl>
	 * <dd>FExp0 (in tipoF0h; out tipoF0) ::= λ
	 * 
	 * @param tipoF0_h El tipo heredado de Expr1
	 * @return Devuelve el tipo correspondiente a la expresión
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private ExpTipo rFExp0(ExpTipo tipoF0_h) throws Exception
	{
		ExpTipo tipoF0 = null;
		// Comprobamos si hay un operador Op0 (<, >, <=, >=, ==, !=)
		// ----------------------------------------------
		tActual = scanner.checkNextToken(); 
		
		if (reconocerOperador(0)){
			
			CategoriaLexica op = rOp0();			// Op0 		(out op)
			parh = false;
			ExpTipo tipo1 = rExp1();				// Exp1 	(out tipo1)
			
			// Comprobamos si el tipo de la expresion es erroneo => si (tipoF0h = terr)
			// ----------------------------------------------
			if (tipoF0_h.getT().equals("err"))	{
				// tipoF0 <- terr;
				tipoF0 = new ExpTipoError();			 
				añadirError(tActual.get_linea(),"La expresión izquierda tiene un tipo erróneo");
			}		
			
			// Comprobamos si el tipo de la expresion es erroneo => si (tipo1 = terr)
			// ----------------------------------------------
			if (tipo1.getT().equals("err"))	{
				// tipoF0 <- terr; 
				tipoF0 = new ExpTipoError();			
				añadirError(tActual.get_linea(),"La expresión derecha tiene un tipo erróneo");
			}
			
			// Comprobamos el tipo de la expresion => si (¬existeOperacion(op,tipoF0h,tipo1))
			// ----------------------------------------------
			CategoriaLexica catLex_tipoF0_h = tipoF0_h.getCategoriaLexica(ts);
			CategoriaLexica catLex_tipo1 = tipo1.getCategoriaLexica(ts);
			
			if (!tOperaciones.existeOperacion(op, null, catLex_tipoF0_h ,catLex_tipo1))  {
				// tipoF0 <- terr;
				tipoF0 = new ExpTipoError();
				añadirError(tActual.get_linea(),"No se pueden operar los tipos de las expresiones");
			}

			// sino
			// ----------------------------------------------
			if (!(tipoF0 instanceof ExpTipoError))
			{ 
				// tipoF0 <- tipoResultante(op,tipoF0h,tipo1)
				tipoF0 = tOperaciones.tipoEsperado(op,null, tipoF0_h, tipo1);			
				
				// si (tipoF0h != terr ^ tipo1 != terr)
				if (!(tipoF0_h instanceof ExpTipoError) && !(tipo1 instanceof ExpTipoError)){
					// Emitimos codigo
					emitirCodigoOperacion(op,null,false); 	// { emite(Op0.op,null) }
					etq = etq + 1;
				}
			}
			
			desig = false;			
		}
		else // FExp0 ::= Lambda
		{
			tipoF0 = tipoF0_h;		// { tipoF0 = tipoF0h }
		}
		
		return tipoF0;		
	}
	
	/**
	 * <dd>Op0 (out op) ::= <
	 * <dd>Op0 (out op) ::= >
	 * <dd>Op0 (out op) ::= <= 
	 * <dd>Op0 (out op) ::= >= 
	 * <dd>Op0 (out op) ::= == 
	 * <dd>Op0 (out op) ::= !=
	 * @return Devuelve el tipo de operación correspondiente
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private CategoriaLexica rOp0() throws Exception {
		tActual = scanner.getNextToken();	
		CategoriaLexica op;
		
		if (reconocer(CategoriaLexica.Menor))
			op = CategoriaLexica.Menor;
		else if (reconocer(CategoriaLexica.Mayor))
			op = CategoriaLexica.Mayor;
		else if (reconocer(CategoriaLexica.MenorIgual))
			op = CategoriaLexica.MenorIgual;
		else if(reconocer(CategoriaLexica.MayorIgual))
			op = CategoriaLexica.MayorIgual;
		else if (reconocer(CategoriaLexica.Igual))
			op = CategoriaLexica.Igual;
		else if (reconocer(CategoriaLexica.Distinto))
			op = CategoriaLexica.Distinto;
		else //error sintactico
			throw new Exception(errorSintactico("No se ha encontrado un operador válido"));
			
		return op;
	}

	/**
	 * <dd>Exp1 (out tipo1) ::= 
	 * <dl>
	 * 		<dd>Exp2(out tipo2) 
	 * 		<dd>RExp1 	(in tipo2; out tipoR1)
	 * </dl>
	 * @return Devuelve el tipo correspondiente a la expresión
	 * @throws Exception: Eleva la excepcion que capture durante el reconocimiento de la expresión
	 */
	private ExpTipo rExp1() throws Exception
	{
		ExpTipo tipo2 = rExp2();				// Exp2 	(out tipo2) 
		ExpTipo tipoR1 = rRExp1(tipo2);			// RExp1 	(in tipo2; out tipoR1)
		return tipoR1;									// { tipo1 <- tipoR1 }
	}

	/**
	 * <dd>RExp1 (in tipoR1h; out tipoR1) ::= 
	 * <dl>
	 * 		<dd>Op1(out op)
	 * 		<dd>Exp2(out tipo2)
	 * </dl>
	 * <dd>RExp1 (in tipoR1h; out tipoR1) ::= λ
	 * @param tipoR1_h tipo heredado
	 * @return  Devuelve el tipo correspondiente a la expresión
	 * @throws Exception si se ha producido algún error sintáctico
	 */ 
	private ExpTipo rRExp1(ExpTipo tipoR1_h) throws Exception {
		
		ExpTipo tipoR1 = null;
		ArrayList<String> a1 = new ArrayList<String>();
		CategoriaLexica op1;
		int aux;
		//Comprobamos si hay un operador Op1 (+, -, or-logica)
		// ----------------------------------------------
		tActual = scanner.checkNextToken(); 
		
		if (reconocerOperador(1))
		{
			op1 = rOp1();			// Op1 		(out op)
			parh = false;
			ExpTipo tipo2;
			aux = etq;
			
			if (reconocer(CategoriaLexica.PalabraReservada,"or")){
				
				// Parcheamos
				cod.parchea(lirf, etq+ 2);
				// Emite
				cod.emite(InstruccionesPila.copia, null);
				a1.add("?");
				cod.emite(InstruccionesPila.ir_v, a1);
				cod.emite(InstruccionesPila.desapila, null);				 
				// Añadimos a la lista
				
				// Avanzamos etq
				etq = etq + 3;
				
				tipo2 = rExp2();		// Exp2 	(out tipo2)
				lirv.add(aux+1);
			
			} else {
				tipo2 = rExp2();		// Exp2 	(out tipo2)
				
				// emite
				emitirCodigoOperacion(op1, null, false);	
				// Avanzamos etq
				etq = etq + 1;
			}
			
			// Comprobamos si el tipo de la expresion es erroneo => si (tipoR1h = terr)
			// ----------------------------------------------
			if (tipoR1_h.getT().equals("err"))
			{
				// tipoR1h <-terr;
				tipoR1_h = new ExpTipoError();			
				añadirError(tActual.get_linea(),"La expresión izquierda tiene un tipo erróneo.");
			}
			
			// Comprobamos si el tipo de la expresion es erroneo => si (tipo2 = terr)
			// ----------------------------------------------
			if (tipo2.getT().equals("err"))
			{
				// tipoR1h <-terr;
				tipoR1_h = new ExpTipoError();	
				añadirError(tActual.get_linea(),"La expresión derecha tiene un tipo erróneo.");
			}
			
			// Comprobamos si el tipo de la expresion es erroneo => si (Â¬existeOperacion(op,tipoR1h,tipo2))
			// ----------------------------------------------
			CategoriaLexica catLex_tipoR1_h = tipoR1_h.getCategoriaLexica(ts);
			CategoriaLexica catLex_tipo2 = tipo2.getCategoriaLexica(ts);
			
			if (!tOperaciones.existeOperacion(op1,"or", catLex_tipoR1_h ,catLex_tipo2))
			{
				// tipoR1h <-terr;
				tipoR1_h = new ExpTipoError();	
				añadirError(tActual.get_linea(),"No se pueden operar los tipos de las expresiones.");
			}

			// sino
			// ----------------------------------------------
			if (!(tipoR1_h instanceof ExpTipoError))
			{
				// tipoR1h <- tipoResultante(op,tipoR1h ,tipo2) 							
				tipoR1_h = tOperaciones.tipoEsperado(op1,"or", tipoR1_h ,tipo2);		
			}
			
			// RExp1(in tipoR1h; out tipoR11) 
			ExpTipo tipoR1_1 = rRExp1(tipoR1_h);
			
			// { tipoR1 <- tipoR11 }
			tipoR1 = tipoR1_1;
			
			desig = false;
		}	
		else // RExp1 (in tipoR1h; out tipoR1) ::= Lambda
		{
			tipoR1 = tipoR1_h;		// { tipoR1 <- tipoR1h }
		}
		return tipoR1;
		
	}
	
	/**
	 * <dd>Exp2 (out tipo2) ::= 
	 * <dl>
	 * 		<dd>Exp3 (out tipo3)
	 * 		<dd>RExp2 (in tipo3; out tipoR2)
	 * </dl>
	 * @return Devuelve el tipo correspondiente a la expresión
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private ExpTipo rExp2() throws Exception {
		ExpTipo tipo3 = rExp3();			// Exp3 (out tipo3) 
		ExpTipo tipoR2 = rRExp2(tipo3);		// RExp2 (in tipo3; out tipoR2)
		return tipoR2;								// { tipo2 <- tipoR2 }	
	}
	
	/**
	 * <dd>Op1 (out op) ::= + 
	 * <dd>Op1 (out op) ::= - 
	 * <dd>Op1 (out op) ::= or
	 * @return Devuelve el tipo de operación correspondiente
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private CategoriaLexica rOp1() throws Exception {
		tActual = scanner.getNextToken();	 //(suma, resta, or-logica)
		CategoriaLexica op;
		
		if (reconocer(CategoriaLexica.Mas))
			op = CategoriaLexica.Mas;
		else if (reconocer(CategoriaLexica.Menos))
			op = CategoriaLexica.Menos;
		else if (reconocer(CategoriaLexica.PalabraReservada, "or"))
			op = CategoriaLexica.PalabraReservada; 
		else //error sintactico
			throw new Exception(errorSintactico("No se ha encontrado un operador válido"));
			
		return op;
	}
	
	/**
	 * <dd>RExp2 (in tipoR2h; out tipoR2) ::= 
	 * <dl>
	 * 		<dd>Op2 (out op)
	 * 		<dd>Exp3 (out tipo3)
	 * </dl>
	 * @param tipoR2_h tipo heredado
	 * @return Devuelve el tipo correspondiente a la expresión
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private ExpTipo rRExp2(ExpTipo tipoR2_h) throws Exception {
		
		ExpTipo tipoR2 = null;
		ArrayList<String> a1 = new ArrayList<String>();
		int aux;
		
		// Comprobamos si hay un operador Op2 (multiplica, divide, modulo, and-logica)
		// ----------------------------------------------
		tActual = scanner.checkNextToken(); 
		
		if (reconocerOperador(2))
		{
			CategoriaLexica op2 = rOp2();				// Op2 (out op) 
			parh = false;
			ExpTipo tipo3;
			aux = etq;
			
			if (reconocer(CategoriaLexica.PalabraReservada,"and")){
				
				// Parcheamos
				cod.parchea(lirv, etq + 2);
				// Emite
				cod.emite(InstruccionesPila.copia, null);
				a1.add("?");
				cod.emite(InstruccionesPila.ir_f, a1);
				cod.emite(InstruccionesPila.desapila, null);				 
				// Añadimos a la lista
				
				// Avanzamos etq
				etq = etq + 3;
								
				tipo3 = rExp3();		// Exp3 	(out tipo3)
				lirf.add(aux+1);
			
			}
			else {
				tipo3 = rExp3();		// Exp3 	(out tipo3)
				
				// emite
				emitirCodigoOperacion(op2, null, false);	
				
				etq = etq + 1;
			}
			
			// Comprobamos si el tipo de la expresion es erroneo => si (tipoR2h = terr)
			// ----------------------------------------------
			if (tipoR2_h.getT().equals("err")){
				// tipoR2h <- terr;
				tipoR2_h = new ExpTipoError();		
				añadirError(tActual.get_linea(),"La expresión izquierda tiene un tipo erróneo.");
			}
			
			// Comprobamos si el tipo de la expresion es erroneo => si (tipo3 = terr)
			// ----------------------------------------------
			if (tipo3.getT().equals("err")){
				// tipoR2h <- terr;
				tipoR2_h = new ExpTipoError();	
				añadirError(tActual.get_linea(),"La expresión derecha tiene un tipo erróneo.");
			}
			
			// Comprobamos si el tipo de la expresion es erroneo => si (Â¬existeOperacion(op,tipoR2h,tipo3))
			// ----------------------------------------------
			CategoriaLexica catLex_tipoR2_h = tipoR2_h.getCategoriaLexica(ts);
			CategoriaLexica catLex_tipo3 = tipo3.getCategoriaLexica(ts);
			if (!tOperaciones.existeOperacion(op2,"and", catLex_tipoR2_h ,catLex_tipo3)){
				// tipoR2h <- terr;
				tipoR2_h = new ExpTipoError();	
				añadirError(tActual.get_linea(),"No se pueden operar los tipos de las expresiones.");
			}
			
			// sino
			// ----------------------------------------------			
			if (!(tipoR2_h instanceof ExpTipoError)){
				// tipoR2h <- tipoResultante(op,tipo3,tipo3)
				tipoR2_h = tOperaciones.tipoEsperado(op2,"and", tipoR2_h, tipo3);
				
			}
			
			// RExp2 (in tipoR2h; out tipoR22)
			ExpTipo tipoR2_2 = rRExp2(tipoR2_h);
			
			// { tipoR2 <- tipoR22 }
			tipoR2 = tipoR2_2;
			
			desig = false;
		}	
		else // RExp2(in tipoR2h; out tipoR2) ::= Lambda
		{ 
			tipoR2 = tipoR2_h;		// { tipoR2 <- tipoR2h }
		}
			
		return tipoR2;
	}

	/**
	 * <dd>Op2 (out op) ::= * 
	 * <dd>Op2 (out op) ::= / 
	 * <dd>Op2 (out op) ::= % 
	 * <dd>Op2 (out op) ::= and
	 * @return Devuelve el tipo de operación correspondiente
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private CategoriaLexica rOp2() throws Exception {
		tActual = scanner.getNextToken();	 //(por, divide, porcentaje, and)
		CategoriaLexica op;
		
		if (reconocer(CategoriaLexica.Por))
			op = CategoriaLexica.Por;
		else if (reconocer(CategoriaLexica.Divide))
			op = CategoriaLexica.Divide;
		else if (reconocer(CategoriaLexica.Modulo))
			op = CategoriaLexica.Modulo;
		else if (reconocer(CategoriaLexica.PalabraReservada, "and"))
			op = CategoriaLexica.PalabraReservada; 
		else //error sintactico
			throw new Exception(errorSintactico("No se ha encontrado un operador válido"));
			
		return op;
	}
	
	/**
	 * <dd>Exp3 (out tipo3) ::= 
	 * <dl>
	 * 		<dd>Exp4 (out tipo4) 
	 * 		<dd>FExp3 (in tipo4; out tipoF3)
	 * @return Devuelve el tipo correspondiente a la expresión
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private ExpTipo rExp3() throws Exception {
		ExpTipo tipo4 = rExp4();				// Exp4 (out tipo4) 
		ExpTipo tipoF3 = rFExp3(tipo4);			// FExp3 (in tipo4; out tipoF3)
		return tipoF3;									// { tipo3 <- tipoF3 }
	}
	
	/**
	 * <dd>FExp3 (in tipoF3h; out tipoF3) ::= 
	 * <dl>
	 * 		<dd>Op3 (out op)
	 * 		<dd>Exp3 (out tipo3)
	 * </dl>
	 * <dd>FExp3 (in tipoF3h; out tipoF3) ::= λ
	 * @param tipoF3_h tipo heredado
	 * @return Devuelve el tipo correspondiente a la expresión
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private ExpTipo rFExp3(ExpTipo tipoF3_h) throws Exception{
		ExpTipo tipoF3 = null;
		
		// Comprobamos si hay un operador Op3 (<<, >>)
		// ----------------------------------------------
		tActual = scanner.checkNextToken(); 
		
		if (reconocerOperador(3)){
			parh = false;
			CategoriaLexica op3 = rOp3();		// Op3 (out op) 
			ExpTipo tipo3 = rExp3();			// Exp3 (out tipo3)
			
			
			// Comprobamos si el tipo de la expresion es erroneo => si (tipoF3h = terr)
			// ----------------------------------------------
			if (tipoF3_h.getT().equals("err")){
				// tipoF3 <- terr;
				tipoF3 = new ExpTipoError();		
				añadirError(tActual.get_linea(),"La expresión izquierda tiene un tipo erróneo.");
			}
			
			// Comprobamos si el tipo de la expresion es erroneo => si (tipo3 = terr)
			// ----------------------------------------------
			if (tipo3.getT().equals("err")){
				// tipoF3 <- terr;
				tipoF3 = new ExpTipoError();		
				añadirError(tActual.get_linea(),"La expresión derecha tiene un tipo erróneo.");
			}
			
			// Comprobamos si el tipo de la expresion es erroneo => si (¬existeOperacion(op,tipoF3h,tipo3))
			// ----------------------------------------------
			CategoriaLexica catLex_tipoF3_h = tipoF3_h.getCategoriaLexica(ts);
			CategoriaLexica catLex_tipo3 = tipo3.getCategoriaLexica(ts);
			
			if (!tOperaciones.existeOperacion(op3,null, catLex_tipoF3_h ,catLex_tipo3)){
				// tipoF3 <- terr;
				tipoF3 = new ExpTipoError();	
				añadirError(tActual.get_linea(),"No se pueden operar los tipos de las expresiones.");
			}
			
			// sino
			// ----------------------------------------------
			if (!(tipoF3 instanceof ExpTipoError)){
				// tipoF3 <- tipoResultante(op,tipoF3h,tipo3)
				tipoF3 = tOperaciones.tipoEsperado(op3,null, tipoF3_h, tipo3);

				if (!(tipoF3_h instanceof ExpTipoError) && !(tipo3 instanceof ExpTipoError))
					emitirCodigoOperacion(op3,null,false); 	// emite(Op3.op,null)
					// etq <- etq + longOp(Op0.op)
					etq = etq + 1;
			}
			
			desig = false;
		}
		else // FExp3 (in tipoF3h; out tipoF3) ::= lambda
		{
			tipoF3 = tipoF3_h;	// { tipoF3 <- tipoF3h }
		}
		
		return tipoF3;	
		
	}
	
	/**
	 * <dd>Op3 (out op) ::= << 
	 * <dd>Op3 (out op) ::= >> 
	 * @return Devuelve el tipo de operación correspondiente
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private CategoriaLexica rOp3() throws Exception {
		tActual = scanner.getNextToken();	 //(<<,>>)
		CategoriaLexica op;
		
		if (reconocer(CategoriaLexica.DesplazDer))
			op = CategoriaLexica.DesplazDer;
		else if (reconocer(CategoriaLexica.DesplazIz))
			op = CategoriaLexica.DesplazIz;
		else //error sintactico
			throw new Exception(errorSintactico("No se ha encontrado un operador válido"));
			
		return op;
	}
	
	/**
	 * <dd>Exp4 (out tipo4) ::= 
	 * <dl>
	 * 		<dd>Op4 (out op)
	 * 		<dd>Exp4 (out tipo41)
	 * </dl>
	 * <dd>Exp4 (out tipo4) ::= 
	 * <dl>
	 * 		<dd>Op4b (out op)
	 * 		<dd>Exp5 (out tipo5)
	 * </dl>
	 * @return Devuelve el tipo correspondiente a la expresión
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private ExpTipo rExp4() throws Exception 
	{
		
		ExpTipo tipo4 = null;
		ExpTipo tipo4_1 = null;
		
		// Comprobamos si hay un "Op4" (not, - (unario)), "Op4b" (casting) o una "Exp5"
		// ----------------------------------------------
		tActual = scanner.checkNextToken();
		
		// Comprobamos si hay un "Op4"
		// ----------------------------------------------
		if (reconocerOperador(4))
		{
			// Reconocer "Op4"
			// ----------------------------------------------
			CategoriaLexica op4 = rOp4();		// Op4 (out op) 
			parh = false;
			
			// Reconocer "Exp4"
			// ----------------------------------------------
			tipo4_1 = rExp4();				// Exp4 (out tipo41)
			
	
			// Comprobamos si el tipo de la expresion es erroneo => si (tipo41 = terr)
			// ----------------------------------------------
			if (tipo4_1.getT().equals("err")){
				// tipo4 <- terr;
				tipo4 = new ExpTipoError();
				añadirError(tActual.get_linea(),"La expresión tiene un tipo erróneo.");
			}
			
			// Comprobamos si el tipo de la expresion es erroneo => si (Â¬existeOperacion(op,tipo41,null))
			// ----------------------------------------------
			CategoriaLexica catLex_tipo4_1 = tipo4_1.getCategoriaLexica(ts);
			
			if (!tOperaciones.existeOperacion(op4,"not", catLex_tipo4_1 ,null)){
				// tipo4 <- terr;
				tipo4 = new ExpTipoError();
				añadirError(tActual.get_linea(),"No se pueden operar el tipo de la expresión.");
			}

			// sino
			// ----------------------------------------------
			else
			{
				// tipo4 <- resultante(op,tipo41,null)
				tipo4 = tOperaciones.tipoEsperado(op4, "not", tipo4_1, null);
				
				// si (tipo41 != terr)
				if (!(tipo4_1 instanceof ExpTipoError))
					emitirCodigoOperacion(op4,"not",true);		// emite(Op4.op,null)
					etq = etq + 1;
			}
			
			desig = false;
		}	
		else // Exp4 (out tipo4) ::= Op4b (out op) Exp5 (out tipo5)
			 // Comprobamos si hay un "Op4b"
			 // ----------------------------------------------
			if (reconocerOperador(5)){

				// Reconocer "("
				// -----------------------------------------------
				tActual = scanner.getNextToken();
				if (!reconocer(CategoriaLexica.AbreParentesis))
					throw new Exception(errorSintactico("Se esperaba '(' (Inicio de Casting)"));
				
				// Reconocer "Op4b"
				// ----------------------------------------------
				CategoriaLexica op4b = rOp4b();

				// Reconocer ")"
				// -----------------------------------------------
				tActual = scanner.getNextToken();
				if (!reconocer(CategoriaLexica.CierraParentesis))
					throw new Exception(errorSintactico("Se esperaba ')' (Fin de Casting)"));	
				
				parh = false;
				
				// Reconocer "Exp5"
				// ----------------------------------------------
				ExpTipo tipo5 = rExp5();			// Exp5 (out tipo5)
				
				// Comprobamos si el tipo de la expresion es erroneo => si (tipo5 = terr)
				// ----------------------------------------------
				if (tipo5.getT().equals("err"))
				{
					// tipo4 <- terr;
					tipo4 = new ExpTipoError();
					añadirError(tActual.get_linea(),"La expresión tiene un tipo erróneo.");
				}
				
				// Comprobamos si el tipo de la expresion es erroneo => si (Â¬existeOperacion(op,tipo5,null))
				// ----------------------------------------------
				CategoriaLexica catLex_tipo5 = tipo5.getCategoriaLexica(ts);
				
				if (!tOperaciones.existeOperacion(op4b,null, catLex_tipo5 ,null))
				{
					// tipo4 <- terr;
					tipo4 = new ExpTipoError();
					añadirError(tActual.get_linea(),"No se pueden operar el tipo de la expresiones.");
				}
				
				// sino
				// ----------------------------------------------
				if (!(tipo4 instanceof ExpTipoError))
				{
					// tipo4 <- resultante(op,tipo5,null)
					tipo4 = tOperaciones.tipoEsperado(op4b, null, tipo5, null);
					
					// si (tipo5 != terr)
					if (!(tipo4 instanceof ExpTipoError))
						emitirCodigoOperacion(op4b,null,false);		// emite(Op4b.op,null)
						etq = etq + 1;
				}
			
				desig = false;
			}
		else // Exp4 (out tipo4) ::= Exp5 (out tipo5)
		{
			// Sino es Op4, ni Op4b es Expr5 (No hay operadores, ni casting) 
			tipo4 = rExp5();	// { tipo4 <- tipo5 }
		}
		return tipo4;
	}
	
	/**
	 * <dd>Op4 (out op) ::= not 
	 * <dd>Op4 (out op) ::= - 
	 * @return Devuelve el tipo de operación correspondiente
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private CategoriaLexica rOp4() throws Exception {
		tActual = scanner.getNextToken();	 //(menos,not)
		CategoriaLexica op;
		
		if (reconocer(CategoriaLexica.PalabraReservada,"not"))
			op = CategoriaLexica.PalabraReservada;
		else if (reconocer(CategoriaLexica.Menos))
			op = CategoriaLexica.Menos;
		else //error sintactico
			throw new Exception(errorSintactico("No se ha encontrado un operador válido"));
		
		return op;
	}

	/**
	 * <dd>(out op) ::= (tipoBasico (out lexTipo) )
	 * @return Devuelve el tipo de operación correspondiente
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private CategoriaLexica rOp4b() throws Exception { 
		CategoriaLexica op; 

		tActual = scanner.getNextToken(); // Avanzar el tokenizer al casting leido 
		CategoriaLexica tipo = reconocerTipo();
		
		if (tipo == CategoriaLexica.LitNatural)
			op = CategoriaLexica.CastingNatural;
		else if (tipo == CategoriaLexica.LitEntero)
			op = CategoriaLexica.CastingEntero;
		else if (tipo == CategoriaLexica.LitDecimal)
			op = CategoriaLexica.CastingDecimal;
		else if (tipo == CategoriaLexica.LitCaracter)
			op = CategoriaLexica.CastingCaracter;
		else //error sintactico
			throw new Exception(errorSintactico("Se esperaba un operador válido para casting"));
		
		return op;

	}
	
	

	/**
	 * <dd>Exp5 (out tipo5) ::= (Exp0 (out tipo0))
	 * <dd>Exp5 (out tipo5) ::= iden (out lex)
	 * <dd>Exp5 (out tipo5) ::= valor (out tipo, lex)
	 * @return Devuelve el tipo correspondiente a la expresión
	 * @throws Exception si se ha producido algún error sintáctico
	 */
	private ExpTipo rExp5() throws Exception {
		
		ExpTipo tipo5 = null;
		ArrayList<String> atribCod; // ArrayList en el que se añdiran los argumentos para construir el código
		
		// Comprobamos si hay una expresion encerrada en parentesis (Exp0), un identificador o un valor
		// ----------------------------------------------
		tActual = scanner.checkNextToken();	// Miramos que hay en el sig token para el caso que no haya parentesis
		
		// Comprobamos si hay '('
		// ----------------------------------------------
		if (reconocer(CategoriaLexica.AbreParentesis)){
			tActual = scanner.getNextToken(); 
			// Reconocer "Expr0" (Expresion entre parentesis)	
			// --------------------------------------------------
			tipo5 = rExp0();		// { tipo5 <- tipo0 }
			
			// Reconocer ")"	
			// --------------------------------------------------
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.CierraParentesis))
				throw new Exception(errorSintactico("Se esperaba ')' cierre de paréntesis de la subexpresión"));
			
			}
			// Comprobamos si un Designador de Tipo
			// ----------------------------------------------
		else if (reconocer(CategoriaLexica.Identificador)){
			
				String idenLex = tActual.get_lexema();
				
				
				ExpTipo tipo = rDesigTipo();
	
				if (tipo.getError()){
					tipo5 = new ExpTipoError();
					return tipo5;
				}
				
				if (tipo.esTipoBasico() && desig && !parh){
					cod.emite(InstruccionesPila.apila_ind, null);					
					etq = etq + 1;					
				}
				else if (tipo.esTipoBasico() && !desig && ts.getClase(idenLex, 0) == ClaseDec.Constante){ //constante
					ExpTipoConstante e = (ExpTipoConstante) ts.getExprTipo(idenLex, 0);
					
					atribCod = new ArrayList<String>();
					atribCod.add(e.getT());					
					atribCod.add(e.getValor());
					
					cod.emite(InstruccionesPila.apila, atribCod);
					etq = etq + 1;					
				}
				else
					tipo5 = tipo;
			
				if (tipo.getError() && ((ExpTipoConstante)tipo).getValor() != null){
					tipo5 = new ExpTipoError();
				}
				else
					tipo5 = tipo;
			}
		else {
		
			// Reconocer "valor"
			// ----------------------------------------------
			
			// Comprobamos el tipo esperado por el tokenizer
			String tipoValor = scanner.checkNextToken().getCatLexica().toString();
			
			// reconocerValores(), necesita ajustar tipoValor para distinguir naturales de enteros positivos. Para el resto no es necesario
			if (tActual.getCatLexica() == CategoriaLexica.LitNatural)
				tipoValor = "natural";
			else if (tActual.getCatLexica() == CategoriaLexica.LitEntero)
					tipoValor = "integer";
			
			// Reconocemos el valor
			retValor valor = rValor(tipoValor);
			
			if (valor.tipoDevuelto != null){
				
				// Ajustamos "tipoValor" en funcion del valor devuelto por "reconocerValor()"
				if (valor.tipoDevuelto.equals(CategoriaLexica.LitNatural))
					tipoValor = "natural";
				else if (valor.tipoDevuelto.equals(CategoriaLexica.LitEntero))
						tipoValor = "integer";
				else if (valor.tipoDevuelto.equals(CategoriaLexica.LitDecimal))
						tipoValor = "float";
				else if (valor.tipoDevuelto.equals(CategoriaLexica.LitBooleano))
						tipoValor = "boolean";
				else if (valor.tipoDevuelto.equals(CategoriaLexica.LitCaracter))
						tipoValor = "character";
				
				
				tipo5 = new ExpTipo(tipoValor,ts);							// tipo5 = tipo
				
				atribCod = new ArrayList<String>();
				atribCod.add(tipoValor);
				atribCod.add(""+tActual.get_lexema());
				
				cod.emite(InstruccionesPila.apila,atribCod);		// emite (apila,<tipo, lex>)			
				etq = etq + 1;
				
				desig = false;
				}
			else
				throw new Exception(errorSintactico("El valor introducido en la expresión no es válido"));	
		}
			
		return tipo5;
		
	}

	
	// METODOS PARA RECONOCER TIPOS DE TOKENS
	// ******************************************************************************
	/**
	 * Sirve para reconocer palabras reservadas
	 * @param catLexEsperada Categoria Lexica esperada
	 * @param tokenEsperado palabra esperada
	 * @return booleano que indica si se ha reconocido o no
	 * @throws Exception
	 */
	private boolean reconocer(CategoriaLexica catLexEsperada, String tokenEsperado) throws Exception{
		
		// Comprobamos si se ha reconocido
		boolean resultado = tActual.get_lexema().equals(tokenEsperado) &&
			  				tActual.getCatLexica() == catLexEsperada;	

		// Si falla el reconocimiento, preguntamos si es por un token erroneo
		if (!resultado && tActual.getCatLexica() == CategoriaLexica.TError)
			throw new Exception(errorLexico());
		
		// Devolver si se ha reconocido o no (error sintactico)
		return resultado;
	}
	
	/**
	 * Sirve para reconocer si una categoría lexica leída es la que se esperaba
	 * @param catLexEsperada Categoria léxica esperada
	 * @return boolean para saber si es true/false
	 * @throws Exception
	 */
	private boolean reconocer(CategoriaLexica catLexEsperada) throws Exception{
		
		// Comprobamos si se ha reconocido
		boolean resultado = tActual.getCatLexica() == catLexEsperada;
		
		// Si falla el reconocimiento, preguntamos si es por un token erroneo
		if (!resultado && tActual.getCatLexica() == CategoriaLexica.TError)
			throw new Exception(errorLexico());
		
		// Devolver si se ha reconocido o no (error sintactico)
		return resultado;
		
	}
	
	/**
	 * Reconoce el tipo y lo asocia a una categoria léxica
	 * @return Categoria lexica del tipo leido
	 * @throws Exception
	 */
	private CategoriaLexica reconocerTipo() throws Exception{
		
		CategoriaLexica resultado = null;
		// Comprobamos si se ha reconocido
		if (tActual.getCatLexica() == CategoriaLexica.PalabraReservada){
			if (tActual.get_lexema().equals("natural"))
				resultado = CategoriaLexica.LitNatural;
			else if (tActual.get_lexema().equals("integer"))
				resultado = CategoriaLexica.LitEntero;
			else if (tActual.get_lexema().equals("float"))
				resultado = CategoriaLexica.LitDecimal;
			else if (tActual.get_lexema().equals("character"))
				resultado = CategoriaLexica.LitCaracter;
			else if (tActual.get_lexema().equals("boolean"))
				resultado = CategoriaLexica.LitBooleano;
		}
		
		
		// Si falla el reconocimiento, preguntamos si es por un token erroneo
		if (resultado == null &&				// Si antes no reconocimos un identificador de tipo
			tActual.getCatLexica() == CategoriaLexica.TError)	// O el tipo de token es de TError
			throw new Exception(errorLexico());
		
		// Devolver si se ha reconocido o no (error sintactico)
		return resultado;
		
	}
	
	/**
	 * Permite reconocer un literal introducido y darle significado (Hace además casting automático)
	 * @param tipo Categoria Lexica del tipo que se ha leido
	 * @param tipoEsperado tipo que se espera
	 * @param valor valor asociado al tipo leido
	 * @return Categoria Lexica a la que pertenece; NULL en otro caso
	 * @throws Exception
	 */
	private CategoriaLexica reconocerValor(CategoriaLexica tipo,String tipoEsperado, String valor) throws Exception{
		
		CategoriaLexica resultado = null;
		
		switch (tipo) {
			// Si es LitNatural es un natural o integer pero pos
			// ------------------------------------------------------------------------------
			case LitNatural: if (tipoEsperado.equals("natural")){
								try {	// Intentar pasar a entero y comprobar que es positivo
									int nat = Integer.parseInt(valor);
									if (nat >= 0)
										resultado = CategoriaLexica.LitNatural;
									}		
								catch (NumberFormatException e){ // Si no se puede pasar a entero
									// Dejamos resultado = null -> Marcamos como no exito y rDec notificara el error
									}
						 		}
							else if (tipoEsperado.equals("integer")){
									try {	// Intentar pasar a entero
										Integer.parseInt(valor);
										resultado = CategoriaLexica.LitEntero;
										}		
									catch (NumberFormatException e){ // Si no se puede pasar a entero:
										// Dejamos resultado = null -> Marcamos como no exito y rDec notificara el error
										}
								}
							else if (tipoEsperado.equals("float")){
								try{
									Float.parseFloat(valor);
									resultado = CategoriaLexica.LitDecimal;
								}
								catch (NumberFormatException e){ // Si no se puede pasar a entero:
									// Dejamos resultado = null -> Marcamos como no exito y rDec notificara el error
									}
							}
							break;
							
			// Si es LitEntero es un entero neg
			// ------------------------------------------------------------------------------				
			case LitEntero: //if (tipoEsperado.equals("integer")){
								try {	// Intentar pasar a entero
									Integer.parseInt(valor);
									resultado = CategoriaLexica.LitEntero;
									}		
								catch (NumberFormatException e){ // Si no se puede pasar a entero, saltara excepcion:
									// Dejamos resultado = null -> Marcamos como no exito y rDec notificara el error
									}
								//}
							break;
							
			case LitDecimal: //if (tipoEsperado.equals("float")){
							 // Comprobar si es un numero real con parte decimal y parte exponencial
							 if ((valor.contains("e") || valor.contains("E")) && valor.contains(".")){
								 
								 int pos = 0;
								 
								 if (valor.contains("e"))
									 pos = valor.indexOf("e");
								 else if (valor.contains("E"))
									 	pos = valor.indexOf("E");
									 
								 String pDecimal = valor.substring(0,pos-1);
								 String pExponencial = valor.substring(pos+1);
								 
								 // Intentar pasar a real la parte decimal
								 try {	
										Float.parseFloat(pDecimal);
								 } catch (NumberFormatException e){ // Si no se puede pasar a real, saltara excepcion:
										// Dejamos resultado = null -> Marcamos como no exito y rDec notificara el error
								 }
								 
								 // Intentar pasar a entero la parte exponencial
								 try {	
										Integer.parseInt(pExponencial);
										// Llegados a este punto, hemos reconocido el decimal-exponencial
										resultado = CategoriaLexica.LitDecimal;
								 } catch (NumberFormatException e){ // Si no se puede pasar a real, saltara excepcion:
										// Dejamos resultado = null -> Marcamos como no exito y rDec notificara el error
								 }
							}
							 // Comprobar si es un numero real exponencial asecas
							else if (valor.contains("e") || valor.contains("E")){
								 	
									int pos = 0;
								 
									if (valor.contains("e"))
										pos = valor.indexOf("e");
									else if (valor.contains("E"))
										 	pos = valor.indexOf("E");
										 
									String pEntera = valor.substring(0,pos-1);
									String pExponencial = valor.substring(pos+1);
									 
									// Intentar pasar a entero la parte decimal
									try {	
										Integer.parseInt(pEntera);
									} catch (NumberFormatException e){ // Si no se puede pasar a real, saltara excepcion:
											// Dejamos resultado = null -> Marcamos como no exito y rDec notificara el error
									}
									 
									// Intentar pasar a entero la parte exponencial
									try {	
										Integer.parseInt(pExponencial);
										// Llegados a este punto, hemos reconocido el exponencial
										resultado = CategoriaLexica.LitDecimal;
									} catch (NumberFormatException e){ // Si no se puede pasar a real, saltara excepcion:
											// Dejamos resultado = null -> Marcamos como no exito y rDec notificara el error
									}
							 }
							else{ // sino, es un numero real normal
								
								try {	// Intentar pasar a real
									Float.parseFloat(valor);
									resultado = CategoriaLexica.LitDecimal;
									}		
								catch (NumberFormatException e){ // Si no se puede pasar a real, saltara excepcion:
									// Dejamos resultado = null -> Marcamos como no exito y rDec notificara el error
									}
								
							}
							 
							 
							 
								
							break;
		
			
			// Si es palabra reservada, es un lit-booleano: true o false 	
			// ------------------------------------------------------------------------------
			case PalabraReservada:	if (valor.equals("true") || valor.equals("false"))
										resultado = CategoriaLexica.LitBooleano;
									break;
									
			// Si es un literal caracter: 'x'
			// ------------------------------------------------------------------------------
			case LitCaracter:	if (valor.length() == 3 ||	// Un caracter ocupa 1-2 posiciones
								   (valor.length() == 4 && valor.charAt(1) == '\\' && 
								   (valor.charAt(2) == 'n' || valor.charAt(2) == 'r' || 
								    valor.charAt(2) == 't' || valor.charAt(2) == '\'' ||
								    valor.charAt(2) == '\\')))
									resultado = CategoriaLexica.LitCaracter;
								break;	
									
			// Si es un token no válido
			// ------------------------------------------------------------------------------
			case TError: throw new Exception(errorLexico());
		default:
			break;
			

		} // switch
		return resultado;
	}
	
	/**
	 * Sirve para reconocer el tipo de operador (indicado por el tipo de prioridad)
	 * @param prioridad
	 * @return boolean para indicar si lo reconoce o no
	 * @throws Exception
	 */
	private boolean reconocerOperador(int prioridad) throws Exception{
		
		switch(prioridad){
		
			case 0: return reconocer(CategoriaLexica.Menor) || 
						   reconocer(CategoriaLexica.Mayor) || 
						   reconocer(CategoriaLexica.MenorIgual) || 
						   reconocer(CategoriaLexica.MayorIgual) || 
						   reconocer(CategoriaLexica.Igual) || 
						   reconocer(CategoriaLexica.Distinto);
			
			case 1: return reconocer(CategoriaLexica.Mas) || 
						   reconocer(CategoriaLexica.Menos) || 
						   reconocer(CategoriaLexica.PalabraReservada,"or");
			
			case 2: return reconocer(CategoriaLexica.Por) || 
						   reconocer(CategoriaLexica.Divide) ||  
						   reconocer(CategoriaLexica.Modulo) ||
						   reconocer(CategoriaLexica.PalabraReservada,"and");
			
			case 3: return reconocer(CategoriaLexica.DesplazDer) || 
						   reconocer(CategoriaLexica.DesplazIz);
			
			case 4: return reconocer(CategoriaLexica.PalabraReservada, "not") || 
						   reconocer(CategoriaLexica.Menos);
			
			// Prioridad 5 = 4b
			case 5: return scanner.esCasting();
			/*case 5: return reconocer(CategoriaLexica.CastingNatural) ||
						   reconocer(CategoriaLexica.CastingEntero) ||
						   reconocer(CategoriaLexica.CastingDecimal) ||
						   reconocer(CategoriaLexica.CastingCaracter);*/
			
			default: return false;
		}	
	}
	
	/**
	 * Intenta reconocer las sentencias "swap1();" y "swap2();"
	 * @param tipoSwap Introducir "1" para intentar reconocer swap1 y "2" para "swap2"
	 * @return True si reconocio el swap correspondiente, false en otro caso
	 * @throws Exception 
	 */
	private boolean reconocerSwap(int tipoSwap) throws Exception{
		
		// Reconocer "swap1" o "swap2"
		// ----------------------------------------------

		if (tipoSwap == 1){
			
			// Reconocer "swap1"
			// ----------------------------------------------
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.PalabraReservada,"swap1"))
				throw new Exception(errorSintactico("Se esperaba palabra reservada 'swap1'"));	
		}
		else {
			// Reconocer "swap2"
			// ----------------------------------------------
			tActual = scanner.getNextToken();
			if (!reconocer(CategoriaLexica.PalabraReservada,"swap2"))
				throw new Exception(errorSintactico("Se esperaba palabra reservada 'swap2'"));
		}
		
		// Reconocer "("
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.AbreParentesis))
			throw new Exception(errorSintactico("Se esperaba apertura de paréntesis '(' después de 'swap''"));
		
		// Reconocer ")"
		// ----------------------------------------------
		tActual = scanner.getNextToken();
		if (!reconocer(CategoriaLexica.CierraParentesis))
			throw new Exception(errorSintactico("Se esperaba cierre de paréntesis ')' después de '('"));
		
		if (tipoSwap == 1)
			cod.emite(InstruccionesPila.swap1, null);
		else
			cod.emite(InstruccionesPila.swap2, null);
		
		return true;

	}
	
	
	// METODOS AUX PARA TS (Por semejanza con memoria)
	// ******************************************************************************
	
	/**
	 * Crea la tabla de símbolos
	 * @return TablaSimbolos (la TS)
	 */
	private TablaSimbolos creaTS(){
		return new TablaSimbolos();
		
	}
	
	
	// METODOS PARA RECONOCER TIPOS DE TOKENS
	// ******************************************************************************
	
	/**
	 * Escribe una frase predefinida para el error sintáctico
	 * @param detalleError Permite dar información adicional sobre el error
	 * @return String con la frase
	 */
	private String errorSintactico(String detalleError){
		return "\nError sintáctico en [línea " + tActual.get_linea() + "]: " + detalleError;
	}
	
	/**
	 * Escribe una frase predefinida para el error léxico
	 * @return String con la frase
	 */
	private String errorLexico() {
		return "\nError léxico en [línea "+ tActual.get_linea() + " ]: El token "+tActual.get_lexema()+" no es válido"; 
	}
	
	/**
	 * Muestra la lista de errores recopilados
	 */
	private void mostrarListaErrores()
	{
		for (int i = 0; i < listaErr.size(); i++)
			consola.append("\n"+ listaErr.get(i));
	}
	
	
	// METODOS DEL TRADUCTOR	
	// ******************************************************************************
	
	/**
	 * Emite el código de operación. Es más general y puede distinguir entre menos y resta, además de emitir también las operacion
	 * con palabras reservadas
	 * @param op operación que se quiere emitir
	 * @param lex palabra reservada (sólo usado para and, not y or)
	 * @param menos booleano para distinguir entre menos y resta (TRUE = menos)
	 * @throws Exception Si ocurre algo inesperado
	 */
	private void emitirCodigoOperacion(CategoriaLexica op, String lex, boolean menos) throws Exception {
		//nuevamente aqui haremos distinciones adicionales si el op entrante es una palabra reservada
		//para tener en cuenta el segundo atributo o pasar de el
		if (op == CategoriaLexica.PalabraReservada)
		{
			if (lex.equals("and"))
			{
				cod.emite(InstruccionesPila.and_logica, null);
			}
			else if (lex.equals("not"))
			{
				cod.emite(InstruccionesPila.niega, null);
			}
			else if (lex.equals("or"))
			{
				cod.emite(InstruccionesPila.or_logica, null);
			}
			
			// No haria falta comprobar errores porque se supone que al llegar aqui ya esta todo correcto
			
		}
		else  
			switch (op) // resto de casos en los que no es una palabra reservada
			{
				case Menor: cod.emite(InstruccionesPila.menor, null); break;
				case Mayor: cod.emite(InstruccionesPila.mayor, null); break;
				case MenorIgual: cod.emite(InstruccionesPila.menoroigual, null); break;
				case MayorIgual: cod.emite(InstruccionesPila.mayoroigual, null); break;
				case Igual: cod.emite(InstruccionesPila.igual, null); break;
				case Distinto: cod.emite(InstruccionesPila.distinto, null); break;
				case Mas:  cod.emite(InstruccionesPila.suma, null); break;
				case Por: cod.emite(InstruccionesPila.multiplica, null); break;
				case Divide: cod.emite(InstruccionesPila.divide, null); break;
				case Modulo: cod.emite(InstruccionesPila.modulo, null); break;
				case DesplazIz: cod.emite(InstruccionesPila.desp_izq, null); break;
				case DesplazDer: cod.emite(InstruccionesPila.desp_der, null); break;
				case CastingCaracter:  cod.emite(InstruccionesPila.casting_character, null); break;
				case CastingDecimal: cod.emite(InstruccionesPila.casting_float, null); break;
				case CastingEntero: cod.emite(InstruccionesPila.casting_integer, null); break;
				case CastingNatural: cod.emite(InstruccionesPila.casting_natural, null); break;
				case Menos: // Para hacer la distincion entre el - y la resta
					if (menos)
						cod.emite(InstruccionesPila.menos, null);
					else
						cod.emite(InstruccionesPila.resta, null); break;
			default: //Caso que no se va a dar
				break;				
			}
	}
	
	/**
	 * Comprueba si un parámetro esta repetido en una lista de parámetros
	 * @param listaParams lista de parámetros de un procedimiento
	 * @param idParam nombre del parámetro a comprobar su repetición
	 * @return TRUE si repetido
	 */
	private boolean paramRepetido(ArrayList<String> listaParams, String idParam) {
		boolean repetido = false;
		for (String iden: listaParams) {
			if (iden.equals(idParam)) {
				repetido = true;
				break;
			}				
		}
		return repetido;
	}
	
	/**
	 * Método que añade un error a la lista de errores
	 * @param linea número de la línea en dónde se da el error
	 * @param msg mensaje que se añade al error
	 */
	private void añadirError(int linea, String msg){
		listaErr.add("ERROR [Linea "+linea+"]: "+msg);
	}

	
	
	// METODOS DE CONEXION CON LA INTERFAZ 			
	// ******************************************************************************
	
	/**
	 * Rellena la tabla de símbolos de la interfaz con la TS del analizador sintáctico
	 * @param ts Tabla de Símbolos
	 */
	@SuppressWarnings("rawtypes")
	private void rellenaTSInterfaz(TablaSimbolos ts){
		
		// Vaciar la tabla anterior
		while (tsInterfaz.getRowCount() > 0)
			tsInterfaz.removeRow(0);
		
		// Iteramos la TS del Sintactico
		Iterator iteradorTS = ts.getTS().entrySet().iterator();

		while (iteradorTS.hasNext()) {
			
			Map.Entry e = (Map.Entry)iteradorTS.next();
			
			ClaveTS clave = (ClaveTS) e.getKey();
			
			String id = clave.getId();
			String nivel = String.valueOf(clave.getNivel());
			
			Propiedades p = (Propiedades)e.getValue();
			
			String ini = String.valueOf(p.getIni());
			if (ini.equals("0")){
				ini = "-";
			}
			
			String dir = String.valueOf(p.getDirMem());
			if (dir.equals("-1")){
				dir = "-";
			}
			

			tsInterfaz.addRow(new String[]{id,
										   nivel,
										   p.getClaseDeclaracion().toString(),
										   dir,
										   p.getExpTipo().toString(),
										   ini });
		
		}
	}
	
	/**
	 * Rellena la tabla de símbolos de la interfaz con la TS del analizador sintáctico
	 * @param ts Tabla de Símbolos
	 */
	@SuppressWarnings("rawtypes")
	private void rellenaTSInterfazLocal(TablaSimbolos ts, String idSubprog){
		
		// Tabla de simbolos local
		DefaultTableModel dtmSimbolos = new DefaultTableModel(0,5);
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
		
		// Iteramos la TS del Sintactico
		Iterator iteradorTS = ts.getTS().entrySet().iterator();

		while (iteradorTS.hasNext()) {
			
			Map.Entry e = (Map.Entry)iteradorTS.next();
			
			ClaveTS clave = (ClaveTS) e.getKey();
			
			String id = clave.getId();
			String nivel = String.valueOf(clave.getNivel());
			
			Propiedades p = (Propiedades)e.getValue();
			
			String ini = String.valueOf(p.getIni());
			if (ini.equals("0")){
				ini = "-";
			}
			
			String dir = String.valueOf(p.getDirMem());
			if (dir.equals("-1")){
				dir = "-";
			}
			

			dtmSimbolos.addRow(new String[]{id,
										   nivel,
										   p.getClaseDeclaracion().toString(),
										   dir,
										   p.getExpTipo().toString(),
										   ini });
		
		}
		
		// Agregar la tabla final a la interfaz
		panelTabuladoTS.addTab(idSubprog, scrollPane);
		
	}
	
	/**
	 * Muestra el código objeto generado en su correspondiente pestaña de la interfaz
	 * @param visor zona de la interfaz a rellenar
	 * @param rutaCO ruta del fichero del código objeto
	 */
	private void rellenaVisorCodigoObjeto(JTextArea visor,String rutaCO){
		
		// Pasar fichero al JTextArea
		// --------------------------
	    try {
	    	FileReader reader = new FileReader(rutaCO);
	    	BufferedReader br = new BufferedReader(reader);
	    	visor.read( br, null );
            br.close();
            visor.requestFocus();            
	     	}
	    catch(Exception e2) { 
	    	JOptionPane.showMessageDialog(null, "ERROR!\nNo se puede leer el fichero de codigo objeto: "+rutaCO);
           }
	}
}
