import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Basic class for a pair of values
 */
class Pair<T, V> {
    public T first;
    public V second;
    Pair(T first, V second) {
        this.first = first;
        this.second = second;
    }
}

/**
 * Class for a coordinate in 2D space
 */
class Point {
    int x;
    int y;
    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    Point() {
        this(0, 0);
    }

    /**
     * Check if coordinate is equal to another coordinate
     * @param other Other coordinate to compare with
     * @return True if both x and y are equal, false otherwise
     */
    public boolean equals(Point other) {
        return x == other.x && y == other.y;
    }

    /**
     * Sum coordinates with another point
     * @param other Other point
     * @return New point with summed coordinates
     */
    Point sum(Point other) {
        return new Point(x + other.x, y + other.y);
    }

    /**
     * Multiply point by an integer coefficient
     * @param coeff Coefficient by how much to multiply the point
     * @return New point with multiplied coordinates
     */
    Point mult(int coeff) {
        return new Point(x * coeff, y * coeff);
    }

    /**
     * Take difference of this point and another point
     * @param other The other point
     * @return New point with difference coordinates
     */
    Point diff(Point other) {
        return sum(other.mult(-1));
    }

    /**
     * Divide point by an integer coefficient
     * @param coeff Integer number by how much to divide the point
     * @return New point with divided coordinates
     */
    Point div(int coeff) {
        return new Point(x / coeff, y / coeff);
    }

    /**
     * Get the diagonal distance cost to another point with specified diagonal and non-diagonal movement costs
     * @param other The point to which compute the distance
     * @param costNormal Cost of going non-diagonally
     * @param costDiagonal Cost of going diagonally
     * @return Total cost of movement
     */
    int diagonalDistance(Point other, int costNormal, int costDiagonal) {
        int diffX = Math.abs(x - other.x);
        int diffY = Math.abs(y - other.y);
        return costNormal * (diffX + diffY) + (costDiagonal - 2 * costNormal) * Math.min(diffX, diffY);
    }

    /**
     * Get the manhattan (strictly non-diagonal) distance cost to another point with specified movement cost
     * @param other The point to which compute the distance
     * @param cost Cost of going non-diagonally
     * @return Total cost of movement
     */
    int manhattanDistance(Point other, int cost) {
        int diffX = Math.abs(x - other.x);
        int diffY = Math.abs(y - other.y);
        return cost * (diffX + diffY);
    }

    @Override
    public String toString() {
        return "{" + x + "," + y + "}";
    }
}

/**
 * Base class for objects which occupy a tile, except the Rock
 */
class TileOccupant {

}

/**
 * Class for Jack Sparrow
 */
class Captain extends TileOccupant {

}

/**
 * Interface for objects which have a perception zone, other than the Rock, as it is not actually a class
 */
interface Hazard {
    /**
     * Gets the perception zone pattern of the hazard, centered on itself.
     * @return 2D list with odd dimensions, where true - perception zone, false - no perception zone
     */
    abstract List<List<Boolean>> getPerceptionZone();
}

/**
 * Class for Davy Jones
 */
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

/**
 * Class for Kraken
 */
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

/**
 * Class for the Dead Man's Chest
 */
class Chest extends TileOccupant {

}

/**
 * Class for the Tortuga Island
 */
class Tortuga extends TileOccupant {

}

/**
 * Container class which stores the occupant of a map tile
 */
class MapTile {
    public TileOccupant occupant = null;
}

/**
 * The sea map itself
 */
class Map {
    static public int defaultSize = 9;
    public int mapSize;
    public List<List<MapTile>> tiles;
    public List<List<Boolean>> dangerZone;
    public List<List<Boolean>> perceptionZone;
    public List<List<Boolean>> krakenZone;

    public Point captainLocation;
    public Point davyLocation;
    public Point krakenLocation;
    public Point rockLocation;
    public Point chestLocation;
    public Point tortugaLocation;

    public Map(int newSize) {
        mapSize = newSize;
        generateEmptyMap();
    }

    public Map() {
        this(defaultSize);
    }

    /**
     * Initializes all the 2D lists and generally initializes the map into a workable state.
     */
    private void generateEmptyMap() {
        tiles = new ArrayList<>(mapSize);
        perceptionZone = new ArrayList<>(mapSize);
        krakenZone = new ArrayList<>(mapSize);
        dangerZone = new ArrayList<>(mapSize);
        for (int i = 0; i < mapSize; i++) {
            ArrayList<MapTile> curList = new ArrayList<>(mapSize);
            ArrayList<Boolean> pList = new ArrayList<>(mapSize);
            ArrayList<Boolean> kList = new ArrayList<>(mapSize);
            ArrayList<Boolean> dList = new ArrayList<>(mapSize);
            for (int j = 0; j < mapSize; j++) {
                curList.add(new MapTile());
                pList.add(false);
                kList.add(false);
                dList.add(false);
            }
            tiles.add(curList);
            perceptionZone.add(pList);
            krakenZone.add(kList);
            dangerZone.add(dList);
        }
    }

    /**
     * Applies a perception zone pattern onto the map at a certain point, switching tiles to true according to the patterns.
     * @param x Center of the pattern horizontally
     * @param y Center of the pattern vertically
     * @param pattern Perception zone pattern
     * @param kraken Whether this pattern is the pattern of the kraken
     */
    public void applyPerceptionPattern(int x, int y, List<List<Boolean>> pattern, boolean kraken) {
        int patternXSize = pattern.get(0).size();
        int patternYSize = pattern.size();
        int xMin = x - patternXSize / 2;
        int xMax = x + patternXSize / 2 + 1;
        int yMin = y - patternYSize / 2;
        int yMax = y + patternYSize / 2 + 1;
        for (int i = 0; i < yMax - yMin; i++) {
            if (yMin + i < 0 || yMin + i >= mapSize)
                continue;
            for (int j = 0; j < xMax - xMin; j++) {
                if (xMin + j < 0 || xMin + j >= mapSize)
                    continue;
                if (pattern.get(i).get(j)) {
                    if (!kraken) {
                        dangerZone.get(yMin + i).set(xMin + j, pattern.get(i).get(j));
                        perceptionZone.get(yMin + i).set(xMin + j, pattern.get(i).get(j));
                    } else {
                        dangerZone.get(yMin + i).set(xMin + j, pattern.get(i).get(j));
                        krakenZone.get(yMin + i).set(xMin + j, pattern.get(i).get(j));
                    }
                }
            }
        }
    }

    /**
     * Gets the map tile at specified coordinate
     * @param x Horizontal coordinate
     * @param y Vertical coordinate
     * @return MapTile object
     */
    public MapTile getTileAtCoord(int x, int y) {
        return tiles.get(y).get(x);
    }

    /**
     * Gets the map tile at specified coordinate
     * @param coord The tile coordinate as a Point object
     * @return MapTile object
     */
    public MapTile getTileAtCoord(Point coord) {
        return getTileAtCoord(coord.x, coord.y);
    }

    /**
     * Gets the visualization of the map as a String
     * @param overlayPerception Whether to overlay the perception zones on the map or not
     * @param path The path to overlay on the map, null for no path overlay
     * @return String representation, including linebreaks
     */
    public String getStringVisualization(boolean overlayPerception, List<Point> path) {
        List<List<Character>> lines = new ArrayList<>(mapSize);
        for (int i = 0; i < mapSize; i++) {
            List<Character> curList = new ArrayList<Character>();
            for (int j = 0; j < mapSize; j++) {
                curList.add('.');
            }
            lines.add(curList);
        }
        if (overlayPerception) {
            for (int i = 0; i < mapSize; i++) {
                for (int j = 0; j < mapSize; j++) {
                    if (dangerZone.get(i).get(j)) {
                        lines.get(i).set(j, '!');
                    }
                }
            }
        }

        lines.get(captainLocation.y).set(captainLocation.x, '@');
        lines.get(davyLocation.y).set(davyLocation.x, 'D');
        lines.get(rockLocation.y).set(rockLocation.x, 'R');
        lines.get(krakenLocation.y).set(krakenLocation.x, 'K');
        lines.get(chestLocation.y).set(chestLocation.x, '#');
        lines.get(tortugaLocation.y).set(tortugaLocation.x, '$');

        if (path != null) {
            for (int i = 1; i < path.size() - 1; i++) {
                Point curPoint = path.get(i);
                Point prevPoint = path.get(i - 1);
                Point nextPoint = path.get(i + 1);
                int diffX = nextPoint.x - prevPoint.x;
                int diffY = nextPoint.y - prevPoint.y;
                if (diffX > 0 && diffY > 0 || diffX < 0 && diffY < 0) {
                    lines.get(curPoint.y).set(curPoint.x, '\\');
                }
                if (diffX > 0 && diffY < 0 || diffX < 0 && diffY > 0) {
                    lines.get(curPoint.y).set(curPoint.x, '/');
                }
                if (diffX == 0 && diffY != 0) {
                    lines.get(curPoint.y).set(curPoint.x, '|');
                }
                if (diffY == 0 && diffX != 0) {
                    lines.get(curPoint.y).set(curPoint.x, '-');
                }
            }
        }

        StringBuilder outStrBuilder = new StringBuilder();
        for (int i = 0; i < mapSize; i++) {
            for (int j = 0; j < mapSize; j++) {
                outStrBuilder.append(lines.get(i).get(j));
            }
            outStrBuilder.append('\n');
        }
        return outStrBuilder.toString();
    }
}

/**
 * Class to store the map input
 */
class MapInput {
    public int scenario;
    public Point captainCoord;
    public Point davyCoord;
    public Point krakenCoord;
    public Point rockCoord;
    public Point chestCoord;
    public Point tortugaCoord;
    MapInput() {};
    MapInput(int scenario, Point captainCoord, Point davyCoord, Point krakenCoord, Point rockCoord, Point chestCoord, Point tortugaCoord) {
        this.scenario = scenario;
        this.captainCoord = captainCoord;
        this.davyCoord = davyCoord;
        this.krakenCoord = krakenCoord;
        this.rockCoord = rockCoord;
        this.chestCoord = chestCoord;
        this.tortugaCoord = tortugaCoord;
    }
}

/**
 * Factory class for the Map object
 */
class MapFactory {
    /**
     * Generates a map from input, or a random map if input is null.
     * @param input Map input. If null, generates random map.
     * @return Valid map object if input is valid, random valid map object if input is null, null if input is invalid.
     */
    static public Map GenerateMap(MapInput input) {
        Random rand = new Random();  // for random generation
        boolean inputConstructed = input != null;
        if (!inputConstructed) {  // if no map input given, we make our own
            input = new MapInput();
            input.captainCoord = new Point(0, 0);  // we always start in the top left corner
        }
        Map map = new Map();
        map.getTileAtCoord(input.captainCoord).occupant = new Captain();
        map.captainLocation = input.captainCoord;

        while (!inputConstructed && (input.davyCoord == null || map.getTileAtCoord(input.davyCoord).occupant != null)) {
            input.davyCoord = new Point(rand.nextInt(Map.defaultSize), rand.nextInt(Map.defaultSize));
        }
        if (map.getTileAtCoord(input.davyCoord).occupant != null) {
            return null;
        }
        DavyJones davy = new DavyJones();
        map.getTileAtCoord(input.davyCoord).occupant = davy;
        map.davyLocation = input.davyCoord;
        map.applyPerceptionPattern(input.davyCoord.x, input.davyCoord.y, davy.getPerceptionZone(), false);

        while (!inputConstructed && (input.krakenCoord == null || map.getTileAtCoord(input.krakenCoord).occupant != null)) {
            input.krakenCoord = new Point(rand.nextInt(Map.defaultSize), rand.nextInt(Map.defaultSize));
        }
        if (map.getTileAtCoord(input.krakenCoord).occupant != null) {
            return null;
        }
        Kraken kraken = new Kraken();
        map.getTileAtCoord(input.krakenCoord).occupant = kraken;
        map.krakenLocation = input.krakenCoord;
        map.applyPerceptionPattern(input.krakenCoord.x, input.krakenCoord.y, kraken.getPerceptionZone(), true);

        while (!inputConstructed && (input.rockCoord == null || map.getTileAtCoord(input.rockCoord).occupant != null && map.getTileAtCoord(input.rockCoord).occupant != kraken)) {
            input.rockCoord = new Point(rand.nextInt(Map.defaultSize), rand.nextInt(Map.defaultSize));
        }
        if (map.getTileAtCoord(input.rockCoord).occupant != null && map.getTileAtCoord(input.rockCoord).occupant != kraken) {
            return null;  // kraken and rock can coexist
        }
        map.rockLocation = input.rockCoord;
        List<List<Boolean>> rockPattern = new ArrayList<>(1);
        rockPattern.add(new ArrayList<>(1));
        rockPattern.get(0).add(true);
        map.applyPerceptionPattern(input.rockCoord.x, input.rockCoord.y, rockPattern, false);

        while (!inputConstructed && (input.chestCoord == null || map.dangerZone.get(input.chestCoord.y).get(input.chestCoord.x) || map.getTileAtCoord(input.chestCoord).occupant != null)) {
            input.chestCoord = new Point(rand.nextInt(Map.defaultSize), rand.nextInt(Map.defaultSize));
        }
        if (map.dangerZone.get(input.chestCoord.y).get(input.chestCoord.x) || map.getTileAtCoord(input.chestCoord).occupant != null) {
            return null;  // chest cannot be in danger zone
        }
        Chest chest = new Chest();
        map.getTileAtCoord(input.chestCoord).occupant = chest;
        map.chestLocation = input.chestCoord;

        while (!inputConstructed && (input.tortugaCoord == null || map.dangerZone.get(input.tortugaCoord.y).get(input.tortugaCoord.x) || map.getTileAtCoord(input.tortugaCoord).occupant != null)) {
            input.tortugaCoord = new Point(rand.nextInt(Map.defaultSize), rand.nextInt(Map.defaultSize));
        }
        if (map.dangerZone.get(input.tortugaCoord.y).get(input.tortugaCoord.x) || map.getTileAtCoord(input.tortugaCoord).occupant != null) {
            return null;  // tortuga cannot be in danger zone
        }
        Tortuga tortuga = new Tortuga();
        map.getTileAtCoord(input.tortugaCoord).occupant = tortuga;
        map.tortugaLocation = input.tortugaCoord;

        return map;
    }
}

/**
 * Class which parses the map text input
 */
class InputParser {
    /**
     * Parses a single coordinate string of format [x,y]
     * @param input Coordinate string
     * @return Coordinate in string, null if coordinate string is invalid
     */
    static private Point parseCoord(String input) {
        Point outPoint = new Point();
        if (input.length() != 5 || input.charAt(0) != '[' || input.charAt(2) != ',' || input.charAt(4) != ']' ||
                !Character.isDigit(input.charAt(1)) || !Character.isDigit(input.charAt(3)) ||
                input.charAt(1) == '9' || input.charAt(3) == '9') {
            return null;
        }
        outPoint.y = Character.digit(input.charAt(1), 10);
        outPoint.x = Character.digit(input.charAt(3), 10);
        return outPoint;
    }

    /**
     * Parses lines of input.txt, with first line having coordinates of all the objects and second line having the scenario number
     * @param lines List of 2 or more Strings
     * @return MapInput object
     */
    static public MapInput parseLines(List<String> lines) {
        if (lines == null) {
            return null;
        }
        if (lines.size() < 2) {
            return null;
        }
        int scenario;

        if (lines.get(1).length() != 1 || lines.get(1).charAt(0) != '1' && lines.get(1).charAt(0) != '2') {
            return null;
        }
        scenario = Character.digit(lines.get(1).charAt(0), 10);
        String[] coordStrings = lines.get(0).split(" ");
        if (coordStrings.length != 6) {
            return null;
        }
        Point[] positions = new Point[6];
        for (int i = 0; i < 6; i++) {
            Point curPoint = parseCoord(coordStrings[i]);
            if (curPoint == null) {
                return null;
            }
            positions[i] = curPoint;
        }
        return new MapInput(scenario, positions[0], positions[1], positions[2], positions[3], positions[4], positions[5]);
    }
}

/**
 * Class which reads lines into a list from a specified file
 */
class FileLinesReader {
    String filename;
    FileLinesReader(String filename) {
        this.filename = filename;
    }
    FileLinesReader() {
        this("input.txt");
    }
    public List<String> getLines() {
        List<String> output = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                output.add(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return output;
    }
}

/**
 * Container class for the algorithm output, which is the algorithm runtime and the path from start to finish
 */
class AlgorithmOutput {
    double millisecondRuntime;
    List<Point> path;
    AlgorithmOutput(double millisecondRuntime, List<Point> path) {
        this.millisecondRuntime = millisecondRuntime;
        this.path = path;
    }
    AlgorithmOutput() {};
}

/**
 * Base class for pathfinding algorithms
 */
abstract class Algorithm {
    Map map;
    public int perceptionType;
    long timeStart;
    Algorithm(Map map, int perceptionType) {
        this.map = map;
        this.perceptionType = perceptionType;
    }

    /**
     * Gets the shortest path from Jack Sparrow to the Dead Man's Chest and the running time of the algorithm
     * @return Algorithm Output with timing and path
     */
    public AlgorithmOutput getPath() {
        AlgorithmOutput output = new AlgorithmOutput();
        timeStart = System.nanoTime();
        output.path = getPathBody();
        output.millisecondRuntime = (double)(System.nanoTime() - timeStart) / 1000000;
        return output;
    }

    /**
     * Gets the shortest path from Jack Sparrow to the Dead Man's Chest
     * @return List of points that are part of the path
     */
    abstract protected List<Point> getPathBody();
}

class AStarAlgorithm extends Algorithm {
    AStarAlgorithm(Map map, int perceptionType) {
        super(map, perceptionType);
    }

    /**
     * Gets the shortest path from Jack Sparrow to the Dead Man's Chest using A*
     * @return List of points that are part of the path
     */
    @Override
    protected List<Point> getPathBody() {
        return null;
    }
}

/**
 * Main class
 */
public class EvseyAntonovich {
    public static void main(String[] args) {
        int n;
        Map myMap;
        System.out.println("Enter 1 for input.txt input, enter 2 for random map generation");
        Scanner myScanner = new Scanner(System.in);
        n = myScanner.nextInt();
        MapInput myInput;
        List<Point> path = null;
        if (n == 1) {
            FileLinesReader reader = new FileLinesReader();
            myInput = InputParser.parseLines(reader.getLines());
            if (myInput == null) {
                System.out.println("Invalid input! Please enter valid input.");
            }
            path = new ArrayList<>();
            path.add(new Point(0, 0));
            path.add(new Point(0, 1));
            path.add(new Point(0, 2));
            path.add(new Point(1, 3));
            path.add(new Point(2, 4));
            path.add(new Point(3, 4));
            path.add(new Point(4, 4));
            path.add(new Point(5, 4));
            path.add(new Point(6, 5));
            path.add(new Point(7, 6));
            path.add(new Point(8, 7));
        } else {
            myInput = null;
        }
        myMap = MapFactory.GenerateMap(myInput);
        if (myMap == null) {
            System.out.println("Invalid map! Please restart program.");
            return;
        }
        System.out.println(myMap.getStringVisualization(true, null));
    }
}
