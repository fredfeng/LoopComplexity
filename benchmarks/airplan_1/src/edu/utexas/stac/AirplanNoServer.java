package edu.utexas.stac;

import edu.cyberapex.flightplanner.AirFailure;
import edu.cyberapex.flightplanner.TextFileLoader;
import edu.cyberapex.flightplanner.framework.Airline;
import edu.cyberapex.flightplanner.framework.Airport;
import edu.cyberapex.flightplanner.framework.FlightWeightType;
import edu.cyberapex.flightplanner.framework.RouteMap;
import edu.cyberapex.flightplanner.guide.OptimalPathGuide;
import edu.cyberapex.flightplanner.store.AirDatabase;
import edu.cyberapex.home.AirPlanLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

public class AirplanNoServer {
    private static final String MAPDB_FILE = "airplan.db";
    private static final String AIRLINE_ID = "test";
    private static final String AIRLINE_PASSWD = "test";
    private static final String MAP_NAME = "TheMap";

    private static AirDatabase getDatabase(String dataPath) throws IOException {
        File parent = new File(dataPath);
        if (!parent.exists() || !parent.isDirectory() || !parent.canWrite()) {
            throw new IllegalArgumentException("Parent directory " + parent + " must exist, be a directory, and be " +
                    "writable");
        }
        File dbFile = new File(parent, MAPDB_FILE);

        AirDatabase airDatabase = new AirDatabase(dbFile, new Random(0));
        AirPlanLoader.populate(airDatabase, AIRLINE_PASSWD);
        return airDatabase;
    }

    private static RouteMap uploadRouteMap(AirDatabase database, String inputFileName) throws
            FileNotFoundException,
            AirFailure {
        Airline airline = database.obtainAirline(AIRLINE_ID);
        RouteMap routeMap = new TextFileLoader().loadRouteMap(inputFileName, database);
        routeMap.fixName(MAP_NAME);
        airline.addRouteMap(routeMap);
        database.commit();

        return routeMap;
    }

    private static void calculateShortestPath(RouteMap routeMap, String originName, String destName) throws AirFailure {
        Airport origin = routeMap.obtainAirport(originName);
        if (origin == null)
            throw new RuntimeException("Cannot find origin airport");
        Airport dest = routeMap.obtainAirport(destName);
        if (dest == null)
            throw new RuntimeException("Cannot find destination airport");
        OptimalPathGuide.pullContentsPublic(routeMap, origin, dest, FlightWeightType.COST, FlightWeightType.COST
                .takeDescription());
    }

    public static void run(String dataPath, String routeMapFile, String originName, String destName) {
        AirDatabase db = null;
        try {
            db = getDatabase(dataPath);
            RouteMap map = uploadRouteMap(db, routeMapFile);
            calculateShortestPath(map, originName, destName);
        } catch (Exception e) {
            System.err.println("Exception thrown: " + e);
        } finally {
            if (db != null)
                db.close();
        }
    }

    public static void main(String args[]) {
        if (args.length < 4) {
            System.out.println("Usage: airplan [dataPath] [routeMapFile] [originName] [destName]");
            System.exit(0);
        }

        run(args[0], args[1], args[2], args[3]);
    }
}
