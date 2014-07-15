package AnalizadorSintactico.ExpTipos;

import AnalizadorSintactico.TSimbolos.TablaSimbolos;
import Atributos.CategoriaLexica;

/**
 * Clase que gestiona la expresiones de tipo
 */
public class ExpTipo {
	
	// ATRIBUTOS
	// ********************************************************************************
	protected boolean errDTipo;	
	
	protected String t;
	protected int tam;
	protected String idTipo;
	
	// CONSTRUCTORA
	// ********************************************************************************
	
	// Espera un tBasico
	public ExpTipo(String t, TablaSimbolos ts){
		if (esBasico(t)){
			this.t = t;
			this.tam = 1;
		}
		else if (ts.existeID(t, 0)){
			this.t = TablaSimbolos.REF;
			this.idTipo = t;
			this.tam = calcTamBuscandoReferencia(t,ts);
		}
		else if (esArrayOTupla(t)){
			this.t = t;
		}
	}
	
	protected ExpTipo(){
	}

	// CONSULTORAS
	// ********************************************************************************
	
	/**
	 * Permite saber si un tipo es un array, una tupla o un procedimiento
	 * @param t2 tipo a comprobar
	 * @return
	 */
	private boolean esArrayOTupla(String t2) {
		return (t2.equals("array") || t2.equals("tupla") || t2.equals("proc"));
	}

	/**
	 * @return tipo básico de la expresión
	 */
	public String getT() {
		return t;
	}

	/**
	 * @return tamaño de la expresión
	 */
	public int getTam() {
		return tam;
	}
	
	/**
	 * @return devuelve el identificador de tipo en caso de que el tipo básico de la expresión sea REF
	 */
	public String getIdTipo(){
		return idTipo;
	}
	
	/**
	 * Comprueba en la ts y devuelve la categoría léxica asociada a la ts
	 * @param ts
	 */
	public CategoriaLexica getCategoriaLexica(TablaSimbolos ts){
		
		if (esBasico(t))
			return ts.getCategoriaLexica(t);
		else if (ts.existeID(t, 0)){
			
			ExpTipo expTipoBase = ts.ref(this);
			String tBase = expTipoBase.getT();
			
			if (esBasico(tBase)) 
				return ts.getCategoriaLexica(t);
			else if (tBase.equals("array"))
					return CategoriaLexica.TArray;
			else if (tBase.equals("tupla"))
					return CategoriaLexica.TTupla;
			
		}
		
		return CategoriaLexica.TError;
	}
	
	/**
	 * Comprueba si un tipo es básico
	 * @return TRUE si es básico
	 */
	public boolean esTipoBasico(){
		if (t.equals("natural") || 
			t.equals("integer") ||
			t.equals("float")  ||	
			t.equals("character") ||
			t.equals("boolean"))
			return true;
		return false;
	}
	
	/**
	 * Comprueba si un string pasado por parametro es un tipo basico, y si no lo es, es una referencia
	 * @param t
	 */
	protected boolean esBasico(String t){
		if (t.equals("natural") || 
			t.equals("integer") ||
			t.equals("float")  ||	
			t.equals("character") ||
			t.equals("boolean"))
			return true;
		return false;
	}
	
	/**
	 * @return TRUE si se ha producido un error en la expresión
	 */
	public boolean getError(){
		return errDTipo;
	}
	
	/**
	 * permite definir si se ha producido un error en la expresión
	 * @param error
	 */
	public void setError(boolean error){
		errDTipo = error;
	}
	
	/**
	 * permite calcular el tamaño de un tipo buscando si es necesario en las referencias
	 * hasta dar con el tipo base
	 * @param t
	 * @param ts
	 */
	private int calcTamBuscandoReferencia(String t, TablaSimbolos ts){		
		if (ts.existeID(t, 0)){
			return ts.getExprTipo(t, 0).tam;
		}
		//TODO Devolver error si no lo encuentra?
		return -1;
	}
	
	/**
	 * Para mostrar la información en un string que se puede añadir a la TS
	 */
	public String toString(){
		
		// Apertura de ExpTipo
		String s = "<";
		
		// Relleno
		if (esBasico(t))
			s += "t: " + this.t;
		else {
			s += "t: " + TablaSimbolos.REF;
			s += ", id: " + this.idTipo;
		}

		s += ", tam: " + this.tam;

		// Cierre de ExpTipo
		s += ">";
		
		return s;	
	}
}

