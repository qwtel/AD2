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
		for(int i=0; i<adjList.length; i++) {
			Collections.sort(adjList[i]);
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
		for(Vertex v : adjList) {
			HashSet<Edge> primsSolution = prims(v);
			int weight = getUpperBound(primsSolution);

			if(weight < upperBound) {
				upperBound = weight;
				setSolution(upperBound, primsSolution);
			}
		}

		/*for(Vertex v : adjList) {
			TreeProblem p = new TreeProblem(v);
			Main.printDebug(p);
			primsEnum(p);
		}*/

		/*
		HashSet<Edge> fixedEdges = new HashSet<Edge>(k-1);
		problem(fixedEdges, 0, 0);
		*/
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

	private void primsEnum(TreeProblem p) {
		Vertex v = p.v;
		HashSet<Edge> fixed = p.fixed;
		int weight = p.weight;
		HashSet<Edge> forbidden = p.forbidden;
		HashSet<Edge> relevant = p.relevant;
		HashSet<Edge> shortest = p.shortest;
		BitSet visited = p.visited;

		// Bounding
		int localLowerBound = weight; //+ getLowerBound(shortest);
		if(localLowerBound < upperBound) {
			if(fixed.size() == k-1) {
				Main.printDebug(p);
				if(weight < upperBound) {
					setSolution(weight, new HashSet<Edge>(fixed));
					upperBound = weight;
				}
				return;
			}

			if(localLowerBound < upperBound) {
				for(Edge x : v) {
					if(!createsCircle(x, visited) && !forbidden.contains(x)) {
						relevant.add(x);
					}
					else {
						relevant.remove(x);
					}
				}

				// Branching
				for(Edge e : relevant) {
					Vertex newVertex = getOuterVertex(e, visited);

					TreeProblem p0 = new TreeProblem(v, fixed, weight, forbidden, relevant, shortest, visited);
					TreeProblem p1 = new TreeProblem(newVertex, fixed, weight, forbidden, relevant, shortest, visited);

					p1.fixed.add(e);
					p1.visited.set(newVertex.node);
					if(!p1.shortest.remove(e)) {
						p1.shortest.remove(Collections.max(p1.shortest));
					}
					p1.weight += e.weight;
					p1.relevant.remove(e);

					p0.forbidden.add(e);
					if(p0.shortest.remove(e)) {
						for(Edge s : edges) {
							if(!fixed.contains(s) && !forbidden.contains(s)) {
								p0.shortest.add(s);
								break;
							}
						}
					}
					p0.relevant.remove(e);

					primsEnum(p1);
					primsEnum(p0);
				}
			}
		}
	}
	
	private int getLowerBound(HashSet<Edge> shortest) {
		int weight = 0;
		for(Edge e : shortest) {
			weight += e.weight;
		}
		return weight;
	}

	class TreeProblem {
		public Vertex v;
		public HashSet<Edge> fixed; 
		public int weight;
		public HashSet<Edge> forbidden;
		public HashSet<Edge> relevant;
		public HashSet<Edge> shortest;
		public BitSet visited;

		/**
		 * @param v Neu hinzugekommener Knoten.
		 * @param fixed Bereits fixierte Kanten.
		 * @param weight Gewicht der fixierten Kanten.
		 * @param forbidden Kanten die nicht Teil des Subproblems sind.
		 * @param relevant Nachbarknoten der fixierten Kanten, welche keinen Kreis bilden.
		 * @param shortest Die kürzesten Kanten die nicht verboten oder fixiert sind und zusammen mit fixed k-1 ausmachen.
		 * @param visited Bereits besuchte Knoten.
		 */
		public TreeProblem(Vertex v, HashSet<Edge> fixed, int weight, HashSet<Edge> forbidden, HashSet<Edge> relevant, HashSet<Edge> shortest, BitSet visited) {
			this.v = v;
			this.fixed = new HashSet<Edge>(fixed);
			this.weight = weight;
			this.forbidden = new HashSet<Edge>(forbidden);
			this.shortest = new HashSet<Edge>(shortest);
			this.relevant = new HashSet<Edge>(relevant);
			this.visited =	(BitSet)visited.clone();
		}

		public TreeProblem(Vertex v) {
			this.v = v;
			fixed = new HashSet<Edge>();
			weight = 0;
			forbidden = new HashSet<Edge>();
			relevant = new HashSet<Edge>();
			visited = new BitSet(numNodes);

			shortest = new HashSet<Edge>();
			for(int i=0; i<k; i++) {
				shortest.add(edges.get(i));
			}
		}

		public String toString() {
			return "\nVertex: " + v.node + 
				"\n Fixed: " + weight + ": " + fixed + 
				"\n Forbidden: " + forbidden + 
				"\n Relevant: " + relevant + 
				"\n Shortest: " + shortest + 
				"\n Visited: " + visited;

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

	class Problem {
		public HashSet<Edge> fixed;
		public int index;
		public int weight;

		public Problem(HashSet<Edge> fixed, int index, int weight) {
			this.fixed = new HashSet<Edge>(fixed);
			this.index = index;
			this.weight = weight;
		}
	}

	private void problem(HashSet<Edge> fixed, int index, int weight) {

		LinkedList<Problem> problems = new LinkedList<Problem>();
		problems.offer(new Problem(fixed, index, weight));

		while(problems.size() != 0) {

			Problem p = problems.pollLast();
			fixed = p.fixed;
			index = p.index;
			weight = p.weight;

			// Bounding
			int localLowerBound = weight + getLowerBound(index, k-fixed.size());

			if(localLowerBound < upperBound && index + k-1 < numEdges) {
				if(fixed.size() == k-1) {
					if(weight < upperBound && validSolution(fixed)) {
						setSolution(weight, new HashSet<Edge>(fixed));
						upperBound = weight;
					}
					continue;
				}

				// Branching
				if(localLowerBound < upperBound) {
					Edge e = edges.get(index);
					Problem p0 = new Problem(fixed, index+1, weight);
					Problem p1 = new Problem(fixed, index+1, weight);
					p1.fixed.add(e);
					p1.weight += e.weight;

					problems.offer(p1);
					problems.offer(p0);
				}
			}
		}
	}

	private boolean validSolution(Set<Edge> edges) {
		DisjointSet unionField = new DisjointSet(numNodes);

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
			Main.printDebug("alles ok");
			return true;
		}

		return false;
	}

	class DisjointSet {
		private int[] parent;
		private BitSet marked;
		private int numSets;

		public DisjointSet(int numNodes) {
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
	}
}
