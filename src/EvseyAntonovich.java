import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Basic record for a pair of values
 * @param first First value
 * @param second Second value
 * @param <T> First value type
 * @param <V> Second value type
 */
record Pair<T, V>(T first, V second) {

}

class TileOccupant {

}

class Captain extends TileOccupant {

}

interface Hazard {
    /**
     * Gets the perception zone pattern of the hazard, centered on itself.
     * @return 2D list with odd dimensions, where true - perception zone, false - no perception zone
     */
    abstract List<List<Boolean>> getPerceptionZone();
}

class DavyJones extends TileOccupant implements Hazard {

    @Override
    public List<List<Boolean>> getPerceptionZone() {
        List<List<Boolean>> returnList = new ArrayList<>(3);
        returnList.add(Arrays.asList(true, true, true));
        returnList.add(Arrays.asList(true, true, true));
        returnList.add(Arrays.asList(true, true, true));
        return returnList;
    }
}

class Kraken extends TileOccupant implements Hazard {

    @Override
    public List<List<Boolean>> getPerceptionZone() {
        List<List<Boolean>> returnList = new ArrayList<>(3);
        returnList.add(Arrays.asList(false, true, false));
        returnList.add(Arrays.asList(true, true, true));
        returnList.add(Arrays.asList(false, true, false));
        return returnList;
    }

}

class Chest extends TileOccupant {

}

class Tortuga extends TileOccupant {

}

class MapTile {
    public TileOccupant occupant = null;
}

class Map {
    static public int defaultSize = 9;
    public int mapSize;
    public List<List<MapTile>> tiles;
    public List<List<Boolean>> perceptionZone;

    public Pair<Integer, Integer> rockLocation;
    public Map(int newSize) {
        mapSize = newSize;
        generateEmptyMap();
    }

    public Map() {
        this(defaultSize);
    }

    /**
     * Fills the tiles 2D list with empty map tiles and generally initializes the map into a workable state.
     */
    private void generateEmptyMap() {
        tiles = new ArrayList<>(mapSize);
        for (int i = 0; i < mapSize; i++) {
            ArrayList<MapTile> curList = new ArrayList<>(mapSize);
            for (int j = 0; j < mapSize; j++) {
                curList.add(new MapTile());
            }
            tiles.add(curList);
        }
    }

    /**
     * Applies a perception zone pattern onto the map at a certain point, switching tiles to true according to the patterns.
     * @param x Center of the pattern horizontally
     * @param y Center of the pattern vertically
     * @param pattern Perception zone pattern
     */
    public void applyPerceptionPattern(int x, int y, List<List<Boolean>> pattern) {
        int patternXSize = pattern.get(0).size();
        int patternYSize = pattern.size();
        int xMin = Math.max(x - patternXSize / 2, 0);
        int xMax = Math.min(x + patternXSize / 2, mapSize - 1);
        int yMin = Math.max(y - patternYSize / 2, 0);
        int yMax = Math.min(y + patternYSize / 2, mapSize - 1);
        for (int i = 0; i < yMax - yMin; i++) {
            for (int j = 0; j < xMax - xMin; j++) {
                perceptionZone.get(yMin + i).set(xMin + j, pattern.get(i).get(j));
            }
        }
    }

}

public class EvseyAntonovich {
    public static void main(String[] args) {
        Map myMap = new Map();
    }
}
