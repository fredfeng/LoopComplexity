/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.commands.layout;

import graph.Graph;
import graph.layout.RandomLayout;
import user.commands.Cmd;

import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author user
 */
public class CmdRandLayout implements Cmd {

    @Override
    public Object runCmd(Map inputs) {

        Map<String, Graph> graphs = (Map<String, Graph>) inputs.get("graphs");
        Iterator<Graph> it = graphs.values().iterator();
        while (it.hasNext()) {

            Graph g = it.next();
            RandomLayout rand = new RandomLayout(250, 250, 2);
            rand.apply(g);

        }
        return null;
    }

}
