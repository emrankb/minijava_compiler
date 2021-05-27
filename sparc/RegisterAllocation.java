package sparc;

import graph.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegisterAllocation {
    static final String [] inArgColors = { "%i1", "%i2", "%i3", "%i4", "%i5" };
    static final String [] outArgColors = { "%o1", "%o2", "%o3", "%o4", "%o5" };
    static final String [] localColors = { "%l0", "%l1", "%l2", "%l3", "%l4", "%l5", "%l6", "%l7" };
    static final String [] globalColors = { "%g0", "%g1", "%g2", "%g3", "%g4", "%g5", "%g6", "%g7" }; // %g0 will never be used

    final private ArrayList<String> availableColors = new ArrayList<>();
    private MyInterferenceGraph g;
    private SparcFrame frame;
    public int calleeSavesUsed = 0;
    
    public RegisterAllocation(SparcFrame frame, MyInterferenceGraph g){
        this.g = g;
        this.frame = frame;
        availableColors.addAll(Arrays.asList(localColors));
        availableColors.addAll(Arrays.asList(globalColors));
        if(frame.formals.size() < 6) {
            availableColors.addAll(frame.formals.size() + 1, Arrays.asList(inArgColors));
            availableColors.addAll(frame.formals.size() + 1, Arrays.asList(outArgColors));
        }
    }

    public void allocRegs(){
    	final List<Node> nodes = g.nodes();
        for(Node n : nodes){
            if(!frame.tempMap.containsKey(g.getTemp(n))){
                final String color = selectColor(n);
                if(color == null)
                    System.err.println("ERROR: Register spilling.");
                else {
                    frame.tempMap.put(g.getTemp(n), color);
                    for(int i = 0; i < globalColors.length; i++) {
                        if (globalColors[i].equals(color)) {
                            calleeSavesUsed++;
                            break;
                        }
                    }
                }
            }
        }
    }

    private String selectColor(Node n){
        for(int i = 0; i < availableColors.size(); i++){
            boolean found = true;
            for(int j = 0; j < n.adj().size(); j++){
                Node tmpNode = n.adj().get(j);
                if(frame.tempMap.containsKey(g.getTemp(tmpNode))
                    && frame.tempMap.get(g.getTemp(tmpNode)).equals(availableColors.get(i))) {
                    found = false;
                    break;
                }
            }
            if(found) return availableColors.get(i);
        }
        return null;
    }
}