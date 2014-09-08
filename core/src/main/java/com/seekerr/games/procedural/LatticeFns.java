package com.seekerr.games.procedural;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;




public class LatticeFns {
    // for speed and convenience we assume wall will always be 1 so
    // we can do a simple count to find the number of walls surrounding us.
    public static final boolean EMPTY = false;
    public static final boolean FILLED = true;
    
    public static final String TAG = "LatticFns";
    
    public static final Point[] MOORE_HOOD = {
            new Point(-1, -1), new Point(0, -1), new Point(1, -1),
            new Point(1, 0), new Point(1, 1), new Point(0, 1), 
            new Point(-1, 1), new Point(-1, 0)
    };       
    
    public static final Point[] VON_NEUMANN_HOOD = {
        new Point(0, -1), new Point(-1, 0), new Point(1, 0), new Point(0, 1)
    };
    
    /** This map provides quick lookup by Point to get the starting index
     *  in the Moore-Neighborhood.
     */
    public static final Map<Point,Integer> MOORE_INDEX = Maps.newHashMap();
    
    static { 
        for (int i = 0; i < MOORE_HOOD.length; ++i) { 
            MOORE_INDEX.put(MOORE_HOOD[i], new Integer(i));
        }
    }

    /**
     * Does the point border empty space.
     * @param y
     * @param x
     * @return 
     */
    public static boolean isBorderPoint(boolean[][] map, int y, int x, boolean type) {
        for (Point p : VON_NEUMANN_HOOD) {
            int x1 = x + p.x;
            int y1 = y + p.y;
            if (x1 < 0 || y1 < 0 || y1 >= map.length || x1 >= map[y1].length) {
                continue;
            }
            
            if (map[y1][x1] != type)
                return true;
        }
        return false;
    }
    
    /**
     * Find an empty neighbor cell around the given point.  Return the next
     * poing in the Moore-Neighborhood that is an empty cell.
     * @param map
     * @param p
     * @return
     */
    public static Point findEmptyNeighbor(boolean[][] map, Point p) { 
        Gdx.app.log(TAG, "Point" + p.x + "," + p.y);
        for (int index = 0; index < MOORE_HOOD.length; ++index) {
            Point current = Point.add(p, MOORE_HOOD[index]);
            if (!current.valid(0, map[0].length, 0, map.length))
                continue;

            if (map[current.y][current.x] == EMPTY) 
                return current;
        }
        return null;
    }

    /**
     * Find the next filled point that is clockwise starting from the given
     * index into the Moore-Neighborhood.
     * @param map
     * @param currentPoint - the filled point we are starting from.
     * @param backtrackPoint - the last empty point before entering the current
     *          point.
     * @return
     */
    public static MoorePixel getNextClockwisePoint(boolean[][] map,
            Point currentPoint, Point backtrackPoint) {

        if (backtrackPoint == null) {
            Gdx.app.error(TAG, "Missing backtrack point");
            throw new RuntimeException("Missing backtrack point. current:" + currentPoint);
        }
        if (currentPoint == null) {
            Gdx.app.error(TAG, "Missing current point");
            throw new RuntimeException("Missing current point.  backtrack:" + backtrackPoint);
        }
        
        Point delta = Point.subtract(backtrackPoint, currentPoint);
        int start = MOORE_INDEX.get(delta);
        
        Point lastPoint = backtrackPoint;
        // No reason to check the white point that we came from.
        for (int i = 1; i < MOORE_HOOD.length; ++i) {
            int currentIndex = (i + start) % MOORE_HOOD.length;

            Point p = Point.add(currentPoint, MOORE_HOOD[currentIndex]);
            if (!p.valid(0, map[0].length, 0, map.length))
                continue;

            if (map[p.y][p.x] == FILLED)
                return new MoorePixel(p, lastPoint);
            lastPoint = p;
        }
        return null;
    }
    
    /**
     * We count the cell that is requested as well.
     * 
     * @param map
     * @param y
     * @param x
     * @return
     */
    public static int getNeighborCount(boolean[][] map, int y, int x) {
        int count = 0;
        for (int i = y - 1; i <= y + 1; ++i) {
            for (int j = x - 1; j <= x + 1; ++j) {
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
     * @param y
     * @param x
     * @return
     */
    public static int getTwoStepNeighborCount(boolean[][] map, int y, int x) {
        int count = 0;
        for (int i = y - 2; i <= y + 2; ++i) {
            for (int j = x - 2; j <= x + 2; ++j) {
                if (Math.abs(i - y) == 2 && Math.abs(j - x) == 2) {
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
    
    /**
     * This is an implementation of the Moore-Neighborhood tracing algorithm.
     * http://en.wikipedia.org/wiki/Moore_neighborhood
     * http://www.learn-cocos2d.com/2013/06/generate-tilemap-physics-collision-shapes-cocos2d/
     * 
     * @param map
     * @return
     */
    public static List<Point> getContour(boolean[][] map) { 
        List<Point> allBoundaryPoints = Lists.newArrayList();
        List<Set<Point>> rooms = getRooms(map, FILLED);
        Gdx.app.log(TAG, "Number of rooms: " + rooms.size());
        for (Set<Point> points : rooms) {
            Point startPoint = null;
            for (Point p : points) { 
                if (isBorderPoint(map, p.y, p.x, LatticeFns.FILLED)) {
                    startPoint = p;
                    break;
                }
            }
            if (startPoint == null) { 
                Gdx.app.log(TAG, "WTF: " + points);
            }
            
//            Point startPoint = LatticeFns.findStartPoint(map);
            Gdx.app.log(TAG, "Start Point: " + startPoint + " type: " + map[startPoint.y][startPoint.x]);
            Point backtrack = LatticeFns.findEmptyNeighbor(map, startPoint);

            List<Point> boundaryPoints = Lists.newArrayList(); 
            boundaryPoints.add(startPoint);
            
            Point currentPoint = startPoint;
            MoorePixel pixel = LatticeFns.getNextClockwisePoint(map, currentPoint, backtrack);
            while (pixel != null && !pixel.point.equals(startPoint)) {
                if (map[pixel.point.y][pixel.point.x] == LatticeFns.FILLED) {
                    boundaryPoints.add(pixel.point);
                } 
                pixel = LatticeFns.getNextClockwisePoint(map, pixel.point,
                        pixel.backtrack);
                Gdx.app.log(TAG, " start " + startPoint + " -- next " + pixel.point + ", backtrack " + pixel.backtrack);
            }
//            return boundaryPoints;
            allBoundaryPoints.addAll(boundaryPoints);
        }
        return allBoundaryPoints;
    }
    
    public static void addNeighbor(Point neighbor, 
            boolean[][] map,
            boolean type, 
            LinkedHashSet<Point> notVisited, 
            LinkedList<Point> frontier) {
        
        if (!neighbor.valid(0, map[0].length, 0, map.length))
            return;

        if (map[neighbor.y][neighbor.x] == type
                && notVisited.contains(neighbor)) {
            notVisited.remove(neighbor);
            frontier.addLast(neighbor);
        }
    }


    public static List<Set<Point>> getRooms(boolean[][] map, boolean type) {
        int height = map.length;
        int width = map[0].length;
        
        Point start = null;
        LinkedHashSet<Point> notVisited = new LinkedHashSet<Point>();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (map[y][x] == type) {
                    if (start == null)
                        start = new Point(x, y);
                    else
                        notVisited.add(new Point(x, y));
                }
            }
        }
        Gdx.app.log(TAG, "Number of points: " + notVisited.size());
        List<Set<Point>> rooms = Lists.newArrayList();
        LinkedList<Point> frontier = Lists.newLinkedList();
        frontier.add(start);
        while (!notVisited.isEmpty()) {
            Set<Point> room = new LinkedHashSet<Point>();
            while (!frontier.isEmpty()) {
                Point current = frontier.removeFirst();
                room.add(current);

                for (Point p : VON_NEUMANN_HOOD) {
                    addNeighbor(Point.add(current, p), map, type, 
                            notVisited, frontier);
                }
            }
            rooms.add(room);

            if (notVisited.size() > 0) {
                Iterator<Point> iterator = notVisited.iterator();
                frontier.add(iterator.next());
                iterator.remove();
            }
        }
        
        if (!frontier.isEmpty()) { 
            Gdx.app.log(TAG, "Frontier not empty! " + frontier.size());
            Set<Point> room = new LinkedHashSet<Point>();
            room.add(frontier.getFirst());
            rooms.add(room);
        }
        
        return rooms;
    }
    
    public static class MoorePixel {
        public Point point;
        public Point backtrack;
        
        public MoorePixel(Point point, Point backtrack) { 
            this.point = point;
            this.backtrack = backtrack;
        }
    }
}
