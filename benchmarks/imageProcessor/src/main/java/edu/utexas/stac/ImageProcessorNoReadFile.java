package edu.utexas.stac;

import com.stac.learning.ClusterController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class ImageProcessorNoReadFile {
    public static String cluster(String workingDir, BufferedImage image) {
        ClusterController clusterController = new ClusterController(Paths.get(workingDir));
        String[] nearestTypes = clusterController.cluster(image);
        if (nearestTypes == null) {
            System.err.append("Failed to classify the given image").println();
            return null;
        }

        HashMap<String, Integer> inCluster = new HashMap<>(nearestTypes.length);
        for (String type : nearestTypes) {
            if (inCluster.containsKey(type)) {
                inCluster.put(type, inCluster.get(type) + 1);
            }
            inCluster.put(type, 1);
        }

        LinkedList<Map.Entry<String, Integer>> types = new LinkedList<>(inCluster.entrySet());
        types.sort((o1, o2) -> Objects.requireNonNull(o2.getValue()) - Objects.requireNonNull(o1.getValue()));
        return types.getFirst().getKey();
    }

    public static void main(String args[]) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: imageProcessor [workingDir] [imageFile]");
            System.exit(0);
        }

        BufferedImage image = ImageIO.read(new File(args[1]));
        String c = cluster(args[0], image);
        System.out.println("Cluster = " + c);
    }
}
