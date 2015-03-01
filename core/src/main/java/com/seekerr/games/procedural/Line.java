package com.seekerr.games.procedural;

import java.util.Objects;

public class Line {
    private Point start;
    private Point end;
    
    public Line(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Line) {
            Line line = (Line) obj;
            return start.equals(line.start) &&
                    end.equals(line.end);
        }
        return super.equals(obj);
    }

    public int hashCode() {
        return Objects.hash(start, end);
    }

    public String toString() {
        return start.toString() + " -- " + end.toString();
    }

    
    public Point getStart() {
        return start;
    }
    
    public Point getEnd() {
        return end;
    }
}
