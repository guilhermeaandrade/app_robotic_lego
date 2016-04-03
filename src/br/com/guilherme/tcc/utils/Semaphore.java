package br.com.guilherme.tcc.utils;

public class Semaphore {

	private int value;
	
	public Semaphore(int init){
		if(init < 0) init = 0;
		this.value = init;
	}
	
	//equivale ao metodo acquire
	public synchronized void p(){
		while(value == 0){
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage().toString());
			}
		}
		value--;
	}
	
	//equivale ao metodo release
	public synchronized void v(){
		value++;
		notify();
	}
}