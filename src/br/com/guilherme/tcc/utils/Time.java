package br.com.guilherme.tcc.utils;

public class Time {
	
	private long startTime;
	private boolean timeInitialized;
	
	public Time(){
		timeInitialized = false;
	}
	
	public void setTime(){
		if(!timeInitialized) {
			startTime = System.currentTimeMillis();
			timeInitialized = true;
		}
	}
	
	public Double getTimeNow(){
		return ((System.currentTimeMillis() - startTime) / 1000.0000);
	}
	
	public void resetTime(){
		this.timeInitialized = false;
	}
}
