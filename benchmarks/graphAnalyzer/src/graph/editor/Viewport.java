package graph.editor;

import graph.Graph;

import java.awt.*;

/**
 * A viewport for panning and zooming
 *
 * @see graph.Graph;
 * @see graph.Node;
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
class Viewport {

    double x = 0;
    double y = 0;
    double w = 100;
    double h = 100;

    void paint(Graphics g, Graph graph, int cw, int ch) {
        //graph.paint(g, -x, -y);
    }
}
