package my_base;

import base.PeriodicLoop;

public class MyPeriodicLoop extends PeriodicLoop {

	private AppContent content = App.content();
	
	@Override
	public void execute() {
		// Let the super class do its work first
		super.execute();
		
		// Then do your own work here ...
		content.ex3Backend().moveCircle2(10, 0);
		
	}

}
