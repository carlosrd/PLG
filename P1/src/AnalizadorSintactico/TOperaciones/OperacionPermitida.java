package AnalizadorSintactico.TOperaciones;

import Atributos.CategoriaLexica;

/**
 * Tupla que almacena los tipos de datos v�lidos para una operaci�n concreta del lenguaje
 * Ej: Suma entre enteros, entre natural y entero... etc
 */
public class OperacionPermitida {

	
	// ATRIBUTOS
	// ***********************************************************************
	
	CategoriaLexica tipo1;
	CategoriaLexica operacion;
	CategoriaLexica tipo2;
	
	String lex; //solo usado para or, not y and
	
	
	// CONSTRUCTORA
	// ***********************************************************************
	
	/**
	 * Construye una nueva Operaci�n Permitida en la que el operador es un s�mbolo
	 * @param tipo1	del operador A
	 * @param operacion a realizar
	 * @param tipo2 del operador B
	 */
	public OperacionPermitida(CategoriaLexica tipo1, CategoriaLexica operacion,CategoriaLexica tipo2) {
		this.tipo1 = tipo1;
		this.tipo2 = tipo2;
		this.operacion = operacion;
		this.lex = null;
	}
	
	/**
	 * Construye una nueva Operaci�n Permitida en la que el operador es una palabra reservada 
	 * @param tipo1	del operador A
	 * @param operacion a realizar
	 * @param tipo2 del operador B
	 * @param lex palabra reservadas que representa el operador
	 */
	public OperacionPermitida(CategoriaLexica tipo1, CategoriaLexica operacion, String lex,CategoriaLexica tipo2) {
		this.tipo1 = tipo1;
		this.tipo2 = tipo2;
		this.operacion = operacion;
		this.lex = lex;
	}

	// METODOS PARA COMPARAR OPERACIONES
	// Necesarios para que la tabla hash no falle al comparar 
	// ***********************************************************************
	/**
	 * Crea un Hash Code unico para los objetos de tipo Operaci�n Permitida 
	 * Sirve para encontrar coincidencias en la tabla de Operaciones Permitidas
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lex == null) ? 0 : lex.hashCode());
		result = prime * result
				+ ((operacion == null) ? 0 : operacion.hashCode());
		result = prime * result + ((tipo1 == null) ? 0 : tipo1.hashCode());
		result = prime * result + ((tipo2 == null) ? 0 : tipo2.hashCode());
		return result;
	}

	/**
	 * Compara 2 objetos de tipo Operaci�n Permitida y devuelve si son iguales (son la misma
	 * operaci�n para los mismos tipos)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		OperacionPermitida otra = (OperacionPermitida) obj;
		if (lex == null) {
			if (otra.lex != null)
				return false;
		} else if (!lex.equals(otra.lex))
			return false;
		if (operacion != otra.operacion)
			return false;
		if (tipo1 != otra.tipo1)
			return false;
		if (tipo2 != otra.tipo2)
			return false;
		return true;
	}

}