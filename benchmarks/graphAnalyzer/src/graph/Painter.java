package graph;

import java.awt.*;

/**
 * An interface which designates that the object knows how to paint into a
 * graphics context. This
 *
 * @see graph.filter.Filter
 * @see graph.editor.Viewer;
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public interface Painter {

    /**
     * @param g	The graphics context.
     */
    void paint(Graphics g);
}
