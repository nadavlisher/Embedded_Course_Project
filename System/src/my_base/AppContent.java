package my_base;

import team.control.Ex3Backend;
import team.model.Canvas;

/*
 * This class should hold the content of the system, i.e., all elements that are
 * related to the essence of the system.
 * 
 */
public class AppContent {
	private Canvas canvas = new Canvas();
	private Ex3Backend ex3Backend;

	public void initContent() {
		ex3Backend = new Ex3Backend();
		canvas.initCanvas();
	};

	public Canvas canvas() {
		return canvas;
	}	
	public Ex3Backend ex3Backend() {
		return ex3Backend;
	}
}
