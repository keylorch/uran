/**
 * 
 */
package examples.colorgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uran.formula.*;
import uran.formula.smt2.SMT2Writer;
import uran.solver.Result;
import uran.solver.Z3SMT2Solver;

/**
 * @author keylorch
 *
 */
public class ColorGraphSolver {
	
	private List<AbstractFormula> formulas = new ArrayList<AbstractFormula>();
	private Graph graph;
	private int k;

	public ColorGraphSolver(Graph graph, int k) {
		this.graph = graph;
		this.k = k;
		generateFormulas();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int k = 3;
		Graph graph = new Graph(3);
		graph.addEdge(0, 1);
		graph.addEdge(0, 2);
		graph.addEdge(1, 2);
		graph.addEdge(2, 1);
		
		//Graph graph = generateRandomGraph(1000, 1500);
		
		graph.initForSolving(k);
		graph.showConstants();
		
		
		
		ColorGraphSolver solver = new ColorGraphSolver(graph, k);
		solver.solve();
		System.out.println("vertices: " + graph.getVerticesCount());
		System.out.println("edges " + graph.getTotalEdges());
		System.out.println("K " + k);
		
		graph.showConstantsValues();
	}
	
	private static Graph generateRandomGraph(int totalVertices, int totalEdges) {
		Random rand = new Random();
		Graph graph = new Graph(totalVertices);
		for (int i = 0; i < totalEdges; i++) {
			int prev = 1, next = 1;
			while (prev == next){
				prev = rand.nextInt(totalVertices);
				next = rand.nextInt(totalVertices);
			}
			graph.addEdge(prev, next);
		}
		return graph;
	}

	private void solve() {
		long timer = System.currentTimeMillis();
		
		SMT2Writer writer = new SMT2Writer ("./graph.smt2",graph.getFactory(), formulas);
		Z3SMT2Solver solver = new Z3SMT2Solver(writer);

		if (solver.solve()==Result.SAT){
			System.out.println("SAT");
		}
		else{
			System.out.println("UNSAT");
		}
		writer.clean();
		System.out.println("Time spent:"+(System.currentTimeMillis()-timer)+" ms");
		
	}

	private void generateFormulas(){
		/**
		 * ; One color per node. 
		 */
		for(int i = 0; i < graph.getVerticesCount(); i++){
			formulas.add(FormulaBuilder.one(graph.getVerticeColors(i).toArray(new Constant[k])));
		}
		
		//For each color
		for(int color = 0; color < k; color++){
			//For each vertice
			for (int vertice = 0; vertice < graph.getVerticesCount(); vertice++){
				AbstractFormula[] excluded = graph.getExcludedVerticesForColor(vertice, color);
				if (excluded.length > 0){
					AbstractFormula implication; 
					if (excluded.length == 1){
						implication = new NegFormula(excluded[0]);
					}
					else {
						implication = FormulaBuilder.none(excluded);
					}
					
					formulas.add(
								new ImpliesFormula(
										graph.getVerticeColors(vertice).get(color),
										implication
										)
							);
				}
			}
			
		}
		
	}

}
