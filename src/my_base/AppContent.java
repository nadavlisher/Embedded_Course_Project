package my_base;

import team.control.Backend;
import team.model.Canvas;

/*
 * This class should hold the content of the system, i.e., all elements that are
 * related to the essence of the system.
 * 
 */
public class AppContent {
	private final Canvas canvas;
	private final Backend backend;

	public AppContent() {
		this.canvas = new Canvas();
		this.backend = new Backend(canvas);
	}

	public void initContent() {
		canvas.initCanvas();
	}

	public Canvas canvas() {
		return canvas;
	}

	public Backend ex3Backend() {
		return backend;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public Backend getBackend() {
		return backend;
	}
}