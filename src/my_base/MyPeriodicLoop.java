package my_base;

import javax.swing.SwingUtilities;

import base.Params;
import base.PeriodicLoop;


public class MyPeriodicLoop extends PeriodicLoop {

    @Override
    public void execute() {
        super.execute();
        SwingUtilities.invokeLater(() -> {
            try {
                App.router().route("/leveldevil/game/tick", Params.of());
            } catch (Throwable t) {
                System.err.println("[periodic] tick failed: " + t);
                t.printStackTrace();
            }
        });
    }
}

