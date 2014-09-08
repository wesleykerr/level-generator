package com.seekerr.games.procedural;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.google.common.collect.Lists;
import com.seekerr.games.procedural.ForestGenerationImpl.Range;


public class ForestGenerationTest {
    byte f = ForestGenerationImpl.FOREST;
    byte e = ForestGenerationImpl.EMPTY;

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
    public void testGenerate() {
        ForestGenerationImpl impl = ForestGenerationImpl.Builder.create()
                .withSize(30, 20)
                .build();
        impl.generate();
        System.out.println(impl.toString());
    }
    
    @Test
    public void testAddTree() { 
        byte[][] expected = {
                { f, f, f, e, e, e, e, e, e, e },
                { f, f, f, f, e, e, e, e, e, e },
                { f, f, f, e, f, e, e, e, e, e },
                { e, f, e, f, f, f, e, e, e, e },
                { e, e, f, f, f, f, f, e, e, e },
                { e, e, e, f, f, f, e, e, e, e },
                { e, e, e, e, f, e, e, e, e, e },
                { e, e, e, e, e, e, e, e, e, f },
                { e, e, e, e, e, e, e, e, f, f },
                { e, e, e, e, e, e, e, f, f, f },
        };

        ForestGenerationImpl impl = ForestGenerationImpl.Builder.create()
                .withSize(10, 10)
                .build();
        
        List<Point> expectedTrees = Lists.newLinkedList();
        expectedTrees.add(new Point(1,1));
        expectedTrees.add(new Point(4,4));
        expectedTrees.add(new Point(9,9));
        for (Point p : expectedTrees)
            impl.addTree(p.x, p.y);

        List<Point> actualTrees = impl.getTrees();
        assertEquals(expectedTrees.size(), actualTrees.size());
        for (int i = 0; i < expectedTrees.size(); ++i) { 
            assertEquals(expectedTrees.get(i), actualTrees.get(i));
        }
        
        System.out.println(impl.toString());
        byte[][] actual = impl.getForest();
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i].length, actual[i].length);
            for (int j = 0; j < expected[0].length; ++j) {
                assertEquals(expected[i][j], actual[i][j]);
            }
        }
    }
        
    @Test
    public void testGetCoverage() {
        ForestGenerationImpl impl = ForestGenerationImpl.Builder.create()
                .withSize(5, 5)
                .build();
        
        impl.addTree(1, 1);
        assertEquals(0.44, impl.getCoverage(), 0.0001);
    }
        
    @Test
    public void testFindRange() {
        ForestGenerationImpl impl = ForestGenerationImpl.Builder.create()
                .withSize(9, 9)
                .build();
        
        List<Range> ranges = impl.findRange(4, 4, 3);
        assertEquals(new Range(1, 4, 7, 4), ranges.get(0));
        assertEquals(new Range(2, 3, 6, 3), ranges.get(1));
        assertEquals(new Range(2, 5, 6, 5), ranges.get(2));
        assertEquals(new Range(2, 2, 6, 2), ranges.get(3));
        assertEquals(new Range(2, 6, 6, 6), ranges.get(4));
        assertEquals(new Range(4, 1, 4, 1), ranges.get(5));
        assertEquals(new Range(4, 7, 4, 7), ranges.get(6));
    }
}
