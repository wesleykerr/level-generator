package com.seekerr.games.generator.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.seekerr.games.generator.ProceduralLevelGenerator;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(960, 640);
        }

        @Override
        public ApplicationListener getApplicationListener () {
                return new ProceduralLevelGenerator();
        }
}