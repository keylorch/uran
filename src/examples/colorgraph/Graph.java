package examples.colorgraph;

import java.util.List;

import uran.formula.AbstractFormula;
import uran.formula.Constant;
import uran.formula.FunctionFactory;
import uran.formula.type.Bool;
import uran.formula.value.BoolValue;
import uran.formula.value.Value;

import java.util.ArrayList;

public class Graph {
	
	private ArrayList<ArrayList<Integer>> edges;
	private ArrayList<ArrayList<Constant>> colors;	
	private FunctionFactory factory = new FunctionFactory(512,0.75f);
	private int totalEdges;
	
	public Graph(int n){
		edges = new ArrayList<ArrayList<Integer>>(n);
		for(int i=0; i < n; i++){
			edges.add(new ArrayList<Integer>());
		}
	}
	
	public FunctionFactory getFactory(){
		return factory;
	}
	
	public void initForSolving(int k){
		colors = new ArrayList<ArrayList<Constant>>(edges.size());
		for(int i=0; i < edges.size(); i++){
			colors.add(new ArrayList<Constant>());
			for (int j = 0; j < k; j++) {
				colors.get(i).add(factory.createConstant("V"+i+"_C"+j,new Bool()));
			}
		}
	}
	
	public ArrayList<Integer> getEdges(int vertice){
		return edges.get(vertice);
	}
	
	public void addEdge(int fromVertice, int toVertice){
		edges.get(fromVertice).add(toVertice);
		totalEdges++;
	}
	
	public void addEdge(int fromVertice, int[] toVertice){
		for (int to : toVertice){
			edges.get(fromVertice).add(to);
		}
	}
	
	public int getVerticesCount(){
		return edges.size();
	}

	public void showConstants() {
		System.out.println("-------- CONSTANTS ---------");
		for (ArrayList<Constant> vert : colors){
			for(Constant constant : vert){
				System.out.println(constant.name());
			}
		}
		System.out.println("----------------------------");
		
	}
	
	public void showConstantsValues() {
		System.out.println("-------- CONSTANT VALUES ---------");
		for (ArrayList<Constant> vert : colors){
			for(Constant constant : vert){
				Value value = factory.getValue(constant.name());
				if (value.IsBool()){
					BoolValue bv = (BoolValue) value;
					System.out.println(constant.name() + ": " + bv.getValue());
				}
				
			}
		}
		System.out.println("----------------------------");
		
	}
	
	public ArrayList<Constant> getVerticeColors(int index){
		return colors.get(index);
	}

	public AbstractFormula[] getExcludedVerticesForColor(int vertice, int color) {
		List<Constant> constants = new ArrayList<Constant>();
		for(int vecino : edges.get(vertice)){
			constants.add(colors.get(vecino).get(color));
		}
		//Find the other way around
		for(int i = 0; i < edges.size(); i++){
			if (i != vertice){
				for(int vecino : edges.get(i)){
					if (vecino == vertice)
					constants.add(colors.get(i).get(color));
				}
			}
		}
		return constants.toArray(new Constant[constants.size()]);
	}

	public int getTotalEdges() {
		return totalEdges;
	}

	public void setTotalEdges(int totalEdges) {
		this.totalEdges = totalEdges;
	}
}
