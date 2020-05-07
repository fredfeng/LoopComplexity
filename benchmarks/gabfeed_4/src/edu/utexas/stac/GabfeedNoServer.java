package edu.utexas.stac;

import com.cyberpointllc.stac.gabfeed.handler.*;
import com.cyberpointllc.stac.gabfeed.model.GabMessage;
import com.cyberpointllc.stac.gabfeed.model.GabRoom;
import com.cyberpointllc.stac.gabfeed.model.GabThread;
import com.cyberpointllc.stac.gabfeed.model.GabUser;
import com.cyberpointllc.stac.gabfeed.persist.GabDatabase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class GabfeedNoServer {

    private static final String MAPDB_FILE = "gabfeed.db";
    private static final String PASSWORD_KEY = "9864765sad4fhwsdf234asdgaswret";
    private static final String USER_ID = "foo";
    private static final String THREAD_ID = "1_0";

    private static GabDatabase getDatabase(String dataPath, String dbDir) throws IOException {
        File parent = new File(dbDir);
        if (!parent.exists() || !parent.isDirectory() || !parent.canWrite()) {
            throw new IllegalArgumentException("Parent directory " + parent +
                    " must exist, be a directory, and be " + "writable");
        }
        File dbFile = new File(parent, MAPDB_FILE);

        GabDatabase gabDatabase = new GabDatabase(dbFile);
        gabDatabase.initialize(dataPath, PASSWORD_KEY);
        return gabDatabase;
    }

    private static void uploadMessage(GabDatabase database, String content) {
        GabThread thread = database.getThread(THREAD_ID);
        if (thread == null)
            throw new RuntimeException("Thread not found");

        GabUser user = database.getUser(USER_ID);
        GabMessage message = thread.addMessage(content, USER_ID);
        user.addMessage(message.getId());
    }

    private static void getThread(GabDatabase database) {
        GabThread thread = database.getThread(THREAD_ID);
        if (thread == null)
            throw new RuntimeException("Thread not found");
        GabUser user = database.getUser(USER_ID);
        GabRoom room = thread.getRoom();
        List<Link> menuItems = new LinkedList();
        menuItems.add(new Link(RoomHandler.getPathToRoom(room.getId()), room
                .getName()));
        menuItems.add(new Link(NewMessageHandler.getPathToPostToThread(thread
                .getId()), "New Message"));
        GabHandler.getTemplateResponsePublic(thread.getName(), ThreadHandler
                .getContentsPublic(thread), user, menuItems);
    }

    public static void run(String dataPath, String dbDir, String msgContent) {
        GabDatabase database = null;
        try {
            database = getDatabase(dataPath, dbDir);
            uploadMessage(database, msgContent);
            getThread(database);
        } catch (Exception e) {
            System.err.println("Exception thrown: " + e);
        } finally {
            if (database != null)
                database.close();
        }
    }

    public static void main(String args[]) {
        if (args.length < 3) {
            System.out.println("Usage: gabfeed [dataPath] [workDir] [messageFile]");
            System.exit(0);
        }

        File msgFile = new File(args[2]);
        try {
            String msgContent = FileUtils.readFileToString(msgFile);
            run(args[0], args[1], msgContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
