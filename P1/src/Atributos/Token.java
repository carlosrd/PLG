package Atributos;

/**
 * Clase que define un Token lexico
 *
 */
public class Token {

	private CategoriaLexica _tipo;
	private String _lexema;
	private int _linea;
	
	/**
	 * Constructora
	 * @param lexema - String
	 * @param linea - int
	 */
	public Token(String lexema, int linea) {
		_lexema = lexema;
		_linea = linea;
	}
	
	/**
	 * Devuelve la Categoria Lexica
	 * @return Categoria Lexica
	 */
	public CategoriaLexica getCatLexica() {
		return _tipo;
	}

	/**
	 * Da valor a la Categoria Lexica
	 * @param _tipo - CategoriaLexica
	 */
	public void setCatLexica(CategoriaLexica _tipo) {
		this._tipo = _tipo;
	}

	/**
	 * Devuelve el lexema
	 * @return lexema
	 */
	public String get_lexema() {
		return _lexema;
	}

	/**
	 * Da valor al lexema
	 * @param _lexema - String
	 */
	public void set_lexema(String _lexema) {
		this._lexema = _lexema;
	}

	/**
	 * Devuelve el numero de linea
	 * @return linea
	 */
	public int get_linea() {
		return _linea;
	}

	/**
	 * Da valor al numero de linea
	 * @param _linea - int
	 */
	public void set_linea(int _linea) {
		this._linea = _linea;
	}
	
	public String toString()
	{
		return _lexema+" " + _tipo +" " + _linea;
	}
}
