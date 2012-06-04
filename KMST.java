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
	private int upperBound = Integer.MAX_VALUE;
	private int i = 0;

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

        /*
         * Adjazenzmatrix erstellen
         */
		for(Edge e : edges) {
			adjMatrix[e.node1][e.node2] = e.weight;
			adjMatrix[e.node2][e.node1] = e.weight;
		}

        /*
         * Adjazenzliste (=Liste von Edges) erstellen
         */
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

        /*
         * Ausgabe
         */
		for(int i=0; i<numNodes; i++) {
			for(int j=0; j<numNodes; j++) {
				Main.printDebug(adjMatrix[i][j]+"\t");
			}
			Main.printDebug("\n");
		}

		for(Vertex v : vertices) {
			Main.printDebug(v.node + " " +v);
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
}

class Vertex extends ArrayList<Edge> {
	public final int node;
  public final int value;

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
