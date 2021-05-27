package translate;

import tree.BINOP;
import tree.CJUMP;
import tree.CONST;
import tree.Exp;
import tree.LABEL;
import tree.MOVE;
import tree.NameOfLabel;
import tree.NameOfTemp;
import tree.RET;
import tree.SEQ;
import tree.Stm;
import tree.TEMP;

public class AndCondLazyIRTree extends LazyIRTree{
	
	final LazyIRTree a, b;
	
	public AndCondLazyIRTree(LazyIRTree aa, LazyIRTree bb) {
        this.a = aa;
        this.b = bb;
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
		
		NameOfLabel test = NameOfLabel.generateLabel();
		Stm seq;
        seq = SEQ.fromList(
            new CJUMP(BINOP.AND, this.a.asExp(), CONST.TRUE, test, f),
            new LABEL(test),
            this.b.asCond(t, f));
        return seq;
	}

}
