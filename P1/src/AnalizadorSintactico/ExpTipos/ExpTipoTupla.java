package AnalizadorSintactico.ExpTipos;

import java.util.ArrayList;

import AnalizadorSintactico.TSimbolos.TablaSimbolos;
import Atributos.CategoriaLexica;

/**
 * Expresión de tipo enfocada a las tuplas
 *
 */
public class ExpTipoTupla extends ExpTipo {

	// ATRIBUTOS
	// ********************************************************************************
	private ArrayList<ElemTupla> tiposTupla;
	
	// CONSTRUCTORA
	// ********************************************************************************
	
	public ExpTipoTupla(ArrayList<ElemTupla> tiposTupla, TablaSimbolos ts) {
		super("tupla", ts);
		this.tiposTupla = tiposTupla;
		setearDesplazamientosTupla();
		this.tam = calcularTamTupla();

	}
	
	// CONSULTORAS
	// ********************************************************************************
	
	/**
	 * permite obtener todos los tipos distintos que tiene una tupla
	 */
	public ArrayList<ElemTupla> getTiposTupla() {
		return tiposTupla;
	}
	
	/**
	 * permite obtener la expresión de tipo asociado a un indice de la tupla
	 * @param pos
	 */
	public ExpTipo getTipoElementoTupla(int pos){
			return tiposTupla.get(pos).getExpTipo();
	}
	
	/**
	 * permite obtener el desplazamiento asociado a un elemento de la tupla
	 * @param pos
	 */
	public int getDespElementoTupla(int pos){
		return tiposTupla.get(pos).getDesp();
	}

	/**
	 * calcula el tamaño de la tupla
	 */
	private int calcularTamTupla(){
		int t = 0;
		if (!tiposTupla.isEmpty())
			t = tiposTupla.get(tiposTupla.size()-1).getExpTipo().tam +
					tiposTupla.get(tiposTupla.size()-1).getDesp();
		return t;
	}
	
	/**
	 * permite calcular los desplazamientos automáticamente de la tupla
	 */
	private void setearDesplazamientosTupla(){
		int des = 0;
		for (int i = 0; i < tiposTupla.size(); i++){
			tiposTupla.get(i).setDesp(des);
			des = des + tiposTupla.get(i).getExpTipo().tam;
		}
	}
	
	/**
	 * indica si la tupla está vacía
	 */
	public boolean esTuplaVacia(){
		return tiposTupla.isEmpty();
	}
	
	/**
	 * indica el número de elementos que tiene la tupla
	 */
	public int numTiposTupla(){
		return tiposTupla.size();
	}
	public CategoriaLexica getCategoriaLexica(TablaSimbolos ts){		
		return CategoriaLexica.TTupla;
	}

	/**
	 * convierte la información en un String que se puede usar despues en la TS
	 */
	public String toString(){
		
		// Apertura de ExpTipo
		String s = "<";
		
		// Relleno
		s += "t: " + this.t;
		s += ", elems: ";
		
		if (this.esTuplaVacia())
			s+="0";
		else {
			
			s += "\n\t[";
			
			for (int i=0; i < tiposTupla.size(); i++){
				
				ElemTupla tipoElem = tiposTupla.get(i);
				
				s += "<tipo: " + tipoElem.getExpTipo().toString();
				s += ", desp: " + tipoElem.getDesp();
					
				s += ">";
				
				if (i < tiposTupla.size()-1)
					s += ",\n\t";
				
			}
			s += "],";
		}

		 s += " tam: " + this.tam;

		// Cierre de ExpTipo
		s += ">";
		
		return s;	
		
	}
}
