package tests;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import org.junit.Test;

import AnalizadorLexico.AnalizadorLexico;
import Atributos.CategoriaLexica;
import Atributos.Token;

public class AnalizadorLexicoTest {
	// Separador
	char sep = File.separatorChar;
	
	// Funcion auxiliar de comparacion de ficheros
	private boolean comparaFicheros(File f1, File f2) throws Exception {
		FileReader fr1 = new FileReader(f1);
		FileReader fr2 = new FileReader(f2);
		 
		BufferedReader bf1 = new BufferedReader(fr1);
		BufferedReader bf2 = new BufferedReader(fr2);
		
		String sCadena1 = bf1.readLine();
		String sCadena2 = bf2.readLine();
		
		boolean iguales = true;
		
		while ((sCadena1!=null) && (sCadena2!=null) && iguales) {
			 
			  if (!sCadena1.equals(sCadena2))
			    iguales = false;			 
			  sCadena1 = bf1.readLine();
			  sCadena2 = bf2.readLine();
		}		
		bf1.close();
		bf2.close();	
		return iguales;
	}
	
	@Test
	public void testSignos() throws Exception {
		// Fichero de lectura
		File f1 = new File("Pruebas"+sep+"Lexico"+sep+"signos.txt");			
		AnalizadorLexico analizador = new AnalizadorLexico(new FileInputStream(f1));
		// Fichero de salida
		File f2 = new File("Pruebas"+sep+"Lexico"+sep+"signos1.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
		// Tokens que procesamos
		Token token = null;
		ArrayList<Token> listaTokens = new ArrayList<Token>();
		// Bucle de lectura/escritura
		token = analizador.getNextToken();
		while (!token.getCatLexica().equals(CategoriaLexica.TFin)) {			
			bw.write(token.get_lexema());
			bw.newLine();
			listaTokens.add(token);
			token = analizador.getNextToken();
		}
		bw.close();
		boolean cierto = this.comparaFicheros(f1, f2);
		f2.delete();	
		assertTrue(cierto);			
	}
	
	@Test
	public void testChars() throws Exception {
		// Fichero de lectura
		File f1 = new File("Pruebas"+sep+"Lexico"+sep+"chars.txt");			
		AnalizadorLexico analizador = new AnalizadorLexico(new FileInputStream(f1));
		// Fichero de salida
		File f2 = new File("Pruebas"+sep+"Lexico"+sep+"chars1.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
		// Tokens que procesamos
		Token token = null;
		ArrayList<Token> listaTokens = new ArrayList<Token>();
		// Bucle de lectura/escritura
		token = analizador.getNextToken();
		while (!token.getCatLexica().equals(CategoriaLexica.TFin)) {			
			bw.write(token.get_lexema());
			bw.newLine();
			listaTokens.add(token);
			token = analizador.getNextToken();
		}
		bw.close();
		boolean cierto = this.comparaFicheros(f1, f2);
		f2.delete();	
		assertTrue(cierto);			
	}
	
	@Test
	public void testNumeros() throws Exception {
		// Fichero de lectura
		File f1 = new File("Pruebas"+sep+"Lexico"+sep+"numeros.txt");			
		AnalizadorLexico analizador = new AnalizadorLexico(new FileInputStream(f1));
		// Fichero de salida
		File f2 = new File("Pruebas"+sep+"Lexico"+sep+"numeros1.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
		// Tokens que procesamos
		Token token = null;
		ArrayList<Token> listaTokens = new ArrayList<Token>();
		// Bucle de lectura/escritura
		token = analizador.getNextToken();
		while (!token.getCatLexica().equals(CategoriaLexica.TFin)) {			
			bw.write(token.get_lexema());
			bw.newLine();
			listaTokens.add(token);
			token = analizador.getNextToken();
		}
		bw.close();
		boolean cierto = this.comparaFicheros(f1, f2);
		f2.delete();	
		assertTrue(cierto);			
	}
	
	@Test
	public void testIdents() throws Exception {
		// Fichero de lectura
		File f1 = new File("Pruebas"+sep+"Lexico"+sep+"idens.txt");			
		AnalizadorLexico analizador = new AnalizadorLexico(new FileInputStream(f1));
		// Fichero de salida
		File f2 = new File("Pruebas"+sep+"Lexico"+sep+"idens1.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
		// Tokens que procesamos
		Token token = null;
		ArrayList<Token> listaTokens = new ArrayList<Token>();
		// Bucle de lectura/escritura
		token = analizador.getNextToken();
		while (!token.getCatLexica().equals(CategoriaLexica.TFin)) {			
			bw.write(token.get_lexema());
			bw.newLine();
			listaTokens.add(token);
			token = analizador.getNextToken();
		}
		bw.close();
		boolean cierto = this.comparaFicheros(f1, f2);
		f2.delete();	
		assertTrue(cierto);			
	}
	
	@Test
	public void testTiposComplejos() throws Exception {
		// Fichero de lectura
		File f1 = new File("Pruebas"+sep+"Lexico"+sep+"tipos_complejos.txt");			
		AnalizadorLexico analizador = new AnalizadorLexico(new FileInputStream(f1));
		// Fichero de salida
		File f2 = new File("Pruebas"+sep+"Lexico"+sep+"tipos_complejos1.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
		// Tokens que procesamos
		Token token = null;
		ArrayList<Token> listaTokens = new ArrayList<Token>();
		// Bucle de lectura/escritura
		token = analizador.getNextToken();
		while (!token.getCatLexica().equals(CategoriaLexica.TFin)) {			
			bw.write(token.get_lexema());
			bw.newLine();
			listaTokens.add(token);
			token = analizador.getNextToken();
		}
		bw.close();
		boolean cierto = this.comparaFicheros(f1, f2);
		//f2.delete();	
		assertTrue(cierto);			
	}
	
	@Test
	public void testPalabrasReservadas() throws Exception {
		// Fichero de lectura
		File f1 = new File("Pruebas"+sep+"Lexico"+sep+"palabras_reservadas.txt");			
		AnalizadorLexico analizador = new AnalizadorLexico(new FileInputStream(f1));
		// Fichero de salida
		File f2 = new File("Pruebas"+sep+"Lexico"+sep+"palabras_reservadas1.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
		// Tokens que procesamos
		Token token = null;
		ArrayList<Token> listaTokens = new ArrayList<Token>();
		// Bucle de lectura/escritura
		token = analizador.getNextToken();
		while (!token.getCatLexica().equals(CategoriaLexica.TFin)) {			
			bw.write(token.get_lexema());
			bw.newLine();
			listaTokens.add(token);
			token = analizador.getNextToken();
		}
		bw.close();
		boolean cierto = this.comparaFicheros(f1, f2);
		//f2.delete();	
		assertTrue(cierto);			
	}
	
}
