package AnalizadorSintactico.TOperaciones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import AnalizadorSintactico.ExpTipos.ElemTupla;
import AnalizadorSintactico.ExpTipos.ExpTipo;
import AnalizadorSintactico.ExpTipos.ExpTipoArray;
import AnalizadorSintactico.ExpTipos.ExpTipoError;
import AnalizadorSintactico.ExpTipos.ExpTipoTupla;
import AnalizadorSintactico.TSimbolos.TablaSimbolos;
import Atributos.CategoriaLexica;

/**
 * Tabla Clave (OperacionPermitida) - Valor (Tipo Resultante) que almacena las operaciones permitidas
 * en el lenguaje y el tipo que se devuelve al realizar dichas operaciones
 */
public class TablaOperaciones {
		
	// ATRIBUTOS
	// ***********************************************************************
	
	private HashMap<OperacionPermitida, ExpTipo> operaciones;
	private TablaSimbolos ts;
	
	// CONSTRUCTORA
	// ***********************************************************************
	
	/**
	 * Construye y rellena la Tabla de Operaciones Permitidas
	 */
	public TablaOperaciones(TablaSimbolos ts_h){
		operaciones = new HashMap<OperacionPermitida,ExpTipo>();
		ts = ts_h;	
		
		// Operaciones permitidas para ':='
		// -----------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.OpAsignConst,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.OpAsignConst,CategoriaLexica.LitEntero), new ExpTipo("float",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.OpAsignConst,CategoriaLexica.LitNatural), new ExpTipo("float",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero, CategoriaLexica.OpAsignConst,CategoriaLexica.LitEntero), new ExpTipo("integer",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero, CategoriaLexica.OpAsignConst,CategoriaLexica.LitNatural), new ExpTipo("integer",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.OpAsignConst,CategoriaLexica.LitNatural), new ExpTipo("natural",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitCaracter,CategoriaLexica.OpAsignConst,CategoriaLexica.LitCaracter),new ExpTipo("character",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitBooleano,CategoriaLexica.OpAsignConst,CategoriaLexica.LitBooleano), new ExpTipo("boolean",ts));
		
		// Operaciones permitidas para '='
		// ----------------------------------------------------------------		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.OpAsignVar,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.OpAsignVar,CategoriaLexica.LitEntero), new ExpTipo("float",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.OpAsignVar,CategoriaLexica.LitNatural), new ExpTipo("float",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero, CategoriaLexica.OpAsignVar,CategoriaLexica.LitEntero), new ExpTipo("integer",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero, CategoriaLexica.OpAsignVar,CategoriaLexica.LitNatural), new ExpTipo("integer",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.OpAsignVar,CategoriaLexica.LitNatural), new ExpTipo("natural",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitCaracter,CategoriaLexica.OpAsignVar,CategoriaLexica.LitCaracter), new ExpTipo("character",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitBooleano,CategoriaLexica.OpAsignVar,CategoriaLexica.LitBooleano), new ExpTipo("boolean",ts));
		
		// Operaciones permitidas para 'and'
		// ----------------------------------------------------------------		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitBooleano,CategoriaLexica.PalabraReservada,"and",CategoriaLexica.LitBooleano), new ExpTipo("boolean",ts));
	
		// Operaciones permitidas para 'or'
		// ----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitBooleano,CategoriaLexica.PalabraReservada,"or",CategoriaLexica.LitBooleano), new ExpTipo("boolean",ts));
		
		// Operaciones permitidas para 'not'
		// ----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitBooleano,CategoriaLexica.PalabraReservada,"not",null), new ExpTipo("boolean",ts));
						
		// Operaciones permitidas para %
		// ----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Modulo,CategoriaLexica.LitNatural), new ExpTipo("integer",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Modulo,CategoriaLexica.LitNatural), new ExpTipo("natural",ts));
	
		// Operaciones permitidas para <<
		// ----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.DesplazIz,CategoriaLexica.LitNatural), new ExpTipo("natural",ts));		
		
		// Operaciones permitidas para >>
		// ----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.DesplazDer,CategoriaLexica.LitNatural), new ExpTipo("natural",ts));		
				
		// Operaciones permitidas para - TODO// PODEMOS USAR EL MENOS COMO EL UNARIO SIN PROBLEMA??
									//XXX Existe la distincion en las producciones, asique no xD
		// ----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Menos,null), new ExpTipo("float",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Menos,null), new ExpTipo("integer",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Menos,null), new ExpTipo("integer",ts));
		
		// Operaciones permitidas para +/-
		// ----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Mas,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Menos,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));
		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Mas,CategoriaLexica.LitEntero), new ExpTipo("float",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Menos,CategoriaLexica.LitEntero), new ExpTipo("float",ts));

		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Mas,CategoriaLexica.LitNatural), new ExpTipo("float",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Menos,CategoriaLexica.LitNatural), new ExpTipo("float",ts));
		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Mas,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Menos,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));
		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Mas,CategoriaLexica.LitEntero), new ExpTipo("integer",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Menos,CategoriaLexica.LitEntero), new ExpTipo("integer",ts));
		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Mas,CategoriaLexica.LitNatural), new ExpTipo("integer",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Menos,CategoriaLexica.LitNatural), new ExpTipo("integer",ts));
		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Mas,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Menos,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));
		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Mas,CategoriaLexica.LitEntero), new ExpTipo("integer",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Menos,CategoriaLexica.LitEntero), new ExpTipo("integer",ts));

		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Mas,CategoriaLexica.LitNatural), new ExpTipo("natural",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Menos,CategoriaLexica.LitNatural), new ExpTipo("natural",ts));
	
		//Operaciones permitidas para *|/
		//----------------------------------------------------------------
				
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Por,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Divide,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));
		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Por,CategoriaLexica.LitEntero), new ExpTipo("float",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Divide,CategoriaLexica.LitEntero), new ExpTipo("float",ts));

		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Por,CategoriaLexica.LitNatural), new ExpTipo("float",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Divide,CategoriaLexica.LitNatural), new ExpTipo("float",ts));
		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Por,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Divide,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));
		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Por,CategoriaLexica.LitEntero), new ExpTipo("integer",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Divide,CategoriaLexica.LitEntero), new ExpTipo("integer",ts));
		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Por,CategoriaLexica.LitNatural), new ExpTipo("integer",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Divide,CategoriaLexica.LitNatural), new ExpTipo("integer",ts));
		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Por,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Divide,CategoriaLexica.LitDecimal), new ExpTipo("float",ts));
		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Por,CategoriaLexica.LitEntero), new ExpTipo("integer",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Divide,CategoriaLexica.LitEntero), new ExpTipo("integer",ts));

		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Por,CategoriaLexica.LitNatural), new ExpTipo("natural",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Divide,CategoriaLexica.LitNatural), new ExpTipo("natural",ts));
	
		//Operaciones permitidas para <
		//----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Menor,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Menor,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Menor,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Menor,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Menor,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Menor,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Menor,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Menor,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Menor,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitCaracter,CategoriaLexica.Menor,CategoriaLexica.LitCaracter), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitBooleano,CategoriaLexica.Menor,CategoriaLexica.LitBooleano), new ExpTipo("boolean",ts));		
		
		//Operaciones permitidas para >
		//----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Mayor,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Mayor,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Mayor,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Mayor,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Mayor,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Mayor,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Mayor,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Mayor,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Mayor,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitCaracter,CategoriaLexica.Mayor,CategoriaLexica.LitCaracter), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitBooleano,CategoriaLexica.Mayor,CategoriaLexica.LitBooleano), new ExpTipo("boolean",ts));
		
		//Operaciones permitidas para <=
		//----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.MenorIgual,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.MenorIgual,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.MenorIgual,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.MenorIgual,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.MenorIgual,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.MenorIgual,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.MenorIgual,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.MenorIgual,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.MenorIgual,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitCaracter,CategoriaLexica.MenorIgual,CategoriaLexica.LitCaracter), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitBooleano,CategoriaLexica.MenorIgual,CategoriaLexica.LitBooleano), new ExpTipo("boolean",ts));
				
		//Operaciones permitidas para >=
		//----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.MayorIgual,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.MayorIgual,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.MayorIgual,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.MayorIgual,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.MayorIgual,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.MayorIgual,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.MayorIgual,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.MayorIgual,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.MayorIgual,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitCaracter,CategoriaLexica.MayorIgual,CategoriaLexica.LitCaracter), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitBooleano,CategoriaLexica.MayorIgual,CategoriaLexica.LitBooleano), new ExpTipo("boolean",ts));
		
		//Operaciones permitidas para ==
		//----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Igual,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Igual,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Igual,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Igual,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Igual,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Igual,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Igual,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Igual,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Igual,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitCaracter,CategoriaLexica.Igual,CategoriaLexica.LitCaracter), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitBooleano,CategoriaLexica.Igual,CategoriaLexica.LitBooleano), new ExpTipo("boolean",ts));
			
		// Operaciones permitidas para !=
		// ----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Distinto,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Distinto,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.Distinto,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Distinto,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Distinto,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.Distinto,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Distinto,CategoriaLexica.LitNatural), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Distinto,CategoriaLexica.LitEntero), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.Distinto,CategoriaLexica.LitDecimal), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitCaracter,CategoriaLexica.Distinto,CategoriaLexica.LitCaracter), new ExpTipo("boolean",ts));		
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitBooleano,CategoriaLexica.Distinto,CategoriaLexica.LitBooleano), new ExpTipo("boolean",ts));
		
		// Operaciones casting(natural)
		// -----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.CastingNatural,null), new ExpTipo("natural",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitCaracter,CategoriaLexica.CastingNatural,null), new ExpTipo("natural",ts));
		
		// Operaciones casting(integer)
		// -----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.CastingEntero,null), new ExpTipo("integer",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.CastingEntero,null), new ExpTipo("integer",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.CastingEntero,null), new ExpTipo("integer",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitCaracter,CategoriaLexica.CastingEntero,null), new ExpTipo("integer",ts));
	
		// Operaciones casting(float)
		// -----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.CastingDecimal,null), new ExpTipo("float",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitEntero,CategoriaLexica.CastingDecimal,null), new ExpTipo("float",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitDecimal,CategoriaLexica.CastingDecimal,null), new ExpTipo("float",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitCaracter,CategoriaLexica.CastingDecimal,null), new ExpTipo("float",ts));
	
		// Operaciones casting(caracter)
		// -----------------------------------------------------------------
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitNatural,CategoriaLexica.CastingCaracter,null), new ExpTipo("character",ts));
		operaciones.put(new OperacionPermitida(CategoriaLexica.LitCaracter,CategoriaLexica.CastingCaracter,null), new ExpTipo("character",ts));
		
	}	
	
	
	// METODOS
	// ***********************************************************************
	
	/**
	 * Comprueba si se puede aplicar la operacion "op"+"lex" (el operador es una palabra reservada)
 	 * o aplicar "op" (el operador se construye con simbolos) para "tipo1" y "tipo2"
	 * @param op a aplicar
	 * @param lex para los casos en los que el operador es una palabra reservada
	 * @param tipo1 tipo del operando A
	 * @param tipo2 tipo del operando B
	 * @return True si se puede operar tipo1 op(lex) tipo2
	 */
	public boolean existeOperacion(CategoriaLexica op,String lex,CategoriaLexica tipo1, CategoriaLexica tipo2){

		OperacionPermitida p;

		// Comprobamos si "op" es palabra reservada para tener en cuenta o no el lexema de la operacion
		// Si no es palabra reservada, no se tendr� en cuenta
		
		if (op == CategoriaLexica.PalabraReservada)				// Si es una operador booleana: AND, OR, NOT
			p = new OperacionPermitida(tipo1,op,lex,tipo2);
		else 													// Sino es cualquier otra: ==, !=, etc...
			p = new OperacionPermitida(tipo1,op,tipo2);

		boolean res = operaciones.containsKey(p);
		return res;
		
	}
	
	/**
	 * @deprecated
	 * Devuelve el tipo resultante de aplicar la operacion "op"+"lex" (el operador es una palabra reservada)
 	 * o aplicar "op" (el operador se construye con simbolos) para "tipo1" y "tipo2"
	 * @param op a aplicar
	 * @param lex para los casos en los que el operador es una palabra reservada
	 * @param tipo1 tipo del operando A
	 * @param tipo2 tipo del operando B
	 * @return tipoDevuelto de realizar tipo1 op(lex) tipo2
	 */
	@SuppressWarnings("rawtypes")
	public ExpTipo tipoEsperado(CategoriaLexica op,String lex,CategoriaLexica tipo1, CategoriaLexica tipo2) throws Exception{
		
		OperacionPermitida aux;
		ExpTipo tipoDevuelto  = null;
		
		// Comprobamos si "op" es palabra reservada para tener en cuenta o no el lexema de la operacion
		// Si no es palabra reservada, no se tendr� en cuenta
		
		if (op == CategoriaLexica.PalabraReservada)				// Si es una operador booleana: AND, OR, NOT
			aux = new OperacionPermitida(tipo1,op,lex,tipo2);
		else 													// Sino es cualquier otra: ==, !=, etc...
			aux = new OperacionPermitida(tipo1,op,tipo2);
		
		
		
		// Si la operacion esta en la tabla de operaciones permitidas
		if (operaciones.containsKey(aux)) {	
			
			// Buscamos el valor (tipoEsperado) asociado a la clave (oP)
			// -----------------------------------------------------------------
			Set conjuntoOp = operaciones.entrySet();
			Iterator iteradorOp = conjuntoOp.iterator();
			boolean encontrado = false;
			
			while(iteradorOp.hasNext() && !encontrado) {
				Map.Entry tablaOp = (Map.Entry)iteradorOp.next();
				
				if (tablaOp.getKey().equals(aux)){
					tipoDevuelto = (ExpTipo) tablaOp.getValue();
					encontrado = true;
					}
			}
			
			if (!encontrado)
				throw new Exception("!!! > Error inesperado al buscar en la TOperaciones: No se ha encontrado el valor pese a contenerlo xD!");	
		
		}
		else	
			tipoDevuelto = new ExpTipoError();

		return tipoDevuelto;
		
	}
	
	@SuppressWarnings("rawtypes")
	public ExpTipo tipoEsperado(CategoriaLexica op,String lex,ExpTipo tipo1, ExpTipo tipo2) throws Exception{
		
		OperacionPermitida aux;
		ExpTipo tipoDevuelto  = null;
		
		CategoriaLexica catLex_tipo1 = tipo1.getCategoriaLexica(ts);
		CategoriaLexica catLex_tipo2 = null;
		
		if (tipo2 != null){
			catLex_tipo2 = tipo2.getCategoriaLexica(ts);
		
			// Arrays
			if (op == CategoriaLexica.OpAsignVar){
				if (catLex_tipo1 == CategoriaLexica.TArray ||
					catLex_tipo2 == CategoriaLexica.TArray){
					if (ts.compatibles(tipo1, tipo2))
						return new ExpTipoArray(0,ts, ((ExpTipoArray)tipo1).getTBase());	
				} 
				else if (catLex_tipo1 == CategoriaLexica.TTupla ||
						 catLex_tipo2 == CategoriaLexica.TTupla){
					if (ts.compatibles(tipo1,tipo2))
						return new ExpTipoTupla(new ArrayList<ElemTupla>(), ts);
						
				}
				else
					return new ExpTipoError();
			}
		}
		// Comprobamos si "op" es palabra reservada para tener en cuenta o no el lexema de la operacion
		// Si no es palabra reservada, no se tendr� en cuenta
		
		if (op == CategoriaLexica.PalabraReservada)				// Si es una operador booleana: AND, OR, NOT
			aux = new OperacionPermitida(catLex_tipo1,op,lex,catLex_tipo2);
		else 													// Sino es cualquier otra: ==, !=, etc...
			aux = new OperacionPermitida(catLex_tipo1,op,catLex_tipo2);
		
		
		
		// Si la operacion esta en la tabla de operaciones permitidas
		if (operaciones.containsKey(aux)) {	
			
			// Buscamos el valor (tipoEsperado) asociado a la clave (oP)
			// -----------------------------------------------------------------
			Set conjuntoOp = operaciones.entrySet();
			Iterator iteradorOp = conjuntoOp.iterator();
			boolean encontrado = false;
			
			while(iteradorOp.hasNext() && !encontrado) {
				Map.Entry tablaOp = (Map.Entry)iteradorOp.next();
				
				if (tablaOp.getKey().equals(aux)){
					tipoDevuelto = (ExpTipo) tablaOp.getValue();
					encontrado = true;
					}
			}
			
			if (!encontrado)
				throw new Exception("!!! > Error inesperado al buscar en la TOperaciones: No se ha encontrado el valor pese a contenerlo xD!");	
		
		}
		else	
			tipoDevuelto = new ExpTipoError();
	
		return tipoDevuelto;
		
	}

}
