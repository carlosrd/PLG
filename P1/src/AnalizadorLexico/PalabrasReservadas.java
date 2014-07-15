package AnalizadorLexico;

import java.util.HashSet;

/**
 * 
 * Clase que contiene una lista de palabras reservadas
 *
 */
public class PalabrasReservadas {
	
	private HashSet<String> palabras;
	
	public PalabrasReservadas()
	{
		palabras = inicializar();
	}

	/**
	 * Método que incluye en el array todas las palabras reservadas
	 */
	private HashSet<String> inicializar() {
		HashSet<String> p = new HashSet<String>();
		// Declaraciones
		p.add("program:");
		p.add("consts");		
		p.add("const");
		p.add("tipos");
		p.add("tipo");
		p.add("vars");
		p.add("var");
		p.add("subprograms");
		p.add("subprogram:");
		p.add("instructions");
		// Instrucciones Basicas
		p.add("swap1");
		p.add("swap2");
		p.add("in");
		p.add("out");
		// Instrucciones Condicionales
		p.add("if");
		p.add("then");
		p.add("else");
		p.add("endif");
		p.add("while");
		p.add("do");
		p.add("endwhile");
		// Instruccion de llamada a subprog
		p.add("call");
		// Tipos Basicos
		p.add("float");
		p.add("integer");
		p.add("natural");
		p.add("character");
		p.add("boolean");
		// Booleanos
		p.add("true");
		p.add("false");
		p.add("not");		
		p.add("and");
		p.add("or");
		p.add("not");
		return p;
	}
	
	/**
	 * M�todo para comprobar si una palabra es reservada
	 * @param str palabra a comprobar - String
	 * @return TRUE = Es reservada, FALSE = No es reservada
	 */
	public boolean esReservada(String str)
	{
		return palabras.contains(str);
	}

	/**
	 * Devuelve las palabras reservadas
	 */
	public HashSet<String> getPalabras() {
		return palabras;
	}

	/**
	 * A�ade las palabras reservadas pasadas como argumento
	 * @param palabras
	 */
	public void setPalabras(HashSet<String> palabras) {
		this.palabras = palabras;
	}
	
	
}
