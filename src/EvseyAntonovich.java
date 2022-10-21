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

    public boolean equals(Point other) {
        return x == other.x && y == other.y;
    }

    Point sum(Point other) {
        return new Point(x + other.x, y + other.y);
    }

    Point mult(int coeff) {
        return new Point(x * coeff, y * coeff);
    }

    Point diff(Point other) {
        return sum(other.mult(-1));
    }

    Point div(int coeff) {
        return new Point(x / coeff, y / coeff);
    }

    int diagonalDistance(Point other, int costNormal, int costDiagonal) {
        int diffX = Math.abs(x - other.x);
        int diffY = Math.abs(y - other.y);
        return costNormal * (diffX + diffY) + (costDiagonal - 2 * costNormal) * Math.min(diffX, diffY);
    }

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
     * Fills the tiles 2D list with empty map tiles and generally initializes the map into a workable state.
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

    public MapTile getTileAtCoord(int x, int y) {
        return tiles.get(y).get(x);
    }

    public MapTile getTileAtCoord(Point coord) {
        return getTileAtCoord(coord.x, coord.y);
    }

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

class MapFactory {
    /**
     * Generates a map from input, or a random map if input is null.
     * @param input Map input. If null, generates random map.
     * @return Valid map object if input is valid, random valid map object if input is null, null if input is invalid.
     */
    static public Map GenerateMap(MapInput input) {
        Random rand = new Random();  // for random generation
        boolean inputConstructed = input != null;
        if (!inputConstructed) {
            input = new MapInput();
            input.captainCoord = new Point(0, 0);  // we always start in the top left corner
        }
        Map map = new Map();
        map.getTileAtCoord(input.captainCoord).occupant = new Captain();
        map.captainLocation = input.captainCoord;

        while (!inputConstructed && (input.davyCoord == null || map.getTileAtCoord(input.davyCoord).occupant != null)) {
            input.davyCoord = new Point(rand.nextInt(9), rand.nextInt(9));
        }
        if (map.getTileAtCoord(input.davyCoord).occupant != null) {
            return null;
        }
        DavyJones davy = new DavyJones();
        map.getTileAtCoord(input.davyCoord).occupant = davy;
        map.davyLocation = input.davyCoord;
        map.applyPerceptionPattern(input.davyCoord.x, input.davyCoord.y, davy.getPerceptionZone(), false);

        while (!inputConstructed && (input.krakenCoord == null || map.getTileAtCoord(input.krakenCoord).occupant != null)) {
            input.krakenCoord = new Point(rand.nextInt(9), rand.nextInt(9));
        }
        if (map.getTileAtCoord(input.krakenCoord).occupant != null) {
            return null;
        }
        Kraken kraken = new Kraken();
        map.getTileAtCoord(input.krakenCoord).occupant = kraken;
        map.krakenLocation = input.krakenCoord;
        map.applyPerceptionPattern(input.krakenCoord.x, input.krakenCoord.y, kraken.getPerceptionZone(), true);

        while (!inputConstructed && (input.rockCoord == null || map.getTileAtCoord(input.rockCoord).occupant != null && map.getTileAtCoord(input.rockCoord).occupant != kraken)) {
            input.rockCoord = new Point(rand.nextInt(9), rand.nextInt(9));
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
            input.chestCoord = new Point(rand.nextInt(9), rand.nextInt(9));
        }
        if (map.dangerZone.get(input.chestCoord.y).get(input.chestCoord.x) || map.getTileAtCoord(input.chestCoord).occupant != null) {
            return null;  // chest cannot be in danger zone
        }
        Chest chest = new Chest();
        map.getTileAtCoord(input.chestCoord).occupant = chest;
        map.chestLocation = input.chestCoord;

        while (!inputConstructed && (input.tortugaCoord == null || map.dangerZone.get(input.tortugaCoord.y).get(input.tortugaCoord.x) || map.getTileAtCoord(input.tortugaCoord).occupant != null)) {
            input.tortugaCoord = new Point(rand.nextInt(9), rand.nextInt(9));
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

class InputParser {
    static private Point parseCoord(String input) {
        Point outPoint = new Point();
        if (input.length() != 5 || input.charAt(0) != '[' || input.charAt(2) != ',' || input.charAt(4) != ']' ||
                !Character.isDigit(input.charAt(1)) || !Character.isDigit(input.charAt(3)) ||
                input.charAt(1) == '9' || input.charAt(3) == '9') {
            return null;
        }
        outPoint.x = Character.digit(input.charAt(1), 10);
        outPoint.y = Character.digit(input.charAt(3), 10);
        return outPoint;
    }
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
        System.out.println(myMap.getStringVisualization(true, path));
    }
}
