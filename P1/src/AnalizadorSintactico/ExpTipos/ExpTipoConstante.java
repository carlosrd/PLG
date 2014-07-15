package AnalizadorSintactico.ExpTipos;

import AnalizadorSintactico.TSimbolos.TablaSimbolos;
import Atributos.CategoriaLexica;

public class ExpTipoConstante extends ExpTipo{
	
	// ATRIBUTOS
	// ********************************************************************************
	
	private String valor;
	
	// CONSTRUCTORA
	// ********************************************************************************
	
	public ExpTipoConstante(String t,TablaSimbolos ts,String valor){
		super(t,ts);
		this.valor = valor;
	}
	
	// CONSULTORAS
	// ********************************************************************************
	
	public String getValor() {
		return valor;
	}
	
	public CategoriaLexica getCategoriaLexica(TablaSimbolos ts){
		
		if (super.esBasico(t))
			return ts.getCategoriaLexica(t);
		else 
			return CategoriaLexica.TError;
	}
	
	// MODIFICADORAS
	// ********************************************************************************
	
	/**
	 * @param _valor que definirá el valor de una constante
	 */
	public void setValor(String _valor) {
		this.valor = _valor;
	}
	
	/**
	 * Muestra la información en un string para añadir a la TS
	 */
	public String toString(){
		
		// Apertura de ExpTipo
		String s = "<";
		
		// Relleno
		s += "t: " + this.t;
		s += ", tam: " + this.tam;
		s += ", valor: " + this.valor;
		
		// Cierre de ExpTipo
		s += ">";
		
		return s;
			
	}
	
}
