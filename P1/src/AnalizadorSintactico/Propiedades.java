package AnalizadorSintactico;


import AnalizadorSintactico.ExpTipos.*;

/**
 * Objeto que hace de 4-tupla y valor en la tabla (clave-valor) de simbolos para 
 * recoger los datos de una declaraci�n
 */
public class Propiedades implements Cloneable {

	// ATRIBUTOS
	// ********************************************************************************
	
	private int dirMem;
	private ClaseDec clase;
	private ExpTipo expTipo;
	private int ini;
	
	// CONSTRUCTORA
	// ********************************************************************************
	
	public Propiedades(int d, ClaseDec c, ExpTipo expTipo){
		
		this.dirMem = d;
		this.clase = c;			// Clase: 
		this.expTipo = expTipo;
		this.ini = 0;
		
		if (c.equals(ClaseDec.Procedimiento)){
			this.ini = ((ExpTipoProcedimiento) expTipo).getParamsIni();
		}
		
	}


	// CONSULTORAS
	// ********************************************************************************
	
	
	/**
	 * Obtiene la direcci�n de memoria de la variable
	 */
	public int getDirMem() {
		return dirMem;
	}
	
	/**
	 * Devuelve la clase del tipo
	 */
	public ClaseDec getClaseDeclaracion() {
		return this.clase;
	}
	
	
	public ExpTipo getExpTipo(){
		return this.expTipo;
	}
	
	public String getTipo() {
		return this.expTipo.getT();
	}
	
	/**
	 * Devuelve el valor para las constantes
	 * @return valor de la constante
	 */
	public String getValor() {
		if (expTipo instanceof ExpTipoConstante)
			return ((ExpTipoConstante) expTipo).getValor();
		return null;
	}
	
	public boolean esConstante(){
		if (expTipo instanceof ExpTipoConstante)
			return true;
		return false;
	}
	
	public int getIni(){
		return this.ini;
	}
	// MODIFICADORAS
	// ********************************************************************************
	
	/**
	 * Setea la direcci�n de memoria de la variable
	 * @param _dirMem
	 */
	public void setDirMem(int _dirMem) {
		this.dirMem = _dirMem;
	}

	/**
	 * Setea el tipo de la variable o constante 
	 */
	public void setClaseDeclaracion(ClaseDec c) {
		this.clase = c;
	}

	/**
	 * Setea el valor para las constantes
	 * @param _valor de la constante
	 */
	public void set_valor(String _valor) {
		if (expTipo instanceof ExpTipoConstante)
			((ExpTipoConstante) expTipo).setValor(_valor);	
	}

	 public Object clone(){
	        Object obj=null;
	        try{
	            obj=super.clone();
	        }catch(CloneNotSupportedException ex){
	            System.out.println(" no se puede duplicar");
	        }
	        return obj;
	    }
	
}
