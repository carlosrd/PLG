package AnalizadorLexico;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Atributos.CategoriaLexica;
import Atributos.Token;

/**
 * Clase que implementa el analizador léxico
 * 
 * �
 */
public class AnalizadorLexico {

	// Atributos del analizador lexico
	private StreamTokenizer tokens;
	private PalabrasReservadas palabrasReservadas;	
	private ArrayList<Token> listaTokens;
	private Integer numeroLinea;
	// Este buffer se usa para almacenar lexemas de restas sin espacios, posibles caracteres compuestos (>=,<=,...),
	// posibles caracteres ('a','\n',...)
	private Cola<Token> bufferLocal;
	
	/**
	 * Constructora
	 * @param program - InputStream
	 */
	public AnalizadorLexico(InputStream program) {
		palabrasReservadas = new PalabrasReservadas();
		listaTokens = new ArrayList<Token>();
		numeroLinea = 1;		
		bufferLocal = new Cola<Token>();
		// Configuramos el Stream Tokenizer
		InputStreamReader reader = new InputStreamReader(program);
		this.configuraTokenizer(reader);		
	}
		
	/**
	 * Mira cual es el siguiente token sin avanzar en la lectura
	 * @return siguiente token
	 * @throws IOException
	 */
	public Token checkNextToken() throws IOException {
		Token token = this.getNextToken();
		this.pushBack();
		return token;	
	}
	
	/**
	 * Lee los tokens. Cuando llega a EOF devuelve fin
	 * @return el token leido con toda la informacion asociada
	 * @throws IOException
	 */
	public Token getNextToken() throws IOException {
		
		Token palabra = this.nextToken();
		// Comprobamos si es un caso especial
		palabra = casoEspecial(palabra);
		palabra.setCatLexica(reconoceCategoria(palabra.get_lexema()));
		 
		listaTokens.add(palabra);
		return palabra;	
	}
	
	/**
	 * Comprueba que haya un casting en los 3 siguientes tokens
	 * @return true si hay un casting, false en caso contrario
	 * @throws IOException
	 */
	public boolean esCasting() throws IOException {
		
		// Procesamos los 3 tokens
		Token token1 = this.nextToken();
		token1.setCatLexica(reconoceCategoria(token1.get_lexema()));
		Token token2 = this.nextToken();
		token2.setCatLexica(reconoceCategoria(token2.get_lexema()));
		Token token3 = this.nextToken();
		token3.setCatLexica(reconoceCategoria(token3.get_lexema()));
		boolean casting = false;
		// Comprobamos que se cumpla que los 3 siguientes tokens corresponden a un operador
		if (token1.getCatLexica().equals(CategoriaLexica.AbreParentesis) 
				&& (token2.get_lexema().equals("natural") || token2.get_lexema().equals("character") 
					|| token2.get_lexema().equals("integer") || token2.get_lexema().equals("float")) 
				&& token3.getCatLexica().equals(CategoriaLexica.CierraParentesis)) {
					casting = true;					
			}			
		
		// Volvemos a poner los tokens en el buffer
		bufferLocal.insertarEnCabeza(token3);
		bufferLocal.insertarEnCabeza(token2);
		bufferLocal.insertarEnCabeza(token1);
		return casting;
	}
	// ---------------------------------- DECLARACIONES PRIVADAS ---------------------------------- //

	/**
	 * Configura los parametros del StreamTokenizer de la clase
	 * @param reader - InputStreamReader
	 */
	private void configuraTokenizer(InputStreamReader reader) {		
			
		tokens = new StreamTokenizer(reader);
		tokens.resetSyntax();							// Resetear la sintaxis
	
		tokens.commentChar('@');						// '@' Delimita los comentarios
		tokens.eolIsSignificant(true);					// EOL es significante
		tokens.slashSlashComments(false);				// NO reconocer comentarios C
		tokens.slashStarComments(false);				// NO reconocer comentarios C++
		//tokens.parseNumbers();  						// NO parsear numeros (Los convierte a real TODOS)
		
		// Sintaxis para reconocer palabras
		tokens.wordChars('a', 'z');						// Rango de caracteres que forman palabras [a-z]
		tokens.wordChars('A', 'Z');						// Rango de caracteres que forman palabras [A-Z]
		//tokens.wordChars('\u0081','\u0082');			// Rango de caracteres: 'ü' y 'é' (XXX NO FUNCIONA)
		//tokens.wordChars('\u00A0','\u00AE');			// Rango de caracteres: 'á', 'í', 'ó', 'ú', ñ y Ñ (XXX NO FUNCIONA)
		tokens.wordChars('á','á');						// Rango de caracteres: 'á' (A acentuada)
		tokens.wordChars('é','é');						// Rango de caracteres: 'é' (E acentuada)
		tokens.wordChars('í','í');						// Rango de caracteres: 'í' (I acentuada)
		tokens.wordChars('ó','ó');						// Rango de caracteres: 'ó' (O acentuada)
		tokens.wordChars('ú','ú');						// Rango de caracteres: 'ú' (U acentuada)
		tokens.wordChars('ñ','ñ');						// Rango de caracteres: 'ñ' (enye minus)
		tokens.wordChars('Ñ','Ñ');						// Rango de caracteres: 'Ñ' (enye mayus)
		// Sintaxis Adicional para reconocer los numeros
		tokens.wordChars('0', '9');						// Rango de caracteres que forman numeros [0-9]
		tokens.wordChars('.', '.');						// Rango de caracteres que forman decimales
		
	    // Sintaxis adicional para reconocer los chars
		// Por ahora ninguna
	}
	
	/**
	 * PushBack que usa el buffer Local para poder almacenar los caracteres
	 */
	private void pushBack() {
		bufferLocal.insertarEnCabeza(listaTokens.get(listaTokens.size()-1));
	}
	
	/**
	 * Devuelve el siguiente token significativo del InputStream o del buffer
	 * @return siguiente token
	 * @throws IOException
	 */
    private Token nextToken() throws IOException {
        // Si hay lexemas en el buffer, leemos de ahi
        if (!bufferLocal.esVacia()) {
            return bufferLocal.extraer();
        }
        // Si no, leemos del Stream
        String lexemaActual = "";
        do {
            int lexema = tokens.nextToken();
            switch (lexema) {
                case StreamTokenizer.TT_WORD:
                    lexemaActual = tokens.sval;
                    break;
                case StreamTokenizer.TT_EOF:
                    lexemaActual = "fin";
                    break;
                default:
                    lexemaActual = String.valueOf((char)tokens.ttype);
                    break;
            }
            // Sumamos una linea en caso de haber salto de linea
            if (lexemaActual.equals("\n"))
                numeroLinea++;
        } while (lexemaActual.matches(" |\t|\n|\r"));
        return new Token(lexemaActual,this.numeroLinea);
    }			
	
	/**
	 * Devuelve el siguiente token del InputStream o del buffer
	 * @return siguiente token o "" si es un blanco,\n,\t,etc...
	 * @throws IOException
	 */
	private Token nextTokenWithWhites() throws IOException {
		String lexemaActual = "";
		// Si hay lexemas en el buffer, leemos de ahi
		if (!bufferLocal.esVacia()) {
			return bufferLocal.extraer();
		} else {
			// Si no, leemos del Stream					
			int lexema = tokens.nextToken();
			switch (lexema) {
				case StreamTokenizer.TT_WORD: 
					lexemaActual = tokens.sval;  
					break;
				case StreamTokenizer.TT_EOF:
					lexemaActual = "fin";
					break;
				default:
					lexemaActual = String.valueOf((char)tokens.ttype);
					break;
			}			
			// Sumamos una linea en caso de haber salto de linea
			if (lexemaActual.equals("\n")) 
				numeroLinea++;			
		}
		return new Token(lexemaActual,this.numeroLinea);
	}
	
	/**
	 * Comprueba si la palabra pertenece a un caso especial
	 * @param palabra - String
	 * @return la palabra reconocida
	 * @throws IOException
	 */
	private Token casoEspecial(Token palabra) throws IOException {
		String lexema = palabra.get_lexema();
		Token aux;
		// Comprobamos si es un posible caracter
		if (lexema.equals("'")) {
 			palabra.set_lexema(construyeCaracter(lexema));	
		}
		// Comprobamos si es "program:"
		else if (lexema.equals("program")) {
			if((aux=this.nextTokenWithWhites()).get_lexema().equals(":")) {
				lexema = lexema+aux.get_lexema();
				palabra.set_lexema(lexema);			
			} else 
				tokens.pushBack();				
		}
		// Comprobamos si es "subprogram:"
		else if (lexema.equals("subprogram")) {
			if((aux=this.nextTokenWithWhites()).get_lexema().equals(":")) {
				lexema = lexema+aux.get_lexema();
				palabra.set_lexema(lexema);			
			} else 
				tokens.pushBack();				
		}
		// Comprobamos si es :=,>=,<=,!=,=,==
		else if (lexema.matches("\\:|\\>|\\<|\\=|\\!")) {
			if((aux = this.nextTokenWithWhites()).get_lexema().equals("=") || lexema.equals("<") && aux.get_lexema().equals("<") ||
					lexema.equals(">") && aux.get_lexema().equals(">")) {
				lexema = lexema+aux.get_lexema();
				palabra.set_lexema(lexema);
			} else 
				tokens.pushBack();	
		}		
		// Comprobamos si es un casting
		/*else if (lexema.equals("(")) {
			if((aux = this.nextToken()).get_lexema().matches("integer|int|float|character|char|natural|nat")) {
				lexema = lexema+aux.get_lexema();
				aux = this.nextToken();
				lexema = lexema+aux.get_lexema();
				palabra.set_lexema(lexema);
			} else
				tokens.pushBack();	
		}*/
		// Comprobamos si es un numero exponencial
		else if (lexema.matches("[0-9]+(.[0-9]+)?(e|E)")) {
			aux=this.nextToken();
			if (aux.get_lexema().equals("-")) {
				lexema = lexema+aux.get_lexema();
				aux=this.nextToken();
				lexema = lexema+aux.get_lexema();
				palabra.set_lexema(lexema);
			} else if (aux.get_lexema().matches("[0-9]+")) {
				lexema = lexema+aux.get_lexema();
				aux=this.nextToken();
				palabra.set_lexema(lexema);
			}else {
				tokens.pushBack();	
			}			
		}
		return palabra;
	}
	
	/**
	 * Funcion privada para construir los caracteres
	 * @param palabra - String
	 * @return Caracter reconocido
	 * @throws IOException
	 */
	private String construyeCaracter(String palabra) throws IOException {
		Token aux = null;
 		while(!((aux = this.nextTokenWithWhites()).get_lexema().equals("'"))) {
 			if (aux.get_lexema().equals("fin"))
 				return palabra;
 			palabra = palabra + aux.get_lexema();
 			// Si vemos que el caracter se alarga mucho, paramos de leer
 			if (palabra.length() > 4) {
 				return palabra;
 			}
 		}
 		palabra = palabra + aux.get_lexema();
		return palabra;
	}

	/**
	 * Reconoce la categoria lexica de la palabra pasada como parametro
	 * @param palabra - String
	 * @return Categoria lexica
	 */
	private CategoriaLexica reconoceCategoria(String palabra) {
			
		// Comprobamos que no sea el fin de fichero
		if (palabra.equals("fin"))
			return CategoriaLexica.TFin;
		
		// Buscamos la categoria lexica a la que pertenece la palabra
		CategoriaLexica c = simbolo(palabra);
		if (c != null) 
			return c;
		
		// Miramos si es una palabra reservada
		if (palabrasReservadas.esReservada(palabra))	
			return CategoriaLexica.PalabraReservada;
		
		// Reconocer Identificadores
		// --------------------------------------------------------------
		Pattern iden = Pattern.compile("([a-z]|á|é|í|ó|ú|ñ)([a-z]|[A-z]|[0-9]|á|é|í|ó|ú|ñ|Ñ)*");
		Matcher m = iden.matcher(palabra);
		
		if (m.matches())			
			return CategoriaLexica.Identificador;
		
		// Reconocer Naturales o Enteros Pos 
		// -------------------------------------------------------------- 
		Pattern natural = Pattern.compile("([0-9]|[1-9][0-9]*)");
		m = natural.matcher(palabra);	
		
		if (m.matches())	
			return CategoriaLexica.LitNatural;
		
		// Reconocer Enteros Negativos 
		// -------------------------------------------------------------- 
		Pattern enteroNegativo = Pattern.compile("-[1-9][0-9]*");
		m = enteroNegativo.matcher(palabra);
		
		if (m.matches())
			return CategoriaLexica.LitEntero;
		 
		// Reconocer Reales
		// -------------------------------------------------------------- 
		// Forma Decimal 
		Pattern realDecimal = Pattern.compile("-?([0-9]|[1-9][0-9]*)\\.([0-9]|[0-9]*[1-9])");
		m = realDecimal.matcher(palabra);
		 
		if (m.matches())
			return CategoriaLexica.LitDecimal;

		//  Forma Exponencial
		Pattern realExponencial = Pattern.compile("-?([0-9]|[1-9][0-9]*)(E|e)-?([0-9]|[1-9][0-9]*)");
		m = realExponencial.matcher(palabra);
		
		if (m.matches())
			return CategoriaLexica.LitDecimal;
		
		// Forma Decimal+Exp
		Pattern realDecimalExponencial = Pattern.compile("-?([0-9]|[1-9][0-9]*)\\.([0-9]|[0-9]*[1-9])" +
				"(E|e)-?([0-9]|[1-9][0-9]*)");
		m = realDecimalExponencial.matcher(palabra);
		
		if (m.matches())
			return CategoriaLexica.LitDecimal;
		
		// Reconocer Caracter
		// -------------------------------------------------------------- 
		Pattern caracter = Pattern.compile("'([^\\\\]|\\\\[tnr\\\\'])'");
		m = caracter.matcher(palabra);
		
		if (m.matches())
			return CategoriaLexica.LitCaracter;
		
		// Si no ha coincidido con ninguna otra expresion, es que el tipo es erroneo
		return CategoriaLexica.TError;	
	}
	
	/**
	 * Comprueba si el atributo es un simbolo
	 * @param palabra - String
	 * @return Categoria lexica o null (en caso de no ser un simbolo)
	 */
	private CategoriaLexica simbolo(String palabra) {
		
	    if (palabra.equals(":="))
	        {
	           return CategoriaLexica.OpAsignConst;
	        }
	    else if (palabra.equals("'"))
	        {
	            return CategoriaLexica.ComillaSimple;
	        }
 		else if (palabra.equals(","))
			{
				return CategoriaLexica.Coma;
			}
 		else if (palabra.equals(";"))
			{
				return CategoriaLexica.PuntoComa;
			}
		else if (palabra.equals("("))
			{
				return CategoriaLexica.AbreParentesis;
			}
		else if (palabra.equals(")"))
			{
				return CategoriaLexica.CierraParentesis;
			}
		else if (palabra.equals("{"))
			{
				return CategoriaLexica.AbreLlave;
			}
		else if (palabra.equals("}"))
			{
				return CategoriaLexica.CierraLlave;
			}
		else if (palabra.equals("["))
			{
				return CategoriaLexica.AbreCorchete;
			}
				else if (palabra.equals("]"))
			{
			return CategoriaLexica.CierraCorchete;
			}
		else if (palabra.equals("."))
			{
				return CategoriaLexica.Punto;
			}
		else if (palabra.equals(":"))
			{
				return CategoriaLexica.DosPuntos;
			}
		else if (palabra.equals("="))
			{
				return CategoriaLexica.OpAsignVar;
			}
		else if (palabra.equals("+"))
			{
				return CategoriaLexica.Mas;
			}
		else if (palabra.equals("-"))
			{
				return CategoriaLexica.Menos;
			}
		else if (palabra.equals("*"))
			{
				return CategoriaLexica.Por;
			}
		else if (palabra.equals("/"))
			{
				return CategoriaLexica.Divide;
			}
		else if (palabra.equals("%"))
			{
				return CategoriaLexica.Modulo;
			}
		else if (palabra.equals("<<"))
			{
				return CategoriaLexica.DesplazIz;
			}
		else if (palabra.equals(">>"))
			{
				return CategoriaLexica.DesplazDer;
			}
		else if (palabra.equals(">"))
			{
				return CategoriaLexica.Mayor;
			}
		else if (palabra.equals("<"))
			{
				return CategoriaLexica.Menor;
			}
		else if (palabra.equals(">="))
			{
				return CategoriaLexica.MayorIgual;
			}
		else if (palabra.equals("<="))
			{
				return CategoriaLexica.MenorIgual;
			}
		else if (palabra.equals("!="))
			{
				return CategoriaLexica.Distinto;
			}
		else if (palabra.equals("=="))
			{
				return CategoriaLexica.Igual;
			}
		else if (palabra.equals("_"))
		{
			return CategoriaLexica.BarraBaja;
		}
		else if (palabra.equals("(integer)") || palabra.equals("(int)"))
			{
				return CategoriaLexica.CastingEntero;
			}
		else if (palabra.equals("(natural)") || palabra.equals("(nat)"))
			{
				return CategoriaLexica.CastingNatural;
			}
		else if (palabra.equals("(float)"))
			{
				return CategoriaLexica.CastingDecimal;
			}
		else if (palabra.equals("(character)") || palabra.equals("(char)"))
			{
				return CategoriaLexica.CastingCaracter;
			}
		else 
			{
				return null;
			}
	}

	/*
	 * Main para hacer pruebas
	 */
	/*public static void main(String args[]) {
		File f = new File("Pruebas/pruebasLexico");
		AnalizadorLexico al = null;
		try {
			al = new AnalizadorLexico(new FileInputStream(f));
			Token t;
			System.out.println(al.esCasting());
			System.out.println(t);
			while ((t = al.getNextToken()).getCatLexica() != CategoriaLexica.TFin) {
				System.out.println(t);
				System.out.println(al.esCasting());
				t=al.checkNextToken();
				System.out.println(t);
			}
			System.out.println(t);
		} catch (Exception e) {
		}
	}*/
}

