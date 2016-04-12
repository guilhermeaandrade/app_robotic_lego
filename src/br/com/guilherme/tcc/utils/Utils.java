package br.com.guilherme.tcc.utils;

public class Utils {

	// metodo responsavel por realizar verificação se ponto encontra-se entre dois raios
	public static boolean checkIfPointBelongsCircumference(double x_d,
			double y_d, double x, double y) {
		float distance = (float) Math.sqrt(Math.pow((x_d - x), 2)
				+ Math.pow((y_d - y), 2));
		if (distance < Constants.R_M)
			return true;
		return false;
	}

	// metodo responsavel por arredondar os valores
	public static double round(Double value) {
		long factor = (long) Math.pow(10, Constants.NUMBER_DECIMAL);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}
}
