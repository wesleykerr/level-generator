package com.seekerr.games.generator.screen;

import java.util.Map;

import com.badlogic.gdx.Screen;
import com.google.common.collect.Maps;

public class ScreenFactory {

    public static enum ScreenEnum { 
        cave, forest, overlay
    };
    
    public static ScreenFactory instance = null;
    
    private Map<ScreenEnum,DefaultScreen> screenMap;
    
    private ScreenFactory() { 
        screenMap = Maps.newTreeMap();
    }
    
    public static ScreenFactory get() { 
        if (instance == null) 
            instance = new ScreenFactory();
        return instance;
    }
    
    public void addScreen(ScreenEnum screenEnum, DefaultScreen screen) { 
        screenMap.put(screenEnum, screen);
    }
    
    public DefaultScreen getScreen(ScreenEnum screen) { 
        return screenMap.get(screen);
    }
}
