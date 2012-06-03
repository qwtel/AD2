package ads1ss12.pa;
      
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * Klasse zum Berechnen eines k-MST mittels Branch-and-Bound. Hier sollen Sie
 * Ihre L&ouml;sung implementieren.
 */
public class KMST extends AbstractKMST {

	private PriorityQueue<Edge> edges;
	private Vertex[] vertices;
 	private int[][] adjMatrix;
	private int k;
	private int numEdges;
	private int numNodes;
	private int i = 0;
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
		this.k = k;
		this.numNodes = numNodes;
		this.numEdges = numEdges;
		this.edges = new PriorityQueue<Edge>(edges);
		this.vertices = new Vertex[numNodes];
		int[][] adjMatrix = new int[numNodes][numNodes];

		for(Edge e : edges) {
			adjMatrix[e.node1][e.node2] = e.weight;
			adjMatrix[e.node2][e.node1] = e.weight;
		}

		for(Edge e : edges) {
			if(vertices[e.node1] == null) {
				vertices[e.node1] = new Vertex(e.node1);
			}
			if(vertices[e.node2] == null) {
				vertices[e.node2] = new Vertex(e.node2);
			}
			vertices[e.node1].add(e);
			vertices[e.node2].add(e);
		}

		for(int i=0; i<numNodes; i++) {
			for(int j=0; j<numNodes; j++) {
				System.out.print(adjMatrix[i][j]+"\t");
			}
			System.out.print("\n");
		}

		for(Vertex v : vertices) {
			System.out.println(v.node + " " +v);
		}
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
		HashSet<Edge> fixedEdges = null;
		BitSet fixedVertices = null;
		PriorityQueue<Edge> allowedEdges = null;
		HashSet<Edge> forbiddenEdges = null;
		
		Edge e = null;
		while((e = edges.poll()) != null) {
			fixedEdges = new HashSet<Edge>();
			fixedEdges.add(e);

			fixedVertices = new BitSet(numNodes);
			fixedVertices.set(e.node1); 
			fixedVertices.set(e.node2); 

            allowedEdges = new PriorityQueue<Edge>();
			for(Edge x : vertices[e.node1]) {
				if(!(allowedEdges.contains(x)) && !(x.equals(e))) {
					allowedEdges.add(x);
				}
			}
			for(Edge x : vertices[e.node2]) {
				if(!(allowedEdges.contains(x)) && !(x.equals(e))) {
					allowedEdges.add(x);
				}
			}

			int lowerBound = e.weight;
			forbiddenEdges = new HashSet<Edge>();
			p(fixedEdges, fixedVertices, allowedEdges, forbiddenEdges, lowerBound, 2); 
		}
		System.out.println(i);
	}

	private void p() {
		/*
		  
		// Bounding
		// berechne für P' lokale untere Schranke L' mit Dualheuristik;
		// Fall L' >= U bracht nicht weiter verfolgt werden!
		if(L' <= U) {
			// berechne für P' gültige heur. Lösung -> obere Schranke U';
			if(U' < U) {
				U = U'; // neue beste Lösung gefunden
				// entferne alle Subprobleme mit lokaler unterer Schranke >= U;
			}
			// Fall L' >= U braucht nicht weiter verfolgt werden!
			if(L' < U) {
				// Branching
				// partitioniere P' in Teilprobleme P1, P2;
				// p(P1);
				// p(P2);
			}
		}

		*/

	}

	private void p(HashSet<Edge> fixedEdges, BitSet fixedVertices, PriorityQueue<Edge> allowedEdges, HashSet<Edge> forbiddenEdges, int lowerBound, int size) {

		/*if(lowerBound >= upperBound) {
			// Cut
			return;
		}*/
		if(size == k) {
			// Lösung gefunden..
			// System.out.println(fixedEdges + " " + lowerBound);
			HashSet<Edge> t = new HashSet<Edge>(fixedEdges);
			setSolution(lowerBound, t);
			upperBound = lowerBound;

			System.out.println(fixedEdges);
			System.out.println(fixedVertices);
			System.out.println(allowedEdges);
			System.out.println(forbiddenEdges);
			System.out.println(lowerBound + "\n");

			return;
		}
		Edge e;
		while((e = allowedEdges.poll()) != null) {
			HashSet<Edge> fixedEdges2 = new HashSet<Edge>(fixedEdges);
			fixedEdges2.add(e);

			int lowerBound2 = lowerBound;
			lowerBound2 += e.weight;

			// hier könnte gleich überprüft werden

			BitSet fixedVertices2 = (BitSet)fixedVertices.clone();
			fixedVertices2.set(e.node1);
			fixedVertices2.set(e.node2);

			PriorityQueue<Edge> allowedEdges2 = new PriorityQueue<Edge>(allowedEdges);
			allowedEdges2.remove(e);
			// Problem: kanten die kreise bilden befinden sich noch in allowedEdges

			boolean n1 = fixedVertices2.get(e.node1);
			boolean n2 = fixedVertices2.get(e.node2);

			// Neu hinzugekommenen Knoten bestimmen
			Vertex v = null;
			if(n1) {
				v = vertices[e.node2];
			}
			else if(n2) {
				v = vertices[e.node1];
			}

			for( Edge x : v) {
				i++;
				// kante erlaubt?
				if(!forbiddenEdges.contains(x) && !(fixedEdges.contains(x))) {
					boolean x1 = fixedVertices2.get(x.node1);
					boolean x2 = fixedVertices2.get(x.node2);

					// entsteht ein kreis?
					if(!(x1 && x2)) {
						// Kante wird zur Front hinzugefügt
						allowedEdges2.add(x);
					}
					else {
						// Durch den neuen Knoten würde durch diese Kante ein Kreis entstehen
						allowedEdges2.remove(x);
					}
				}
			}

			HashSet<Edge> forbiddenEdges2 = new HashSet<Edge>(forbiddenEdges);
			forbiddenEdges2.add(e);

			p(fixedEdges2, fixedVertices2, allowedEdges2, forbiddenEdges, lowerBound2, size+1);
			p(fixedEdges, fixedVertices, allowedEdges, forbiddenEdges2, lowerBound, size);
		}
	}
}

class Vertex extends ArrayList<Edge> {
	public final int node;

	public Vertex(int node) {
		this.node = node;
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

	public int hashCode() {
		return node;
	}
}
