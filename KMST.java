package ads1ss12.pa;

import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.BitSet;

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
	private Vertex startVertex;

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

		adjList = new Vertex[numNodes];
		adjMatrix = new Edge[numNodes][numNodes];

		/*
		 * Adjazenzmatrix erstellen
		 */
		for(Edge e : edges) {
			adjMatrix[e.node1][e.node2] = e;
			adjMatrix[e.node2][e.node1] = e;
		}

		/*
		 * Adjazenzliste (=Liste von Edges) erstellen
		 */
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

		Collections.sort(this.edges);
		startVertex = Collections.min(Arrays.asList(adjList));

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
			s += v.node + ": " + v + "\n";
		}
		Main.printDebug(s);
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
		HashSet<Edge> heuristicSol = prims(startVertex);

		upperBound = getUpperBound(heuristicSol);
		lowerBound = getLowerBound(edges, k);

		Main.printDebug(lowerBound + " / " + upperBound);

		setSolution(upperBound, heuristicSol);

		HashSet<Edge> fixedEdges = new HashSet<Edge>(k-1);
		p(fixedEdges, 0, 0);
	}

	private int getLowerBound(Queue<Edge> edges, int k) {
		if(k > 0) {
			ArrayList<Edge> temp = new ArrayList<Edge>();
			int weight = 0;
			for(int i=0; i<k-1; i++) {
				Edge e = edges.poll();
				temp.add(e);
				weight += e.weight;
			}
			edges.addAll(temp);
			
			return weight;
		}
		return 0;
	}

	private int getLowerBound(ArrayList<Edge> edges, int k) {
		if(k > 0) {
			int weight = 0;
		  	for(int i=0; i<k-1; i++) {
				weight += edges.get(i).weight;
			}
			return weight;
		}
		return 0;
	}

	/*
	private int getLowerBound(int i, int k) {
		if(k > 0) {
			int weight = 0;
			j = 0;
			while(j < k-1) {
				Edge e = edges.get(i+j);
				if(!fixedEdges.contains(e)) {
					weight += edges.get(i).weight;
					j++;
				}
			}
			return weight;
		}
		return 0;
	}
	*/

	private int getUpperBound(HashSet<Edge> solution) {
		int weight = 0;
		for(Edge e : solution) {
			weight += e.weight;
		}
		return weight;
	}

	private void p(HashSet<Edge> fixedEdges, Queue<Edge> availableEdges, int weight) {

		// Bounding
		// berechne für P' lokale untere Schranke L' mit Dualheuristik;
		int localLowerBound = weight + getLowerBound(availableEdges, k-fixedEdges.size());
		// Fall L' >= U bracht nicht weiter verfolgt werden!
		if(localLowerBound <= upperBound) {
			// berechne für P' gültige heur. Lösung -> obere Schranke U';
			if(fixedEdges.size() == k-1) {
				//U = U'; // neue beste Lösung gefunden
				// Zusammenhang und Kreisfreiheit überprüfen -> DFS
				Main.printDebug(weight + " " + fixedEdges);
				return;
			}
			// Fall L' >= U braucht nicht weiter verfolgt werden!
			if(localLowerBound <= upperBound) {
				// Branching
				// partitioniere P' in Teilprobleme P1, P2;
				Edge e = availableEdges.poll();
				p(new HashSet<Edge>(fixedEdges), new LinkedList<Edge>(availableEdges), weight);

				fixedEdges.add(e);
				weight += e.weight;
				p(new HashSet<Edge>(fixedEdges), new LinkedList<Edge>(availableEdges), weight);
			}
		}
	}

	private void p(HashSet<Edge> fixed, int index, int weight) {

		// Bounding
		// berechne für P' lokale untere Schranke L' mit Dualheuristik;
		int localLowerBound = weight + getLowerBound(index, k-fixed.size());
		// Fall L' >= U bracht nicht weiter verfolgt werden!
		if(localLowerBound <= upperBound) {
			// berechne für P' gültige heur. Lösung -> obere Schranke U';
			if(fixed.size() == k-1) {
				//U = U'; // neue beste Lösung gefunden
				// Zusammenhang und Kreisfreiheit überprüfen -> DFS
				Main.printDebug(weight + " " + fixed);
				return;
			}
			// Fall L' >= U braucht nicht weiter verfolgt werden!
			if(localLowerBound <= upperBound) {
				// Branching
				// partitioniere P' in Teilprobleme P1, P2;
				p(new HashSet<Edge>(fixed), index+1, weight);

				Edge e = fixed.get(index);
				fixed.add(e);
				p(new HashSet<Edge>(fixed), index+1, weight + e.weight);
			}
		}
	}

	private boolean DFS(Vertex s) {
		return false;
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
