package edu.utexas.stac;

import com.roboticcusp.organizer.AirException;
import com.roboticcusp.organizer.XmlFileLoader;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.place.AirPlanLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

public class AirplanNoServer {

    private static final String MAPDB_FILE = "airplan.db";
    private static final String AIRLINE_PASSWD = "test";

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

    private static void uploadRouteMap(AirDatabase database, String
            inputFileName) throws FileNotFoundException, AirException {
        new XmlFileLoader().loadRouteMap(inputFileName,
                database);
    }

    public static void run(String dataPath, String routeMapFile) {
        AirDatabase db = null;
        try {
            db = getDatabase(dataPath);
            uploadRouteMap(db, routeMapFile);
        } catch (Exception e) {
            System.err.println("Exception thrown: " + e);
        } finally {
            if (db != null)
                db.close();
        }
    }

    public static void main(String args[]) {
        if (args.length < 2) {
            System.out.println("Usage: airplan [dataPath] [routeMapFile]");
            System.exit(0);
        }

        run(args[0], args[1]);
    }
}
