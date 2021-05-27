package translate;

import tree.CJUMP;
import tree.CONST;
import tree.EVAL;
import tree.Exp;
import tree.NameOfLabel;
import tree.Stm;

public class ExpLazyIRTree extends LazyIRTree{
	private final Exp exp;
	
    public ExpLazyIRTree(Exp exp) { 
    	this.exp = exp;
    } 
    
	public Exp asExp() {
		return this.exp;
	}
	
	public Stm asStm() {
		return new EVAL(exp);
	}

	public Stm asCond(NameOfLabel t, NameOfLabel f) {
		Exp exp = this.asExp();
        return new CJUMP(CJUMP.EQ, exp, CONST.TRUE, t, f);
	}
}
