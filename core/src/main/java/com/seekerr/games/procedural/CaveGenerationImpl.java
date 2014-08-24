package com.seekerr.games.procedural;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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

    // for speed and convenience we assume wall will always be 1 so
    // we can do a simple count to find the number of walls surrounding us.
    public static final boolean EMPTY = false;
    public static final boolean FILLED = true;

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

    /**
     * We count the cell that is requested as well.
     * 
     * @param map
     * @param row
     * @param col
     * @return
     */
    @VisibleForTesting
    int getNeighborCount(boolean[][] map, int row, int col) {
        int count = 0;
        for (int i = row - 1; i <= row + 1; ++i) {
            for (int j = col - 1; j <= col + 1; ++j) {
                if (i < 0 || j < 0 || i >= map.length || j > map[i].length) {
                    continue;
                }
                count += map[i][j] == FILLED ? 1 : 0;
            }
        }
        return count;
    }

    /**
     * We count the cell that is requested as well.
     * 
     * @param map
     * @param row
     * @param col
     * @return
     */
    @VisibleForTesting
    int getTwoStepNeighborCount(boolean[][] map, int row, int col) {
        int count = 0;
        for (int i = row - 2; i <= row + 2; ++i) {
            for (int j = col - 2; j <= col + 2; ++j) {
                if (Math.abs(i - row) == 2 && Math.abs(j - col) == 2) {
                    continue;
                }
                if (i < 0 || j < 0 || i >= map.length || j >= map[i].length) {
                    continue;
                }
                count += map[i][j] == FILLED ? 1 : 0;
            }
        }
        return count;
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
        Gdx.app.log(TAG, "Initial");
        Gdx.app.log(TAG, toString(map));
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
                int count1 = getNeighborCount(map, i, j);
                int count2 = getTwoStepNeighborCount(map, i, j);
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

    public void generate() {
        initialize();
        for (Phase p : phases) {
            for (int i = 0; i < p.rounds; ++i) {
                step(p.min, p.max);
                Gdx.app.log(TAG, "Round: " + i);
                Gdx.app.log(TAG, toString(map));
            }
        }

        System.out.println(toString(map));
        bufferMap = null;

        List<Set<Point>> rooms = getRooms();
        Gdx.app.log(TAG, "Rooms: " + rooms.size());
        for (int i = 1; i < rooms.size(); ++i) {
            fixRoom(rooms.get(i));
        }
    }

    public void addNeighbor(Point current, int dx, int dy,
            LinkedHashSet<Point> notVisited, LinkedList<Point> frontier) {
        Point neighbor = new Point(current.x + dx, current.y + dy);
        if (neighbor.x < 0 || neighbor.y < 0 || neighbor.x >= width
                || neighbor.y >= height)
            return;

        if (map[neighbor.y][neighbor.x] == EMPTY
                && notVisited.contains(neighbor)) {
            notVisited.remove(neighbor);
            frontier.addLast(neighbor);
        }
    }

    public List<Set<Point>> getRooms() {
        Point start = null;
        LinkedHashSet<Point> notVisited = new LinkedHashSet<Point>();
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                if (map[i][j] == EMPTY) {
                    if (start == null)
                        start = new Point(j, i);
                    else
                        notVisited.add(new Point(j, i));
                }
            }
        }
        Gdx.app.log(TAG, "Number of points: " + notVisited.size());
        List<Set<Point>> rooms = new ArrayList<Set<Point>>();
        LinkedList<Point> frontier = new LinkedList<Point>();
        frontier.add(start);
        while (!notVisited.isEmpty()) {
            Set<Point> room = new HashSet<Point>();
            while (!frontier.isEmpty()) {
                Point current = frontier.removeFirst();
                room.add(current);

                // getNeighbors() -- only allow moving up or down
                addNeighbor(current, 1, 0, notVisited, frontier);
                addNeighbor(current, -1, 0, notVisited, frontier);
                addNeighbor(current, 0, 1, notVisited, frontier);
                addNeighbor(current, 0, -1, notVisited, frontier);
            }
            rooms.add(room);

            if (notVisited.size() > 0) {
                Iterator<Point> iterator = notVisited.iterator();
                frontier.add(iterator.next());
                iterator.remove();
            }
        }
        return rooms;
    }

    /**
     * Choose a random point from the room and walk towards the center of the
     * map until we encounter an empty cell that is not part of this room.
     * 
     * @param room
     *            - all of the points in this room.
     */
    public void fixRoom(Set<Point> room) {
        Point point = room.iterator().next();
        Gdx.app.log(TAG, "Starting point " + point.x + "," + point.y);

        int dx = (int) Math.signum((width / 2) - point.x);
        int dy = (int) Math.signum((height / 2) - point.y);

        int x = point.x;
        int y = point.y;
        while (x > 0 && y > 0 && x < width && y < height) {
            if (random.nextDouble() < 0.5)
                x += dx;
            else
                y += dy;

            if (x < 0 || y < 0 || x >= width || y >= width)
                break;

            point.setLocation(x, y);
            if (map[y][x] == EMPTY && !room.contains(point)) {
                return;
            }

            if (map[y][x] == FILLED) {
                map[y][x] = EMPTY;
            }
        }

        Gdx.app.log(TAG, "Encountered a boundary before finding an open space!");
        Gdx.app.log(TAG, ".. last location: " + x + ", " + y);
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
}
