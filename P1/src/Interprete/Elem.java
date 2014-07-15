package Interprete;

/**La clase Elem es un contenedor que almacena tanto el valor como el tipo de un dato.
 * Por otro lado, instrucciones como apila-dir y desapila-dir utilizan su campo valor a modo de dirección de acceso a memoria.
 */

public class Elem {
	private String tipo;	//String que puede ser "float", "integer", "natural", "boolean" o "character"
	private Object valor;	//Object para adoptar polimorfismo, una vez creado el objeto se concreta su tipo si es necesario
	
	public Elem (){
		this.setTipo(null);
		this.setValor(null);
	}
	/**
	 * 
	 * @param tipo: String
	 */
	public Elem(String tipo){
		this.setTipo(tipo);
		this.setValor(null);
	}
	/**
	 * 
	 * @param tipo: String
	 * @param valor: Object
	 */
	public Elem(String tipo, Object valor){		
		this.setTipo(tipo);
		this.setValor(valor);
	}
	
	/**
	 * Devuelve el tipo de Elem.
	 * @return tipo: String
	 */
	public String getTipo() {
		return tipo;
	}
	
	/**
	 * Devuelve el valor de Elem.
	 * @return valor: Object
	 */
	public Object getValor() {
		return valor;
	}
		
	/**
	 * Asigna el tipo t al Elem.
	 * @param tipo: String
	 */
	public void setTipo(String t) {
		this.tipo = t;
	}
	
	/**
	 * Asigna v como valor de Elem.
	 * @param valor: Object
	 */
	public void setValor(Object v) {
		this.valor = v;
	}	
	

	public boolean equals(Elem e) {
		if (this == e)
			return true;
		if (e == null)
			return false;
		return tipo.equals(e.getTipo()) & valor.equals(e.getValor());
	}
	
	public String toString(){
        String valorString;
		if(tipo.equals("character"))        	
			//valorString = "'" + (char)valor + "'";
			valorString = "" + (Character)valor;
        else if(tipo.equals("boolean"))        	
        	if((Boolean)valor)
        		valorString = "true";
        	else
        		valorString = "false";
        else 
        	valorString = valor.toString();
		//return "("+ tipo + ", " + valorString + ")";
		return valorString;
	}
}
