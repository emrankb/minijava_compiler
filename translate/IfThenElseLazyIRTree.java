package translate;

import tree.*;
import tree.Stm;

public class IfThenElseLazyIRTree extends LazyIRTree{
	
	
	private final LazyIRTree cond, e2, e3;
	final NameOfLabel t    = NameOfLabel.generateLabel("if","then");
	final NameOfLabel f    = NameOfLabel.generateLabel("if","else");
	final NameOfLabel join = NameOfLabel.generateLabel("if","end");
	
	IfThenElseLazyIRTree(final LazyIRTree cc, final LazyIRTree aa, final LazyIRTree bb) {
		this.cond=cc; 
		this.e2=aa; 
		this.e3=bb;
	}
	
	public Exp asExp() {
		NameOfTemp r = NameOfTemp.generateTemp();
        return new RET(
            new SEQ(
                new SEQ(
                    new LABEL(t),
                    new MOVE(new TEMP(r), e2.asExp())
                ),
                new SEQ(
                    new LABEL(f),
                    new MOVE(new TEMP(r), e3.asExp())
                )
            ),
            new TEMP(r)
        );
	}

	public Stm asStm() {
		final Stm seq;
	    if (e3 == null) {
	    	seq = SEQ.fromList (cond.asCond(t, f), new LABEL(t), e2.asStm(), new LABEL(f));
	    } else {
	        seq =  SEQ.fromList(
	                cond.asCond(t, f),
	                SEQ.fromList(new LABEL(t), e2.asStm(), new JUMP(join)),
	                SEQ.fromList(new LABEL(f), e3.asStm(), new JUMP(join)),
	                new LABEL(join)
	            );
	     }
	     return seq;
	}

	public Stm asCond(NameOfLabel t, NameOfLabel f) {
		return null;
	}

}
