package br.com.guilherme.tcc.test;

import java.io.DataInputStream;
import java.io.IOException;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.RConsole;

public class ClientTest {
	// DEFINI��O DE VARI�VEIS PARA APLICA��O
	
	// DEFINI��O DE VARI�VEIS UTILIZADAS PARA A��ES DE CONTROLE
	public static final long MS_20 = 100;
	public static final NXTRegulatedMotor MOTOR_RIGTH = Motor.B;
	public static final NXTRegulatedMotor MOTOR_LEFT = Motor.C;
	public static final Integer SPEED = 30;
	public static final Double DEG_TO_RAD = (Math.PI / 180);
	public static final Double RAD_TO_DEG = (180 / Math.PI);
	public static final Double L = 0.122; // tamanho do eixo das rodas do rob�
	public static final Double r = 0.0215; // raio da roda
	public static final Double R = 0.15;
	public static final Double R_M = 0.22;
	public static final Double CONST_EQ = 0.1;

	// DEFINI��O DA FUN��O REDUZIDA DA CIRCUNFERENCIA
	// (x - a)^2 + (y - b)^2 = r^2; -> equa��o reduzida
	// x^2 + y^2 - 2ax - 2by + a^2 + b^2 - r^2 = 0 -> equa��o geral
	// sqrt((x - x0)^2 + (y - y0)^2)-> distancia entre dois pontos

	// DEFINI��O DE VARI�VEIS UTILIZADAS PARA CONTROLE MANUAL
	public static final char LEFT = 'a'; // esquerda
	public static final char RIGHT = 'd'; // direita
	public static final char FWD = 'w'; // para frente
	public static final char BWD = 's'; // para tras
	public static final char STOP = 'q'; // parar movimento
	public static final char CONNECT = 'c';
	public static final char MANUAL_CONTROL = 'm';
	public static final char AUTO_CONTROL = 'u';

	// DEFINI��O DE VARI�VEIS UTILIZADAS PARA ESCREVER EM UM ARQUIVO
	// private static ManagerCSVFiles manager;

	// metodo principal
	public static void main(String[] args) {
		doControl();
		/*char controle = 0;
		boolean autoProcessed = false;

		LCD.drawString("Esperando", 0, 0); 
		NXTConnection conexao = Bluetooth.waitForConnection(); 
		// configurando a conx�o para se comunicar com dispositivos m�veis, como um celular Android
		conexao.setIOMode(NXTConnection.RAW);
		// fluxo de entrada de dados
		DataInputStream in = conexao.openDataInputStream();
		
		LCD.clear(); // limpando e tela
		LCD.drawString("Conectado", 0, 0);
		
		while (!Button.ESCAPE.isDown()) {
			try {
				controle = in.readChar();
				if(controle == 'm') {
					executeMoveManul(in);
				}
				if(controle == 'u' && !autoProcessed) { 
					doControl();
					autoProcessed = true;
				}
			 } catch (IOException e) {
				 System.out.println(e.getMessage().toString());
			 }
		}
		*/
	}

	// metodo responsavel por realizar o movimento no Robo
	public static void performMove(char cmd, int speed) {
		int velocidade = speed * 9; // define velocidade
		Motor.B.setSpeed(velocidade);
		Motor.C.setSpeed(velocidade);
		switch (cmd) {
		case FWD:
			Motor.B.forward();
			Motor.C.forward();
			break;
		case BWD:
			Motor.B.backward();
			Motor.C.backward();
			break;
		case LEFT:
			Motor.B.forward();
			Motor.C.backward();
			break;
		case RIGHT:
			Motor.C.forward();
			Motor.B.backward();
			break;
		case STOP:
			Motor.C.stop();
			Motor.B.stop();
			break;
		}
	}
	
	// metodo responsavel por realizar movimento manual
	public static void executeMoveManul(DataInputStream in){
		char letra = 0;
		byte speed = 0;
		try {
			letra = in.readChar();
			speed = in.readByte();
			LCD.clear();
		} catch (IOException e) {
			e.getMessage();
		}
		performMove(letra, speed);
	}
	
	// metedo reponsavel por realizar o controle sobre o robo
	public static void doControl() {
		RConsole.open();
		
		
		Long time = new Long(0);
		long prev_deg_r = 0;
		long prev_deg_l = 0;
		long t0 = System.currentTimeMillis();
		
		Double x = 0.0d, y = 0.0d, theta = 0.0d;
		float x_a = 0.6f, y_a = 0.6f;
		
		float e_x, e_y, e_theta, theta_d;
		double x_d = 0.0, y_d = 0.0;
		
		Double D_l, D_r, D_c;
		float v, w, w_r, w_l;
		float k_theta = 1.0f;

		//37700
		while (System.currentTimeMillis() - t0 <= 18800) {

			time = System.currentTimeMillis() - t0;
			//RConsole.println("Time: " + time.doubleValue() + " | Den " + Double.valueOf(0.5) * time.doubleValue() + " | " + (Double.valueOf(0.5) * time.doubleValue())/1000);
			
			//if(checkIfPointBelongsCircumference(x_a, y_a, x, y)){
				//RConsole.println("if");
				//x_d = R * (Math.cos((Double.valueOf(0.333) * time.doubleValue())/1000)) + x_a;
				//RConsole.println("x_d: "+x_d);
				//y_d = R * (Math.sin((Double.valueOf(0.333) * time.doubleValue())/1000)) + y_a;
				//RConsole.println("y_d: "+y_d);
			//}else{
				//x_d = x_a;
				//y_d = y_a;
			//}
			
			//x_d = R * (Math.cos((Double.valueOf(0.333) * time.doubleValue())/1000)) + x_a;
			//y_d = R * (Math.sin((Double.valueOf(0.333) * time.doubleValue())/1000)) + y_a;
			x_d = x_a;
			y_d = y_a;
			
			e_x = (float) (x_d - x);
			//RConsole.println("x_d - x = e_x => "+x_d + "-" +x + " = "+e_x);
			e_y = (float) (y_d - y);
			//RConsole.println("y_d - y = e_y => "+y_d + "-" +y + " = "+e_y);
			
			if(Math.sqrt(Math.pow(e_x, 2)+Math.pow(e_y, 2)) < 0.05){
				MOTOR_RIGTH.stop();
				MOTOR_LEFT.stop();
				break;
			}
			
			theta_d = (float) (Math.atan2(e_y, e_x)); // radianos
			e_theta = (float) (theta_d - theta);
			e_theta = (float) (Math.atan2(Math.sin(e_theta), Math.cos(e_theta)));
			
			double value = ((Math.exp(Math.sqrt(Math.pow(e_x, 2) + Math.pow(e_y, 2))))
					- Math.exp(-(Math.sqrt(Math.pow(e_x, 2) + Math.pow(e_y, 2)))))
					/ ((Math.exp(Math.sqrt(Math.pow(e_x, 2) + Math.pow(e_y, 2))))
							+ Math.exp(-(Math.sqrt(Math.pow(e_x, 2) + Math.pow(e_y, 2)))));
			
			v = (float) (0.1 * value + CONST_EQ);
			
			w = k_theta * e_theta;
			
			w_r = (float) ((2 * v + w * L) / (2 * r)); // rad/s
			w_r = (float) (w_r * RAD_TO_DEG);
			
			w_l = (float) ((2 * v - w * L) / (2 * r)); // rad/s
			w_l = (float) (w_l * RAD_TO_DEG);
			
			MOTOR_RIGTH.setSpeed((float) w_r);
			MOTOR_RIGTH.forward();

			MOTOR_LEFT.setSpeed((float) w_l);
			MOTOR_LEFT.forward();

			long deg_r = MOTOR_RIGTH.getTachoCount() - prev_deg_r;
			prev_deg_r = MOTOR_RIGTH.getTachoCount();
			
			long deg_l = MOTOR_LEFT.getTachoCount() - prev_deg_l;
			prev_deg_l = MOTOR_LEFT.getTachoCount();
			
			D_r = ((2 * Math.PI * r * deg_r) / 360);
			D_l = ((2 * Math.PI * r * deg_l) / 360);
			D_c = (D_r + D_l) / 2;
			
			x = x + (D_c * Math.cos(theta));
			y = y + (D_c * Math.sin(theta));
			theta = (theta + ((D_r - D_l) / L));
		}
		
		RConsole.close();
	}

	public static boolean checkIfPointBelongsCircumference(double x_d, double y_d, double x, double y) {
		float distance = (float) Math.sqrt(Math.pow((x - x_d), 2) + Math.pow((y - y_d), 2));
		if (distance > R && distance <= R_M)
			return true;
		return false;
	}
}
