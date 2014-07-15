package InterfazGrafica;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;


/**
 * Construye la interfaz del panel de ayuda
 */
@SuppressWarnings("serial")
public class InterfazAyuda extends JFrame{

	/** 
	 * Construye la ventana con el texto de ayuda
	 */
	public InterfazAyuda(){
		
		JPanel panelPrincipal = new JPanel();
		
		// Borde del panel izquierdo
		TitledBorder tituloSeccion;
		tituloSeccion = BorderFactory.createTitledBorder("Instrucciones Básicas");
		tituloSeccion.setTitleJustification(TitledBorder.LEFT);
		
		panelPrincipal.setLayout(new BorderLayout());
		
		JTextArea campoTexto = new JTextArea(0,80);
		campoTexto.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
		campoTexto.setWrapStyleWord(true);
		campoTexto.setEditable(false);
		
		try {
			
		    InputStream is = getClass().getResourceAsStream("/resources/strings/Ayuda.txt");
		    BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String linea;
			
		    while ((linea = br.readLine()) != null) 
		    	campoTexto.append(linea+"\n");        
		      
            br.close();
            campoTexto.requestFocus();            
	     	}
	    catch(Exception e2) { 
	    	JOptionPane.showMessageDialog(null, "ERROR!\nNo se puede cargar archivo de prueba");
           }	
		
		JScrollPane scrollPane = new JScrollPane(campoTexto);
		scrollPane.setBorder(tituloSeccion);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//scrollPane.setMinimumSize(new Dimension(350,300));	// Ancho: 350 - Alto: 300
		panelPrincipal.add(scrollPane);
		
		this.setTitle("Ayuda - Compilador PLG (G05)");

		this.setContentPane(panelPrincipal);
		this.setSize(750,500);
		this.setResizable(false);
		//this.setVisible(true);

		ImageIcon iconoVentana = new ImageIcon((this.getClass().getResource("/resources/icons/menu/iconHelpMenu.png")));
		this.setIconImage(iconoVentana.getImage());
		
		this.addWindowListener(new WindowAdapter()
		{public void windowClosing(WindowEvent e)
			{
			setVisible(false);
			}
		}
		);
	}
	
	/**
	 * Muestra la ventana de ayuda con el contenido del fichero de ayuda
	 */
	public void mostrarAyuda(){
		this.setVisible(true);
		this.setEnabled(true);
		this.requestFocus();
	}
}
