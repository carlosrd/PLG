package AnalizadorSintactico.ExpTipos;

/**
 * Clase que contiene la información acerca de los elementos de una tupla, como la expresión de tipos
 * de sus elementos y el desplazamiento asociado
 */
public class ElemTupla {
	
	private ExpTipo expTipo;
	private int desp;
	
	/**
	 * @deprecated
	 * @param expTipo
	 * @param desp
	 */
	public ElemTupla(ExpTipo expTipo, int desp){
		this.expTipo = expTipo;
		this.desp = desp;
	}
	
	/**
	 * Construcción de un elemento de la tupla, pasándo una expresión de tipo
	 * @param expTipo
	 */
	public ElemTupla(ExpTipo expTipo){
		this.expTipo = expTipo;
	}

	/**
	 * Permite obtener la Expresión de tipo de un elemento de la tupla
	 * @return
	 */
	public ExpTipo getExpTipo() {
		return expTipo;
	}

	/**
	 * Permite obtener el desplazamiento asociado a un elemento de la tupla
	 * @return
	 */
	public int getDesp() {
		return desp;
	}
	
	/**
	 * permite definir el desplazamiento asociado a un elemento de la tupla
	 * @param desp
	 */
	public void setDesp(int desp){
		this.desp = desp;
	}
	
}
