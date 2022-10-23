import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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
    Point(Point other) {
        this.x = other.x;
        this.y = other.y;
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
        return "[" + y + "," + x + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
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
    public String getStringVisualization(boolean overlayPerception, PathResult path) {
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

        char lastChar = '.';
        if (path != null) {
            for (int i = 1; i < path.path.size() - 1; i++) {
                Point curPoint = path.path.get(i);
                Point prevPoint = path.path.get(i - 1);
                Point nextPoint = path.path.get(i + 1);
                int diffX = nextPoint.x - prevPoint.x;
                int diffY = nextPoint.y - prevPoint.y;
                if (diffX > 0 && diffY > 0 || diffX < 0 && diffY < 0) {
                    lines.get(curPoint.y).set(curPoint.x, '⟍');
                }
                if (diffX > 0 && diffY < 0 || diffX < 0 && diffY > 0) {
                    lines.get(curPoint.y).set(curPoint.x, '⟋');
                }
                if (diffX == 0 && diffY != 0) {
                    lines.get(curPoint.y).set(curPoint.x, '|');
                }
                if (diffY == 0 && diffX != 0) {
                    lines.get(curPoint.y).set(curPoint.x, '—');
                }
                if (diffX == 0 && diffY == 0 && lastChar != '.') {
                    lines.get(curPoint.y).set(curPoint.x, lastChar);
                }
                lastChar = lines.get(curPoint.y).get(curPoint.x);
            }
        }

        StringBuilder outStrBuilder = new StringBuilder();
        for (int i = 0; i < mapSize; i++) {
            for (int j = 0; j < mapSize; j++) {
                outStrBuilder.append(lines.get(i).get(j));
                outStrBuilder.append(' ');
            }
            outStrBuilder.append('\n');
        }
        outStrBuilder.setLength(outStrBuilder.length() - 1);
        return outStrBuilder.toString();
    }

    /**
     * Gets the neighbors that you can move into from the point origin
     * @param origin Point to find the neighbors of
     * @param krakenPresent Whether the Kraken is alive or not
     * @return List of neighbor points
     */
    List<Point> getNeighbors(Point origin, boolean krakenPresent) {
        List<Point> neighbors = new ArrayList<>();
        for (int i = origin.y - 1; i <= origin.y + 1; i++) {
            if (i < 0 || i >= mapSize) {  // Checking if y is within bounds
                continue;
            }
            for (int j = origin.x - 1; j <= origin.x + 1; j++) {
                if (j < 0 || j >= mapSize) {  // Checking if x is within bounds
                    continue;
                }
                if (i == origin.y && j == origin.x) {  // Checking that we are not at the origin, as it is not a neighbour
                    continue;
                }
                if (perceptionZone.get(i).get(j) || krakenPresent && krakenZone.get(i).get(j)) {  // Checking that the point is not within a perception zone
                    continue;
                }
                Point neighbor = new Point();
                neighbor.x = j;  // Do this instead of using the constructor because we may swap the y and x arguments in the constructor at one point
                neighbor.y = i;
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    /**
     * Checks whether the specified point is within map bounds
     * @param point Point to check the validity of
     * @return True if point is within map bounds, false otherwise
     */
    boolean pointWithinBounds(Point point) {
        return point.x >= 0 && point.x < mapSize && point.y >= 0 && point.y < mapSize;
    }

    /**
     * Checks whether the specified point is on a perception zone or not
     * @param point Point to check
     * @param krakenPresent Whether the kraken is alive or not
     * @return True if point is on a perception zone, false otherwise
     */
    boolean pointOnPerceptionZone(Point point, boolean krakenPresent) {
        return perceptionZone.get(point.y).get(point.x) || krakenPresent && krakenZone.get(point.y).get(point.x);
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
    MapInput() {}
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
            input.captainCoord = new Point(rand.nextInt(Map.defaultSize), rand.nextInt(Map.defaultSize));
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

        while (!inputConstructed && (input.tortugaCoord == null || map.dangerZone.get(input.tortugaCoord.y).get(input.tortugaCoord.x) || map.getTileAtCoord(input.tortugaCoord).occupant == chest)) {
            input.tortugaCoord = new Point(rand.nextInt(Map.defaultSize), rand.nextInt(Map.defaultSize));
        }
        if (map.dangerZone.get(input.tortugaCoord.y).get(input.tortugaCoord.x) || map.getTileAtCoord(input.tortugaCoord).occupant == chest) {
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

    /**
     * Gets a list of lines from the specified filename
     * @return List of lines
     */
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
    Map map;
    double millisecondRuntime;
    PathResult path;
    AlgorithmOutput(double millisecondRuntime, PathResult path, Map map) {
        this.millisecondRuntime = millisecondRuntime;
        this.path = path;
        this.map = map;
    }
    AlgorithmOutput(double millisecondRuntime, PathResult path) {
        this(millisecondRuntime, path, null);
    }
    AlgorithmOutput() {}
}

/**
 * Class for the result of a pathfinding algorithm (start, end and the path itself)
 */
class PathResult {
    public Point start;
    public Point end;  // We need this as the path can end *somewhere* near the goal
    public Point goal;
    public List<Point> path;

    @Override
    public String toString() {
        StringBuilder outStr = new StringBuilder();
        for (Point p : path) {
            outStr.append(p.toString());
            outStr.append(' ');
        }
        outStr.setLength(outStr.length() - 1);
        return outStr.toString();
    }
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
        output.map = map;
        return output;
    }

    /**
     * Gets the shortest path from Jack Sparrow to the Dead Man's Chest
     * @return PathResult that contains the list of points that are part of the path
     */
    abstract protected PathResult getPathBody();
}

/**
 * Class for the A* algorithm
 */
class AStarAlgorithm extends Algorithm {
    /**
     * Class for a single A* grid tile
     */
    class AStarTile {
        int g = -1;
        int h = -1;
        int f = -1;
        Point location = null;
        AStarTile parent = null;
        AStarTile(Point location) {
            this.location = location;
        }
        AStarTile() {}

        /**
         * Updates the costs of an A* tile
         * @param neighbor The neighbor from which the cost is being updated
         * @param cost Cost of movement to this tile
         * @param target Goal point for computing h cost
         */
        public void updateCosts(AStarTile neighbor, int cost, Point target) {
            h = location.diagonalDistance(target, 1, 1);
            if (neighbor != null) {
                if (g == -1 || neighbor.g + cost < g) {
                    g = neighbor.g + cost;
                    parent = neighbor;
                }
            } else {
                g = 0;
            }
            f = g + h;
        }
    }
    AStarAlgorithm(Map map, int perceptionType) {
        super(map, perceptionType);
    }

    boolean krakenDiscovered = false;

    /**
     * Gets the shortest path from Jack Sparrow to the Dead Man's Chest using A*.
     * First, it gets the shortest path from Jack Sparrow to the chest, without making a detour to Tortuga Island.
     * Then, it calculates a path to Tortuga Island, if the Kraken has been discovered during the first path.
     * Afterwards, it calculates four paths - from Tortuga to each of the corners near the Kraken.
     * Afterwards, for each of the valid paths generated to the corners, it generates another path - from that corner to the chest.
     * Afterwards, it concatenates all the intermediate paths, if they are valid (not null), and gets the minimum path.
     * If this minimum path is shorter than the standard non-Tortuga path, or if there is NO non-Tortuga path, it instead returns the Tortuga path.
     * Otherwise, it returns the standard non-Tortuga path, or no path at all, if the goal is completely unreachable.
     * @return PathResult that contains the list of points that are part of the path
     */
    @Override
    protected PathResult getPathBody() {
        PathResult shortestPath = getPathBetweenPoints(map.captainLocation, map.chestLocation, 0, true);
        if (krakenDiscovered) {
            PathResult toTortuga = getPathBetweenPoints(map.captainLocation, map.tortugaLocation, 0, true);
            if (toTortuga == null) {
                return shortestPath;
            }
            toTortuga.path.remove(toTortuga.path.size() - 1);
            List<Point> krakenPoints = Arrays.asList(
                    map.krakenLocation.sum(new Point(-1, -1)),
                    map.krakenLocation.sum(new Point(1, -1)),
                    map.krakenLocation.sum(new Point(1, 1)),
                    map.krakenLocation.sum(new Point(-1, 1)));
            List<PathResult> toKrakenPaths = new ArrayList<>(4);
            for (int i = 0; i < 4; i++) {
                Point curKrakenPoint = krakenPoints.get(i);
                if (map.pointWithinBounds(curKrakenPoint) && !map.pointOnPerceptionZone(curKrakenPoint, true)) {
                    PathResult curPath = getPathBetweenPoints(map.tortugaLocation, krakenPoints.get(i), 0, true);
                    if (curPath != null) {
                        curPath.path.remove(curPath.path.size() - 1);
                        toKrakenPaths.add(curPath);
                    }
                }
            }
            List<PathResult> totalPaths = new ArrayList<>(4);
            for (PathResult toKrakenPath : toKrakenPaths) {
                Point curKrakenPoint = toKrakenPath.end;
                PathResult curPathAfter = getPathBetweenPoints(curKrakenPoint, map.chestLocation, 0, false);
                if (curPathAfter != null) {
                    PathResult totalPath = new PathResult();
                    totalPath.start = map.captainLocation;
                    totalPath.end = map.chestLocation;
                    totalPath.goal = map.chestLocation;
                    List<Point> curActualPath = new ArrayList<>();
                    curActualPath.addAll(toTortuga.path);
                    curActualPath.addAll(toKrakenPath.path);
                    curActualPath.addAll(curPathAfter.path);
                    totalPath.path = curActualPath;
                    totalPaths.add(totalPath);
                }
            }
            PathResult minPath = null;
            for (PathResult curPath : totalPaths) {
                if (curPath != null && (minPath == null || curPath.path.size() < minPath.path.size())) {
                    minPath = curPath;
                }
            }
            if (minPath != null && (shortestPath == null || minPath.path.size() < shortestPath.path.size())) {
                shortestPath = minPath;
            }
        }
        return shortestPath;
    }

    /**
     * Gets path between point start and point end, with distance being the distance between the possible end point and actual goal
     * @param start Starting point
     * @param end Goal/end point
     * @param distance Diagonal distance to the end point, reaching which the algorithm stops
     * @param krakenPresent Whether to consider the Kraken alive or not
     * @return PathResult containing the path and start, end, goal points, null if no path found
     */
    protected PathResult getPathBetweenPoints(Point start, Point end, int distance, boolean krakenPresent) {
        PathResult result = new PathResult();
        result.start = start;
        result.goal = end;
        List<Point> path = new ArrayList<>();
        Set<AStarTile> closed = new HashSet<>();
        List<AStarTile> open = new ArrayList<>();
        List<List<AStarTile>> tiles = new ArrayList<>(map.mapSize);
        for (int i = 0; i < map.mapSize; i++) {
            List<AStarTile> curList = new ArrayList<>(map.mapSize);
            for (int j = 0; j < map.mapSize; j++) {
                curList.add(new AStarTile(new Point(j, i)));
            }
            tiles.add(curList);
        }
        open.add(tiles.get(start.y).get(start.x));
        open.get(0).updateCosts(null, 0, end);
        while (open.size() > 0) {
            AStarTile current = null;
            int chosenI = -1;
            for (int i = 0; i < open.size(); i++) {  // Pick tile with the lowest cost
                AStarTile curOpen = open.get(i);
                if (current == null || open.get(i).f < current.f ||
                        curOpen.f == current.f && curOpen.h < current.h ||
                        curOpen.f == current.f && curOpen.h == current.h && curOpen.location.manhattanDistance(end, 1) < current.location.manhattanDistance(end, 1)) {
                    // First we check if the total cost is less, then, we check if the total cost is equal but the heuristic cost is smaller (so it's closer).
                    // Then, if both total cost and equal cost are equal, we check if the manhattan distance is smaller than the current manhattan distance.
                    // As manhattan distance is a direct difference of coordinates, this will prioritize paths that try to minimize the coordinate difference
                    // instead of paths that may also be the shortest, but look slightly less logical, due to Chebyshev cost heuristics.
                    chosenI = i;
                    current = open.get(i);
                }
            }
            open.remove(chosenI);
            Point curLoc = current.location;
            if (perceptionType == 1 && curLoc.diagonalDistance(map.krakenLocation, 1, 1) == 1 || perceptionType == 2 && curLoc.manhattanDistance(map.krakenLocation, 1) <= 2) {
                krakenDiscovered = true;
            }
            if (curLoc.diagonalDistance(end, 1, 1) == distance) {  // We are close enough to the goal to finish
                List<Point> outputPath = new ArrayList<>();
                result.end = curLoc;
                AStarTile curTile = current;
                while (curTile != null) {
                    outputPath.add(0, curTile.location);
                    curTile = curTile.parent;
                }
                result.path = outputPath;
                return result;
            }
            closed.add(current);
            List<Point> pointNeighbors = map.getNeighbors(curLoc, krakenPresent);
            for (Point pointNeighbor : pointNeighbors) {
                AStarTile neighborTile = tiles.get(pointNeighbor.y).get(pointNeighbor.x);
                if (closed.contains(neighborTile)) {
                    continue;
                }
                neighborTile.updateCosts(current, 1, end);
                if (!open.contains(neighborTile)) {
                    open.add(neighborTile);
                }
            }
        }
        return null;
    }
}

/**
 * A class to implement a visit tree structure, so that we don't have to copy the visited nodes each time
 */
class VisitTree {
    VisitTree parent = null;
    Point curElem = null;
    VisitTree(Point elem) {
        curElem = elem;
    }

    /**
     * Adds an element to the tree
     * @param elem Element to add
     * @param parent Parent node
     */
    VisitTree(Point elem, VisitTree parent) {
        curElem = elem;
        this.parent = parent;
    }

    /**
     * Check if element is in the tree
     * @param elem The element to check for
     * @return True if element is in tree, false otherwise
     */
    public boolean isInTree(Point elem) {
        VisitTree curNode = this;
        while (curNode != null) {
            if (curNode.curElem.equals(elem)) {
                return true;
            }
            curNode = curNode.parent;
        }
        return false;
    }
}

/**
 * Class for the Backtracking Algorithm
 */
class BacktrackingAlgorithm extends Algorithm {
    /**
     * Class which stores the current game state and possible next game states
     */
    class GameState {
        Point curLoc = null;
        Point goal = null;
        VisitTree visitedBeforeTortuga;
        VisitTree visitedAfterTortuga;
        boolean tortugaVisited = false;
        boolean krakenAlive = true;
        boolean davyDiscovered = false;
        boolean krakenDiscovered = false;
        boolean rockDiscovered = false;
        GameState parent = null;
        PriorityQueue<GameState> nextPossibleStates = new PriorityQueue<>(8, new Comparator<GameState>() {
            @Override
            public int compare(GameState o1, GameState o2) {
                // First compare diagonal distance to goal, then compare manhattan distance if diagonal distance is equal
                if (o1.curLoc.diagonalDistance(o1.goal, 1, 1) < o2.curLoc.diagonalDistance(o2.goal, 1, 1)) {
                    return -1;
                } else if (o1.curLoc.diagonalDistance(o1.goal, 1, 1) == o2.curLoc.diagonalDistance(o2.goal, 1, 1)) {
                    if (o1.curLoc.manhattanDistance(o1.goal, 1) < o2.curLoc.manhattanDistance(o2.goal, 1)) {
                        return -1;
                    } else if (o1.curLoc.manhattanDistance(o1.goal, 1) == o2.curLoc.manhattanDistance(o2.goal, 1)) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    return 1;
                }
            }
        });
        int iteration = 1;

        GameState(Point startLoc, Point goal) {
            curLoc = new Point(startLoc);
            this.goal = new Point(goal);
            visitedBeforeTortuga = new VisitTree(startLoc);
        }

        /**
         * Copies the previos game state but with new location
         * @param previousState The previous state
         * @param newLoc New location
         */
        GameState(GameState previousState, Point newLoc) {
            curLoc = new Point(newLoc);
            goal = previousState.goal;
            visitedBeforeTortuga = previousState.visitedBeforeTortuga;
            visitedAfterTortuga = previousState.visitedAfterTortuga;
            tortugaVisited = previousState.tortugaVisited;
            krakenAlive = previousState.krakenAlive;
            davyDiscovered = previousState.davyDiscovered;
            krakenDiscovered = previousState.krakenDiscovered;
            rockDiscovered = previousState.rockDiscovered;
            parent = previousState;
            iteration = previousState.iteration + 1;
        }
    }
    Set<Point> visitedTotal = new HashSet<>();
    PathResult shortestPath = null;
    List<List<Integer>> shortestIterationBeforeTortuga = new ArrayList<>();
    List<List<Integer>> shortestIterationAfterTortuga = new ArrayList<>();
    BacktrackingAlgorithm(Map map, int perceptionType) {
        super(map, perceptionType);
        for (int i = 0; i < map.mapSize; i++) {
            List<Integer> list1 = new ArrayList<Integer>();
            List<Integer> list2 = new ArrayList<Integer>();
            for (int j = 0; j < map.mapSize; j++) {
                list1.add(-1);
                list2.add(-1);
            }
            shortestIterationBeforeTortuga.add(list1);
            shortestIterationAfterTortuga.add(list2);
        }
    }

    /**
     * Gets the shortest path from Jack Sparrow to the Dead Man's Chest using backtracking with heuristics.
     * @return PathResult that contains the list of points that are part of the path
     */
    @Override
    protected PathResult getPathBody() {
        GameState initialState = new GameState(map.captainLocation, map.tortugaLocation);
        exploreFutureGameStates(initialState);
        return shortestPath;
    }

    /**
     * Explores many possible future game states to get the shortest path, and puts the path into shortestPath
     * @param state Game state to explore future game states of
     */
    void exploreFutureGameStates(GameState state) {
        if (state.curLoc.equals(map.chestLocation)) {
            // We have discovered the exit
            PathResult result = new PathResult();
            result.start = map.captainLocation;
            result.goal = map.chestLocation;
            result.end = map.chestLocation;
            List<Point> outputPath = new ArrayList<>();
            GameState curState = state;
            while (curState != null) {
                outputPath.add(0, curState.curLoc);
                curState = curState.parent;
            }
            result.path = outputPath;
            if (shortestPath == null || result.path.size() < shortestPath.path.size()) {
                shortestPath = result;
            }
            return;
        }

        if (shortestPath != null && state.iteration >= shortestPath.path.size()) {
            return;  // Our iteration is already the length of the shortest path, and we have not found the exit yet, therefore, this is definitely not the shortest path
        }
        if (state.iteration > 25) {  // ENOUGH
            return;
        }

        // We found Tortuga
        if (state.curLoc.equals(map.tortugaLocation)) {
            state.tortugaVisited = true;
            state.visitedAfterTortuga = new VisitTree(state.curLoc);
            state.goal = map.chestLocation;
        }

        // In this block, we make sure that we don't go to a cell which we know we can get to in a shorter way already
        int curShortBeforeTortugaIter = shortestIterationBeforeTortuga.get(state.curLoc.y).get(state.curLoc.x);
        int curShortAfterTortugaIter = shortestIterationAfterTortuga.get(state.curLoc.y).get(state.curLoc.x);
        if (!state.tortugaVisited && curShortBeforeTortugaIter != -1 && state.iteration > curShortBeforeTortugaIter ||
                state.tortugaVisited && curShortAfterTortugaIter != -1 && state.iteration > curShortAfterTortugaIter) {
            return;
        } else {
            if (!state.tortugaVisited) {
                shortestIterationBeforeTortuga.get(state.curLoc.y).set(state.curLoc.x, state.iteration);
            } else {
                shortestIterationAfterTortuga.get(state.curLoc.y).set(state.curLoc.x, state.iteration);
            }
        }

        if (!state.tortugaVisited) {  // We separate the visited cells into two sets, because after visiting Tortuga we can move into previously visited cells
            if (!state.visitedBeforeTortuga.isInTree(state.curLoc))
                state.visitedBeforeTortuga = new VisitTree(state.curLoc, state.visitedBeforeTortuga);
        } else {
            if (!state.visitedAfterTortuga.isInTree(state.curLoc))
                state.visitedAfterTortuga = new VisitTree(state.curLoc, state.visitedAfterTortuga);
        }


        if (perceptionType == 1 && state.curLoc.diagonalDistance(map.krakenLocation, 1, 1) == 1 || perceptionType == 2 && state.curLoc.manhattanDistance(map.krakenLocation, 1) <= 2) {
            state.krakenDiscovered = true;
        }
        // Commenting this out as it is not used anywhere
        /*if (perceptionType == 1 && state.curLoc.diagonalDistance(map.davyLocation, 1, 1) == 1 || perceptionType == 2 && state.curLoc.manhattanDistance(map.davyLocation, 1) <= 2) {
            state.davyDiscovered = true;
        }
        if (perceptionType == 1 && state.curLoc.diagonalDistance(map.rockLocation, 1, 1) == 1 || perceptionType == 2 && state.curLoc.manhattanDistance(map.rockLocation, 1) <= 2) {
            state.rockDiscovered = true;
        }*/

        if (state.tortugaVisited && state.curLoc.diagonalDistance(map.krakenLocation, 1, 1) == 1) {
            state.krakenAlive = false;
        }

        List<Point> pointNeighbors = map.getNeighbors(state.curLoc, state.krakenAlive);
        for (Point pointNeighbor : pointNeighbors) {
            if ((!state.tortugaVisited && state.visitedBeforeTortuga.isInTree(pointNeighbor) || state.tortugaVisited && state.visitedAfterTortuga.isInTree(pointNeighbor))) {
                continue;  // Already visited this point, don't go there again
            }
            if (shortestPath != null && pointNeighbor.diagonalDistance(map.chestLocation, 1, 1) >= shortestPath.path.size()) {
                continue;  // The point to be explored is further away from the chest location than the length of the shortest path, therefore, we will not get the shortest path if we go there
            }
            state.nextPossibleStates.add(new GameState(state, pointNeighbor));
        }
        while (!state.nextPossibleStates.isEmpty()) {
            GameState stateToExplore = state.nextPossibleStates.poll();
            exploreFutureGameStates(stateToExplore);
        }
    }
}

/**
 * Class for generating a statistical analysis report on the algorithms
 */
class StatisticalAnalysisReport {
    /**
     * A part of the statistical analysis report
     */
    class StatisticalAnalysisReportPart {
        List<AlgorithmOutput> unsortedOutput = new ArrayList<>();
        List<AlgorithmOutput> sortedOutput = new ArrayList<>();
        AlgorithmOutput minimumExecutionTimeRun;
        AlgorithmOutput maximumExecutionTimeRun;
        List<Map> lossMaps = new ArrayList<>();
        int n = 0;
        int losses = 0;
        int wins = 0;
        double executionTimeMean;
        double executionTimeMedian;
        String executionTimeMode;
        int executionTimeModeFrequency;
        double executionTimeStandardDeviation;

        /**
         * Generates a report for the given algorithm outputs
         * @param runs Algorithm outputs for different map runs
         */
        void generateReport(List<AlgorithmOutput> runs) {
            unsortedOutput = new ArrayList<>(runs);
            sortedOutput = unsortedOutput.stream().sorted(Comparator.comparingDouble(o -> o.millisecondRuntime)).toList();
            minimumExecutionTimeRun = sortedOutput.get(0);
            maximumExecutionTimeRun = sortedOutput.get(sortedOutput.size() - 1);

            n = sortedOutput.size();
            double sum = 0;
            Hashtable<String, Integer> timeFrequencies = new Hashtable<>();
            DecimalFormat df = new DecimalFormat("#.######");
            df.setRoundingMode(RoundingMode.CEILING);

            for (AlgorithmOutput output : unsortedOutput) {
                sum += output.millisecondRuntime;
                String curTime = df.format(output.millisecondRuntime);
                timeFrequencies.put(curTime, timeFrequencies.getOrDefault(curTime, 0) + 1);
                if (output.path == null) {
                    losses += 1;
                    lossMaps.add(output.map);
                } else {
                    wins += 1;
                }
            }

            executionTimeMean = sum / sortedOutput.size();
            if (sortedOutput.size() % 2 == 1) {
                executionTimeMedian = sortedOutput.get((n + 1) / 2).millisecondRuntime;
            } else {
                executionTimeMedian = (sortedOutput.get(n / 2).millisecondRuntime + sortedOutput.get(n / 2 + 1).millisecondRuntime) / 2;
            }

            String maxKey = "-1";
            int maxValue = -1;
            for (String key : timeFrequencies.keySet()) {
                int curValue = timeFrequencies.get(key);
                if (curValue > maxValue) {
                    maxValue = curValue;
                    maxKey = key;
                }
            }
            executionTimeMode = maxKey;
            executionTimeModeFrequency = maxValue;

            double squaredMeanDifferenceSum = 0;
            for (AlgorithmOutput output : unsortedOutput) {
                squaredMeanDifferenceSum += (output.millisecondRuntime - executionTimeMean) * (output.millisecondRuntime - executionTimeMean);
            }
            executionTimeStandardDeviation = Math.sqrt(squaredMeanDifferenceSum / (n - 1));
        }
    }
    int n;
    List<Map> maps = new ArrayList<>();
    StatisticalAnalysisReportPart backtrackingVariantOneReport = new StatisticalAnalysisReportPart();
    StatisticalAnalysisReportPart backtrackingVariantTwoReport = new StatisticalAnalysisReportPart();
    StatisticalAnalysisReportPart AStarVariantOneReport = new StatisticalAnalysisReportPart();
    StatisticalAnalysisReportPart AStarVariantTwoReport = new StatisticalAnalysisReportPart();

    StatisticalAnalysisReport(int n) {
        this.n = n;
    }
    StatisticalAnalysisReport() {
        this(1000);
    }

    /**
     * Generates a full report for the n given
     */
    void generateFullReport() {
        List<AlgorithmOutput> backtrackingVariantOneRuns = new ArrayList<>();
        List<AlgorithmOutput> backtrackingVariantTwoRuns = new ArrayList<>();
        List<AlgorithmOutput> AStarVariantOneRuns = new ArrayList<>();
        List<AlgorithmOutput> AStarVariantTwoRuns = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Map curMap = MapFactory.GenerateMap(null);
            maps.add(curMap);
            AStarAlgorithm astarAlgoV1 = new AStarAlgorithm(curMap, 1);
            AStarAlgorithm astarAlgoV2 = new AStarAlgorithm(curMap, 2);
            BacktrackingAlgorithm backtrackAlgoV1 = new BacktrackingAlgorithm(curMap, 1);
            BacktrackingAlgorithm backtrackAlgoV2 = new BacktrackingAlgorithm(curMap, 2);
            AlgorithmOutput outputAStarV1 = astarAlgoV1.getPath();
            AlgorithmOutput outputAStarV2 = astarAlgoV2.getPath();
            AlgorithmOutput outputBacktrackV1 = backtrackAlgoV1.getPath();
            AlgorithmOutput outputBacktrackV2 = backtrackAlgoV2.getPath();
            backtrackingVariantOneRuns.add(outputBacktrackV1);
            backtrackingVariantTwoRuns.add(outputBacktrackV2);
            AStarVariantOneRuns.add(outputAStarV1);
            AStarVariantTwoRuns.add(outputAStarV2);
            /*
            AlgorithmResultFormatter formatter = new AlgorithmResultFormatter(curMap, outputAStarV1);
            System.out.println(formatter.makeString());
            System.out.println("------------------");
             */
        }

        backtrackingVariantOneReport.generateReport(backtrackingVariantOneRuns);
        backtrackingVariantTwoReport.generateReport(backtrackingVariantTwoRuns);
        AStarVariantOneReport.generateReport(AStarVariantOneRuns);
        AStarVariantTwoReport.generateReport(AStarVariantTwoRuns);
    }
}

/**
 * Class to format the algorithm results
 */
class AlgorithmResultFormatter {
    Map map;
    AlgorithmOutput algorithmOutput;
    AlgorithmResultFormatter(Map map, AlgorithmOutput algorithmOutput) {
        this.map = map;
        this.algorithmOutput = algorithmOutput;
    }

    /**
     * Makes a string that is valid output for the algorithm, including Win/Lose, shortest distance, the shortest path itself, and path visualized on the map
     * @return The string
     */
    String makeString() {
        StringBuilder outStr = new StringBuilder();
        if (algorithmOutput.path != null) {
            outStr.append("Win\n");
            outStr.append(algorithmOutput.path.path.size() - 1);
            outStr.append('\n');
            outStr.append(algorithmOutput.path);
            outStr.append('\n');
            outStr.append(map.getStringVisualization(true, algorithmOutput.path));
            outStr.append('\n');
            outStr.append(algorithmOutput.millisecondRuntime).append(" ms\n");
        } else {
            outStr.append("Lose\n");
        }
        return outStr.toString();
    }
}

/**
 * Class to format the statistical analysis report
 */
class StatisticalAnalysisReportFormatter {
    /**
     * Class to format a part of the statistical analysis report
     */
    class StatisticalAnalysisReportPartFormatter {
        StatisticalAnalysisReport.StatisticalAnalysisReportPart part;
        public StatisticalAnalysisReportPartFormatter(StatisticalAnalysisReport.StatisticalAnalysisReportPart part) {
            this.part = part;
        }

        /**
         * Generates a statistics report string for this report part
         * @return Report string
         */
        String makeString() {
            StringBuilder outStr = new StringBuilder();
            outStr.append("Execution time mean: ").append(part.executionTimeMean).append("ms\n");
            outStr.append("Execution time mode: ").append(part.executionTimeMode).append("ms\n");
            outStr.append("Execution time median: ").append(part.executionTimeMedian).append("ms\n");
            outStr.append("Execution time standard deviation: ").append(part.executionTimeStandardDeviation).append("ms\n");
            outStr.append("Wins: ").append(part.wins).append("\n");
            outStr.append("Losses: ").append(part.losses).append("\n");
            outStr.append("Percentage of wins: ").append((double)part.wins / (double)part.n * 100).append("%\n");
            outStr.append("Percentage of losses: ").append((double)part.losses / (double)part.n * 100).append("%\n");
            return outStr.toString();
        }
    }
    StatisticalAnalysisReport report;
    public StatisticalAnalysisReportFormatter(StatisticalAnalysisReport report) {
        this.report = report;
    }
    /**
     * Generates a statistics report string
     * @return Report string
     */
    String makeString() {
        StatisticalAnalysisReportPartFormatter AStarV1Formatter = new StatisticalAnalysisReportPartFormatter(report.AStarVariantOneReport);
        StatisticalAnalysisReportPartFormatter AStarV2Formatter = new StatisticalAnalysisReportPartFormatter(report.AStarVariantTwoReport);
        StatisticalAnalysisReportPartFormatter backtrackingV1Formatter = new StatisticalAnalysisReportPartFormatter(report.backtrackingVariantOneReport);
        StatisticalAnalysisReportPartFormatter backtrackingV2Formatter = new StatisticalAnalysisReportPartFormatter(report.backtrackingVariantTwoReport);
        StringBuilder outStr = new StringBuilder();
        outStr.append("For ").append(report.n).append(" runs:\n");
        outStr.append("A* Variant 1:\n").append(AStarV1Formatter.makeString()).append('\n');
        outStr.append("A* Variant 2:\n").append(AStarV2Formatter.makeString()).append('\n');
        outStr.append("Backtracking Variant 1:\n").append(backtrackingV1Formatter.makeString()).append('\n');
        outStr.append("Backtracking Variant 2:\n").append(backtrackingV2Formatter.makeString()).append('\n');
        return outStr.toString();
    }
}

/**
 * Main class
 */
public class EvseyAntonovich {
    public static void main(String[] args) {
        int n;
        Map myMap;
        System.out.println("Enter 1 for input.txt input, enter 2 for random map generation, and enter 3 to generate a statistical report.");
        Scanner myScanner = new Scanner(System.in);
        n = myScanner.nextInt();
        int perceptionType = 1;
        MapInput myInput;
        List<Point> path = null;
        if (n == 1) {
            FileLinesReader reader = new FileLinesReader("input5.txt");
            myInput = InputParser.parseLines(reader.getLines());
            if (myInput == null) {
                System.out.println("Invalid input! Please enter valid input.");
                return;
            } else {
                perceptionType = myInput.scenario;
            }
        } else if (n == 2) {
            myInput = null;
        } else {
            int runs = 1000;
            StatisticalAnalysisReport report = new StatisticalAnalysisReport(runs);
            report.generateFullReport();
            StatisticalAnalysisReportFormatter formatter = new StatisticalAnalysisReportFormatter(report);
            String reportString = formatter.makeString();
            try (PrintWriter reportWriter = new PrintWriter("statisticalAnalysisReport.txt")) {
                reportWriter.print(reportString);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            System.out.println(reportString);

            StringBuilder lossMapStr = new StringBuilder();
            for (Map lossMap : report.AStarVariantOneReport.lossMaps) {
                lossMapStr.append(lossMap.getStringVisualization(true, null)).append("\n-----------------\n");
            }

            try (PrintWriter lossWriter = new PrintWriter("lossMaps.txt")) {
                lossWriter.print(lossMapStr.toString());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        myMap = MapFactory.GenerateMap(myInput);
        if (myMap == null) {
            System.out.println("Invalid map! Please restart program.");
            return;
        }
        AStarAlgorithm astarAlgo = new AStarAlgorithm(myMap, perceptionType);
        AlgorithmOutput outputA = astarAlgo.getPath();
        AlgorithmResultFormatter formatterAstar = new AlgorithmResultFormatter(myMap, outputA);
        try (PrintWriter astarWriter = new PrintWriter("outputAStar.txt")) {
            astarWriter.print(formatterAstar.makeString());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println(formatterAstar.makeString());
        System.out.println();

        BacktrackingAlgorithm backtrackAlgo = new BacktrackingAlgorithm(myMap, perceptionType);
        AlgorithmOutput outputB;
        if (outputA.path != null) {
            outputB = backtrackAlgo.getPath();
        } else {  // No point in running backtracking if we know there is no solution
            outputB = new AlgorithmOutput();
            outputB.millisecondRuntime = 1;
            outputB.path = null;
        }
        AlgorithmResultFormatter formatterBacktracking = new AlgorithmResultFormatter(myMap, outputB);
        try (PrintWriter backtrackingWriter = new PrintWriter("outputBacktracking.txt")) {
            backtrackingWriter.print(formatterBacktracking.makeString());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println(formatterBacktracking.makeString());
    }
}
