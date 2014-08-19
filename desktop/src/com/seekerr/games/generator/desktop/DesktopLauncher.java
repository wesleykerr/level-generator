package com.seekerr.games.generator.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.seekerr.games.generator.ProceduralLevelGenerator;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Procedural Generator";
        config.width = 960;
        config.height = 640;
		new LwjglApplication(new ProceduralLevelGenerator(), config);
	}
}
