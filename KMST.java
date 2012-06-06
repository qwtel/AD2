package ads1ss12.pa;

import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.BitSet;
import java.util.Collection;

/**
 * Klasse zum Berechnen eines k-MST mittels Branch-and-Bound. Hier sollen Sie
 * Ihre L&ouml;sung implementieren.
 */
public class KMST extends AbstractKMST {

	private ArrayList<Edge> edges;
	private Vertex[] adjList;
	private Edge[][] adjMatrix;
	private int k;
	private int numEdges;
	private int numNodes;
	private int upperBound = Integer.MAX_VALUE;
	private int lowerBound = Integer.MAX_VALUE;

	private int problemNumber = 0;

	/**
	 * Der Konstruktor. Hier ist die richtige Stelle f&uuml;r die
	 * Initialisierung Ihrer Datenstrukturen.
	 * 
	 * @param numNodes
	 *						Die Anzahl der Knoten
	 * @param numEdges
	 *						Die Anzahl der Kanten
	 * @param edges
	 *						Die Menge der Kanten
	 * @param k
	 *						Die Anzahl der Knoten, die Ihr MST haben soll
	 */
	public KMST(Integer numNodes, Integer numEdges, HashSet<Edge> edges, int k) {
		this.numNodes = numNodes;
		this.numEdges = numEdges;
		this.edges = new ArrayList<Edge>(edges);
		this.k = k;

		adjList = createAdjacencyList(this.edges, numNodes);
		adjMatrix = createAdjacencyMatrix(this.edges, numNodes);

		Collections.sort(this.edges);

		/*
		 * Ausgabe
		 */
		String s = "";
		for(int i=0; i<numNodes; i++) {
			s += "\n";
			for(int j=0; j<numNodes; j++) {
				if(adjMatrix[i][j] != null)
					s += String.format("%6s", "" + adjMatrix[i][j].weight);
				else
					s += String.format("%6s", ".");
			}
		}
		Main.printDebug(s);

		s = "";
		for(Vertex v : adjList) {
			s += "\n" + v.node + ": " + v;
		}
		Main.printDebug(s);
	}

	private Vertex[] createAdjacencyList(Collection<Edge> edges, int numNodes) {
		Vertex[] adjList = new Vertex[numNodes];
		for(Edge e : edges) {
			if(adjList[e.node1] == null) {
				adjList[e.node1] = new Vertex(e.node1);
			}
			if(adjList[e.node2] == null) {
				adjList[e.node2] = new Vertex(e.node2);
			}
			adjList[e.node1].add(e);
			adjList[e.node2].add(e);
		}
		return adjList;
	}

	private Edge[][] createAdjacencyMatrix(Collection<Edge> edges, int numNodes) {
		Edge[][] adjMatrix = new Edge[numNodes][numNodes];
		for(Edge e : edges) {
			if(adjMatrix[e.node1][e.node2] == null || adjMatrix[e.node1][e.node2].weight > e.weight) {
				adjMatrix[e.node1][e.node2] = e;
				adjMatrix[e.node2][e.node1] = e;
			}
		}
		return adjMatrix;
	}

	/**
	 * Diese Methode bekommt vom Framework maximal 30 Sekunden Zeit zur
	 * Verf&uuml;gung gestellt um einen g&uuml;ltigen k-MST zu finden.
	 * 
	 * <p>
	 * F&uuml;gen Sie hier Ihre Implementierung des Branch-and-Bound Algorithmus
	 * ein.
	 * </p>
	 */
	@Override
	public void run() {
		//Vertex startVertex = Collections.min(Arrays.asList(adjList));

		for(Vertex v : adjList) {
			HashSet<Edge> primsSolution = prims(v);
			int weight = getUpperBound(primsSolution);

			if(weight < upperBound) {
				upperBound = weight;
				setSolution(upperBound, primsSolution);
			}
		}

		//Main.printDebug(lowerBound + " / " + upperBound);

		HashSet<Edge> fixedEdges = new HashSet<Edge>(k-1);
		p(fixedEdges, 0, 0);
	}

	private int getLowerBound(int i, int k) {
		int weight = 0;
		int j = 0;
		while(j < k-1) {
			weight += edges.get(i+j).weight;
			j++;
		}
		return weight;
	}

	private int getUpperBound(HashSet<Edge> solution) {
		int weight = 0;
		for(Edge e : solution) {
			weight += e.weight;
		}
		return weight;
	}

	private void p(HashSet<Edge> fixed, int index, int weight) {

		//Main.printDebug(++problemNumber);
		//Main.printDebug(getLowerBound(index, k-fixed.size()));
		//locallowerbound -= edges.get(...).weight; 
		//locallowerbound += edges.get(...).weight;

		// Bounding
		int localLowerBound = weight + getLowerBound(index, k-fixed.size());
		if(localLowerBound < upperBound && index + k-1 < numEdges) {
			if(fixed.size() == k-1) {
				if(weight < upperBound && validSolution(fixed)) {
					setSolution(weight, new HashSet<Edge>(fixed));
					upperBound = weight;
					//Main.printDebug(weight + ", " + fixed);
				}
				return;
			}
			// Branching

			if(localLowerBound < upperBound) {
				Edge e = edges.get(index);
				HashSet<Edge> asdf = new HashSet<Edge>(fixed);
				asdf.add(e);
				p(asdf, index+1, weight + e.weight);
			}
			if(localLowerBound < upperBound) {
				p(new HashSet<Edge>(fixed), index+1, weight);
			}
		}
	}

	private boolean validSolution(Set<Edge> edges) {
		DDM unionField = new DDM(numNodes);

		for(Edge e : edges) {
			unionField.makeSet(e.node1);
			unionField.makeSet(e.node2);
		}

		// Kreisfreiheit prüfen
		for(Edge e : edges) {
			if(unionField.findSet(e.node1) != unionField.findSet(e.node2)) {
				unionField.union(e.node1, e.node2);
			}
			else {
				return false;
			}
		}

		// Zusammenhang prüfen
		if(unionField.getNumSets() == 1) {
			return true;
		}

		return false;
	}


	class DDM {
		private int[] parent;
		private BitSet marked;
		private int numSets;

		public DDM(int numNodes) {
			parent = new int[numNodes];
			marked = new BitSet(numNodes);
			numSets = 0;
		}

		public void makeSet(int v) {
			if(!marked.get(v)) {
				parent[v] = v; 
				marked.set(v);
				numSets++;
			}
		}

		public void union(int v, int w) {
			parent[v] = w;
			numSets--;
		}

		public int findSet(int v) {
			int h = v;
			while(parent[h] != h) {
				h = parent[h];
			}
			return h;
		}

		public int getNumSets() {
			return numSets;
		}
	}

	private HashSet<Edge> prims(Vertex s) {
		PriorityQueue<Edge> relevant = new PriorityQueue<Edge>(s);
		HashSet<Edge> selected = new HashSet<Edge>();
		BitSet visited = new BitSet(numNodes);

		visited.set(s.node);

		while(selected.size() < k-1) {
			Edge e = relevant.poll();
			Vertex newVertex = getOuterVertex(e, visited);
			visited.set(newVertex.node);
			selected.add(e);

			for(Edge x : newVertex) {
				if(createsCircle(x, visited) == false) {
					relevant.add(x);
				}
				else {
					relevant.remove(x);
				}
			}
		}

		return selected;
	}

	private boolean createsCircle(Edge e, BitSet visited) {
		return visited.get(e.node1) && visited.get(e.node2);
	}

	private Vertex getOuterVertex(Edge e, BitSet visited) {
		Vertex v = null;
		if(visited.get(e.node1)) {
			v = adjList[e.node2];
		}
		else {
			v = adjList[e.node1];
		}
		return v;
	}

	class Vertex extends PriorityQueue<Edge> implements Comparable<Vertex> {
		public final int node;
		public int value;
		public int metric;
		public int shortestPath;

		public Vertex(int node) {
			this.node = node;
			this.value = 0;
			this.metric = 0;
		}

		public boolean add(Edge e) {
			boolean success;
			if(success = super.add(e)) {
				value += e.weight;
				metric = value/size(); 
			}
			return success;
		}

		public boolean equals(Object other) {
			if (other instanceof Vertex) {
				Vertex o = (Vertex) other;
				return node == o.node;
			}
			else {
				return false;
			}
		}

		public int compareTo(Vertex o) {
			int d = this.metric - o.metric;
			if(d < 0) {
				return -1;
			}
			else if(d > 0) {
				return 1;
			}
			return 0;
		}
	}
}
