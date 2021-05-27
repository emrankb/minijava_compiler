package sparc;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import assem.*;
import graph.AbstractInterferenceGraph;
import graph.Node;
import tree.NameOfLabel;
import tree.NameOfTemp;

public class MyFlowGraph extends AbstractInterferenceGraph<Object> {
	private Hashtable<NameOfLabel, Node> labelTable = new Hashtable<>();
	
	public MyFlowGraph(List<Instruction> instList){
		Node prevNode = null;

		for(Instruction inst : instList) {
			if(inst.isLabel())
				if(!labelTable.containsKey(((LabelInstruction) inst).label))
					labelTable.put(((LabelInstruction) inst).label, this.ensureNode(inst));
		}
		

		for(Instruction inst : instList) {
			Node n = this.ensureNode(inst);
			// If this instruction falls through to the next, add an edge
			if(prevNode != null) addEdge(prevNode, n);

			// If it is a label, remember it for when jumps are resolved
			if(inst.isLabel())
				if(!labelTable.containsKey(((LabelInstruction) inst).label))
					labelTable.put(((LabelInstruction) inst).label, n);

			// If this instruction has jumps add edges or remember them for later resolution
			if(!inst.assem.equals("\tcall `j0")  // This condition is a dirty dirty hack.
					&& inst.jumps() != null && !inst.jumps().isEmpty()) {
				for (NameOfLabel j : inst.jumps()) {
					if (!labelTable.containsKey(j)) {
						System.err.println("Unresolved label.");
					}
					else addEdge(n, labelTable.get(j));
				}
				prevNode = null; // Make sure this instruction does not fall through
			} else
				prevNode = n;
		}
	}
	
	public Set<NameOfTemp> def (final Node node) {
		if (instruction(node).def()==null) {
			// "null" means none
			return Collections.emptySet(); // Immutable
		} else {
			return Collections.unmodifiableSet (new HashSet<> (instruction (node) . def ()));
		}
	}
	
	public Set<NameOfTemp> use (final Node node) {
		if (instruction(node).use()==null) {
			// "null" means none
			return Collections.emptySet(); // Immutable
		} else {
			return Collections.unmodifiableSet (new HashSet<> (instruction (node) . use ()));
		}
	}
	
	public boolean isMove (final Node node) {
		return instruction (node). isMove ();
	}

	public Instruction instruction(Node n) {		
		return (Instruction) getTemp(n);
	}

}
