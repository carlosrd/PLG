package AnalizadorSintactico;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Clase encargada de crear el c�digo de la m�quina P para que lo pueda ejecutar dicha m�quina
 *
 */
public class Codigo {
	
	private Stack<String> codigo;
	
	/**
	 * Constructora
	 */
	public Codigo() {
		codigo = new Stack<String>();
	}
	
	/**
	 * Genera codigo maquina segun los parametros
	 * @param operador
	 * @param atributos
	 */
	public void emite(InstruccionesPila operador, ArrayList<String> atributos) throws Exception {
		String inst;
		// codigo con dos parametros
		if (operador.equals(InstruccionesPila.apila) || operador.equals(InstruccionesPila.desapila_dir)) {
			inst = operador.toString()+"("+atributos.get(0)+","+atributos.get(1)+")";
		} // codigo con un parametro
		else if (operador.equals(InstruccionesPila.apila_dir) || operador.equals(InstruccionesPila.ir_a) || 
				operador.equals(InstruccionesPila.ir_f) ||
				operador.equals(InstruccionesPila.mueve) ||
				operador.equals(InstruccionesPila.ir_v) ||
				operador.equals(InstruccionesPila.in)
				) {
			inst = operador.toString()+"("+atributos.get(0)+")";
		} 
	/*	else if(operador.equals(InstruccionesPila.in))
				{
					inst = operador.toString() +"("+atributos.get(0)+","+atributos.get(1)+")";
				}*/
		else //codigo sin parametros
			inst = operador.toString();
		codigo.push(inst);
	}	
	
	/**
	 * Deja el código listo para ser procesado
	 * @return código final para la máquina P
	 */
	public ArrayList<String> preparaCodigoParaMaquinaP(){
		
		ArrayList<String> codigoLista = new ArrayList<String>();
		
		for (String c: codigo) 
			 codigoLista.add(c);
		 
		return codigoLista;
		
	}
	
	/**
	 * Imprime el codigo generado a un fichero
	 * @param fichero ruta del fichero
	 */
	public void imprimeCodigo(String fichero) {
		
		 BufferedWriter bufferedWriter = null;

		 try {

			 //Creamos el fichero
			 bufferedWriter = new BufferedWriter(new FileWriter(fichero));
		 
			 
			for (String c: codigo) {
				 bufferedWriter.write(c);
				 bufferedWriter.newLine();
			 }
			 

		 } catch (FileNotFoundException ex) {
		 ex.printStackTrace();
		 } catch (IOException ex) {
		 ex.printStackTrace();
		 } finally {
		 //Cerramos el fichero (independientemente de si se ha podido crear o no)
			 try {
				 if (bufferedWriter != null) {
					 bufferedWriter.flush();
					 bufferedWriter.close();
				 }
			 } 
			 catch (IOException ex) {
				 ex.printStackTrace();
			 }
		 }
	}
	
	/**
	 * buscar todas las instrucciones de listarParchear, y coger el codigo de instruccion
	 * luego sustituir en ese trozo de codigo, el (?) por (dirParcheo)
	 * @param listaParchear
	 * @param dirParcheo
	 */
	public void parchea(ArrayList<Integer> listaParchear, int dirParcheo){
		for (int i = 0; i < listaParchear.size(); i++){
			modificaDirCodigo(listaParchear.get(i), dirParcheo);
		}
		
	}
	
	/**
	 * Recibe un codigo de instruccion con un (?) a la que saltar y hay que modificarla
	 * para poner (dir)
	 * @param dirParcheo dirección nueva
	 * @param dirCod dirección de la instrucción que va a ser modificada
	 */
	private void modificaDirCodigo(int dirCod, int dirParcheo){
		
		String cod = codigo.get(dirCod);
		cod = cod.replace("?", ""+dirParcheo);
		codigo.remove(dirCod);
		codigo.insertElementAt(cod, dirCod);
	}
	
	public static void main (String[] args) throws Exception{
		Codigo c = new Codigo();
		ArrayList<String> at = new ArrayList<String>();
		at.add("?");
		
		
		for (int i = 0; i < 12; i++)
			c.emite(InstruccionesPila.apila_dir, at);
		
		// parcheamos las 1, 3 y 6
		ArrayList<Integer> par = new ArrayList<Integer>();
		par.add(1);
		par.add(3);
		par.add(6);
		
		// que apunten a la 128
		
		c.parchea(par, 128);
		c.inicio(12);
		
		c.codPrologo(3);
		
		c.codEpilogo(2);
		
		
		c.imprimeCodigo("pruebaCod.txt");
		
	}

	public void inicio(int tamVars) throws Exception {
		ArrayList<String> a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add(""+tamVars);
		
		
		ArrayList<String> a2 = new ArrayList<String>();
		a2.add("natural");
		a2.add("0");
		
		this.emite(InstruccionesPila.apila, a1);
		this.emite(InstruccionesPila.desapila_dir, a2);
		
	}
	
	public void codEpilogo(int tam) throws Exception{
		
		ArrayList<String> a1 = new ArrayList<String>(); 
		a1.add("0");
		
		this.emite(InstruccionesPila.apila_dir,a1);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add((tam+2)+"");
		
		this.emite(InstruccionesPila.apila, a1);
		
		this.emite(InstruccionesPila.resta, null);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add("0");
		
		this.emite(InstruccionesPila.desapila_dir, a1);
		
		a1 = new ArrayList<String>();
		a1.add("0");
		
		this.emite(InstruccionesPila.apila_dir, a1);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add("2");
		
		this.emite(InstruccionesPila.apila, a1);
		
		this.emite(InstruccionesPila.suma, null);
		
		this.emite(InstruccionesPila.apila_ind, null);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add("1");
		
		this.emite(InstruccionesPila.desapila_dir, a1);
		
		a1 = new ArrayList<String>();
		a1.add("0");
		
		this.emite(InstruccionesPila.apila_dir, a1);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add("1");
		
		this.emite(InstruccionesPila.apila, a1);
		
		this.emite(InstruccionesPila.suma, null);
	
		this.emite(InstruccionesPila.apila_ind, null);
		
		this.emite(InstruccionesPila.ir_ind, null);		
	}
	
	public void codPrologo(int tam) throws Exception{
		
		ArrayList<String> a1 = new ArrayList<String>();
		a1.add("0");
		
		this.emite(InstruccionesPila.apila_dir, a1);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add("2");
		
		this.emite(InstruccionesPila.apila, a1);
		
		this.emite(InstruccionesPila.suma, null);
		
		a1 = new ArrayList<String>();
		a1.add("1");
		
		this.emite(InstruccionesPila.apila_dir, a1);
		
		this.emite(InstruccionesPila.desapila_ind, null);
		
		a1 = new ArrayList<String>();
		a1.add("0");
		
		this.emite(InstruccionesPila.apila_dir, a1);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add("3");
		
		this.emite(InstruccionesPila.apila, a1);
		
		this.emite(InstruccionesPila.suma, null);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add("1");
		
		this.emite(InstruccionesPila.desapila_dir, a1);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add((tam+2)+"");
		
		this.emite(InstruccionesPila.apila, a1);
		
		a1 = new ArrayList<String>();
		a1.add("0");
		
		this.emite(InstruccionesPila.apila_dir, a1);

		this.emite(InstruccionesPila.suma, null);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add("0");
		
		this.emite(InstruccionesPila.desapila_dir, a1);
	}
	
	public void retorno(int dir) throws Exception{
		
		ArrayList<String> a1 = new ArrayList<String>();
		a1.add("0");
		
		this.emite(InstruccionesPila.apila_dir, a1);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add("1");
		
		this.emite(InstruccionesPila.apila, a1);
		
		this.emite(InstruccionesPila.suma, null);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add(""+dir);
		
		this.emite(InstruccionesPila.apila, a1);

		this.emite(InstruccionesPila.desapila_ind, a1);
		
	}
	
	public void retorno(String dir) throws Exception{
		
		ArrayList<String> a1 = new ArrayList<String>();
		a1.add("0");
		
		this.emite(InstruccionesPila.apila_dir, a1);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add("1");
		
		this.emite(InstruccionesPila.apila, a1);
		
		this.emite(InstruccionesPila.suma, null);
		
		a1 = new ArrayList<String>();
		a1.add("natural");
		a1.add(dir);
		
		this.emite(InstruccionesPila.apila, a1);

		this.emite(InstruccionesPila.desapila_ind, a1);
		
	}

}
