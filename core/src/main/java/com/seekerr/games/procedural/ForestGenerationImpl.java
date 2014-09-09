package com.seekerr.games.procedural;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
/**
 * The forest generation algorithm is based on a paper written in 2005:
 * 
 * Strategies for Multi-Asset Surveillance
 * http://ieeexplore.ieee.org/lpdocs/epic03/wrapper.htm?arnumber=1461318
 * 
 * Although the paper is about swarm control, one section is dedicated to a 
 * forest geneation algorithm used in the simulator.  We recreate that 
 * algorithm here for the level generator code.
 * 
 * @author  Wesley Kerr
 */
public class ForestGenerationImpl {
    public static final String TAG  = "ForestGenerationImpl";
    
    // forest constants
    public static final byte EMPTY  = 0;
    public static final byte FOREST = 1;
    public static final byte SEEDED = 2;
    
    public int initialTrees         = 10;
    
    public int seedRadius           = 5;
    public double seedDecay         = 0.2;
    public double seedStrength      = 0.05;

    public double desiredCoverage   = 0.25;
    
    public long   seed              = 7;
    public Random rand              = null;

    private int width               = 100;
    private int height              = 100;
        
    private byte[][] forest         = null;
    private List<Point> trees       = null;
    private Map<String,Double> seeds = null;
    
    private ForestGenerationImpl() {

    }
    
    @VisibleForTesting void create() { 
        rand = new Random(seed);
        
        forest = new byte[height][width];
        trees = Lists.newLinkedList();
        seeds = Maps.newHashMap();
    }
    
    /**
     * Initialize the forest.
     */
    public void initialize() {
        Gdx.app.debug(TAG, "...Initializing forest generation");
        for (int i = 0; i < initialTrees; ++i) {
            int x = 0;
            int y = 0;
            boolean found = false;
            while (!found) {
                x = rand.nextInt(width);
                y = rand.nextInt(height);
                if (forest[y][x] == EMPTY) {
                    forest[y][x] = FOREST;
                    addTree(x, y);
                    found = true;
                }
            }
        }
        Gdx.app.debug(TAG, "...Finished Initialization");
    }
    
    public void step() { 
        Map<String,Double> tmpMap = Maps.newHashMap();
        for (Map.Entry<String,Double> entry : seeds.entrySet()) {
            double value = entry.getValue();
            tmpMap.put(entry.getKey(), value - (seedDecay*value));
        }
        seeds = tmpMap;

        createTrees();
        removeDupTrees();
        seedTrees();
    }

    public void generate() {
        initialize();
        Gdx.app.debug(TAG, "\n" + toString());

        double currentlyCovered = getCoverage();
        Gdx.app.debug(TAG, "...Growing Forest, inital coverage "
                + currentlyCovered);
        while (currentlyCovered < desiredCoverage) {
            step();
            currentlyCovered = getCoverage();
        }
        removeSeeds();
        Gdx.app.debug(TAG, "...Forest Growth Complete, final coverage "
                + currentlyCovered);
    }
    
    /**
     * Return the forest.
     * @return
     */
    public byte[][] getForest() {
        return forest;
    }
    
    @VisibleForTesting Map<String,Double> getSeeds() { 
        return seeds;
    }
    
    @VisibleForTesting List<Point> getTrees() { 
        return trees;
    }
    
    @VisibleForTesting double getCoverage() {
        double size = width * height;
        double count = 0;
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                if (forest[i][j] == FOREST)
                    ++count;
            }
        }
        return count / size;
    }
    
    private void createTrees() {
        for (Map.Entry<String,Double> entry : seeds.entrySet()) {
            String key = entry.getKey();
            int pos = key.indexOf(',');
            int x = new Integer(key.substring(0, pos)).intValue();
            int y = new Integer(key.substring(pos+1)).intValue();
            
            if (rand.nextDouble() < entry.getValue()) {
                addTree(x,y);
            }
        }
    }
    
    private void removeDupTrees() {
        for (Point p : trees) {
            String key = p.x + "," + p.y;
            if (seeds.containsKey(key))
                seeds.remove(key);
        }
    }
    
    @VisibleForTesting void seedTrees() {
        for (Point p : trees) { 
            List<Range> ranges = findRange(p.x, p.y, seedRadius);
            for (Range r : ranges) {
                Point p1 = r.p1;
                Point p2 = r.p2;
                
                for (int x = p1.x; x <= p2.x; ++x) {
                    if (forest[p1.y][x] == FOREST)
                        continue;
                    
                    forest[p1.y][x] = SEEDED;
                    String key = x + "," + p1.y;
                    Double value = seeds.get(key);
                    if (value == null) 
                        value = 0.0d;
                    seeds.put(key, value + seedStrength);
                }
            }
        }
    }
    
    @VisibleForTesting void addTree(int x, int y) {
        trees.add(new Point(x, y));
        for (int i = x-2; i <= x+2; ++i) {
            if (i < 0 || i >= width)
                continue;
            forest[y][i] = FOREST;
        }
        
        for (int i = y-2; i <= y+2; ++i) {
            if (i <0 || i >= height)
                continue;
            forest[i][x] = FOREST;
        }
            
        if (x-1 >= 0 && y-1 >= 0)
            forest[y-1][x-1] = FOREST;
        if (x+1 < width && y-1 >= 0)
            forest[y-1][x+1] = FOREST;
        
        if (x-1 >= 0 && y+1 < height)
            forest[y+1][x-1] = FOREST;
        if (x+1 < width && y+1 < height)
            forest[y+1][x+1] = FOREST;
    }
    
    @VisibleForTesting public void removeSeeds() {
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                if (forest[i][j] == SEEDED)
                    forest[i][j] = EMPTY;
            }
        }
    }
    
    // TODO(wkerr): check for stones .
    private boolean valid(int x, int y) {
        return (x >= 0 && x < width &&
                y >= 0 && y < height);
    }

    @VisibleForTesting List<Range> findRange(int x, int y, int R) {
        List<Range> list = Lists.newLinkedList();
        if (valid(x, y)) {
            int min = Math.max(0, x - R);
            int max = Math.min(width-1, x + R);
            
            list.add(new Range(new Point(min, y), new Point(max, y)));
            for (int i = 1; i <= R; ++i) {
                int r = (int) Math.sqrt(R * R - i * i);
                if (y - i >= 0) {
                    min = Math.max(0, x-r);
                    max = Math.min(width-1, x+r);
                    list.add(new Range(new Point(min, y-i), new Point(max, y-i)));
                }

                if (y + i < height) {
                    min = Math.max(0, x-r);
                    max = Math.min(width-1, x+r);
                    list.add(new Range(new Point(min, y+i), new Point(max, y+i)));
                }
            }
        }
        return list;
    }
    
    public String toString() { 
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < forest.length; ++i) {
            for (int j = 0; j < forest[i].length; ++j) {
                if (forest[i][j] == SEEDED) 
                    buf.append(".");
                else if (forest[i][j] == FOREST)
                    buf.append("+");
                else 
                    buf.append("_");
            }
            buf.append("\n");
        }
        return buf.toString();
    }

    public static class Range {
        public Point p1;
        public Point p2;
        
        public Range(Point p1, Point p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
        
        public Range(int x1, int y1, int x2, int y2) {
            this.p1 = new Point(x1, y1);
            this.p2 = new Point(x2, y2);
        }
        
        @Override
        public boolean equals(Object o) { 
            if (!(o instanceof Range))
                return false;
            Range r = (Range) o;
            return r.p1.equals(p1) && r.p2.equals(p2);
        }
    }

    public static class Builder {
        ForestGenerationImpl forest;
        
        private Builder() {
            forest = new ForestGenerationImpl();
        }
        
        public Builder withSize(int width, int height) {
            forest.width = width;
            forest.height = height;
            return this;
        }
        
        public Builder withWidth(int width) { 
            return this;
        }
        
        public Builder withHeight(int height) { 
            forest.height = height;
            return this;
        }
        
        public Builder withRandomSeed(long seed) {
            forest.seed = seed;
            return this;
        }
        
        public Builder withInitialTrees(int initialTrees) { 
            forest.initialTrees = initialTrees;
            return this;
        }
        
        public Builder withSeedParams(int seedRadius, double seedDecay, 
                double seedStrenth) {
            forest.seedRadius = seedRadius;
            forest.seedDecay = seedDecay;
            forest.seedStrength = seedStrenth;
            return this;
        }
        
        public Builder withDesiredCoverage(double desiredCoverage) {
            forest.desiredCoverage = desiredCoverage;
            return this;
        }
                
        public ForestGenerationImpl build() { 
            Preconditions.checkNotNull(forest);
            forest.create();

            ForestGenerationImpl tmp = forest;
            forest = null;
            return tmp;
        }
        
        public static Builder create() { 
            return new Builder();
        }
    }
}
