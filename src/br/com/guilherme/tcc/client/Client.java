package br.com.guilherme.tcc.client;

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
import br.com.guilherme.tcc.utils.Utils;

public class Client {
	// DEFINIÇÃO DE VARIÁVEIS PARA APLICAÇÃO
	public static Semaphore semaphore;
	private static boolean flag = true;

	private static Double k_p = 1.35;
	private static Double k_i = 0.35;
	private static Double x = 0d;
	private static Double y = 0d;
	private static Double x_a = 0d;
	private static Double y_a = 0d;
	private static long prev_deg_r_manual = 0;
	private static long prev_deg_l_manual = 0;
	private static Double theta =  0d;
	private static Time time;
	private static NXTConnection conexao;

	public static File manualFile = null;
	public static FileOutputStream fousManualFile = null;
	public static File hybridFile = null;
	public static FileOutputStream fousHybridFile = null;
	public static File config = null;
	public static FileOutputStream fousConfigFile = null;

	private static long manualTime = 0;
	private static long prevManualTime = 0;

	// metodo principal
	public static void main(String[] args) {
		char command = 0;
		semaphore = new Semaphore(1);
		byte[] connected = null;

		time = new Time();

		conexao = null;
		DataInputStream dataIn = null;
		DataOutputStream dataOut = null;
		
		while (true) {
			LCD.clear();
			LCD.drawString("Esperando", 0, 0);
			conexao = Bluetooth.waitForConnection();

			// configurando a conxão para se comunicar com dispositivos móveis
			conexao.setIOMode(NXTConnection.RAW);

			// fluxo de entrada de dados
			dataIn = conexao.openDataInputStream();
			dataOut = conexao.openDataOutputStream();

			try {
				connected = Constants.CONNECTED.getBytes();
				dataOut.write(connected);
				dataOut.flush();

			} catch (IOException err) {
				LCD.clear();
				LCD.drawString("Error: " + err.getCause().toString(), 0, 0);
			}

			LCD.clear(); // limpando e tela
			LCD.drawString("Conectado", 0, 0);

			while (!Button.ESCAPE.isDown()) {
				try {
					try {
						command = dataIn.readChar();
					} catch (IOException e) {
						flag = false;
						conexao = null;

						// reinicializando as variáveis
						prev_deg_r_manual = 0;
						prev_deg_l_manual = 0;
						break;
					}

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
							k_p = dataIn.readDouble();
							k_i = dataIn.readDouble();
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
						break;
					}
				} catch (IOException e) {
					LCD.clear();
					LCD.drawString("Error: " + e.getCause().toString(), 0, 0);
				}
			}
		}
	}

	// metodo responsavel por realizar o movimento no Robo
	public static void performMove(char cmd, Double speed, DataOutputStream out) {
		Integer velocidade = 0;
		if (speed < 20) {
			velocidade = 150;
		} else {
			velocidade = Constants.CONSTANT_VELOCITY * speed.intValue();
		}

		Constants.MOTOR_RIGTH.setSpeed(velocidade);
		Constants.MOTOR_LEFT.setSpeed(velocidade);

		switch (cmd) {
		case Constants.FWD:
			Constants.MOTOR_RIGTH.forward();
			Constants.MOTOR_LEFT.forward();
			prevManualTime = System.currentTimeMillis();
			break;

		case Constants.BWD:
			Constants.MOTOR_RIGTH.backward();
			Constants.MOTOR_LEFT.backward();
			prevManualTime = System.currentTimeMillis();
			break;

		case Constants.LEFT:
			Constants.MOTOR_RIGTH.forward();
			Constants.MOTOR_LEFT.backward();
			prevManualTime = System.currentTimeMillis();
			break;

		case Constants.RIGHT:
			Constants.MOTOR_RIGTH.backward();
			Constants.MOTOR_LEFT.forward();
			prevManualTime = System.currentTimeMillis();
			break;

		case Constants.STOP:
			Constants.MOTOR_RIGTH.stop();
			Constants.MOTOR_LEFT.stop();
			manualTime = System.currentTimeMillis() - prevManualTime;
			trackManualControl(velocidade, out);
			prevManualTime = 0;
			break;
		}
	}

	// metodo responsavel por realizar rastreio manual
	private static void trackManualControl(Integer velocidade,
			DataOutputStream out) {
		String information = null;
		byte[] info = null;

		Double e_x = 0d, e_y = 0d;
		Double D_l = 0d;
		Double D_r = 0d;
		Double D_c = 0d;

		try {
			e_x = x_a - x;
			e_y = y_a - y;

			long deg_r = Constants.MOTOR_RIGTH.getTachoCount()
					- prev_deg_r_manual;
			prev_deg_r_manual = Constants.MOTOR_RIGTH.getTachoCount();

			long deg_l = Constants.MOTOR_LEFT.getTachoCount()
					- prev_deg_l_manual;
			prev_deg_l_manual = Constants.MOTOR_LEFT.getTachoCount();

			D_r = ((2 * Math.PI * Constants.r * deg_r) / 360);
			D_l = ((2 * Math.PI * Constants.r * deg_l) / 360);
			D_c = (D_r + D_l) / 2;

			// envia informações para o dispositivo
			information = Utils.round(x)
					+ ","
					+ Utils.round(y)
					+ ","
					+ Utils.round(theta)
					+ ","
					+ velocidade.doubleValue()
					* Constants.DEG_TO_RAD
					+ ","
					+ (((D_r / (manualTime / 1000.0000)) - (D_l / (manualTime / 1000.0000))) / Constants.L)
					+ ","
					+ Utils.round((Math.sqrt(Math.pow(e_x, 2)
							+ Math.pow(e_y, 2)))) + "," + time.getTimeNow()
					+ "," + Constants.OPT_MANUAL;

			info = information.getBytes();
			out.write(info);
			out.flush();
			
			x = x + (D_c * Math.cos(theta));
			y = y + (D_c * Math.sin(theta));
			theta = (theta + ((D_r - D_l) / Constants.L));

			// envia informações para o dispositivo
			information = Utils.round(x)
					+ ","
					+ Utils.round(y)
					+ ","
					+ Utils.round(theta)
					+ ","
					+ velocidade.doubleValue()
					* Constants.DEG_TO_RAD
					+ ","
					+ (((D_r / (manualTime / 1000.0000)) - (D_l / (manualTime / 1000.0000))) / Constants.L)
					+ ","
					+ Utils.round((Math.sqrt(Math.pow(e_x, 2)
							+ Math.pow(e_y, 2)))) + "," + time.getTimeNow()
					+ "," + Constants.OPT_MANUAL;

			info = information.getBytes();
			out.write(info);
			out.flush();

		} catch (IOException e) {
			LCD.clear();
			LCD.drawString("Falha trackManualControl", 0, 0);
		}
	}

	// metodo responsavel por realizar movimento manual
	public static void executeMoveManual(DataInputStream in,
			DataOutputStream out) {
		char letra = 0;
		Double speed = 0d;
		try {
			letra = in.readChar();
			speed = in.readDouble();
			performMove(letra, speed, out);
		} catch (IOException e) {
			LCD.clear();
			LCD.drawString("Falha executeMoveManual", 0, 0);
		}
	}

	// metodo reponsavel por realizar o controle sobre o robo
	public static void doControl(DataOutputStream dataOut) {
		byte[] pos = null;
		String information = null;
		byte[] info = null;
		Long timeControl = new Long(0);
		Long timeAction = new Long(0);
		long prev_deg_r = 0;
		long prev_deg_l = 0;
		long t0 = System.currentTimeMillis();
		Long prevTimeControl = t0;

		Double e_x = 0d, e_y = 0d, e_theta = 0d, theta_d;
		Double x_d = x_a, y_d = y_a;

		Double D_l, D_r, D_c;
		Float v = 0f, w_r = 0f, w_l = 0f;
		Double C_i = 0d, C_p = 0d, w = 0d;

		try {
			while (System.currentTimeMillis() - t0 <= 45500 && flag) {
				semaphore.p();

				timeControl = System.currentTimeMillis() - t0;

				if (Utils.checkIfPointBelongsCircumference(x_a, y_a, x, y)) {
					x_d = Constants.R
							* (Math.cos((Double.valueOf(0.3) * timeControl
									.doubleValue()) / 1000)) + x_a;
					y_d = Constants.R
							* (Math.sin((Double.valueOf(0.3) * timeControl
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
				
				v = (float) ((Double.valueOf(0.93)*Double.valueOf(0.3)*Double.valueOf(0.2)) + (Double.valueOf(0.1))*value);

				// Wk = Ci(k) + Cp(k)
				// Ci(k) = Ci(k-1) + k_i*T.e
				C_p =  k_p * e_theta;
				timeAction = System.currentTimeMillis() - prevTimeControl;
				C_i = C_i + k_i*(timeAction.doubleValue()/1000)*e_theta;
				prevTimeControl = System.currentTimeMillis();
				
				w = C_i + C_p;

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
				
				information = Utils.round(x)
						+ ","
						+ Utils.round(y)
						+ ","
						+ Utils.round(theta)
						+ ","
						+ Utils.round(v.doubleValue())
						+ ","
						+ Utils.round(w.doubleValue())
						+ ","
						+ Utils.round((Math.sqrt(Math.pow(e_x, 2)
								+ Math.pow(e_y, 2)))) + "," + time.getTimeNow()
						+ "," + Constants.OPT_AUTOMATIC;

				info = information.getBytes();
				dataOut.write(info);
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
			LCD.clear();
			LCD.drawString("Falha abrir arquivo", 0, 0);
			Constants.MOTOR_RIGTH.stop();
			Constants.MOTOR_LEFT.stop();
		}
		return;
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