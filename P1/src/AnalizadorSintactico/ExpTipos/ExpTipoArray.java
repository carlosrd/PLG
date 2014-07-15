package AnalizadorSintactico.ExpTipos;


import AnalizadorSintactico.TSimbolos.TablaSimbolos;
import Atributos.CategoriaLexica;

/**
 * expresión de tipo especializada en arrays, con información específica
 */
public class ExpTipoArray extends ExpTipo{
	
	// ATRIBUTOS
	// ********************************************************************************
	private int nElems;
	private ExpTipo tbase;
	
	// CONSTRUCTORA
	// ********************************************************************************
	
	public ExpTipoArray(int nElems,TablaSimbolos ts, ExpTipo tbase){
		super("array",ts);
		this.nElems = nElems;
		this.tbase = tbase;
		this.tam = calcularTamArray();
	}
	
	// CONSULTORAS
	// ********************************************************************************
	
	/**
	 * @return número de elementos asociados al array
	 */
	public int getNumElems() {
		return nElems;
	}

	/**
	 * @return expresión de tipo asociado al array
	 */
	public ExpTipo getTBase() {
		return tbase;
	}
	
	/**
	 * permite calcular el tamaño total del array
	 * @return tamaño
	 */
	private int calcularTamArray(){
		return tbase.tam * nElems;
	}
	
	/**
	 * permite añadir la información a la TS
	 */
	public String toString(){
		
		// Apertura de ExpTipo
		String s = "<";
		
		// Relleno
		s += "t: " + this.t;
		s += ", nElems: " + this.nElems;
		s += ", tBase: " + this.tbase.toString();
		s += ", tam: " + this.tam;
		
		// Cierre de ExpTipo
		s += ">";
		
		return s;	
		
	}
	
	public CategoriaLexica getCategoriaLexica(TablaSimbolos ts){
		return CategoriaLexica.TArray;
	}
	
}
