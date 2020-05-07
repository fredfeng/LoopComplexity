package graph.filter;

import graph.Node;

import java.awt.*;

/**
 * A class for performing mappings of semantic node/edge information onto the
 * display.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public interface Filter {

    /**
     * Apply this filter to the node, modifying the node's display attributes
     * and those of its outgoing edges based on their semantic properties.
     */
    void apply(Node n);

    /**
     * Build a dialog by which the user can easily configure parameters to the
     * Filter.
     */
    Frame buildGUI();

    /**
     * Build a "thumbnail" control with which the user can more quickly tweak
     * parameters.
     */
    Component buildThumbnail();

    /**
     * Get the title of this filter
     */
    String getName();
}
