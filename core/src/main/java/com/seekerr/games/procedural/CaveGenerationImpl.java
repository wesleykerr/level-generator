package com.seekerr.games.procedural;

import static com.seekerr.games.procedural.LatticeFns.EMPTY;
import static com.seekerr.games.procedural.LatticeFns.FILLED;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * This is the implementation of a cave generation algorithm detailed:
 * http://www.roguebasin.com/index.php?title=
 * Cellular_Automata_Method_for_Generating_Random_Cave-Like_Levels Even though
 * this was originally designed for generating caves, it presents natural
 * looking structures that from the top down could be considered internals of
 * forests or an area where people can walk around.
 * 
 * @author wkerr
 *
 */
public class CaveGenerationImpl {
    /** Tag used for logging purposes. */
    private static final String TAG = "CaveGenerationImpl";

    private long seed;
    private Random random;

    private boolean[][] map;
    private boolean[][] bufferMap;

    private int width;
    private int height;

    private List<Phase> phases;

    private CaveGenerationImpl() {
        this.seed = 7;
        this.phases = Lists.newArrayList();
    }

    /**
     * Return the map for rendering.
     * 
     * @return
     */
    public boolean[][] getMap() {
        return map;
    }

    /**
     * Return the parameters for the phase.
     * 
     * @param index
     * @return
     */
    public Phase getPhase(int index) {
        return phases.get(index);
    }

    public void initialize() {
        random = new Random(seed);

        map = new boolean[height][width];
        bufferMap = new boolean[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                bufferMap[i][j] = FILLED;

                if (i == 0 || j == 0 || i == map.length - 1
                        || j == map[i].length - 1) {
                    map[i][j] = FILLED;
                    continue;
                }

                if (random.nextDouble() < 0.4) {
                    map[i][j] = FILLED;
                }
            }
        }
        Gdx.app.debug(TAG, "Initial");
        Gdx.app.debug(TAG, toString(map));
    }

    public void step(int minCount, int maxCount) {
        // if we haven't called initialize yet
        // go ahead and do it it ourselves.
        if (bufferMap == null) {
            initialize();
        }
        boolean[][] tmpMap;
        for (int i = 1; i < height - 1; ++i) {
            for (int j = 1; j < width - 1; ++j) {
                int count1 = LatticeFns.getNeighborCount(map, i, j);
                int count2 = LatticeFns.getTwoStepNeighborCount(map, i, j);
                if (count1 >= minCount || count2 <= maxCount) {
                    bufferMap[i][j] = FILLED;
                } else {
                    bufferMap[i][j] = EMPTY;
                }
            }
        }
        tmpMap = map;
        map = bufferMap;
        bufferMap = tmpMap;
    }

    public void iterate() { 
        for (Phase p : phases) {
            for (int i = 0; i < p.rounds; ++i) {
                step(p.min, p.max);
                Gdx.app.debug(TAG, "Round: " + i);
                Gdx.app.debug(TAG, toString(map));
            }
        }
    }
    
    @VisibleForTesting void fixRooms() { 
        List<Set<Point>> rooms = LatticeFns.getRooms(map, EMPTY);
        Collections.sort(rooms, new Comparator<Set<Point>>() {
            @Override
            public int compare(Set<Point> set1, Set<Point> set2) {
                return Integer.compare(set2.size(), set1.size());
            } 
        });
        Gdx.app.log(TAG, "Rooms: " + rooms.size());
        for (int i = 1; i < rooms.size(); ++i) {
            fixRoom(rooms.get(i));
        }
    }
    
    public void generate() {
        initialize();
        iterate();

        System.out.println(toString(map));
        bufferMap = null;
        fixRooms();
    }

    /**
     * Choose a random point from the room and walk towards the center of the
     * map until we encounter an empty cell that is not part of this room.
     * 
     * TODO(wkerr): There are still cases where we march across the map and
     * fail to connect the room to anything else.  The fix I put in stops this
     * algorithm from overwriting a wall, but doesn't address the underlying
     * cause of this problem.
     * 
     * @param room
     *            - all of the points in this room.
     */
    @VisibleForTesting void fixRoom(Set<Point> room) {
        Point point = room.iterator().next();
        Gdx.app.log(TAG, "Starting point " + point.x + "," + point.y);

        Point delta = new Point(
                (int) Math.signum((width / 2) - point.x),
                (int) Math.signum((height / 2) - point.y));
        
        Gdx.app.log(TAG, "Delta (" + delta.x + "," + delta.y + ")");

        while (point.valid(0, width, 0, height)) { 
            move(point, delta);
            
            if (!point.valid(1, width-1, 1, height-1))
                break;
            
            if (map[point.y][point.x] == EMPTY && !room.contains(point)) {
                Gdx.app.log(TAG, " Point: " + point.toString() + " -- connected ");
                return;
            }
            if (map[point.y][point.x] == FILLED) {
                map[point.y][point.x] = EMPTY;
            }
            
        }

        Gdx.app.log(TAG, "Encountered a boundary before finding an open space!");
        Gdx.app.log(TAG, ".. last location: " + point.x + ", " + point.y);
    }
    
    /**
     * This will move the point along the delta direction provided.  This 
     * ensures that we actually move before returning to prevent returning to
     * the same location.
     * @param point
     */
    @VisibleForTesting void move(Point point, Point delta) { 
        int x = point.x;
        int y = point.y;
        while (x == point.x && y == point.y) { 
            if (random.nextDouble() < 0.5)
                x += delta.x;
            else
                y += delta.y;
        }
        point.setLocation(x, y);
    }

    @Override
    public String toString() {
        return toString(map);
    }

    public static String toString(boolean[][] map) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < map.length; ++i) {
            for (int j = 0; j < map[i].length; ++j) {
                if (map[i][j] == EMPTY)
                    buf.append(".");
                else
                    buf.append("#");
            }
            buf.append("\n");
        }
        return buf.toString();
    }

    public static class Phase {
        int min;
        int max;
        int rounds;

        public Phase(int min, int max, int rounds) {
            this.min = min;
            this.max = max;
            this.rounds = rounds;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        public int getRounds() {
            return rounds;
        }
    }

    public static class Builder {
        CaveGenerationImpl cave;

        private Builder() {
            cave = new CaveGenerationImpl();
        }

        public Builder withSize(int width, int height) {
            cave.width = width;
            cave.height = height;
            return this;
        }

        public Builder withWidth(int width) {
            cave.width = width;
            return this;
        }

        public Builder withHeight(int height) {
            cave.height = height;
            return this;
        }

        public Builder withRandomSeed(long seed) {
            cave.seed = seed;
            return this;
        }

        public Builder addPhase(int min, int max, int rounds) {
            cave.phases.add(new Phase(min, max, rounds));
            return this;
        }

        public CaveGenerationImpl build() {
            Preconditions.checkNotNull(cave);
            CaveGenerationImpl tmp = cave;
            cave = null;
            return tmp;
        }

        public static Builder create() {
            return new Builder();
        }
    }
    
    public static CaveGenerationImpl getDefaultImpl(long seed) { 
        return CaveGenerationImpl.Builder.create()
                .withSize(60, 40)
                .withRandomSeed(seed)
                .addPhase(5, 2, 4)
                .addPhase(5, -1, 5)
                .build();
    }
}
