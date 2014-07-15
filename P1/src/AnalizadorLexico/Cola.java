package AnalizadorLexico;

import java.util.ArrayList;

/**
 * 
 * Clase que implementa una cola de objetos genéricos
 *
 * @param <T>
 */
public class Cola<T> extends ArrayList<T> {

	/**
	 * Serial por defecto
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Comprueba si la cola es vacia
	 * @return true si es vacia, false si no
	 */
	public boolean esVacia() {
		return this.isEmpty();
	}
	
	/**
	 * Encola un elemento
	 * @param elemento del tipo de los que contenga la cola
	 */
	public void encolar(T elemento) {
		if (elemento != null) 
			this.add(elemento);
	}
	
	/**
	 * Inserta un elemento en la cabeza de la cola
	 * @param elemento del tipo de los que contenga la cola
	 */
	public void insertarEnCabeza(T elemento){
		if (elemento != null)
			this.add(0, elemento);
	}
	
	/**
	 * Extrae un elemento de la cola
	 * @return Elemento extraido
	 */
	public T extraer() {
		T dato = null;
		if (this.size() > 0) {
			dato = this.get(0);
		}
		this.remove(0);
		return dato;
	}
}
