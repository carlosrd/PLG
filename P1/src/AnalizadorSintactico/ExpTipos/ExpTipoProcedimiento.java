package AnalizadorSintactico.ExpTipos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import AnalizadorSintactico.TSimbolos.TablaSimbolos;
import Atributos.CategoriaLexica;

/**
 * Expresión de tipo con información adicional para los procedimientos
 */
public class ExpTipoProcedimiento extends ExpTipo {
	
	// ATRIBUTOS
	// ********************************************************************************
	
	private ArrayList<String> listaIdParams; //lista indexada de los nombres de los parámetros 
	private HashMap<String, Param> listaParams; //información de los parámetros por su nombre
	private int ini; //incio del subprograma

	// CONSTRUCTORA
	// ********************************************************************************
	
	public ExpTipoProcedimiento(ArrayList<String> ordenIdListaParam, HashMap<String, Param> listaParams, int ini, TablaSimbolos ts) {
		super("proc",ts);
		this.listaIdParams = ordenIdListaParam;
		this.listaParams = listaParams;
		this.ini = ini;
		setDespProc(ts);
		this.tam = 0;
	}
	
	// CONSULTORAS
	// ********************************************************************************
	
	/**
	 * devuelve la lista de los parámetros en el orden que han sido declarados
	 */
	public Iterator<Param> getListaParamEnOrden(){
		ArrayList<Param> p = new ArrayList<Param>();
		
		for (int i = 0; i < listaIdParams.size(); i++){
			p.add(listaParams.get(listaIdParams.get(i)));
		}
		return p.iterator();
	}
	
	/**
	 * Devuelve los nombres de los parámetros en el orden que han sido declarados
	 */
	public Iterator<String> getListaIdEnOrden(){
		return listaIdParams.iterator();
	}
	
	/**
	 * Comprueba si un procedimiento tiene un parámetro
	 * @param id
	 */
	public boolean existeParam(String id){
		if (listaParams.get(id) != null)
			return true;
		else
			return false;
	}
	
	/**
	 * Devuelve el modo por el que está pasado un parámetro específico
	 * @param id
	 */
	public Modo getParamModo(String id){
		return listaParams.get(id).getModo();
	}
	
	/**
	 * devuelve el tipo de un parámetro específico
	 * @param id
	 */
	public ExpTipo getParamTipo(String id){
		return listaParams.get(id).getTipo();
	}
	
	/**
	 * devuelve el desplazamiento de un parámetro específico
	 * @param id
	 */
	public int getParamDesp(String id){
		return listaParams.get(id).getDesp();
	}
	/**
	 * devuelve el número de parámetros de un procedimiento
	 */
	public int getSizeListaParams(){
		return listaParams.size();
	}
	
	/**
	 * devuelve la dirección de comienzo de un procedimiento
	 */
	public int getParamsIni(){
		return ini;
	}
	
	public CategoriaLexica getCategoriaLexica(TablaSimbolos ts){
		return CategoriaLexica.TProc;
	}
	// MODIFICADORAS
	// ********************************************************************************
	
	public void setIni(int ini){
		this.ini = ini;
	}
	
	/**
	 * 	setear desp teniendo en cuenta que si es por variable, el desp sera 1 y no
		el tamaño (al ser un puntero)
	 */
	private void setDespProc(TablaSimbolos ts){
		int des = 0;
		for (int i = 0; i < listaIdParams.size(); i++){
			listaParams.get(listaIdParams.get(i)).setDesp(des);
			if (ts.getClase(listaIdParams.get(i), 1).equals(ClaseDec.ParamVar))
			//if (listaParams.get(listaIdParams.get(i)).getModo().equals(Modo.Variable))
				des++;
			else
				des = des + listaParams.get(listaIdParams.get(i)).getTipo().tam;
		}
	}
	
	/**
	 * información convertida a un string para su posterior uso en la TS
	 */
	public String toString(){
		
		// Apertura de ExpTipo
		String s = "<";
		
		// Relleno
		s += "t: " + this.t;
		s += ", params: ";
		
		Iterator<Param> iteradorParams = getListaParamEnOrden();
		s += "\n\t[";
		
		while (iteradorParams.hasNext()) {
			
			Param p = iteradorParams.next();
			
			s += "<";
			
			s += " id: " + p.getId();
			s += ", tipo:" + p.getTipo().toString();
			s += ", modo: " + p.getModo();
			s += ", desp: " + p.getDesp();
			
			s += ">";
			
			if (iteradorParams.hasNext())
				s += ",\n\t ";
			
		}

		// Cierre de ExpTipo
		s += "]>";
		
		return s;	
		
	}
}
