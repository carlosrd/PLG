package AnalizadorSintactico.ExpTipos;

/**
 * Clase que encapsula la información asociada a los parámetros, así como su id, modo, tipo y desplazamiento
 */
public class Param {
	
	// ATRIBUTOS
	// ********************************************************************************
	
	private String id;
	private Modo modo;
	private ExpTipo tipo;
	private int desp;
	
	// CONSTRUCTORA
	// ********************************************************************************
	
	public Param(String id, Modo modo, ExpTipo tipo){
		this.id = id;
		this.tipo = tipo;
		this.modo = modo;
		//TODO Calcular desp como en las tuplas...
	}

	// CONSULTORAS
	// ********************************************************************************
	
	/**
	 *	permite obtener el id de un parámetro
	 */
	public String getId() {
		return id;
	}

	/**
	 * permite obtener el modo de un parámetro
	 */
	public Modo getModo() {
		return modo;
	}

	/**
	 * permite obtener la expresión de tipo asociada a un parámetro
	 */
	public ExpTipo getTipo() {
		return tipo;
	}

	/**
	 * permite obtener el desplazamiento asociado a un parámetro
	 */
	public int getDesp() {
		return desp;
	}
	
	/**
	 * permite especificar el desplazamiento de un parámetro
	 * @param desp
	 */
	public void setDesp(int desp){
		this.desp = desp;
	}
	
	/**
	 * permite especificar el modo de un parámetro
	 * @param m
	 */
	public void setModo(Modo m){
		this.modo = m;
	}
}
