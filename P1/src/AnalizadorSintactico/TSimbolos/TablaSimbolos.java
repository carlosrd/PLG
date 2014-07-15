package AnalizadorSintactico.TSimbolos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import AnalizadorSintactico.Propiedades;
import AnalizadorSintactico.ExpTipos.ClaseDec;
import AnalizadorSintactico.ExpTipos.ElemTupla;
import AnalizadorSintactico.ExpTipos.ExpTipo;
import AnalizadorSintactico.ExpTipos.ExpTipoArray;
import AnalizadorSintactico.ExpTipos.ExpTipoConstante;
import AnalizadorSintactico.ExpTipos.ExpTipoError;
import AnalizadorSintactico.ExpTipos.ExpTipoProcedimiento;
import AnalizadorSintactico.ExpTipos.ExpTipoTupla;
import AnalizadorSintactico.ExpTipos.Param;
import Atributos.CategoriaLexica;

/**
 * Tabla Clave (String (identificador)) - Valor (Propiedades) que almacena las declaraciones
 * de variables y constantes realizadas en la secci�n de declaraciones
 */
public class TablaSimbolos {
	
	public static final String REF= "ref";
	
	// ATRIBUTOS
	// ***********************************************************************
	
	private HashMap<ClaveTS,Propiedades> ts;
	//private ArrayList<Propiedades> ts;
	
	
	// CONSTRUCTORA
	// ***********************************************************************
	/**
	 * Crea una nueva tabla de s�mbolos
	 */
	public TablaSimbolos(){
		//ts = new ArrayList<Propiedades>();
		ts = new HashMap<ClaveTS,Propiedades>();

	}
	
	// GETTERS (No incluyen el parametro TS, ya que es la propia clase)
	// ****************************************************************************
	
	/**
	 * Devuelve la tabla de simbolos
	 * @return Tabla S�mbolos
	 */
	public HashMap<ClaveTS,Propiedades> getTS() {
		return ts;
	}
	
	/**
	 * Obtiene las propiedades (direccion, constante/variable, valor) para una variable
	 * o constante con identificador (id)
	 * @param id de la variable o constante
	 * @param nivel nivel de la variable (global o local)
	 * @return Propiedades de la variable o constante id
	 */
	public Propiedades getPropiedades(String id, int nivel){
		return ts.get(new ClaveTS(id,nivel));
	}
	
	/** 
	 * Devuelve el nivel (0-1) más alto en el que esta el identificador "id"
	 * @param id
	 * @return nivel
	 */
	public int getNivel(String id){
		if (ts.containsKey(new ClaveTS(id,1)))
			return 1;
		else if (ts.containsKey(new ClaveTS(id,0)))
				return 0;
		else 
			return -1;
	}
	
	/**
	 * El resultado es el registro Dir de propiedades de id en el nivel n.
	 * @param id
	 */
	
	public int getDir(String id, int nivel){
		
		Propiedades p = ts.get(new ClaveTS(id,nivel));
		if (p != null)
			return p.getDirMem();
		else 
			return -1;
			
	}
	
	/*
	public int getDir(String id){
		if (existeID(id))
			return getPropiedades(id).getDirMem();
		return -1;
	}*/
	
	/**
	 * El resultado es el campo valor de una constante
	 * @param id
	 * @return tipoBasico
	 */
	public CategoriaLexica getValor(String id, int nivel){
		
		Propiedades p = ts.get(new ClaveTS(id,nivel));
		if (p != null)
			return getCategoriaLexica(p.getValor());
		else
			return CategoriaLexica.TError;
	}
	
	public CategoriaLexica getCategoriaLexica(String t){
		
		if (t.equals("natural"))
			return CategoriaLexica.LitNatural;
		if (t.equals("boolean"))
			return CategoriaLexica.LitBooleano;
		if (t.equals("integer"))
			return CategoriaLexica.LitEntero;
		if (t.equals("float"))
			return CategoriaLexica.LitDecimal;
		if (t.equals("character"))
			return CategoriaLexica.LitCaracter;
		
		return CategoriaLexica.TError;
	}
	
	/*
	
	/**
	 * 	El resultado es la expresión de tipo de las propiedades de id en el nivel n.
	 * @param id
	 * @return
	 */
	public ExpTipo getExprTipo(String id, int nivel){
		
		Propiedades p = ts.get(new ClaveTS(id,nivel));
		if (p != null)
			return p.getExpTipo();
		else
			return null;
	}

	
	/**
	 * Devuelve el campo tipo del par�metro con nombre id del procedimiento proc.
	 * @param id_proc
	 * @param id_param
	 */
	public ExpTipo getParam(int nivel, String id_proc, String id_param){
		
		Propiedades p = ts.get(new ClaveTS(id_proc,nivel));
		
		if (p != null){
			ExpTipoProcedimiento e = (ExpTipoProcedimiento) p.getExpTipo();
			return e.getParamTipo(id_param);
		}
		
		return null;
	}
	
	public ClaseDec getClase(String id, int nivel){
		
		Propiedades p = ts.get(new ClaveTS(id,nivel));
		
		if (p != null)
			return p.getClaseDeclaracion();
		
		return null;
	}
	
	public TablaSimbolos clone(){
		TablaSimbolos t = new TablaSimbolos();
		
		HashMap<ClaveTS,Propiedades> hm = new HashMap<ClaveTS,Propiedades>();
		Set<Entry<ClaveTS, Propiedades>> set1 = ts.entrySet();
		
		for (Entry<ClaveTS, Propiedades> e : set1)
		    hm.put((ClaveTS)e.getKey().clone(), (Propiedades)e.getValue().clone());
		//t.setTS((HashMap<ClaveTS, Propiedades>) ts.clone());
		t.setTS(hm);
		return t;
		
	}
	
	// SETTERS
	// ***********************************************************************
	


	/**
	 * Setea la tabla actual a la tabla pasada por parámetro
	 * @param ts tabla de símbolos nueva
	 */
	public void setTS(HashMap<ClaveTS,Propiedades> ts){
		this.ts = ts;
	}

	
	// METODOS
	// ***********************************************************************
	
	/**
	 * Añade un id a la tabla si no se encuentra ya en ella. Si no lo añade, devuelve un booleano que indica
	 * que ya está en la TS
	 * @param id - identificador
	 * @param ps - propiedades asociadas al identificador
	 * @return boolean (TRUE si ya estaba el identificador)
	 */
	
	public boolean añadeID(String id, int nivel, Propiedades ps){
		
		if (!ts.containsKey(new ClaveTS(id,nivel))){
			ts.put(new ClaveTS(id,nivel), ps);	
			return true;
			}
		else
			return false;
	}

	
	/**
	 * Recide el lexema de un identificador y dice si ya está en la tabla
	 * @param id identificador que se quiere comprobar
	 * @param nivel nivel en el que se quiere comprobar (local o global)
	 * @return boolean (TRUE si esta ya el identificador)
	 */
	public boolean existeID(String id, int nivel){
		return ts.containsKey(new ClaveTS(id,nivel));
	}
	
	/**
	 * Devuelve el tipo base de una expresión
	 */
	public ExpTipo ref(ExpTipo expTipo){
		if (expTipo.getT().equals(REF)){
			// Es una referencia, por tanto hay que buscar la base
			if (existeID(expTipo.getIdTipo(), 0)){
				return ref(getExprTipo(expTipo.getIdTipo(), 0));
			}
			else {
				return new ExpTipoError();
			}
		}
		return expTipo;
	}
	
	/**
	 * Comprueba si dos expresiones de tipo son compatibles
	 * @param e1
	 * @param e2
	 * @return TRUE si son compatibles
	 */
	public boolean compatibles(ExpTipo e1, ExpTipo e2){
		
		ExpTipo e11 = ref(e1);
		ExpTipo e12 = ref(e2);
		
		if (e11.getT().equals("integer") && e12.getT().equals("integer"))
			return true;
		
		if (e11.getT().equals("integer") && e12.getT().equals("natural"))
			return true;

		if (e11.getT().equals("boolean") && e12.getT().equals("boolean"))
			return true;
		
		if (e11.getT().equals("natural") && e12.getT().equals("natural"))
			return true;
		
		if (e11.getT().equals("float") && e12.getT().equals("float"))
			return true;
		
		if (e11.getT().equals("float") && e12.getT().equals("integer"))
			return true;
		
		if (e11.getT().equals("float") && e12.getT().equals("natural"))
			return true;
		
		
		if (e11.getT().equals("character") && e12.getT().equals("character"))
			return true;
		
		if (e11.getT().equals("array") && e12.getT().equals("array")){
			//comprobar que tienen el mismo num de elem y el mismo tbase
			return (((ExpTipoArray) e11).getNumElems() == ((ExpTipoArray) e12).getNumElems()) &&
					compatibles(((ExpTipoArray) e11).getTBase(),((ExpTipoArray) e12).getTBase());
		}
		
		if (e11.getT().equals("tupla") && e12.getT().equals("tupla")){
			//Comprobamos que tengan el mismo num de tipos
			if (((ExpTipoTupla) e11).numTiposTupla() != ((ExpTipoTupla) e12).numTiposTupla())
				return false;
			//Ahora comprobamos que los tipos sean compatibles
			for (int i = 0; i < ((ExpTipoTupla) e11).numTiposTupla(); i++ ){
				if (!compatibles(((ExpTipoTupla) e11).getTipoElementoTupla(i),((ExpTipoTupla) e12).getTipoElementoTupla(i)))
					return false;
			}
			return true;
		}
		return false;
	}
	
	public boolean existeParam(String idSubProg, String idParam){
		
		ExpTipoProcedimiento expSub = (ExpTipoProcedimiento) getExprTipo(idSubProg,0);
		return expSub.existeParam(idParam);
			
	}
	/**
	 * Comprueba que la lista de parametros del procedimiento proc esté completa
	 */
	public boolean listaParamsCompleta(ArrayList<String> listaParams, String idProc){
		// Si no existe el nombre del procedimiento, no está completa
		if (!existeID(idProc, 0))
			return false;
				
		ExpTipoProcedimiento expProc = (ExpTipoProcedimiento) getExprTipo(idProc, 0);
		int numP = expProc.getSizeListaParams();
		// Si no tienen la misma longitud, tampoco
		if (numP != listaParams.size())
			return false;		

		return true;
	}
	
	
	
	//********************************************************************************************************************

	public static void main(String[] args){
		// Añadimos un tipo básico a la ts
		TablaSimbolos ts = new TablaSimbolos();
		
		/*
		 * añadimosuna constante global
		 *  const boolean miIden := true
		 */
		ExpTipo et1 = new ExpTipoConstante("boolean", ts, "true"); //(String t, int tam,String valor)
		Propiedades p1 = new Propiedades(-1, ClaseDec.Constante, et1); //(int d, ClaseDec c, ExpTipo expTipo)
		ts.añadeID("miIden", 0, p1);
		
		ts.mostrarDatosPantalla("miIden",0);
		
		/*
		 * añadimosuna variable global
		 *  tipo integer[3] miTipoArray 
		 */
		ExpTipo et2 = new ExpTipoArray(3, ts,new ExpTipo("integer",ts)); //(int tam,int nElems, String tbase)
		Propiedades p2 = new Propiedades(-1, ClaseDec.Tipo, et2);//(int d, ClaseDec c, ExpTipo expTipo)
		ts.añadeID("miTipoArray",0,p2);
		
		ts.mostrarDatosPantalla("miTipoArray",0);
		
		/*
		 * añadimosotra variable local
		 * var miTuplaArray (integer,boolean,miTipoArray)[2]
		 */
		ArrayList<ElemTupla> tiposTupla = new ArrayList<ElemTupla>();
		tiposTupla.add(new ElemTupla(new ExpTipo("integer",ts)));
		tiposTupla.add(new ElemTupla(new ExpTipo("boolean",ts)));
		tiposTupla.add(new ElemTupla(ts.getExprTipo("miTipoArray",0)));
		ExpTipo etTup = new ExpTipoTupla(tiposTupla,ts);
		ExpTipo et3 = new ExpTipoArray(2,ts,etTup);
		Propiedades p3 = new Propiedades(5, ClaseDec.Variable, et3);
		ts.añadeID("miTuplaArray", 0, p3);
		
		ts.mostrarDatosPantalla("miTuplaArray",0);
		
		/*
		 * tipo integer tipoEntero
		 */
		ExpTipo et4 = new ExpTipo("integer",ts);
		Propiedades p4 = new Propiedades(-1, ClaseDec.Tipo, et4);
		ts.añadeID("tipoEntero", 0, p4);
		
		ts.mostrarDatosPantalla("tipoEntero",0);
		
		/*
		 * Declaramos una variable con dicho alias
		 * var miEntero i
		 */
		
		ExpTipo et5 = new ExpTipo("tipoEntero",ts);
		Propiedades p5 = new Propiedades(16, ClaseDec.Variable, et5);
		ts.añadeID("i", 0, p5);
		
		ts.mostrarDatosPantalla("i",0);
		
		/*
		 * tipo (entero,miArray) miTipo
		 */
		ArrayList<ElemTupla> tiposTupla6 = new ArrayList<ElemTupla>();
		
		tiposTupla6.add(new ElemTupla(ts.getExprTipo("tipoEntero",0)));
		tiposTupla6.add(new ElemTupla(ts.getExprTipo("miTipoArray", 0)));
		ExpTipo et6 = new ExpTipoTupla(tiposTupla6,ts);
		Propiedades p6 = new Propiedades(-1, ClaseDec.Tipo, et6);
		
		ts.añadeID("miTipo", 0, p6);
		
		ts.mostrarDatosPantalla("miTipo",0);
		
		/*
		 * tipo miTipo elSuperTipo
		 */
		ExpTipo et7 = new ExpTipo("miTipo",ts); 
		Propiedades p7 = new Propiedades(-1, ClaseDec.Tipo, et7);
		ts.añadeID("elSuperTipo", 0, p7);
		
		ts.mostrarDatosPantalla("elSuperTipo",0);
		
		/*
		 * tipo (miTipo,elSuperTipo) elTipoSupremo 
		 */
		ArrayList<ElemTupla> tiposTupla8 = new ArrayList<ElemTupla>();
		
		tiposTupla8.add(new ElemTupla(ts.getExprTipo("miTipo",0)));
		tiposTupla8.add(new ElemTupla(ts.getExprTipo("elSuperTipo", 0)));
		ExpTipo et8 = new ExpTipoTupla(tiposTupla8,ts);
		Propiedades p8 = new Propiedades(-1, ClaseDec.Tipo, et8);
		
		ts.añadeID("elTipoSupremo", 0, p8);
		
		ts.mostrarDatosPantalla("elTipoSupremo",0);
		
		/*
		 * creamos 1 proc, con param por valor y por ref
		 * subprogram: leeMatriz (miTipo j,elSuperTipo * e,natural n)
		 */
		
			// Creamos los locales -> miTipo j
			ExpTipo etParam0 = ts.getExprTipo("miTipo", 0);
			Propiedades param0 = new Propiedades(0, ClaseDec.Variable, etParam0);
			ts.añadeID("j", 1, param0);
			
			ts.mostrarDatosPantalla("j",1);
			
			// Creamos los locales -> elSuperTipo* e
			ExpTipo etParam2 = ts.getExprTipo("elSuperTipo", 0);
			Propiedades param2 = new Propiedades(0, ClaseDec.ParamVar, etParam2);
			ts.añadeID("e", 1, param2);
			
			ts.mostrarDatosPantalla("e",1);
			
			// Creamos los locales -> natural n
			ExpTipo etParam1 = new ExpTipo("natural",ts);
			Propiedades param1 = new Propiedades(0, ClaseDec.Variable, etParam1);
			ts.añadeID("n", 1, param1);
			
			ts.mostrarDatosPantalla("n",1);
			
			
			// creamos el proc
			HashMap<String,Param> listaParam = new HashMap<String,Param>();
			//Param p_0 = new Param("j",ts.getExprTipo("j", 1));
			//Param p_1 = new Param("e",ts.getExprTipo("e", 1));
			//Param p_2 = new Param("n",ts.getExprTipo("n", 1)); //String id, Modo modo, String tipo)
			
			// Para setear el orden
			ArrayList<String> listaIdParams = new ArrayList<String>();
			listaIdParams.add("j");
			listaIdParams.add("e");
			listaIdParams.add("n");
			
			// no importa como los a�adamos al hashmap...
		//	listaParam.put("j", p_0);
		//	listaParam.put("e", p_1);	
		//	listaParam.put("n", p_2);
				

			
			ExpTipo etProc = new ExpTipoProcedimiento(listaIdParams,listaParam,0,ts);
			Propiedades pProc = new Propiedades(-1, ClaseDec.Procedimiento, etProc);
			ts.añadeID("leeMatriz", 0, pProc);
			
			ts.mostrarDatosPantalla("leeMatriz",0);
		
		/*
		 * creamos 1 var local, 1 con el mismo nombre que la const
		 */
			
		ExpTipo et10 = new ExpTipo("tipoEntero",ts);
		Propiedades p10 = new Propiedades(16, ClaseDec.Variable, et10);
		ts.añadeID("miIden", 1, p10);
			
		ts.mostrarDatosPantalla("miIden",1);
		
		System.out.println("--COMPATIBLES--");
		
		
		ts.mostrarCompatibilidad("miIden",0,"miTipoArray",0,ts);
		ts.mostrarCompatibilidad("integer[3]",0,"miTipoArray",0,ts);
		ts.mostrarCompatibilidad("tipoEntero",0,"i",0,ts);
		ts.mostrarCompatibilidad("miTipo",0,"elSuperTipo",0,ts);
		ts.mostrarCompatibilidad("elTipoSupremo",0,"elSuperTipo",0,ts);
		
	}
	
	/**
	 * ONLY DEBUG
	 * @param id
	 */
	public void mostrarDatosPantalla(String id, int nivel){
		System.out.print("----");
		System.out.print(id);
		System.out.println("----");
		System.out.print("ExisteID: ");
		System.out.println(existeID(id, nivel));
		System.out.print("Nivel: ");
		System.out.println(nivel);
		System.out.print("Clase: ");
		System.out.println(getPropiedades(id, nivel).getClaseDeclaracion());
		
		System.out.print("tipo: ");
		System.out.println(this.getExprTipo(id, nivel).getT());
		
		if (this.getExprTipo(id, nivel).getT().equals("ref")){
			System.out.print("id: ");
			System.out.println(getExprTipo(id,nivel).getIdTipo());
			System.out.print("ref_base ->: ");
			System.out.println(ref(getExprTipo(id, nivel)).getT());
		}
		
		if (this.getExprTipo(id, nivel).getT().equals("tupla")){
			System.out.println("-> Elementos :");
			for (int i = 0; i < ((ExpTipoTupla) this.getExprTipo(id, nivel)).getTiposTupla().size(); i++){
				System.out.print("---->");
				System.out.print("t: ");
				if (((ExpTipoTupla) this.getExprTipo(id, nivel)).getTiposTupla().get(i).getExpTipo().getT().equals("ref")){
					System.out.println("ref");
					System.out.print("--->id: ");
					System.out.println(((ExpTipoTupla) this.getExprTipo(id, nivel)).getTiposTupla().get(i).getExpTipo().getIdTipo());
				}
				else
					System.out.println(((ExpTipoTupla) this.getExprTipo(id, nivel)).getTiposTupla().get(i).getExpTipo().getT());
				System.out.print("---->tam: ");
				System.out.println(((ExpTipoTupla) this.getExprTipo(id, nivel)).getTiposTupla().get(i).getExpTipo().getTam());
				System.out.print("---->desp: ");
				System.out.println(((ExpTipoTupla) this.getExprTipo(id, nivel)).getTiposTupla().get(i).getDesp());
			}
		}
		
		if (this.getExprTipo(id, nivel).getT().equals("proc")){
			System.out.println("Params :");
			Iterator<Param> it = ((ExpTipoProcedimiento) getExprTipo(id, nivel)).getListaParamEnOrden();
			
			while (it.hasNext()){
				Param p = it.next();
				
				System.out.print("------> id: ");
				System.out.println(p.getId());
				
				System.out.print("------> modo :");
				System.out.println(p.getModo());
				
				System.out.print("--> t: ");
				System.out.println(p.getTipo().getT());
				
				if (p.getTipo().getT().equals("ref")){
					System.out.print("--> id:");
					System.out.println(p.getTipo().getIdTipo());
				}
				
				System.out.print("--> tam:");
				System.out.println(p.getTipo().getTam());
				
				System.out.print("------> desp: ");
				System.out.println(p.getDesp());
				
				System.out.println(",");
				
			}
			
		}
		
		System.out.print("dir: ");
		System.out.println(getDir(id, nivel));
		System.out.print("tam: ");
		System.out.println(getPropiedades(id, nivel).getExpTipo().getTam());
	}
	
	private void mostrarCompatibilidad(String id1, int nivel1, String id2, int nivel2,TablaSimbolos ts){
		System.out.print("--Compatibles ");
		System.out.print(id1);
		System.out.print(",N:");
		System.out.print(nivel1);
		System.out.print(" y ");
		System.out.print(id2);
		System.out.print(",N:");
		System.out.print(nivel2);
		
		if (id1.equals("integer[3]")){
			
			ExpTipo et2 = new ExpTipoArray(3, ts,new ExpTipo("integer",ts));
			
			if (compatibles(et2,getExprTipo(id2, nivel2)))
				System.out.println(" --- �COMPATIBLES!");
		}
		else if (compatibles(getExprTipo(id1, nivel1),getExprTipo(id2, nivel2))){
			System.out.println(" --- �COMPATIBLES!");
		}
		else
			System.out.println(" --- NOO compatibles");
	}
}
