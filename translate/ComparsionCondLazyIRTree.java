package translate;

import tree.*;

public class ComparsionCondLazyIRTree extends LazyIRTree {
	
	final private int compOp;
    final private Exp l, r;
    
    public ComparsionCondLazyIRTree (int op, Exp l, Exp r) {
    	this.compOp = op;
        this.l = l;
        this.r = r;
    }
    
	public Exp asExp() {
        final NameOfTemp r = NameOfTemp.generateTemp();
        final NameOfLabel t = NameOfLabel.generateLabel();
        final NameOfLabel f = NameOfLabel.generateLabel();
        Exp ret;
        ret = new RET(
            SEQ.fromList(
                new MOVE(new TEMP(r), CONST.TRUE),
                asCond(t,f),
                new LABEL(f),
                new MOVE(new TEMP(r), CONST.FALSE),
                new LABEL(t)),
            new TEMP(r));
        
        return ret;
    }
	
    public Stm asStm() { 
        final NameOfLabel t = NameOfLabel.generateLabel();
        final NameOfLabel f = NameOfLabel.generateLabel();
        Stm seq;
        seq = SEQ.fromList(
            asCond(t,f),
            new LABEL(t), 
            new LABEL(f));
        
        return seq;
    }
    
    public Stm asCond(NameOfLabel t, NameOfLabel f) {
    	return new CJUMP(compOp, l, r, t, f);
	}

}
