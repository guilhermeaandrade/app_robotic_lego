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
import br.com.guilherme.tcc.utils.Time;

public class ClientTest {
	// DEFINIÇÃO DE VARIÁVEIS PARA APLICAÇÃO
	public static Semaphore semaphore;
	private static boolean flag = true;

	private static Double k_theta = 1.35;
	private static Double x = 0d;
	private static Double y = 0d;
	private static Double x_a = 0d;
	private static Double y_a = 0d;
	private static long prev_deg_r_manual = 0;
	private static long prev_deg_l_manual = 0;
	private static Double theta = 0d;
	private static Time time;
	
	// metodo principal
	public static void main(String[] args) {
		char command = 0;
		semaphore = new Semaphore(1);
		
		time = new Time();
		
		NXTConnection conexao = null;
		DataInputStream dataIn = null;
		DataOutputStream dataOut = null;

		FileOutputStream fous = null;
		File file = null;

		LCD.drawString("Esperando", 0, 0);
		conexao = Bluetooth.waitForConnection();

		// configurando a conxão para se comunicar com dispositivos móveis, como
		// um celular Android
		conexao.setIOMode(NXTConnection.RAW);

		// fluxo de entrada de dados
		dataIn = conexao.openDataInputStream();
		dataOut = conexao.openDataOutputStream();

		LCD.clear(); // limpando e tela
		LCD.drawString("Conectado", 0, 0);

		String commands = null;
		byte[] pos = null;

		try {
			file = new File("test.txt");
			if (file.exists()) {
				file.delete();
				file.createNewFile();
			}
			fous = new FileOutputStream(file);

			while (!Button.ESCAPE.isDown()) {
				try {
					command = dataIn.readChar();

					commands = "" + command;
					pos = commands.getBytes();
					fous.write(pos);
					fous.flush();
					
					if (command == Constants.MANUAL_CONTROL) {
						time.setTime();
						flag = false;
						semaphore.p();
						executeMoveManual(dataIn, dataOut);
						semaphore.v();
					}
					if (command == Constants.AUTO_CONTROL) {
						time.setTime();
						flag = true;
						dataIn.readChar();
						dataIn.readDouble();
						new ControlThread(dataOut).start();
					}
					if (command == Constants.C_SETTINGS) {
						char identify = dataIn.readChar();
						switch (identify) {
						case 'k':
							k_theta = dataIn.readDouble();
							break;
						case 'i':
							x = dataIn.readDouble();
							y = dataIn.readDouble();
							break;
						case 'f':
							x_a = dataIn.readDouble();
							y_a = dataIn.readDouble();
							break;
						}
					}
					if (command == Constants.C_STOP_CONNECTION) {
						dataIn.readChar();
						dataIn.readDouble();

						if (dataIn != null)
							dataIn.close();
						if (dataOut != null)
							dataOut.close();
						if (conexao != null)
							conexao.close();

						dataIn = null;
						dataOut = null;
						conexao = null;

						break;
					}
				} catch (IOException e) {
					LCD.clear();
					LCD.drawString("Deu erro: " + e.getCause().toString(), 0, 0);
				}
			}
			fous.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// metodo responsavel por realizar o movimento no Robo
	public static void performMove(char cmd, Double speed, DataOutputStream out) {
		Integer velocidade;
		if (speed < 0)
			velocidade = 20;
		velocidade = Constants.MUL_CONST * speed.intValue();

		Constants.MOTOR_RIGTH.setSpeed(velocidade);
		Constants.MOTOR_LEFT.setSpeed(velocidade);

		switch (cmd) {
		case Constants.FWD:
			Constants.MOTOR_RIGTH.forward();
			Constants.MOTOR_LEFT.forward();
			trackManualControl(velocidade, out);
			break;

		case Constants.BWD:
			Constants.MOTOR_RIGTH.backward();
			Constants.MOTOR_LEFT.backward();
			trackManualControl(velocidade, out);
			break;

		case Constants.LEFT:
			Constants.MOTOR_RIGTH.forward();
			Constants.MOTOR_LEFT.backward();
			trackManualControl(velocidade, out);
			break;

		case Constants.RIGHT:
			Constants.MOTOR_RIGTH.backward();
			Constants.MOTOR_LEFT.forward();
			trackManualControl(velocidade, out);
			break;

		case Constants.STOP:
			Constants.MOTOR_RIGTH.stop();
			Constants.MOTOR_LEFT.stop();
			break;
		}
	}

	// metodo responsavel por realizar rastreio manual
	private static void trackManualControl(Integer velocidade, DataOutputStream out) {
		String position = null;
		String information = null;
		byte[] pos = null;
		byte[] info = null;
		FileOutputStream fous = null;
		File file = null;

		long deg_r = 0;
		long deg_l = 0;

		Double e_x, e_y;
		Double D_l;
		Double D_r;
		Double D_c;

		try {
			file = new File("log.txt");
			if (file.exists()) {
				file.delete();
				file.createNewFile();
			}
			fous = new FileOutputStream(file);

			//armazena informações no arquivo
			position = round(x) + "," + round(y) + "," + round(theta)
					+ "," + deg_r + "," + deg_l;
			pos = position.getBytes();
			fous.write(pos);
			fous.write("#".getBytes());
			fous.flush();

			e_x = x_a - x;
			e_y = y_a - y;
			
			//envia informações para o dispositivo
			information = round(x) 
					+ "," 
					+ round(y) 
					+ "," 
					+ round(theta)
					+ "," 
					+ velocidade.doubleValue() 
					+ ","
					+ 0d
					+ ","
					+ round((Math.sqrt(Math.pow(e_x, 2) + Math.pow(e_y, 2))))
					+ "," + time.getTimeNow();;
			
			info = information.getBytes();
			out.write(info);
			out.flush();
			
			deg_r = Constants.MOTOR_RIGTH.getTachoCount() - prev_deg_r_manual;
			prev_deg_r_manual = Constants.MOTOR_RIGTH.getTachoCount();

			deg_l = Constants.MOTOR_LEFT.getTachoCount() - prev_deg_l_manual;
			prev_deg_l_manual = Constants.MOTOR_LEFT.getTachoCount();

			D_r = ((2 * Math.PI * Constants.r * deg_r) / 360);
			D_l = ((2 * Math.PI * Constants.r * deg_l) / 360);
			D_c = (D_r + D_l) / 2;

			position = round(x) + "," + round(y) + "," + round(theta)
					+ "," + deg_r + "," + deg_l;
			pos = position.getBytes();
			fous.write(pos);
			fous.write("#".getBytes());
			fous.flush();
			
			x = x + (D_c * Math.cos(theta));
			y = y + (D_c * Math.sin(theta));
			theta = (theta + ((D_r - D_l) / Constants.L));

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			fous.close();
		} catch (Exception e) {
			e.getMessage();
		}
	}

	// metodo responsavel por realizar movimento manual
	public static void executeMoveManual(DataInputStream in, DataOutputStream out) {
		char letra = 0;
		Double speed = 0d;
		try {
			letra = in.readChar();
			speed = in.readDouble();
		} catch (IOException e) {
			e.getMessage();
		}
		performMove(letra, speed, out);
	}

	// metodo reponsavel por realizar o controle sobre o robo
	public static void doControl(DataOutputStream dataOut) {
		String position = null;
		byte[] pos = null;
		Long timeControl = new Long(0);
		long prev_deg_r = 0;
		long prev_deg_l = 0;
		long t0 = System.currentTimeMillis();

		//Double theta = Math.PI;
		Double e_x, e_y, e_theta, theta_d;
		Double x_d = 0.0, y_d = 0.0;

		Double D_l, D_r, D_c;
		float v, w, w_r, w_l;

		FileOutputStream out = null;
		File data = null;
		try {
			data = new File("data.txt");
			if (data.exists()) {
				data.delete();
				data.createNewFile();
			}
			out = new FileOutputStream(data);

			while (System.currentTimeMillis() - t0 <= 35000 && flag) {
				semaphore.p();

				timeControl = System.currentTimeMillis() - t0;

				if (checkIfPointBelongsCircumference(x_a, y_a, x, y)) {
					x_d = Constants.R
							* (Math.cos((Double.valueOf(0.5) * timeControl
									.doubleValue()) / 1000)) + x_a;
					y_d = Constants.R
							* (Math.sin((Double.valueOf(0.5) * timeControl
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
						//+ "," + (timeControl / 1000.0000);
						+ "," + time.getTimeNow();
				pos = position.getBytes();

				out.write(pos);
				out.write("\n".getBytes());
				out.flush();

				dataOut.write(pos);
				dataOut.flush();

				x = x + (D_c * Math.cos(theta));
				y = y + (D_c * Math.sin(theta));
				theta = (theta + ((D_r - D_l) / Constants.L));

				semaphore.v();
			}

			if (flag) {
				pos = Constants.FIM.getBytes();
				dataOut.write(pos);
				dataOut.flush();
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

	// metodo responsavel por realizar verificação se ponto encontra-se entre
	// dois raios
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