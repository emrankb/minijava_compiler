package translate;

import tree.Exp;
import tree.NameOfLabel;
import tree.Stm;

public abstract class LazyIRTree {
	public abstract Exp asExp();
    public abstract Stm asStm();
    public abstract Stm asCond(NameOfLabel t, NameOfLabel f);
}
