package translate;

import tree.Exp;
import tree.NameOfLabel;
import tree.Stm;

public class StmLazyIRTree extends LazyIRTree{
	private final Stm stm;
	
	public StmLazyIRTree (Stm stm) {
		this.stm = stm;
	}
	
	public Exp asExp() {
		return null;
	}

	public Stm asStm() {
		return this.stm;
	}

	public Stm asCond(NameOfLabel t, NameOfLabel f) {
		return null;
	}

}
