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
import java.util.Comparator;
import java.util.TreeSet;

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
	private ArrayList<Vertex> vertices;

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

		Collections.sort(this.edges);

		adjList = createAdjacencyList(this.edges, numNodes);
		//adjMatrix = createAdjacencyMatrix(this.edges, numNodes);

		vertices = new ArrayList<Vertex>(Arrays.asList(adjList));

		/*
		 * Ausgabe
		 */

		String s = "";

		/*
		s = "";
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

		for(Vertex v : vertices) {
			s += "\n" + v;
		}
		Main.printDebug(s);
		*/
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
		/*
		for(int i=0; i<adjList.length; i++) {
			Collections.sort(adjList[i]);
		}
		*/
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
		for(Vertex v : vertices) {
			HashSet<Edge> primsSolution = prim(v);
			int weight = getUpperBound(primsSolution);

			if(weight < upperBound) {
				upperBound = weight;
				setSolution(upperBound, primsSolution);
			}

			v.value = weight;
		}

		Collections.sort(vertices);

		for(Vertex v : vertices) {
			Main.printDebug(v);
			PriorityQueue<Edge> relevant = new PriorityQueue<Edge>(v);
			HashSet<Edge> selected = new HashSet<Edge>();
			BitSet visited = new BitSet(numNodes);

			visited.set(v.node);

			primEnum(relevant, selected, visited, 0);

			for(Vertex w : vertices) {
				if(!w.equals(v)) {
					w.removeNode(v.node);
				}
			}
		}
	}

	private HashSet<Edge> prim(Vertex s) {
		PriorityQueue<Edge> relevant = new PriorityQueue<Edge>(s);
		HashSet<Edge> selected = new HashSet<Edge>();
		BitSet visited = new BitSet(numNodes);

		visited.set(s.node);

		while(selected.size() < k-1) {
			Edge e = relevant.poll();
			if(!createsCircle(e, visited)) {
				Vertex newVertex = getOuterVertex(e, visited);
				visited.set(newVertex.node);
				selected.add(e);

				for(Edge x : newVertex) {
					if(!createsCircle(x, visited)) {
						relevant.add(x);
					}
				}
			}
		}

		return selected;
	}

	private int getLowerBound(BitSet visited, int n) {
		int i = 0;
		int j = 0;
		int weight = 0;
		while(j < n) {
			Edge e = edges.get(i);
			if(!createsCircle(e, visited)) {
				weight += e.weight;
				j++;
			}
			i++;
		}
		return weight;
	}

	int i = 0;
	private void primEnum(PriorityQueue<Edge> relevant, HashSet<Edge> selected, BitSet visited, int weight) {
		i++;
		if(i%100000==0) {
			Main.printDebug(i);
		}

		int localLowerBound = weight + getLowerBound(visited, k-1-selected.size());

		if(localLowerBound < upperBound) {
			if(selected.size() < k-1) {
				Edge e = null;
				while((e = relevant.poll()) != null) {
					if(!createsCircle(e, visited)) {
						Vertex newVertex = getOuterVertex(e, visited);
						PriorityQueue<Edge> newRelevant = new PriorityQueue<Edge>(relevant);
						HashSet<Edge> newSelected = new HashSet<Edge>(selected);
						BitSet newVisited = (BitSet)visited.clone();

						newVisited.set(newVertex.node);
						newSelected.add(e);

						for(Edge x : newVertex) {
							if(createsCircle(x, newVisited) == false) {
								newRelevant.offer(x);
							}
						}
						primEnum(newRelevant, newSelected, newVisited, weight + e.weight);
					}
				}
			}
			else {
				if(weight < upperBound) {
					setSolution(weight, selected);
					upperBound = weight;
					Main.printDebug("It's getting warmer");
				}
			}
		}
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

	private int getUpperBound(HashSet<Edge> solution) {
		int weight = 0;
		for(Edge e : solution) {
			weight += e.weight;
		}
		return weight;
	}

	class Vertex extends ArrayList<Edge> implements Comparable<Vertex> {
		public final int node;
		public int value = 0;

		public Vertex(int node) {
			this.node = node;
		}

		public void removeNode(int node) {
			LinkedList<Edge> blackList = new LinkedList<Edge>();
			for(Edge e : this) {
				if(e.node1 == node || e.node2 == node) {
					blackList.add(e);
				}
			}
			for(Edge e: blackList) {
				remove(e);
			}
		}

		public int compareTo(Vertex e) {
			return this.value - e.value;
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

		public String toString() {
			return node + ": ("+ value +") " + super.toString();
		}
	}
}
