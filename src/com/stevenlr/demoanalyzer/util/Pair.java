package com.stevenlr.demoanalyzer.util;

public class Pair<A, B> {
	
	private A a;
	private B b;
	
	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}
	
	public A getFirst() {
		return a;
	}
	
	public B getSecond() {
		return b;
	}
	
	public String toString() {
		return "<" + a + ", " + b + ">";
	}
}
