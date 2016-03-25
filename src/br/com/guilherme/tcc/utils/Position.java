package br.com.guilherme.tcc.utils;

import java.io.Serializable;

public class Position implements Serializable{

	private static final long serialVersionUID = 3478985022899129257L;
	
	private Double x;
	private Double y;
	
	public Position(Double x, Double y){
		this.x = x;
		this.y = y;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}
}
