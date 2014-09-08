package com.seekerr.games.procedural;

import static com.seekerr.games.procedural.LatticeFns.EMPTY;
import static com.seekerr.games.procedural.LatticeFns.FILLED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.google.common.collect.Lists;
import com.seekerr.games.procedural.LatticeFns.MoorePixel;

public class LatticeFnsTest {
    public static final boolean w = FILLED;
    public static final boolean e = EMPTY;
    
    @Before
    public void setupMocks() {
        Application app = mock(Application.class);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println(args[1]);
                return null;
            }
        }).when(app).log(anyString(), anyString());    
        Gdx.app = app;
    }
    

    @Test
    public void testGetNeighborCount() {
        boolean[][] testMap = {
                { w, w, w, w, w },
                { w, e, e, e, w },
                { w, w, e, e, w },
                { w, e, w, e, w },
                { w, w, w, w, w },
        };
        
        assertEquals(6, LatticeFns.getNeighborCount(testMap, 1, 1));
        assertEquals(5, LatticeFns.getNeighborCount(testMap, 2, 1));
        assertEquals(2, LatticeFns.getNeighborCount(testMap, 2, 2));

    }
    
    @Test
    public void testGetTwoStepNeighborCount() {
        boolean[][] testMap = {
                { w, w, w, w, w },
                { w, e, e, e, w },
                { w, w, e, e, w },
                { w, e, w, e, w },
                { w, w, w, w, w },
        };
        
        assertEquals(9, LatticeFns.getTwoStepNeighborCount(testMap, 1, 1));
        assertEquals(11, LatticeFns.getTwoStepNeighborCount(testMap, 2, 1));
        assertEquals(14, LatticeFns.getTwoStepNeighborCount(testMap, 2, 2));
    }
    
    @Test
    public void testIsBorderPoint() { 
        boolean[][] testMap = {
                { w, w, w, w, w },
                { w, w, e, e, w },
                { w, w, e, e, w },
                { w, e, e, e, w },
                { w, w, w, w, w },
        };
        
        assertFalse(LatticeFns.isBorderPoint(testMap, 0, 0, w));
        assertTrue(LatticeFns.isBorderPoint(testMap, 1, 1, w));
        assertFalse(LatticeFns.isBorderPoint(testMap, 1, 0, w));
        assertFalse(LatticeFns.isBorderPoint(testMap, 0, 1, w));
        assertTrue(LatticeFns.isBorderPoint(testMap, 3, 0, w));
        assertFalse(LatticeFns.isBorderPoint(testMap, 4, 0, w));
    }
    
    @Test
    public void testFindEmptyNeighbor() { 
        boolean[][] testMap = {
                { w, w, w, w, w },
                { w, w, e, e, w },
                { w, w, e, e, w },
                { w, e, e, e, w },
                { w, w, w, w, w },
        };
        
        assertEquals(new Point(1,3), LatticeFns.findEmptyNeighbor(testMap, new Point(0, 2)));
        assertEquals(new Point(3,1), LatticeFns.findEmptyNeighbor(testMap, new Point(2, 0)));
        assertEquals(new Point(2,1), LatticeFns.findEmptyNeighbor(testMap, new Point(1, 2)));
        assertEquals(new Point(3,1), LatticeFns.findEmptyNeighbor(testMap, new Point(4, 2)));
        assertEquals(new Point(3,2), LatticeFns.findEmptyNeighbor(testMap, new Point(4, 1)));
        
    }

    @Test
    public void testFindEmptyNeighborMissing() { 
        boolean[][] testMap = {
                { w, w, w, w, w },
                { w, w, e, e, w },
                { w, w, e, e, w },
                { w, e, e, e, w },
                { w, w, w, w, w },
        };
        assertEquals(null, LatticeFns.findEmptyNeighbor(testMap, new Point(0, 1)));
    }
    
    @Test
    public void testGetNextClockwisePoint() {
        boolean[][] testMap = {
                { w, w, w, w, w },
                { w, w, e, e, w },
                { w, w, e, e, w },
                { w, e, e, e, w },
                { w, w, w, w, w },
        };
        
        MoorePixel pixel;
        pixel = LatticeFns.getNextClockwisePoint(testMap, new Point(2, 0), new Point(2,1));
        assertEquals(new Point(1, 1), pixel.point);
        assertEquals(new Point(2, 1), pixel.backtrack);

        pixel = LatticeFns.getNextClockwisePoint(testMap, new Point(1, 1), new Point(2,1));
        assertEquals(new Point(1, 2), pixel.point);
        assertEquals(new Point(2, 2), pixel.backtrack);
        
        pixel = LatticeFns.getNextClockwisePoint(testMap, new Point(2, 4), new Point(2, 3));
        assertEquals(new Point(3, 4), pixel.point);
        assertEquals(new Point(3, 3), pixel.backtrack);
        
        boolean[][] map = {
                { w, e, e, e, e },
                { w, w, e, e, e },
                { w, w, e, e, e },
                { w, w, e, e, e },
                { w, w, w, e, e },
        };

        pixel = LatticeFns.getNextClockwisePoint(map, new Point(0, 0), new Point(1,0));
        assertEquals(new Point(1, 1), pixel.point);
        assertEquals(new Point(1, 0), pixel.backtrack);

        pixel = LatticeFns.getNextClockwisePoint(map, new Point(1, 1), new Point(1,0));
        assertEquals(new Point(1, 2), pixel.point);
        assertEquals(new Point(2, 2), pixel.backtrack);
    }
    
    @Test
    public void testGetNextClockwisePointMissing() { 
        boolean[][] testMap = {
                { e, e, e },
                { e, w, e },
                { e, e, e },
        };
        MoorePixel pixel = LatticeFns.getNextClockwisePoint(testMap, 
                    new Point(1, 1), new Point(1,0));
        assertEquals(null, pixel);
    }
    
    @Test
    public void testGetContour() { 
        boolean w = FILLED;
        boolean e = EMPTY;
        boolean[][] testMap = {
                { w, w, w, w, w },
                { w, w, e, e, w },
                { w, w, e, e, w },
                { w, e, e, e, w },
                { w, w, w, w, w },
        };
        
        List<Point> points = LatticeFns.getContour(testMap);
        List<Point> expected = Lists.newArrayList(
                new Point(2,0), new Point(1, 1), new Point(1, 2),
                new Point(0, 3), new Point(1, 4), new Point(2, 4), 
                new Point(3, 4), new Point(4, 3), new Point(4, 2),
                new Point(4, 1), new Point(3, 0)
        );
        assertEquals(expected, points);
    }
    
    @Test
    public void testGetContourLarge() { 
        CaveGenerationImpl impl = CaveGenerationImpl.Builder.create()
                .withSize(30, 20)
                .withRandomSeed(13L)
                .addPhase(5, 2, 4)
                .addPhase(5, -1, 5)
                .build();
        impl.generate();

        LatticeFns.getContour(impl.getMap());
    }
    
    @Test
    public void testFullContourRun() { 
        CaveGenerationImpl impl = CaveGenerationImpl.Builder.create()
                .withSize(60, 40)
                .withRandomSeed(1409760206706L)
                .addPhase(5, 2, 4)
                .addPhase(5, -1, 5)
                .build();
        impl.generate();
        
        List<Point> points = LatticeFns.getContour(impl.getMap());
    }
    
    @Test
    public void testSinglePointContour() { 
        boolean[][] testMap = {
                { e, e, e },
                { e, w, e },
                { e, e, e },
        };
        List<Point> points = LatticeFns.getContour(testMap);
        assertEquals(1, points.size());
        assertEquals(new Point(1,1), points.get(0));
    }
    
    @Test
    public void testGetRoomsSingle() { 
        boolean[][] testMap = {
                { e, e, e },
                { e, w, e },
                { e, e, e },
        };
        List<Set<Point>> rooms = LatticeFns.getRooms(testMap, FILLED);    
        assertEquals(1, rooms.size());
        assertEquals(1, rooms.get(0).size());
        assertEquals(new Point(1,1), rooms.get(0).iterator().next());
    }
    
    @Test
    public void testGetContourMissingSingle() { 
        CaveGenerationImpl impl = CaveGenerationImpl
                .getDefaultImpl(1410186023359L);
        impl.generate();
        List<Point> points = LatticeFns.getContour(impl.getMap());
    }
}
