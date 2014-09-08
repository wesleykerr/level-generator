package com.seekerr.games.procedural;

import static com.seekerr.games.procedural.LatticeFns.EMPTY;
import static com.seekerr.games.procedural.LatticeFns.FILLED;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;


public class CaveGenerationTest {

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
        CaveGenerationImpl impl = CaveGenerationImpl.Builder.create()
                .withSize(30, 20)
                .build();
        impl.generate();
    }
    
    @Test
    public void testFixRooms() { 
        CaveGenerationImpl impl = CaveGenerationImpl.Builder.create()
                .withSize(60, 40)
                .withRandomSeed(1410187129987L)
                .addPhase(5, 2, 4)
                .addPhase(5, -1, 5)
                .build();
        
        impl.initialize();
        impl.iterate();

        System.out.println(impl.toString());

        List<Set<Point>> rooms = LatticeFns.getRooms(impl.getMap(), EMPTY);
        System.out.println("Number of Rooms: " + rooms.size());
        Collections.sort(rooms, new Comparator<Set<Point>>() {
            @Override
            public int compare(Set<Point> set1, Set<Point> set2) {
                return Integer.compare(set2.size(), set1.size());
            } 
        });
        
        boolean[][] map = impl.getMap();
        Point p = new Point(0,0);
        for (int y = 0; y < map.length; ++y) { 
            for (int x = 0; x < map[y].length; ++x) { 
                p.setLocation(x, y);
                
                int roomIndex = -1;
                for (int i = 0; i < rooms.size(); ++i) { 
                    Set<Point> room = rooms.get(i);
                    if (room.contains(p)) {
                        roomIndex = i;
                        break;
                    }
                }

                if (p.x == 9 && p.y == 20) {
                    System.out.print("-");
                }
                else if (roomIndex >= 0) { 
                    System.out.print(roomIndex);
                } else if (map[y][x] == FILLED) {
                    System.out.print("#");
                } else if (map[y][x] == EMPTY) {
                    System.out.print(".");
                }
            }
            System.out.println();
        }
        System.out.println(map[20][9]);
        System.out.println(rooms.get(2).contains(new Point(9, 20)));
        
        for (int i = 1; i < rooms.size(); ++i) { 
            impl.fixRoom(rooms.get(i));
        }
        
        System.out.println(impl.toString());
    }
}
