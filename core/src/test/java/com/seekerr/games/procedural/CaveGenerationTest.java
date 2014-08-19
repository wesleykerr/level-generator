package com.seekerr.games.procedural;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.seekerr.games.procedural.CaveGenerationImpl;


public class CaveGenerationTest {

    @Before
    public void setupMocks() {
        Application app = mock(Application.class);
        Gdx.app = app;
    }
    
    @Test
    public void testGetNeighborCount() {
        boolean w = CaveGenerationImpl.WALL;
        boolean e = CaveGenerationImpl.EMPTY;
        boolean[][] testMap = {
                { w, w, w, w, w },
                { w, e, e, e, w },
                { w, w, e, e, w },
                { w, e, w, e, w },
                { w, w, w, w, w },
        };
        
        CaveGenerationImpl impl = CaveGenerationImpl.Builder.create()
                .withSize(30, 20)
                .build();
        assertEquals(6, impl.getNeighborCount(testMap, 1, 1));
        assertEquals(5, impl.getNeighborCount(testMap, 2, 1));
        assertEquals(2, impl.getNeighborCount(testMap, 2, 2));

    }
    
    @Test
    public void testGetTwoStepNeighborCount() {
        boolean w = CaveGenerationImpl.WALL;
        boolean e = CaveGenerationImpl.EMPTY;
        boolean[][] testMap = {
                { w, w, w, w, w },
                { w, e, e, e, w },
                { w, w, e, e, w },
                { w, e, w, e, w },
                { w, w, w, w, w },
        };
        
        CaveGenerationImpl impl = CaveGenerationImpl.Builder.create()
                .withSize(30, 20)
                .build();
        assertEquals(9, impl.getTwoStepNeighborCount(testMap, 1, 1));
        assertEquals(11, impl.getTwoStepNeighborCount(testMap, 2, 1));
        assertEquals(14, impl.getTwoStepNeighborCount(testMap, 2, 2));
    }

    @Test
    public void testGenerate() {
        Gdx.app = mock(Application.class);

        CaveGenerationImpl impl = CaveGenerationImpl.Builder.create()
                .withSize(30, 20)
                .build();
        impl.generate();
    }
}
