package my_base;

import team.domain.LevelDevilBackend;

/*
 * This class should hold the content of the system, i.e., all elements that are
 * related to the essence of the system.
 * 
 */
public class AppContent {

    private LevelDevilBackend levelDevilBackend;

    public void initContent() {
        levelDevilBackend = new LevelDevilBackend();
    }

    public LevelDevilBackend levelDevilBackend() {
        return levelDevilBackend;
    }
}
