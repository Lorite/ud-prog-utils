package utils.juego2d.pruebas;
import java.awt.Color;
import java.awt.Point;

import utils.juego2d.utils.Fisica;
import utils.juego2d.utils.PolarPoint;
import utils.ventanas.ventanaBitmap.VentanaGrafica;

/** Clase que permite crear y gestionar rectángulos
 * Realizada sin herencia (copiar-y-pegar desde Pelota)
 */
public class Bloque extends ObjetoMovil {
	private double anchura;
	private double altura;
	
	/** Constructor de bloque con datos
	 * @param x	Coordenada x de la esquina superior izquierda del bloque
	 * @param y	Coordenada y de la esquina superior izquierda del bloque
	 * @param ancho Anchura del bloque
	 * @param alto	Altura del bloque
	 * @param color	Color del bloque
	 * @param bota	Información de si el bloque bota (true) o no (false)
	 */
	public Bloque(double x, double y, double ancho, double alto, Color color, boolean bota) {
		super( x, y, color );
		anchura = ancho;
		altura = alto;
	}
	
	/** Constructor de bloque con datos mínimos. Crea un bloque azul que bota
	 * @param x	Coordenada x de la esquina sup izqda
	 * @param y	Coordenada y de la esquina sup izqda
	 * @param ancho Anchura del bloque
	 * @param alto	Altura del bloque
	 */
	public Bloque(double x, double y, double ancho, double alto) {
		this( x, y, ancho, alto, Color.blue, true );  // Reutilizar el constructor más específico desde otro
	}
	
	public double getAnchura() {
		return anchura;
	}

	public void setAnchura(double anchura) {
		this.anchura = anchura;
	}

	public double getAltura() {
		return altura;
	}

	public void setAltura(double altura) {
		this.altura = altura;
	}

	/** Calcula el volumen del bloque suponiendo que su fondo es el mismo que la anchura
	 * @return	Volumen del bloque
	 */
	@Override
	public double getVolumen() {
		return anchura * altura * anchura;
	}
	
	/** Calcula el área del bloque partiendo de su información
	 * @return	Area del bloque suponiendo un rectángulo perfecto
	 */
	@Override
	public double getArea() {
		return anchura * altura;
	}
	
	/** Dibuja el bloque en una ventana, en el color correspondiente (por defecto, negro)
	 * @param v	Ventana en la que dibujar el bloque
	 */
	@Override
	public void dibuja( VentanaGrafica v ) {
		v.dibujaRect( x, y, anchura, altura, 1.5f, color );
		super.dibuja( v );  // Posible dibujo de velocidad
	}
	
	/** Borra el bloque en una ventana
	 * @param v	Ventana en la que borrar el bloque
	 */
	@Override
	public void borra( VentanaGrafica v ) {
		v.borraRect( x, y, anchura, altura, 1.5f );
	}

	@Override
	public void corrigeChoqueVertical(VentanaGrafica v, boolean dibujar) {
		corrigeChoqueInferior( v, dibujar );
	}
	
	/** Ajusta el bloque al suelo, si se ha "pasado" del suelo. Ajusta la velocidad a la que tenía cuando lo tocó.
	 * @param v	Ventana de la que ajustar al suelo
	 * @param dibujar	true si se quiere dibujar el bloque, false en caso contrario
	 */
	public void corrigeChoqueInferior( VentanaGrafica v, boolean dibujar ) {
		double dondeToca = v.getAltura() - altura;  // Coordenada y en la que se toca el suelo, a la que hay que ajustar
		if (dondeToca >= y) return;  // Si no está pasando el suelo, no se hace nada 
		setY( dondeToca );
		// Si estaba cayendo con una velocidad positiva (hacia abajo), entonces la velocidad real que tenía cuando
		// "tocó" el suelo era algo menor. Corregimos posición y velocidad (aproximadamente)
		if (velYInicial>0 && velocidadY>0) {
			double tiempoChoque = Fisica.calcTiempoHastaEspacio( velYInicial, antY, Fisica.getGravedad(), dondeToca);
			if (tiempoChoque > 0) {  // Si hay un error en el cálculo no se aplica corrección
				if (dibujar) borra( v );
				long tiempoMsgs = Math.round(tiempoChoque*1000.0);
				setX( Fisica.calcEspacio( antX, tiempoMsgs, velXInicial ) );  // Corrige la posición
				setVelocidadY( Fisica.calcVelocidad( velYInicial, tiempoMsgs, Fisica.getGravedad() )); // Corrige la velocidad
				if (dibujar) dibuja( v );
			}
		}
	}
	
	@Override
	public void corrigeChoqueLateral(VentanaGrafica v, boolean dibujar) {
		if (x<0) {
			corrigeChoqueLateral( v, true, dibujar );
		} else if (x + anchura >= v.getAnchura() ){
			corrigeChoqueLateral( v, false, dibujar );
		}
	}
	
	/** Ajusta el bloque al lateral
	 * @param v	Ventana de la que ajustar al lateral
	 * @param izquierda	true para izquierda, false para derecha
	 * @param dibujar	true si se quiere dibujar el bloque, false en caso contrario
	 */
	public void corrigeChoqueLateral( VentanaGrafica v, boolean izquierda, boolean dibujar ) {
		// Corregimos al lateral sin tocar la velocidad
		if (dibujar) borra( v );
		if (izquierda) {
			setX( 0 );
		} else {
			setX( v.getAnchura() - anchura );
		}
		if (dibujar) dibuja( v );
	}
	
	/** Detecta el choque del bloque con los bordes de la ventana
	 * @param v	Ventana con la que probar el choque
	 * @return	Devuelve un número formado por la suma de: 0 si no choca, 1 si choca con la izquierda, 2 con la derecha, 4 arriba, 8 abajo.
	 */
	@Override
	public int chocaConBorde( VentanaGrafica v ) {
		int ret = 0;
		if (x<=0) ret += 1;
		if (x+anchura>=v.getAnchura()) ret += 2;
		if (y<=0) ret += 4;
		if (y+altura>=v.getAltura()) ret += 8;
		return ret;
	}
	
	/** Detecta el choque del bloque con otra
	 * @param bloque2	Bloque con el que probar el choque
	 * @return	Devuelve null si no chocan, un vector con forma de punto indicando el ángulo y amplitud del choque sobre el bloque en curso, 
	 * 			(0,0) si el bloque 2 incluye al bloque 1
	 */
	@Override
	public Point chocaConObjeto( ObjetoMovil objeto2 ) {
		if (objeto2 instanceof Bloque) {
			Bloque bloque2 = (Bloque) objeto2;
			Point p = new Point();
			if (bloque2.x > x+anchura) {
				return null;
			} else {
				if (bloque2.y > y+altura) {
					return null;
				} else {
					if (bloque2.x+bloque2.anchura < x) {
						return null;
					} else {
						if (bloque2.y+bloque2.altura < y) {
							return null;
						} else {  // Chocan. Veamos cómo
							if (bloque2.x > x) {  // Bloque2 a la derecha del 1
								if (bloque2.x+bloque2.anchura<x+anchura) {  // El bloque2 está "dentro" en horizontal
									if (bloque2.y > y) {  
										if (bloque2.y+bloque2.altura < y+altura) {  // El bloque2 está "dentro" en vertical
											p.setLocation( 0.0, 0.0 );
										} else {  // Bloque2 abajo dentro (toque inferior)
											p.setLocation( 0.0, y+altura-bloque2.y );
										}
									} else if (bloque2.y+bloque2.altura < y+altura) {  // Bloque2 arriba dentro (toque superior)
										p.setLocation( 0.0, y-bloque2.y-bloque2.altura );
									} else {  // Bloque 2 vertical integrando al bloque 1 (toque vertical total)
										p.setLocation( 0.0, altura );
									}
								} else {  // Bloque2 a la derecha
									if (bloque2.y > y) {  
										if (bloque2.y+bloque2.altura < y+altura) {  // El bloque2 está "dentro" en vertical (a la derecha)
											p.setLocation( x+anchura-bloque2.x, 0.0 );
										} else {  // Bloque2 abajo a la derecha (toque esquina)
											p.setLocation( x+anchura-bloque2.x, y+altura-bloque2.y );
										}
									} else if (bloque2.y+bloque2.altura < y+altura) {  // Bloque2 arriba a la derecha (toque esquina)
										p.setLocation( x+anchura-bloque2.x, y-bloque2.y-bloque2.altura );
									} else {  // Bloque 2 a la derecha integrando al bloque 1 (toque derecha total)
										p.setLocation( x+anchura-bloque2.x, 0.0 );
									}
								}
							} else if (bloque2.x+bloque2.anchura < x+anchura) {   // Bloque2 a la izquierda del 1
								if (bloque2.y > y) {
									if (bloque2.y+bloque2.altura < y+altura) {  // El bloque2 está "dentro" en vertical (a la izquierda)
										p.setLocation( x-bloque2.x-bloque2.anchura, 0.0 );
									} else {   // Bloque2 abajo a la izquierda(toque esquina)
										p.setLocation( x-bloque2.x-bloque2.anchura, y+altura-bloque2.y );
									}
								} else if (bloque2.y+bloque2.altura < y+altura) {  // Bloque2 arriba a la izquierda (toque esquina)
									p.setLocation( x-bloque2.x-bloque2.anchura, y-bloque2.y-bloque2.altura );
								} else {  // Bloque 2 a la izquierda integrando al bloque 1 (toque izquierda total)
									p.setLocation( x-bloque2.x-bloque2.anchura, 0.0 );
								}
							} else {  // Bloque 2 integra toda la anchura del 1
								if (bloque2.y > y) {  // Bloque2 abajo (toque abajo total)
									if (bloque2.y+bloque2.altura < y+altura) {  // El bloque2 está "dentro" en vertical (toque horizontal total) {
										p.setLocation( anchura, 0.0 );
									} else {
										p.setLocation( 0.0, y+altura-bloque2.y );
									}
								} else if (bloque2.y+bloque2.altura < y+altura) {  // Bloque2 arriba (toque arriba total)
									p.setLocation( 0.0, y-bloque2.y-bloque2.altura );
								} else {  // Bloque 2 integrando al bloque 1 (bloque1 dentro total del bloque 2)
									p.setLocation( 0.0, 0.0 );
								}
							}
							return p;
						}
					}
				}
			}
		} else if (objeto2 instanceof Pelota) {
			Pelota pelota = (Pelota) objeto2;
			Point p = new Point();
			int cuadrante = cuadrante( pelota.x, pelota.y );
			if (cuadrante==1) {  // 1. Cuadrante izquierdo
				double origenX = x + altura/2.0;
				double origenY = y + altura/2.0;
				p.setLocation(pelota.x - origenX, pelota.y - origenY);  // Vector de origen a centro pelota
				PolarPoint pp = PolarPoint.pointToPolar( p );
				pp.setModulo( pp.getModulo() - pelota.getRadio() );  // Vector de origen al punto más cercano de la esfera
				double moduloABorde = pp.getModuloParaX( altura/2.0 );
				if (moduloABorde < pp.getModulo())  // no hay choque
					return null;
				else {  // Sí hay choque
					pp.setModulo( moduloABorde - pp.getModulo() );
					p.setLocation( pp.getX(), pp.getY() );
				}
			} else if (cuadrante==2) {  // 2. Test cuadrante derecho
				double origenX = x + anchura - altura/2.0;
				double origenY = y + altura/2.0;
				p.setLocation(pelota.x - origenX, pelota.y - origenY);  // Vector de origen a centro pelota
				PolarPoint pp = PolarPoint.pointToPolar( p );
				pp.setModulo( pp.getModulo() - pelota.getRadio() );  // Vector de origen al punto más cercano de la esfera
				double moduloABorde = pp.getModuloParaX( altura/2.0 );
				if (moduloABorde < pp.getModulo())  // no hay choque
					return null;
				else {  // Sí hay choque
					pp.setModulo( moduloABorde - pp.getModulo() );
					p.setLocation( pp.getX(), pp.getY() );
				}
			} else if (cuadrante==3) {  // 3. Test cuadrante superior
				double origenX = x + anchura/2.0;
				double origenY = y + anchura/2.0;
				p.setLocation(pelota.x - origenX, pelota.y - origenY);  // Vector de origen a centro pelota
				PolarPoint pp = PolarPoint.pointToPolar( p );
				pp.setModulo( pp.getModulo() - pelota.getRadio() );  // Vector de origen al punto más cercano de la esfera
				double moduloABorde = pp.getModuloParaY( anchura/2.0 );
				if (moduloABorde < pp.getModulo())  // no hay choque
					return null;
				else {  // Sí hay choque
					pp.setModulo( moduloABorde - pp.getModulo() );
					p.setLocation( pp.getX(), pp.getY() );
				}
			} else if (cuadrante==4) {  // 4. Test cuadrante inferior
				double origenX = x + altura - anchura/2.0;
				double origenY = y + anchura/2.0;
				p.setLocation(pelota.x - origenX, pelota.y - origenY);  // Vector de origen a centro pelota
				PolarPoint pp = PolarPoint.pointToPolar( p );
				pp.setModulo( pp.getModulo() - pelota.getRadio() );  // Vector de origen al punto más cercano de la esfera
				double moduloABorde = pp.getModuloParaX( anchura/2.0 );
				if (moduloABorde < pp.getModulo())  // no hay choque
					return null;
				else {  // Sí hay choque
					pp.setModulo( moduloABorde - pp.getModulo() );
					p.setLocation( pp.getX(), pp.getY() );
				}
			} else {  // 5.- Nunca debería no estar en ningún cuadrante
				return null;
			}
			return p;
		} else {
			// Si fueran otros tipos de objetos no hay choque
			return null;
		}
	}
	
		// Devuelve el cuadrante donde está (1-izquierda, 2-derecha, 3-arriba, 4-abajo)
		private int cuadrante( double xPelota, double yPelota ) {
			int cuad = 0;
			double yIzquierdaDesde = y + (xPelota - x);  // 1. Test cuadrante izquierdo
			double yIzquierdaHasta = y + altura - (xPelota - x);
			if (yPelota >= yIzquierdaDesde && yPelota <= yIzquierdaHasta) {
				cuad = 1;
			}
			double yDerechaDesde = y + (xPelota - x - anchura);  // 2. Test cuadrante derecho
			double yDerechaHasta = y + altura - (xPelota - x - anchura);
			if (yPelota >= yDerechaDesde && yPelota <= yDerechaHasta) {
				if (cuad==1) {  //  Puede ser lo mismo izquierda que derecha... ver cuál está más cerca
					if (xPelota > x+anchura/2.0) cuad = 2;
				} else {
					cuad = 2;
				}
			} 
			double xSupDesde = x + (yPelota - y);  // 3. Test cuadrante superior
			double xSupHasta = x + altura - (yPelota - y);
			if (xPelota >= xSupDesde && xPelota <= xSupHasta) {
				cuad = 3;
			} 
			double xInfDesde = x + (yPelota - y - altura);  // 4. Test cuadrante inferior
			double xInfHasta = x + anchura - (yPelota - y - altura);
			if (xPelota >= xInfDesde && xPelota <= xInfHasta) {
				if (cuad==3) {  //  Puede ser lo mismo izquierda que derecha... ver cuál está más cerca
					if (yPelota > y+altura/2.0) cuad = 4;
				} else {
					cuad = 4;
				}
			}
			return cuad;
		}
	
	/** Comprueba si el bloque incluye a un punto dado
	 * @param punto	Punto a chequear
	 * @return	true si el punto está dentro del bloque, false en caso contrario
	 */
	@Override
	public boolean contieneA( Point punto ) {
		return punto.x >= x && punto.y >= y && punto.x<=x+anchura && punto.y<=y+altura;
	}

	@Override
	public boolean isBota() {
		return false;
	}
	
	@Override
	public String toString() {
		return String.format( "Bloque %1s (%2$7.2f,%3$7.2f) - (%4$5.1f,%4$5.1f) Vel.=(%6$6.3f,%7$6.3f)", nombre, x, y, x+anchura, y+altura, velocidadX, velocidadY );
	}
	
}

