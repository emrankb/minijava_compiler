package sparc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import assem.Instruction;
import graph.AbstractInterferenceGraph;
import graph.Node;
import tree.NameOfTemp;

public class MyInterferenceGraph extends AbstractInterferenceGraph<NameOfTemp>{
	private MyFlowGraph fg;
	// Table of temp lists that are live for their corresponding instruction nodes
	private Hashtable<Node, ArrayList<NameOfTemp>> liveTable = new Hashtable<>();
	// These are reset every time analyzeLiveliness is called from the constructor
	private NameOfTemp currTemp;
	private HashSet<Node> visited = new HashSet<>();
	// Record the life of temps in a DFS manner
	private void analyzeLiveliness(Node n){
		// Base cases
		if(currTemp == null || fg == null) return;
		if(visited.contains(n) || (fg.def(n) != null && fg.def(n).contains(currTemp))) return;
		// Visit
		visited.add(n);
		// Operate and traverse
		for(Node next : n.pred()) {
			if(visited.contains(next)) continue;
			if(!liveTable.containsKey(next))
				liveTable.put(next, new ArrayList<>());
			if(!liveTable.get(next).contains(currTemp))
				liveTable.get(next).add(currTemp);
			analyzeLiveliness(next);
		}
	}
	public MyInterferenceGraph(MyFlowGraph g){
		fg = g;
		// Analyze liveliness of temporaries
		for(Node instNode : g.nodes()) {
			if(g.use(instNode) != null)
				for (NameOfTemp t : g.use(instNode)) {
					if(t == null) continue;
					currTemp = t;
					visited.clear();
					analyzeLiveliness(instNode);
				}
		}
		// Construct interference graph
		for(Node instNode : g.nodes()){
			if(g.def(instNode) != null) {
				for (NameOfTemp d : g.def(instNode)) {
					if(d == null) continue;
					Node src = this.ensureNode(d);
					
					if (((Instruction)g.getTemp(instNode)).isMove()) {
						ArrayList<NameOfTemp> liveTemps = liveTable.get(instNode);
						if(liveTemps != null)
							for (NameOfTemp liveTemp : liveTable.get(instNode))
								if (!((Instruction)g.getTemp(instNode)).uses(liveTemp))
									addEdge(src, this.ensureNode(liveTemp));
					} else {
						ArrayList<NameOfTemp> liveTemps = liveTable.get(instNode);
						if(liveTemps != null)
							for (int i = 0; i < liveTemps.size(); i++)
								addEdge(src, this.ensureNode(liveTemps.get(i)));
					}
				}
			}
		}
	}

}
