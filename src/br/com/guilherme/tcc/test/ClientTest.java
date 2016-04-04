package br.com.guilherme.tcc.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;
import br.com.guilherme.tcc.utils.Constants;
import br.com.guilherme.tcc.utils.Semaphore;

public class ClientTest {
	// DEFINI��O DE VARI�VEIS PARA APLICA��O
	public static Semaphore semaphore;
	private static boolean flag = true;
	
	private static Double valueController;
	private static Double coordXInitial;
	private static Double coordYInitial;
	private static Double coordXFinal;
	private static Double coordYFinal;

	// metodo principal
	public static void main(String[] args) {
		char command = 0;
		semaphore = new Semaphore(1);

		LCD.drawString("Esperando", 0, 0);
		NXTConnection conexao = Bluetooth.waitForConnection();
		// configurando a conx�o para se comunicar com dispositivos m�veis, como
		// um celular Android
		conexao.setIOMode(NXTConnection.RAW);
		// fluxo de entrada de dados
		DataInputStream dataIn = conexao.openDataInputStream();
		DataOutputStream dataOut = conexao.openDataOutputStream();

		LCD.clear(); // limpando e tela
		LCD.drawString("Conectado", 0, 0);

		while (!Button.ESCAPE.isDown()) {
			try {
				command = dataIn.readChar();
				if (command == Constants.MANUAL_CONTROL) {
					flag = false;
					semaphore.p();
					executeMoveManual(dataIn);
					semaphore.v();
				}
				if (command == Constants.AUTO_CONTROL) {
					flag = true;
					dataIn.readChar();
					dataIn.readDouble();
					new ControlThread(dataOut).start();
				}
				if (command == Constants.C_SETTINGS) {
					char identify = dataIn.readChar();
					switch(identify){
						case 'k':
							valueController = dataIn.readDouble();
							break;
						case 'i':				
							coordXInitial = dataIn.readDouble();
							coordYInitial = dataIn.readDouble();
							break;
						case 'f':
							coordXFinal = dataIn.readDouble();
							coordYFinal = dataIn.readDouble();
							break;
					}
				}
				if (command == Constants.C_STOP_CONNECTION) {
					LCD.drawString("comm: "+command, 0, 5);
					dataIn.readChar();
					dataIn.readDouble();
				}
			} catch (IOException e) {
				LCD.clear();
				LCD.drawString(e.getMessage().toString(), 0, 0);
			}
		}
	}

	// metodo responsavel por realizar o movimento no Robo
	public static void performMove(char cmd, Double speed) {
		int velocidade = 9*speed.intValue(); // define velocidade
		Constants.MOTOR_RIGTH.setSpeed(velocidade);
		Constants.MOTOR_LEFT.setSpeed(velocidade);
		switch (cmd) {
			case Constants.FWD:
				Constants.MOTOR_RIGTH.forward();
				Constants.MOTOR_LEFT.forward();
				break;
			case Constants.BWD:
				Constants.MOTOR_RIGTH.backward();
				Constants.MOTOR_LEFT.backward();
				break;
			case Constants.LEFT:
				Constants.MOTOR_RIGTH.forward();
				Constants.MOTOR_LEFT.backward();
				break;
			case Constants.RIGHT:
				Constants.MOTOR_RIGTH.backward();
				Constants.MOTOR_LEFT.forward();
				break;
			case Constants.STOP:
				Constants.MOTOR_RIGTH.stop();
				Constants.MOTOR_LEFT.stop();
				break;
		}
	}

	// metodo responsavel por realizar movimento manual
	public static void executeMoveManual(DataInputStream in) {
		char letra = 0;
		Double speed = 0d;
		try {
			letra = in.readChar();
			speed = in.readDouble();
		} catch (IOException e) {
			e.getMessage();
		}
		performMove(letra, speed);
	}

	// metodo reponsavel por realizar o controle sobre o robo
	public static void doControl(DataOutputStream dataOut) {
		String position = null;
		byte[] pos = null;
		Long time = new Long(0);
		long prev_deg_r = 0;
		long prev_deg_l = 0;
		long t0 = System.currentTimeMillis();

		Double x = 0.0, y = 0.0, theta = Math.PI;// 0.0;
		Double x_a = 0.9, y_a = 0.9;

		Double e_x, e_y, e_theta, theta_d;
		Double x_d = 0.0, y_d = 0.0;

		Double D_l, D_r, D_c;
		float v, w, w_r, w_l;
		float k_theta = 1.35f;

		FileOutputStream out = null;
		File data = null;
		try {
			data = new File("data.txt");
			if (data.exists()) {
				data.delete();
				data.createNewFile();
			}
			out = new FileOutputStream(data);

			while (System.currentTimeMillis() - t0 <= 37700 && flag) {
				semaphore.p();
				
				time = System.currentTimeMillis() - t0;

				if (checkIfPointBelongsCircumference(x_a, y_a, x, y)) {
					x_d = Constants.R * (Math.cos((Double.valueOf(0.5) * time
									.doubleValue()) / 1000)) + x_a;
					y_d = Constants.R * (Math.sin((Double.valueOf(0.5) * time
									.doubleValue()) / 1000)) + y_a;
				} else {
					x_d = x_a;
					y_d = y_a;
				}

				e_x = x_d - x;
				e_y = y_d - y;

				theta_d = (Math.atan2(e_y, e_x)); // radianos
				e_theta = (theta_d - theta);
				e_theta = (Math.atan2(Math.sin(e_theta), Math.cos(e_theta)));

				double value = ((Math.exp(Math.sqrt(Math.pow(e_x, 2)
						+ Math.pow(e_y, 2)))) - Math.exp(-(Math.sqrt(Math.pow(
						e_x, 2) + Math.pow(e_y, 2)))))
						/ ((Math.exp(Math.sqrt(Math.pow(e_x, 2)
								+ Math.pow(e_y, 2)))) + Math.exp(-(Math
								.sqrt(Math.pow(e_x, 2) + Math.pow(e_y, 2)))));

				v = (float) (0.1 * value + Constants.CONST_EQ);

				w = (float) (k_theta * e_theta);

				w_r = (float) ((2 * v + w * Constants.L) / (2 * Constants.r)); // rad/s
				w_r = (float) (w_r * Constants.RAD_TO_DEG);

				w_l = (float) ((2 * v - w * Constants.L) / (2 * Constants.r)); // rad/s
				w_l = (float) (w_l * Constants.RAD_TO_DEG);

				Constants.MOTOR_RIGTH.setSpeed((float) w_r);
				Constants.MOTOR_RIGTH.forward();

				Constants.MOTOR_LEFT.setSpeed((float) w_l);
				Constants.MOTOR_LEFT.forward();

				long deg_r = Constants.MOTOR_RIGTH.getTachoCount() - prev_deg_r;
				prev_deg_r = Constants.MOTOR_RIGTH.getTachoCount();

				long deg_l = Constants.MOTOR_LEFT.getTachoCount() - prev_deg_l;
				prev_deg_l = Constants.MOTOR_LEFT.getTachoCount();

				D_r = ((2 * Math.PI * Constants.r * deg_r) / 360);
				D_l = ((2 * Math.PI * Constants.r * deg_l) / 360);
				D_c = (D_r + D_l) / 2;

				position = round(x)
						+ ","
						+ round(y)
						+ ","
						+ round(theta)
						+ ","
						+ round(v)
						+ ","
						+ round(w)
						+ ","
						+ round((Math.sqrt(Math.pow(e_x, 2) + Math.pow(e_y, 2))))
						+ "," + (time / 1000.0000);
				pos = position.getBytes();

				out.write(pos);
				out.write("\n".getBytes());
				out.flush();

				// dataOut.write(pos);
				// dataOut.flush();

				x = x + (D_c * Math.cos(theta));
				y = y + (D_c * Math.sin(theta));
				theta = (theta + ((D_r - D_l) / Constants.L));

				semaphore.v();
			}

			if (flag) {
				pos = Constants.FIM.getBytes();
				// dataOut.write(pos);
				// dataOut.flush();
				Constants.MOTOR_RIGTH.stop();
				Constants.MOTOR_LEFT.stop();
			}
		} catch (IOException e) {
			System.err.println("Failed to create output stream");
			System.exit(1);
		} 
		try {
			out.close();
		} catch (IOException e) {
			System.err.println("Failed to close output stream");
			System.exit(1);
		}
	}

	// metodo responsavel por realizar verifica��o se ponto encontra-se entre dois raios
	public static boolean checkIfPointBelongsCircumference(double x_d,
			double y_d, double x, double y) {
		float distance = (float) Math.sqrt(Math.pow((x_d - x), 2)
				+ Math.pow((y_d - y), 2));
		if (distance < Constants.R_M)
			return true;
		return false;
	}

	// metodo responsavel por arredondar os valores
	public static double round(double value) {
		long factor = (long) Math.pow(10, Constants.NUMBER_DECIMAL);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	// thread responsavel pelo controle automatico
	public static class ControlThread extends Thread {

		private DataOutputStream dataOut;
		
		public ControlThread(DataOutputStream data) {
			this.dataOut = data;
		}

		public void run() {
			doControl(dataOut);
		}
	}
}