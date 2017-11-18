package utils.juego2d.pruebas;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import utils.juego2d.utils.Fisica;
import utils.ventanas.ventanaBitmap.VentanaGrafica;

public class MundoNaves {
	private ArrayList<ObjetoMovil> objetos;
	private VentanaGrafica ventana;
	
	private static long MILIS_POR_MOVIMIENTO = 16;
	private static long MILIS_ENTRE_MOVTOS = 16;
	private static boolean PAUSA = false;
	private static boolean VER_CHOQUES = false;
	
	public MundoNaves() {
		objetos = new ArrayList<ObjetoMovil>();
		ventana = new VentanaGrafica( 1000, 800, "MundoNaves" );
	}
	
	public ArrayList<ObjetoMovil> getObjetos() {
		return objetos;
	}
	
	public VentanaGrafica getVentana() {
		return ventana;
	}
	
	public boolean addObjeto( ObjetoMovil objeto ) {
		if (objeto.getNombre()==null || objeto.getNombre().isEmpty()) objeto.setNombre( "" + objetos.size() );
		objetos.add( objeto );
		return true;
	}
	

	public static void main(String[] args) {
		Fisica.setGravedad( false );
		crearYMoverMundo();
	}
	
	private static void crearYMoverMundo() {
		MundoNaves mundo = new MundoNaves();
		mundo.init();
		mundo.crearMundoTest( 1 );
		mundo.moverMundo();
	}
	
	private void init() {
		ventana.anyadeBoton( "Reinicio test 1", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ejecutaSeguro( new Runnable() {
					@Override
					public void run() {
						crearMundoTest( 1 );
					}
				} );
			}
		});
//		ventana.anyadeBoton( "Reinicio test 2", new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				ObjetoEspacial.DIBUJAR_VELOCIDAD = false;
//				VER_CHOQUES = true;
//				ventana.setMensaje( "Parar c�lculo en choques ON" );
//				crearMundoTest( 2 );
//			}
//		});
		ventana.anyadeBoton( "Test", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ejecutaSeguro( new Runnable() {
					@Override
					public void run() {
						test( objetos );
					}
				} );
			}
		});
		ventana.anyadeBoton( "Cargar", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ejecutaSeguro( new Runnable() {
					@Override
					public void run() {
						cargarDeFichero( objetos );
					}
				} );
			}
		});
		ventana.anyadeBoton( "Guardar", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ejecutaSeguro( new Runnable() {
					@Override
					public void run() {
						guardarAFichero( objetos );
					}
				} );
			}
		});
	}
	
		private static Random random = new Random();
		public static int cont = 0;

		// TAREA 1
		private transient NaveTripulada navePrincipal = null;
		private transient Runnable run = null;
		private transient int tipoInit = 0;
	// Crea objetos de test en el mundo
	private void crearMundoTest( int tipoTest ) {
		tipoInit = tipoTest;
		objetos.clear();
		if (tipoInit==1) {  // Unas cuantas naves chocando y movi�ndose
			MundoNaves.this.addObjeto( new Nave( 200, 100, 20, 100, 100, Color.red ) );  // Pone un nombre secuencial ... "1", "2"... y as� sucesivamente
			MundoNaves.this.addObjeto( new Nave( 200, 400, 20, -100, -50, Color.red ) );
			MundoNaves.this.addObjeto( new Nave( 300, 350, 28, -40, 45, Color.blue ) );
			MundoNaves.this.addObjeto( new Nave( 420, 550, 28, 420, 550, Color.blue ) );
			MundoNaves.this.addObjeto( new Nave( 500, 120, 28, 530, 180, Color.blue ) );
			MundoNaves.this.addObjeto( new Nave( 600, 280, 28, 690, 300, Color.blue ) );
			MundoNaves.this.addObjeto( new Nave( 700, 500, 14, 630, 450, Color.green ) );
			MundoNaves.this.addObjeto( new Asteroide( 350, 300, 150, Color.magenta ) );
			MundoNaves.this.addObjeto( new Asteroide( 700, 130, 80, Color.magenta ) );
			navePrincipal = new NaveTripulada( 600, 500, 40, 600, 500, Color.magenta );
			MundoNaves.this.addObjeto( navePrincipal );
		} else if (tipoInit==2) {  // Dos naves grandes que van a chocar
			MundoNaves.this.addObjeto( new Nave( 300, 100, 100, 1900, 420, Color.blue ) );
			MundoNaves.this.addObjeto( new Nave( 565, 120, 100, -215, 560, Color.magenta ) );
		}
	}
	
		// M�todo privado para ejecutar un c�digo sin interferir con el ciclo de ejecuci�n del juego
		private void ejecutaSeguro( Runnable miRun ) {
			if (!running)
				miRun.run();  // Si no est� ejecut�ndose el juego, se inicia
			else
				run = miRun;  // Si ya est� ejecut�ndose el juego se almacena el trabajo y el propio juego lo lanzar� cuando sea seguro
		}
		
		private boolean running = false;
	// Permitiendo interacci�n con el rat�n para crear naves y/o lanzarlas en diagonal
	private void moverMundo() {
		running = true;
		primerClick = null;
		ultimoClick = null;
		objetoClickado = null;
		VentanaGrafica v = this.getVentana();
		PAUSA = true;
		ventana.setMensaje( "Pausa ON. Pulsa P para iniciar");
		while (!v.estaCerrada()) {  // hasta que se cierre la ventana
			// 0.- Cambiar par�metros con posibles indicaciones de teclado
			procesarTeclado();
			// 1.- Chequear posible interacci�n de rat�n
			procesarRaton(v);
			// 2.- Hacer movimiento de los objetos en el lapso de tiempo ocurrido
			moverObjetos(v);
			// 3.- Calcular y corregir choques en el mundo
			corregirMovimiento(v);
			// 4.- Dibujado expl�cito de todos los objetos
			dibujadoMundo(v);
			// 5.- Ciclo de espera hasta la siguiente iteraci�n
			this.getVentana().espera( MILIS_ENTRE_MOVTOS );
			// 6.- Posible trabajo de reinicializaci�n que est� pendiente
			if (run!=null) { run.run(); run = null; }
		}
		running = false;
	}

	// 0.- Cambiar par�metros con posibles indicaciones de teclado
	private void procesarTeclado() {
		int tecla = ventana.getCodUltimaTeclaTecleada(); 
		if (tecla==KeyEvent.VK_V) {
			ObjetoMovil.DIBUJAR_VELOCIDAD = !ObjetoMovil.DIBUJAR_VELOCIDAD;
			ventana.setMensaje( "Dibujar velocidad " + (ObjetoMovil.DIBUJAR_VELOCIDAD ? "ON" : "OFF") );
		} else if (tecla==KeyEvent.VK_P) {
			PAUSA = !PAUSA;
			ventana.setMensaje( "Pausa " + (PAUSA ? "ON" : "OFF") );
		} else if (tecla==KeyEvent.VK_C) {
			VER_CHOQUES = !VER_CHOQUES;
			ventana.setMensaje( "Dibujar y parar c�lculo en choques " + (VER_CHOQUES ? "ON" : "OFF") );
		} else if (tecla==KeyEvent.VK_PLUS) {
			if (MILIS_POR_MOVIMIENTO<132) {
				MILIS_POR_MOVIMIENTO = MILIS_POR_MOVIMIENTO * 2;
				if (MILIS_POR_MOVIMIENTO >= MILIS_ENTRE_MOVTOS)
					ventana.setMensaje( "Tiempo visualizaci�n x" + (1.0 * MILIS_POR_MOVIMIENTO / MILIS_ENTRE_MOVTOS) );
				else 
					ventana.setMensaje( "Tiempo visualizaci�n /" + (1.0 * MILIS_ENTRE_MOVTOS / MILIS_POR_MOVIMIENTO) );
			}
		} else if (tecla==KeyEvent.VK_MINUS) {
			if (MILIS_POR_MOVIMIENTO>1) {
				MILIS_POR_MOVIMIENTO = MILIS_POR_MOVIMIENTO / 2;
				if (MILIS_POR_MOVIMIENTO >= MILIS_ENTRE_MOVTOS)
					ventana.setMensaje( "Tiempo visualizaci�n x" + (1.0 * MILIS_POR_MOVIMIENTO / MILIS_ENTRE_MOVTOS) );
				else 
					ventana.setMensaje( "Tiempo visualizaci�n /" + (1.0 * MILIS_ENTRE_MOVTOS / MILIS_POR_MOVIMIENTO) );
			}
		}
		// Pulsaciones activas
		tecla = ventana.getCodTeclaQueEstaPulsada(); 
		if (ventana.isTeclaPulsada( KeyEvent.VK_LEFT )) {
			navePrincipal.gira( -Math.PI/20 );
		} else if (ventana.isTeclaPulsada( KeyEvent.VK_RIGHT )) {
			navePrincipal.gira( Math.PI/20 );
		} else if (ventana.isTeclaPulsada( KeyEvent.VK_UP )) {
			navePrincipal.acelera( 10 );
		} else if (ventana.isTeclaPulsada( KeyEvent.VK_DOWN )) {
			navePrincipal.acelera( -10 );
		}
	}

		private Point primerClick = null;
		private Point ultimoClick = null;
		private ObjetoMovil objetoClickado = null;
	// 1.- Chequear posible interacci�n de rat�n
	private void procesarRaton(VentanaGrafica v) {
		Point clickRaton = v.getRatonPulsado();
		if (clickRaton==null) {
			if (primerClick!=null && ultimoClick!=null && objetoClickado!=null && !primerClick.equals(ultimoClick)) { // Ha habido un drag sobre un objeto
				// Aplicar fuerza a objeto
				objetoClickado.setVelocidadX( (ultimoClick.x - primerClick.x)*10.0 );
				objetoClickado.setVelocidadY( (ultimoClick.y - primerClick.y)*10.0 );
			} else if (primerClick!=null && ultimoClick!=null && objetoClickado==null && !primerClick.equals(ultimoClick)) {  // No hay drag. Creaci�n de objeto nuevo
				double radio = primerClick.distance( ultimoClick );
				if (radio >= 5) {  // Por debajo de 10 p�xels no se considera
					Color color = Color.blue;
					switch (random.nextInt( 3 )) {
						case 0: {
							color = Color.red;
							break;
						}
						case 1: {
							color = Color.green;
							break;
						}
					}
					Nave nave = new Nave( primerClick.x, primerClick.y, 20, ultimoClick.x, ultimoClick.y, color );
					if (this.addObjeto( nave ))
						nave.dibuja( this.getVentana() );
				}
			}
			primerClick = null;
		} else {
			if (primerClick==null) {
				primerClick = clickRaton;
				ultimoClick = null;
				objetoClickado = null;
				for (ObjetoMovil objeto : this.getObjetos()) {
						if (objeto!=null) {
						if (objeto.contieneA(primerClick)) {
							objetoClickado = objeto;
							break;
						}
					}
				}
			} else {
				ultimoClick = clickRaton;
			}
		}
	}
	
	// 2.- Hacer movimiento de los objetos en el lapso de tiempo ocurrido
	private void moverObjetos(VentanaGrafica v) {
		if (!PAUSA) {
			for (ObjetoMovil objeto : this.getObjetos()) {
				if (objeto != null) {  // Ojo, solo con los objetos que haya!
					// Se mueve el objeto
					objeto.mueveUnPoco( v, MILIS_POR_MOVIMIENTO, false );  // M�todo para movimiento con influencia de gravedad   (sin dibujado)
				}
			}
		}
	}

	// 3.- Calcular y corregir choques en el mundo
	private void corregirMovimiento(VentanaGrafica v) {
		if (!PAUSA) {
			boolean hayChoques;
			int numIteraciones = 0;
			do { 
				numIteraciones++;
				hayChoques = false;
				// 3a.- Comprobamos choques con los l�mites de la ventana
				for (ObjetoMovil objeto : this.getObjetos()) {
					if (objeto != null) {  // Ojo, solo con los objetos que haya!
						// Choque lateral
						int choque = objeto.chocaConBorde( v );
						// System.out.println( choque );
						if ((choque & 0b0001) != 0 && objeto.getVelocidadX()<0) { // Choque izquierda
							objeto.rebotaIzquierda( 1.0 );  // Rebota al 100% -sale hacia la derecha-
							objeto.corrigeChoqueLateral( v, false );
							hayChoques = true;
							reboteEnBorde( objeto, 0 );
						} else if ((choque & 0b0010) != 0 && objeto.getVelocidadX()>0) {  // Choque derecha
							objeto.rebotaDerecha( 1.0 );  // Rebota al 100% -sale hacia la izquierda-
							objeto.corrigeChoqueLateral( v, false );
							hayChoques = true;
							reboteEnBorde( objeto, 1 );
						}
						// Choque en vertical
						if (choque>=8) {  // Abajo
							hayChoques = true;
							if (objeto.getVelocidadY()>0) objeto.corrigeChoqueVertical( v, false );
							objeto.rebotaAbajo( 1.0 );
							objeto.corrigeChoqueVertical( v, false );
							reboteEnBorde( objeto, 3 );
						} else if (choque>=4) {  // Arriba
							hayChoques = true;
							if (objeto.getVelocidadY()<0) objeto.corrigeChoqueVertical( v, false );
							objeto.rebotaArriba( 1.0 );
							objeto.corrigeChoqueVertical( v, false );
							reboteEnBorde( objeto, 2 );
						}
					}
				}
				// 3b.- Comprobamos choques entre objetos
				// Probamos todas con todas (salen rebotadas en la direcci�n del choque)
				for (int i=0; i<this.getObjetos().size(); i++) {
					ObjetoMovil objeto = this.getObjetos().get(i); 
					if (objeto != null) {
						for (int j=i+1; j<this.getObjetos().size(); j++) {
							ObjetoMovil objeto2 = this.getObjetos().get(j); 
							if (objeto2 != null) {
								if (objeto.chocaConObjeto( objeto2 )!=null) {
									procesaChoque( objeto, objeto2 );
								}
							}
						}
					}
				}
			} while (hayChoques && numIteraciones<=3);
		}
	}

	// 4.- Dibujado expl�cito de todos los objetos
	private void dibujadoMundo(VentanaGrafica v) {
		// Dibujado de mundo
		this.getVentana().borra();  // Borra todo
		for (ObjetoMovil objeto : this.getObjetos()) {  // Y dibuja de nuevo todos los objetos
			if (objeto != null) {
				objeto.dibuja( this.getVentana() );
			}
		}
		// Feedback visual de interacciones
		if (primerClick!=null && ultimoClick!=null) {
			if (objetoClickado!=null) {  // Se est� queriendo imprimir velocidad a un objeto
				ventana.dibujaFlecha( primerClick.getX(), primerClick.getY(), ultimoClick.getX(), ultimoClick.getY(), 1.0f, Color.orange, 25 );
			} else {  // Se est� queriendo crear un objeto
				ventana.dibujaCirculo( primerClick.getX(), primerClick.getY(), 20, 1.0f, Color.orange );
				ventana.dibujaFlecha( primerClick.getX(), primerClick.getY(), ultimoClick.getX(), ultimoClick.getY(), 1.0f, Color.orange, 25 );
			}
		}
		if (!PAUSA) trasCadaFotograma( this.getObjetos() );
	}
	
	// M�todos de l�gica de la animaci�n
	
	// Se ejecuta en cada choque y recibe los objetos que chocan
	private void procesaChoque( ObjetoMovil objeto, ObjetoMovil objeto2 ) {
		double milis = MILIS_POR_MOVIMIENTO;
//TODO Hacer bien el tema de los choques con aproximaciones sucesivas
//		if (objeto instanceof Nave && objeto2 instanceof Nave) {
//			Nave nave = (Nave) objeto;
//			Nave nave2 = (Nave) objeto2;
//			nave.dibuja( ventana );
//			nave2.dibuja( ventana );
//			nave.deshazUltimoMovimiento( ventana );
//			nave2.deshazUltimoMovimiento( ventana );
//			double milisChoque = calcularChoqueExacto( 0, ventana, nave, nave2, MILIS_POR_MOVIMIENTO );
//			milis = milis - milisChoque ;
//			nave.dibuja( ventana );
//			nave2.dibuja( ventana );
//			nave.mueveUnPoco( ventana, milis, false );
//			nave2.mueveUnPoco( ventana, milis, false );
//		}
		// Aplica velocidad de choque en funci�n de las masas (el que tiene masa m�s grande se ve menos afectado y viceversa)
		Fisica.calcChoqueEntreObjetos(ventana, objeto, objeto2, milis, VER_CHOQUES );
//TODO Aproximaciones sucesivas
//		if (objeto instanceof Nave && objeto2 instanceof Nave) {
//			Nave nave = (Nave) objeto;
//			Nave nave2 = (Nave) objeto2;
//			nave.deshazUltimoMovimiento( ventana );
//			nave2.deshazUltimoMovimiento( ventana );
//		}
		if (VER_CHOQUES) {  // Espera a pulsaci�n de rat�n
			if (ventana.getRatonPulsado()==null) { // Si el rat�n no est� pulsado...
				while (ventana.getRatonPulsado()==null && !ventana.estaCerrada()) {}  // Espera a pulsaci�n...
				while (ventana.getRatonPulsado()!=null && !ventana.estaCerrada()) {}  // ...y suelta
			}
		}
		// TAREA 1
		if (objeto==navePrincipal || objeto2==navePrincipal) {
			ObjetoMovil chocado = objeto;
			if (objeto==navePrincipal) chocado = objeto2;
			navePrincipal.cambiaEnergia( - chocado.getArea()/20 );
			ventana.setMensaje( "Choque: Energ�a = " + navePrincipal.getEnergia() );
			if (navePrincipal.getEnergia()<0) {
				objetos.remove( navePrincipal );
				ventana.setMensaje( "Juego terminado! Has perdido" );
			}
		}
	}

	// Se ejecuta tras cada fotograma y recibe todos los objetos en pantalla
	private void trasCadaFotograma( ArrayList<ObjetoMovil> listaObjetos ) {
		
	}
	
	// Se ejecuta tras rebotar en un borde de la ventana un objeto.
	// C�digo de borde: 0-Izquierda 1-Derecha 2-Arriba 3-Abajo
	private void reboteEnBorde( ObjetoMovil objeto, int codigoBorde ) {
		// TODO
	}
	
	// Se ejecuta al pulsar el bot�n de test y recibe todos los objetos en pantalla
	private void test( ArrayList<ObjetoMovil> listaObjetos ) {
		// TODO
	}
	public void cargarDeFichero( ArrayList<ObjetoMovil> listaObjetos ) {
		// TODO
	}

	public void guardarAFichero( ArrayList<ObjetoMovil> listaObjetos ) {
		// TODO
	}

	// TODO Aproximaciones sucesivas
	@SuppressWarnings("unused")
	private double calcularChoqueExacto( int numLlamadas, VentanaGrafica ventana, Nave nave, Nave nave2, double milisMovimiento ) {
		if (numLlamadas>=5) return 0; // Caso base
		double milisMitad = milisMovimiento / 2;
		nave.mueveUnPoco( ventana, milisMitad, false );
		nave2.mueveUnPoco( ventana, milisMitad, false );
		if (nave.chocaConObjeto( nave2 )==null) {   // Ahora no chocan. Validamos el movimiento y probamos con la mitad siguiente
			return milisMitad + calcularChoqueExacto( numLlamadas+1, ventana, nave, nave2, milisMitad );
		} else {  // Ahora s� chocan. Anulamos el movimiento y nos vamos hacia la mitad anterior
			nave.deshazUltimoMovimiento( ventana );
			nave2.deshazUltimoMovimiento( ventana );
			return calcularChoqueExacto( numLlamadas+1, ventana, nave, nave2, milisMitad );
		}
	}
	
}
