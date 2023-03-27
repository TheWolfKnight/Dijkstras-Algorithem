
/*
 * Author: Philip Alexander Lundgaard Knudsen
 * Case Writer: Tobias Sonne Schyt
 * Date: 28 - 03 - 2022
 * License: Only to kill
*/

package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.io.FileReader;

// An external lib used to parse JSON-files into a usable format.
// can be downloaded at the URL: https://github.com/fangyidong/json-simple
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;


public class Main {

	public static void main(String[] args) throws Exception {
		// Keeps track of the vertex names that has been traveled
		// and bellow is the array list of all vertices
		HashSet<String> closed = new HashSet<String>();
		ArrayList<Vertex> path = loadFullGraph();
		// Makes an ArrayList to keep track of the active node heads;
		ArrayList<PathNode> activeHeads = new ArrayList<PathNode>();
		// This variable is made to keep track of all possible end routes
		// used later to make sure all routes are found.
		PathNode endNode = null;
		// Starts out by finding the start node, and creates a PathNode,
		String startName = "vejle", endName = "jerlev";
		boolean run = true;
		for (Vertex v: path) {
			if (v.name.equals(startName.toLowerCase())) {
				activeHeads.add(
						new PathNode(0, null, v)
				);
			}
		}

		// The program will now run until it has found every route to the end node
		while (run) {
			if (closed.size() == path.size())
				throw new Exception("You have not designated an end node, or the node name was spelled wrong");

			// Starts out by getting the smallest PathNode, based on the length parameter.
			// If the node already has been scanned, the node is removed from activeHeads
			// and the program goes to the next iteration.
			PathNode currNode = getMinPathNode(activeHeads);
			if (closed.contains(currNode.node.name)) {
				activeHeads.remove(currNode);
				continue;
			}
			// Adds the node to the list of closed nodes
			closed.add(currNode.node.name);

			// It now finds the smallest edge in the currNodes Vertex
			Edge edge = new Edge(Integer.MAX_VALUE, null, null);
			for (Edge e: currNode.node.edges) {
				if (e.length < edge.length && !closed.contains(e.dest))
					edge = e;
			}

			// If the edge leads to the end node the program adds a PathNode to the endNode,
			// and the terminates the loop
			System.out.println(edge.dest);

			if (edge.dest.equals(endName)) {
				Vertex end = new Vertex(endName, null);
				endNode = new PathNode(currNode.length+edge.length, currNode, end);
				run = false;
			}
			// If the dest vertex is found to not be the end,
			// it is added to the activeHeads and the program loops again
			else {
				Vertex dest = null;
				for (Vertex v: path) {
					if (v.name.equals(edge.dest)) {
						dest = v;
						break;
					}
				}
				if (dest == null)
					throw new Exception("An edge does not lead to an actual vertex. Please read the graf through");
				activeHeads.add(
						new PathNode(currNode.length+edge.length, currNode, dest)
				);
			}
		}
		System.out.println("End node reached!");
		// This function is then used to print the path to the console
		printNodePath(endNode);
	}

	// Used in the end to print the found path
	static void printNodePath(PathNode head) {
		ArrayList<String> r = new ArrayList<String>();
		int t = head.length;
		while (head.prev != null) {
			r.add(head.node.name);
			head = head.prev;
		}
		// Just getting the last node
		r.add(head.node.name);

		for (int i = r.size()-1; i >= 0; i--) {
			if (i != 0) {
				String s = String.format("%s, ", r.get(i));
				System.out.print(s);
			} else {
				String s = String.format("%s", r.get(i));
				System.out.println(s);
			}
		}
		String s = String.format("The total distans was: %d", t);
		System.out.println(s);
	}

	// Gets the PathNode with the smallest length property
	static PathNode getMinPathNode(ArrayList<PathNode> activeHeads) {
		PathNode r = new PathNode(Integer.MAX_VALUE, null, null);
		for (PathNode n: activeHeads) {
			for (Edge e: n.node.edges) {
				if (n.length+e.length < r.length);
				r = n;
			}
		}
		return r;
	}

	// Helper function to load in the graph from a JSON-file
	static ArrayList<Vertex> loadFullGraph() {
		ArrayList<Vertex> r = new ArrayList<Vertex>();
		JSONParser parser = new JSONParser();
		// The file from which the data is loaded, this must be an absolute path to the file.
		// This is also different from individual to individual.
		String target = "E:/programmering/Java/Dijkstras_Algorithem/src/main/case.json";
		// Honestly, this is out of the scope for the project.
		// Therefore i will just see it as something for the reader to figure out
		try {
			JSONArray a = (JSONArray) parser.parse(new FileReader(target));
			
			for (Object o: a) {
				JSONObject vertices = (JSONObject) o;
				String name = ((String) vertices.get("name")).toLowerCase();
				ArrayList<Edge> edges = JSONArrayToArrayList((JSONArray) vertices.get("edges"));
				r.add(
						new Vertex(name, edges)
				);
			}
		} catch (Exception e) {
			System.out.println("You fucked up the file!");
			System.out.println("That is fair, as this is some shit I made up without much thought");
			System.out.println(e);
			System.exit(1);
		}
		return r;
	}

	static ArrayList<Edge> JSONArrayToArrayList(JSONArray in) {
		ArrayList<Edge> r = new ArrayList<Edge>();
		if (in == null) {
			return r;
		}
		for (Object o: in) {
			JSONObject e = (JSONObject) o;
			r.add(
				new Edge(Math.toIntExact((long) e.get("length")),
						((String) e.get("source")).toLowerCase(),
						((String) e.get("dest")).toLowerCase())
			);
		}
		return r;
	}

	// A test to see if all the inputs from the file has been done correctly
	// Just add it after the path variable, and then see if everything appears correctly
	static void testPrintPath(ArrayList<Vertex> path) {
		for (Vertex v: path) {
			String s = String.format("Name %s", v.name);
			System.out.println(s);
			for (Edge e: v.edges) {
				s = String.format("\tEdge from %s, to %s with a length of %d", e.source, e.dest, e.length);
				System.out.println(s);
			}
		}
	}
	
	static void testPrintActiveHeads(ArrayList<PathNode> ah) {
		for (PathNode p: ah) {
			String s = String.format("\t%s", p.node.name);
			System.out.println(s);
		}
	}
}


// Bellow exists some data classes, to make the storages and accessing easier.
class PathNode {
	// Create a var to keep track of the distance between the node and the start
	int length;
	// Create var used in backtracking for returning the shortest path
	PathNode prev;
	// Var to tell which node this PathNode relates to
	Vertex node;

	PathNode(int _length, PathNode _prev, Vertex _name) {
		this.length = _length;
		this.prev = _prev;
		this.node = _name;
	}
}


class Vertex {
	// Name of the vertex
	String name;
	// All edges connecting to the vertex
	ArrayList<Edge> edges;

	Vertex(String _name, ArrayList<Edge> _edges) {
		this.name = _name;
		this.edges = _edges;
	}
}


class Edge {
	// Name of the source, and destination, vertex
	String source, dest;
	// length of the edge
	int length;

	Edge(int _length, String _source, String _dest) {
		this.source = _source;
		this.dest = _dest;
		this.length = _length;
	}
}
