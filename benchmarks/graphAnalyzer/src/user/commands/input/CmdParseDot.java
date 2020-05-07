/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.commands.input;

import graph.Edge;
import graph.Graph;
import graph.GraphException;
import graph.Node;
import graph.dot.DotParser;
import user.commands.Cmd;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author user
 */
public class CmdParseDot implements Cmd {

    List<Exception> exs;

    public CmdParseDot() {

        exs = new ArrayList<Exception>();
    }

    @Override
    public List<Exception> runCmd(Map inputmap) {
        try {
            String fname = (String) inputmap.get("inputfile");

            Map<String, Graph> graphs = DotParser.parseDot(new DataInputStream(new FileInputStream(fname)));
            inputmap.put("graphs", graphs);
            /*Graph graph = graphs.get("main");

             Collection<Graph> graphsc = graphs.values();

             Iterator<Graph> itgs = graphsc.iterator();
             while (itgs.hasNext()) {

             Graph itg = itgs.next();

             GraphHolder gh = new GraphHolder();
             inputmap.put("graphs", gh);

             gh.nodes = new ArrayList<Node>();
             gh.edges = new ArrayList<Edge>();

             Iterator it = itg.nodes.iterator();
             while (it.hasNext()) {
             Node n = (Node) it.next();
             gh.nodes.add(n);
             Iterator<Edge> ite = n.out.iterator();
             while (ite.hasNext()) {
             Edge e = ite.next();
             gh.edges.add(e);
             }
             }
             }*/
        } catch (FileNotFoundException ex) {
            exs.add(ex);
        } catch (IOException ex) {
            exs.add(ex);
        } catch (GraphException ex) {
            exs.add(ex);
        }

        return exs;
    }

    public class GraphHolder {

        public java.util.List<Node> nodes;
        public java.util.List<Edge> edges;
    }
}
