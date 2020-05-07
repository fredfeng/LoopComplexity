/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tigris.gef.base;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigPainter;

import java.awt.*;

/**
 *
 * @author user
 */
public interface GraphicsSpecial {

    void paintContents(LayerDiagram layer, Graphics g, FigPainter painter);

    void paintContents(LayerDiagram layer, Graphics g, FigPainter painter,
                       int x, int y, int scale, Fig parent);

}
