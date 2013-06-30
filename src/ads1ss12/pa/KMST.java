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
	private ArrayList<Vertex> vertices;
	//private Edge[][] adjMatrix;
	private int k;
	private int numEdges;
	private int numNodes;
	private int upperBound = Integer.MAX_VALUE;

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
		this.k = k;

		this.edges = new ArrayList<Edge>(edges);
		Collections.sort(this.edges);

		adjList = createAdjacencyList(this.edges, numNodes);
		vertices = new ArrayList<Vertex>(Arrays.asList(adjList));

		//adjMatrix = createAdjacencyMatrix(this.edges, numNodes);

		/* Ausgabe */
		/*
		String s = "";
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

	/**
	 * Die Adjazenz Liste ist ein Vertex Array. 
	 * Der Index entspricht dabei der Nummer (node) des Knoten.
	 * Außerdem werden die Kanten die von diesem Vertex weg gehen sortiert.
	 */
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
		for(int i=0; i<adjList.length; i++) {
			Collections.sort(adjList[i]);
		}
		return adjList;
	}

	/**
	 * Die Adjazenz Matrix ist ein 2D Array von Edges.
	 * Die Indizes entsprechen dabei den Nummern (node) der Knoten.
	 */
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
		prim(vertices);
		Collections.sort(vertices);
		primEnum(vertices);
	}

	/**
	 * Führt den Prim Algorithmus auf jeden Knoten aus um eine initiale Lösung und eine gute obere Schranke zu erhalten.
	 *
	 * Außerdem wird jedem Knoten als Wert das Gewicht der Prim Lösung zugeordnet, denn auch diese Information ist
	 * wertvoll. Damit kann das Branch and Bound-Verfahren in der Nähe einer guten Lösung gestartet werden, wodurch
	 * schnell lokale Verbesserungen erreicht werden.
	 * O(n*k*m)
	 */
	private void prim(Collection<Vertex> vertices) {
		for(Vertex v : vertices) {
			HashSet<Edge> primSolution = prim(v);
			int weight = getUpperBound(primSolution);

			if(weight < upperBound) {
				upperBound = weight;
				setSolution(upperBound, primSolution);
			}

			v.value = weight;
		}
	}

	/**
	 * Prim Algorithmus für einen einzelnen Knoten.
	 * O(k*m)
	 */
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

	/**
	 * Addiert die n kleinsten Kanten, die noch nicht besucht sind und keinen Kreis verursachen. 
	 * Voraussetzung ist, dass edges sortiert ist.
	 * O(k)
	 */
	private int getLowerBound(BitSet visited, BitSet forbidden, int n) {
		int i = 0;
		int j = 0;
		int weight = 0;
		while(j < n) {
			Edge e = edges.get(i);
			if(!createsCircle(e, visited) && !isForbidden(e, forbidden)) {
				weight += e.weight;
				j++;
			}
			i++;
		}
		return weight;
	}

	/**
	 * Kapselt die Parameter eines Problems und kümmert sich um das Klonen der Objekte.
	 */
    class Problem {
		public PriorityQueue<Edge> relevant;
		public HashSet<Edge> selected;
		public BitSet forbidden;
		public BitSet visited;
		public int weight;

		/**
		 * Erzeugt ein Initialproblem welches vom Knoten v ausgeht.
		 */
		public Problem(Vertex v) {
			relevant = new PriorityQueue<Edge>(v);
			selected = new HashSet<Edge>();
			forbidden = new BitSet(numNodes);
			visited = new BitSet(numNodes);
			weight = 0;

			visited.set(v.node);
		}

		/**
		 * Kopierkonstruktor.
		 */
		public Problem(Problem p) {
			relevant = new PriorityQueue<Edge>(p.relevant);
			selected = new HashSet<Edge>(p.selected);
			forbidden = (BitSet)p.forbidden.clone();
			visited = (BitSet)p.visited.clone();
			weight = p.weight;
		}
	}

    private void primEnum(Collection<Vertex> vertices) {
		for(Vertex v : vertices) {
			Main.printDebug(v);

			Problem p = new Problem(v);
			primEnum(p);

			for(Vertex w : vertices) {
				if(!w.equals(v)) {
					w.removeNode(v.node);
				}
			}
		}
	}

	int i = 0;
	int level = 0;
	int reachedLevel = 0;
	private void primEnum(Problem p) {
		i++;
		level++;
		if(i%100000==0) {
			Main.printDebug(i);
		}
		if(level==17) {
			reachedLevel++;
			Main.printDebug("Level "+level+": "+reachedLevel);
		}

		int localLowerBound = p.weight + getLowerBound(p.visited, p.forbidden, k-1-p.selected.size());

		if(localLowerBound < upperBound) {
			if(p.selected.size() < k-1) {
				Edge e = null;
				while((e = p.relevant.poll()) != null) {
					if(!createsCircle(e, p.visited)) {
						Vertex nextVertex = getOuterVertex(e, p.visited);

						Problem next = new Problem(p);
						next.visited.set(nextVertex.node);
						next.selected.add(e);
						next.weight += e.weight;

						for(Edge x : nextVertex) {
							if(!createsCircle(x, next.visited)) {
							   	next.relevant.offer(x);
							}
						}

					   	primEnum(next);
						p.forbidden.set(nextVertex.node);
					}
				}
			}
			else {
				if(p.weight < upperBound) {
					setSolution(p.weight, p.selected);
					upperBound = p.weight;
					Main.printDebug("It's getting warmer");
				}
			}
		}
		level--;
	}
	
	/**
	 * Überprüft anhand der bereits besuchten Knoten, ob die Kante e einen Kreis verursacht.
	 * O(1)
	 */
	private boolean createsCircle(Edge e, BitSet visited) {
		return visited.get(e.node1) && visited.get(e.node2);
	}

	/**
	 * Gleiche Logik wie createsCircle, aber der Name wäre irreführend.
	 * O(1)
	 */
	private boolean isForbidden(Edge e, BitSet forbidden) {
		return createsCircle(e, forbidden);
	}

	/**
	 * Liefert den noch unbesuchten (äußeren) Knoten einer Kante zurück.
	 * O(1)
	 */
	private Vertex getOuterVertex(Edge e, BitSet visited) {
		Vertex v = null;
		if(visited.get(e.node1) ^ visited.get(e.node2)) {
			if(visited.get(e.node1)) {
				v = adjList[e.node2];
			}
			else {
				v = adjList[e.node1];
			}
			return v;
		}
		return null;
	}

	/**
	 * Berechnet das Gewicht einer Menge von Kanten.
	 * O(k)
	 */
	private int getUpperBound(HashSet<Edge> solution) {
		int weight = 0;
		for(Edge e : solution) {
			weight += e.weight;
		}
		return weight;
	}

	/**
	 * Ein Vertex ist eine Sammlung von Edges.
	 */
	class Vertex extends ArrayList<Edge> implements Comparable<Vertex> {
		public final int node;
		public int value = 0;

		public Vertex(int node) {
			this.node = node;
		}

		/**
		 * Löscht alle Kanten, die eine Verbindung zum Knoten mit Index node herstellen. 
		 * O(m)
		 */
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
