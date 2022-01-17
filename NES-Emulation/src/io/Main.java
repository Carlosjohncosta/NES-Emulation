package io;
import processing.core.*;
import processor.Processor;

public class Main extends PApplet{
	Processor i; 
	public static int length = 8;
	public static int size = 60;
	public static void main(String[] args) {
		PApplet.main("io.Main");
	}
	
	public void settings() {
		size(length * size, length * size);
	}
	
	public void setup() {
		frameRate(1000000);
		i = new Processor(this);
	}
	
	public void draw() {
		i.exec();
		i.display();
	}
}
