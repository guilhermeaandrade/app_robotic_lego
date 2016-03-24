package br.com.guilherme.tcc.client;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class Client {
	// DEFINIÇÃO DE VARIÁVEIS PARA APLICAÇÃO
	// private static int tConnection; //1-manual control, 2- automatic control
	// private static DataInputStream entradaDeDados;
	// private static DataOutputStream saidaDeDados;

	// DEFINIÇÃO DE VARIÁVEIS UTILIZADAS PARA AÇÕES DE CONTROLE
	public static final long MS_20 = 100;
	public static final NXTRegulatedMotor MOTOR_RIGTH = Motor.B;
	public static final NXTRegulatedMotor MOTOR_LEFT = Motor.C;
	public static final Integer SPEED = 30;
	public static final Double DEG_TO_RAD = (Math.PI / 180);
	public static final Double RAD_TO_DEG = (180 / Math.PI);
	public static final Double L = 0.122; // tamanho do eixo das rodas do robô
	public static final Double r = 0.0215; // raio da roda
	public static final Float R = 0.15f;

	// DEFINIÇÃO DA FUNÇÃO REDUZIDA DA CIRCUNFERENCIA
	// (x - a)^2 + (y - b)^2 = r^2; -> equação reduzida
	// x^2 + y^2 - 2ax - 2by + a^2 + b^2 - r^2 = 0 -> equação geral
	// sqrt((x - x0)^2 + (y - y0)^2)-> distancia entre dois pontos

	// DEFINIÇÃO DE VARIÁVEIS UTILIZADAS PARA CONTROLE MANUAL
	public static final char LEFT = 'a'; // esquerda
	public static final char RIGHT = 'd'; // direita
	public static final char FWD = 'w'; // para frente
	public static final char BWD = 's'; // para tras
	public static final char STOP = 'q'; // parar movimento
	public static final char CONNECT = 'c';
	public static final char MANUAL_CONTROL = 'm';
	public static final char AUTO_CONTROL = 'u';

	// DEFINIÇÃO DE VARIÁVEIS UTILIZADAS PARA ESCREVER EM UM ARQUIVO
	// private static ManagerCSVFiles manager;

	// metodo principal
	public static void main(String[] args) {
		doControl();

		// LCD.drawString("Esperando", 0, 0); // escrevendo na tela
		// // Classe de conexão que proporciona acesso ao padrão E/S e esperando
		// por uma conexão
		// NXTConnection conexao = Bluetooth.waitForConnection();
		// // configurando a conxão para se comunicar com dispositivos móveis,
		// como um celular Android
		// conexao.setIOMode(NXTConnection.RAW);
		// // fluxo de entrada de dados
		// DataInputStream in = conexao.openDataInputStream();
		//
		// LCD.clear(); // limpando e tela
		// LCD.drawString("Conectado", 0, 0);
		//
		// char i = 0;
		// byte j = 0;
		//
		// // o loop será finalizado quando o botão escape do nxt for
		// pressionado
		// RConsole.printlnln("Start while loop ");
		// while (!Button.ESCAPE.isDown()) {
		//
		// try {
		//
		// i = in.readChar(); // leitura do caracter representando a direção
		// j = in.readByte(); // leitura do byte representando a velocidade
		//
		// LCD.clear();
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printlnStackTrace();
		// }
		//
		// // imprimindo o valor recebido na tela, apenas usado para teste
		// iniciais
		// //System.out.println(i + ", " + j);
		// RConsole.println(i + ", " + j);
		//
		// // chamada do método que execultara a movimentação do robô
		// performMove(i, j);
		// }
		// RConsole.printlnln("\n done ");
		// RConsole.close();

		/*
		 * char typeConnection = 0 ; // m - manual control, u - automatic
		 * control char direction = 0; char letra = 0; Byte speed = 0;
		 * LCD.clear(); LCD.drawString("Esperando..", 0, 0); LCD.refresh();
		 * 
		 * // Espera por uma conexão do dispositivo móvel NXTConnection
		 * connectionWithDevice = Bluetooth.waitForConnection(); // seta o modo
		 * de comunicação -> especificamente comunincação com // dispositivos
		 * móveis connectionWithDevice.setIOMode(NXTConnection.RAW);
		 * 
		 * entradaDeDados = connectionWithDevice.openDataInputStream(); // dados
		 * que chegam // saidaDeDados =
		 * connectionWithDevice.openDataOutputStream(); //dados que saem
		 * if(connectionWithDevice != null){ LCD.clear();
		 * LCD.drawString("Conectado", 0, 0); }
		 * 
		 * while(!Button.ESCAPE.isDown()){ try { letra =
		 * entradaDeDados.readChar(); speed = entradaDeDados.readByte();
		 * 
		 * //difine typeConnection or direction if(letra == 'm' || letra ==
		 * 'u'){ typeConnection = letra; }else{ direction = letra; }
		 * 
		 * LCD.clear(); LCD.drawString(String.valueOf(letra), 0, 0);
		 * LCD.drawString(speed.toString(), 0, 1); } catch (IOException e) {
		 * LCD.drawString(e.getMessage(), 0, 0); } if(typeConnection == 'u')
		 * doControl(); performMove(direction, speed);
		 * 
		 * }
		 */

		/*
		 * LCD.clear(); LCD.drawString("Esperando", 0, 0); // escrevendo na tela
		 * LCD.refresh();
		 * 
		 * char direcao = 0; byte velocidade = 0;
		 * 
		 * BTConnection conexaoWithLego = Bluetooth.waitForConnection();
		 * DataInputStream entradaDeDados = null; DataOutputStream saidaDeDados
		 * = null; while(!Button.ESCAPE.isDown()){ try { if(conexaoWithLego ==
		 * null){ throw new IOException("Falha de Conexao"); } LCD.clear();
		 * LCD.drawString("Conectado", 0, 0);
		 * 
		 * entradaDeDados = conexaoWithLego.openDataInputStream(); saidaDeDados
		 * = conexaoWithLego.openDataOutputStream();
		 * 
		 * direcao = entradaDeDados.readChar(); velocidade =
		 * entradaDeDados.readByte();
		 * 
		 * } catch (Exception e) { LCD.clear(); LCD.drawString(
		 * "Falha de Comunicacao", 0, 0); LCD.drawString(e.getMessage(), 2, 0);
		 * LCD.refresh(); } performMove(direcao, velocidade); }
		 */

		/*
		 * char typeConnection; //m - manual control, u - automatic control char
		 * direcao = 0; byte velocidade = 0;
		 * 
		 * LCD.clear(); LCD.drawString("Esperando", 0, 0); // escrevendo na tela
		 * LCD.refresh();
		 * 
		 * BTConnection conexaoWithLego = Bluetooth.waitForConnection();
		 * DataInputStream entradaDeDados = null;
		 * 
		 * try { if(conexaoWithLego == null) throw new IOException(
		 * "Falha de Conexão"); LCD.clear(); LCD.drawString("Connected", 0, 0);
		 * } catch (Exception e) { LCD.clear(); LCD.drawString(
		 * "Falha de Comunicacao", 0, 0); LCD.drawString(e.getMessage(), 2, 0);
		 * LCD.refresh(); }
		 * 
		 * /*while(true){ try { if(conexaoWithLego == null) throw new
		 * IOException("Falha de Conexão"); LCD.clear();
		 * LCD.drawString("Connected", 0, 0);
		 * 
		 * entradaDeDados = conexaoWithLego.openDataInputStream();
		 * typeConnection = entradaDeDados.readChar(); if(typeConnection ==
		 * 'u'){ tConnection = 2; doControl(); }else if(typeConnection == 'm'){
		 * tConnection = 1; direcao = entradaDeDados.readChar(); velocidade =
		 * entradaDeDados.readByte();
		 * 
		 * performMove(direcao, velocidade); } } catch (Exception e) {
		 * LCD.clear(); LCD.drawString("Falha de Comunicacao", 0, 0);
		 * LCD.drawString(e.getMessage(), 2, 0); LCD.refresh(); } }
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

	
	// metedo reponsavel por realizar o controle sobre o robo
	public static void doControl() {
		Long time = new Long(0);
		long prev_deg_r = 0;
		long prev_deg_l = 0;
		long t0 = System.currentTimeMillis();
		
		float x = 0.0f, y = 0.0f, theta = 0.0f;//(float) Math.PI;//// (float) Math.PI;//
		float x_a = 0.35f, y_a = 0.35f;
		
		float e_x, e_y, e_theta, theta_d;
		float x_d = 0.0f, y_d = 0.0f;
		
		float D_l, D_r, D_c;
		float v, w, w_r, w_l;
		float k_theta = 1;

		while (System.currentTimeMillis() - t0 <= 37700) {

			time = System.currentTimeMillis() - t0;
			
			x_d = (float) (0.15 * (Math.cos(((1/2) * time)/1000)) + x_a); 
			y_d = (float) (0.15 * (Math.sin(((1/2) * time)/1000)) + y_a);
			//x_d = 0.4f;
			//y_d = 0.85f;
			
			e_x = x_d - x;
			e_y = y_d - y;
			
//			if(Math.sqrt(Math.pow(e_x, 2)+Math.pow(e_y, 2)) <= 0.05){
//				MOTOR_RIGTH.stop();
//				MOTOR_LEFT.stop();
//				break;
//			}
			
			theta_d = (float) Math.atan2(e_y, e_x); // radianos
			e_theta = theta_d - theta;
			e_theta = (float) Math.atan2(Math.sin(e_theta), Math.cos(e_theta));
			
			double value = ((Math.exp(Math.sqrt(Math.pow(e_x, 2) + Math.pow(e_y, 2))))
					- Math.exp(-(Math.sqrt(Math.pow(e_x, 2) + Math.pow(e_y, 2)))))
					/ ((Math.exp(Math.sqrt(Math.pow(e_x, 2) + Math.pow(e_y, 2))))
							+ Math.exp(-(Math.sqrt(Math.pow(e_x, 2) + Math.pow(e_y, 2)))));
			
			v = (float) (0.1 * value + 0.1);
			
			w = k_theta * e_theta;
			
			w_r = (float) ((2 * v + w * L) / (2 * r)); // rad/s
			w_r = (float) (w_r * RAD_TO_DEG);
			
			w_l = (float) ((2 * v - w * L) / (2 * r)); // rad/s
			w_l = (float) (w_l * RAD_TO_DEG);
			
			MOTOR_RIGTH.setSpeed(w_r);
			MOTOR_RIGTH.forward();

			MOTOR_LEFT.setSpeed(w_l);
			MOTOR_LEFT.forward();

			long deg_r = MOTOR_RIGTH.getTachoCount() - prev_deg_r;
			prev_deg_r = MOTOR_RIGTH.getTachoCount();
			
			long deg_l = MOTOR_LEFT.getTachoCount() - prev_deg_l;
			prev_deg_l = MOTOR_LEFT.getTachoCount();
			
			D_r = (float) ((2 * Math.PI * r * deg_r) / 360);
			D_l = (float) ((2 * Math.PI * r * deg_l) / 360);
			D_c = (D_r + D_l) / 2;
			
			x = (float) (x + (D_c * Math.cos(theta)));
			y = (float) (y + (D_c * Math.sin(theta)));
			theta = (float) (theta + ((D_r - D_l) / L));
		}
		//RConsole.close();
	}

	public static boolean checkIfPointBelongsCircumference(float x_d, float y_d, float x, float y) {
		float distance = (float) Math.sqrt(Math.pow((x - x_d), 2) + Math.pow((y - y_d), 2));
		if (distance >= R)
			return true;
		return false;
	}
}