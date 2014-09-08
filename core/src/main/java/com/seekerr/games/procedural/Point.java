package com.seekerr.games.procedural;

import java.util.Objects;

public class Point {
    protected int x;
    protected int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point p = (Point) obj;
            return (p.x == x) && (p.y == y);
        }
        return super.equals(obj);
    }

    public int hashCode() {
        return Objects.hash(x, y);
    }

    public String toString() {
        return "[" + x + "," + y + "]";
    }

    public int getX() {
        return x;
    }
    
    public int getY() { 
        return y;
    }

    /**
     * Does this point lie within the given range.
     * @param minx
     * @param maxx
     * @param miny
     * @param maxy
     * @return
     */
    public boolean valid(int minx, int maxx, int miny, int maxy) {
        return x >= minx && x < maxx &&
               y >= miny && y < maxy;
    }

    /**
     * Subtract Point b from Point a.
     * @param a
     * @param b
     * @return
     */
    public static Point subtract(Point a, Point b) { 
        return new Point(a.x - b.x, a.y - b.y);
    }
    
    /**
     * Add Point a to Point b.
     * @param a
     * @param b
     * @return
     */
    public static Point add(Point a, Point b) { 
        return new Point(a.x + b.x, a.y + b.y);
    }
}
