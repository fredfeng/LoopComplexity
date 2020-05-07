// File: GraphController.java
// Interfaces: GraphController
// Original Author: thorsten Oct 2000
// $Id: GraphController.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.graph;

import java.util.List;

/**
 * This interface is the basis for each class that handles the control of pairs
 * of data and representational objects.
 */
public interface GraphController extends java.io.Serializable {

    /**
     * Add a new presentation to the list of known presentations. Each
     * presentation consists of a data object (referrer) and an object for its
     * graphical representation.
     */
    boolean addPresentation(Object representation, Object referrer);

    /**
     * Remove a presentation from the list of known presentations.
     */
    boolean removePresentation(Object element);

    /**
     * Get the graphical representation of the given object.
     */
    Object presentationFor(Object data);

    /**
     * Get the related data object for the given object.
     */
    Object referrerFor(Object presentation);

    /**
     * Tests, if the given object is a node known by the controller.
     */
    boolean containsNode(Object node);

    /**
     * Tests, if the given object is an edge known by the controller.
     */
    boolean containsEdge(Object edge);

    List getNodes();

    List getEdges();

    /**
     * Counts the number of presentations known to the GraphController for the
     * given referrer.
     */
    int countPresentationsFor(Object referrer);
} /* end interface GraphController */
