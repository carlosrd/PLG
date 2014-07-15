package AnalizadorSintactico.ExpTipos;

/**
 * Expresión de tipo que indica que ha habido un error en las expresiones de tipo
 */
public class ExpTipoError extends ExpTipo{

	public ExpTipoError() {
		super();
		this.t = "err";
	}

	/**
	 * Permite sacar la iformación a un String y usarlo en la TS si es necesario
	 */
	public String toString(){
		
		// Apertura de ExpTipo
		String s = "<";
		
		// Relleno
		s += "t: " + this.t;
		
		// Cierre de ExpTipo
		s += ">";
		
		return s;	
		
	}
}
