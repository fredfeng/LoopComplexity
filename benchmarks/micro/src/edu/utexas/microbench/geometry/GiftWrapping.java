package edu.utexas.microbench.geometry;

import java.util.*;

public class GiftWrapping {

    private enum Orientation {
        COLINEAR, CLOCKWISE, COUNTERCLOCKWISE
    }

    private static Orientation orientation(Point p, Point q, Point r)
    {
        long val = (long)(q.y - p.y) * (long)(r.x - q.x) -
                (long)(q.x - p.x) * (long)(r.y - q.y);
        if (val == 0) return Orientation.COLINEAR;
        return (val > 0)? Orientation.CLOCKWISE: Orientation.COUNTERCLOCKWISE;
    }

    private static double distance(Point p, Point q) {
        long xdiff = p.x - q.x;
        long ydiff = p.y - q.y;
        return Math.sqrt(xdiff * xdiff + ydiff * ydiff);
    }

    private static Point nextHullPoint(Point p, List<Point> points) {
        Point q = p;
        for (Point r: points) {
            Orientation orientation = orientation(p, q, r);
            if (orientation == Orientation.COUNTERCLOCKWISE || orientation == Orientation.COLINEAR && distance(p, r) > distance(p, q))
                q = r;
        }
        return q;
    }

    // Prints convex hull of a set of n points.
    public static List<Point> convexHull(List<Point> points)
    {
        int n = points.size();

        // There must be at least 3 points
        if (n < 3) return points;

        // Initialize Result
        List<Point> hull = new ArrayList<>();
        Set<Point> hullPoints = new HashSet<>();

        // Find the leftmost point
        int l = 0;
        for (int i = 1; i < n; i++)
            if (points.get(i).x < points.get(l).x)
                l = i;
        hull.add(points.get(l));
        hullPoints.add(points.get(l));

        for (int i = 0; i < hull.size(); ++i) {
            Point p = hull.get(i);
            Point q = nextHullPoint(p, points);
            if (!q.equals(hull.get(0)) && !hullPoints.contains(q)) {
                hull.add(q);
                hullPoints.add(q);
            }
        }

        return hull;
    }

    public static List<Point> convexHullArray(Point points[]) {
        return convexHull(Arrays.asList(points));
    }

    /* Driver program to test above function */
    public static void main(String[] args)
    {
        List<Point> points = Arrays.asList(
                new Point(5, 3), new Point(6,3), new Point(4,2), new Point(4,6), new Point(4,1)
        );

        List<Point> hull = convexHull(points);
        for (Point p: hull) {
            System.out.println(p.x + ", " + p.y);
        }
    }
}
