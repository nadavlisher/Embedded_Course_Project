package my_base;

import team.control.LevelDevilBackend;
import team.model.Canvas;

/*
 * This class should hold the content of the system, i.e., all elements that are
 * related to the essence of the system.
 * 
 */
public class AppContent {
	private final Canvas canvas;
	private final LevelDevilBackend backend;

	public AppContent() {
		this.canvas = new Canvas();
		this.backend = new LevelDevilBackend(canvas);
	}

	public void initContent() {
		canvas.initCanvas();
	}

	public Canvas canvas() {
		return canvas;
	}

	public LevelDevilBackend levelDevilBackend() {
		return backend;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public LevelDevilBackend getBackend() {
		return backend;
	}
}
