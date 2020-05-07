package edu.utexas.stac;

import net.cybertip.home.AirPlanLoader;
import net.cybertip.routing.AirTrouble;
import net.cybertip.routing.TextFileLoader;
import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.framework.Airport;
import net.cybertip.routing.framework.FlightWeightType;
import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.routing.manager.LimitCoach;

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
            throw new IllegalArgumentException("Parent directory " + parent +
                    " must exist, be a directory, and be " +
                    "writable");
        }
        File dbFile = new File(parent, MAPDB_FILE);

        AirDatabase airDatabase = new AirDatabase(dbFile, new Random(0));
        AirPlanLoader.populate(airDatabase, AIRLINE_PASSWD);
        return airDatabase;
    }

    private static RouteMap uploadRouteMap(AirDatabase database, String
            inputFileName) throws FileNotFoundException, AirTrouble {
        Airline airline = database.grabAirline(AIRLINE_ID);
        RouteMap routeMap = new TextFileLoader().loadRouteMap(inputFileName,
                database);
        routeMap.setName(MAP_NAME);
        airline.addRouteMap(routeMap);
        database.commit();

        return routeMap;
    }

    private static void queryCapacity(RouteMap routeMap, String originName,
                                      String
            destName) throws AirTrouble {
        Airport origin = routeMap.getAirport(originName);
        if (origin == null)
            throw new RuntimeException("Cannot find origin airport");
        Airport dest = routeMap.getAirport(destName);
        if (dest == null)
            throw new RuntimeException("Cannot find destination airport");

        LimitCoach.grabContentsPublic(routeMap, origin, dest,
                FlightWeightType.COST, FlightWeightType.COST.grabDescription());
    }

    public static void run(String dataPath, String routeMapFile, String
            originName, String destName) {
        AirDatabase db = null;
        try {
            db = getDatabase(dataPath);
            RouteMap map = uploadRouteMap(db, routeMapFile);
            queryCapacity(map, originName, destName);
        } catch (Exception e) {
            System.err.println("Exception thrown: " + e);
        } finally {
            if (db != null)
                db.close();
        }
    }

    public static void main(String args[]) {
        if (args.length < 4) {
            System.out.println("Usage: airplan [dataPath] [routeMapFile] " +
                    "[originName] [destName]");
            System.exit(0);
        }

        long start = System.currentTimeMillis();
        run(args[0], args[1], args[2], args[3]);
        long duration = System.currentTimeMillis() - start;
        System.out.println("Time (sec) = " + (duration / 1000));
    }
}
