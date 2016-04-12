package br.com.guilherme.tcc.utils;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class Constants {

	// DEFINIÇÃO DE VARIÁVEIS UTILIZADAS PARA AÇÕES DE CONTROLE
	public static final long MS_20 = 100;
	public static final NXTRegulatedMotor MOTOR_RIGTH = Motor.B;
	public static final NXTRegulatedMotor MOTOR_LEFT = Motor.C;
	public static final Integer SPEED = 30;
	public static final Double DEG_TO_RAD = (Math.PI / 180);
	public static final Double RAD_TO_DEG = (180 / Math.PI);
	public static final Double L = 0.1218; // tamanho do eixo das rodas do robô
	public static final Double r = 0.0215; // raio da roda
	public static final Double R = 0.20;
	public static final Double R_M = 0.40;
	public static final Double CONST_EQ = 0.0878;
	public static final Double CONST_FREQ = 0.3;
	public static final String FIM = "fim";
	public static final int NUMBER_DECIMAL = 7;
	public static final Integer MUL_CONST = 4;
	public static final Integer OPT_MANUAL = 1;
	public static final Integer OPT_AUTOMATIC = 0;

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
	public static final char C_SETTINGS = 's';
	public static final char C_STOP_CONNECTION = 't';
}
