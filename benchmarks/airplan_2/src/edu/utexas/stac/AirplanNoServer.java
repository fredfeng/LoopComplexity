package edu.utexas.stac;

import com.networkapex.airplan.AirRaiser;
import com.networkapex.airplan.TextFileLoader;
import com.networkapex.airplan.coach.LimitManager;
import com.networkapex.airplan.prototype.Airline;
import com.networkapex.airplan.prototype.Airport;
import com.networkapex.airplan.prototype.FlightWeightType;
import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.save.AirDatabase;
import com.networkapex.start.AirPlanLoader;

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
            inputFileName) throws FileNotFoundException, AirRaiser {
        Airline airline = database.obtainAirline(AIRLINE_ID);
        RouteMap routeMap = new TextFileLoader().loadRouteMap(inputFileName,
                database);
        routeMap.defineName(MAP_NAME);
        airline.addRouteMap(routeMap);
        database.commit();

        return routeMap;
    }

    private static void queryCapacity(RouteMap routeMap, String originName,
                                      String
            destName) throws AirRaiser {
        Airport origin = routeMap.fetchAirport(originName);
        if (origin == null)
            throw new RuntimeException("Cannot find origin airport");
        Airport dest = routeMap.fetchAirport(destName);
        if (dest == null)
            throw new RuntimeException("Cannot find destination airport");

        LimitManager.pullContentsPublic(routeMap, origin, dest,
                FlightWeightType.COST, FlightWeightType.COST.getDescription());
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

        run(args[0], args[1], args[2], args[3]);
    }
}
