/* Author: Emran Kebede
 * Spring 2021
 */

package translate;

import java.util.ArrayList;
import frame.Frame;
import symbol.Table;
import syntax.*;
import tree.*;

public class Translate implements SyntaxTreeVisitor<LazyIRTree> {
	
	// c functions called at runtime
	private static final NAME alloc_object = new NAME(new NameOfLabel("alloc_object"));
    private static final NAME new_arr = new NAME(new NameOfLabel("new_arr"));
    private static final NAME print_int = new NAME(new NameOfLabel("print_int"));
    
	private String currentClass;
	private String currentMethod;
	private Frame currentFrame;
	private Table scope;
	
	private final Frame fact;
	
	public ArrayList<Frag> frags = new ArrayList<Frag>();
	
	public Translate(Table table, Frame fact) {
		this.scope = table;
		this.fact = fact;
    }
	
	private Exp variableAccess(String id) {
		// local + parameteres 
        if(this.scope.isAccess(this.currentClass, this.currentMethod, id))
            return this.scope.getAccess(this.currentClass, this.currentMethod, id).exp(new TEMP(this.currentFrame.FP()));
       
        // variables in the class holding the function 
        if(this.scope.fieldExistInClass(this.currentClass, id)){
            int offset = this.scope.fieldOffsetGetter(this.currentClass, id);
            return new MEM(
                    new BINOP(
                            BINOP.PLUS,
                            this.currentFrame.formals.get(0).exp(new TEMP(this.currentFrame.FP())),
                            new CONST(offset)
                    )
            );
        }
        return null;
    }
	
	private void addFrag(Frame frame, LazyIRTree body){
        this.frags.add(new Frag(frame.procEntryExit1(body.asStm()), frame));
    }
	
	public LazyIRTree visit(Program n) {
	    for (ClassDecl c : n.cl)
			c.accept(this);
			
        n.m.accept(this);
        return null;
	}

	public LazyIRTree visit(MainClass n) {
		this.currentClass = n.i1.toString();
		
		 @SuppressWarnings({ "unchecked", "rawtypes" })
		Frame newFrame = fact.newFrame(
	                new NameOfLabel(n.i1.s+"$main"),
	                new ArrayList());
		 
	    addFrag(newFrame, 
	    		new StmLazyIRTree(
	            new SEQ(
	            new SEQ(
	            new LABEL(n.i1.s+"$main"), n.s.accept(this).asStm()), 
	            new JUMP(newFrame.temp))));
	    this.currentClass = "";
	    return null;
	}

	public LazyIRTree visit(SimpleClassDecl n) {
		this.currentClass = n.i.toString();
		for (FieldDecl v : n.fields)
			v.accept(this);
		for (MethodDecl m : n.methods)
			m.accept(this);
		
		this.currentClass = "";
		return null;
	}

	public LazyIRTree visit(ExtendingClassDecl n) {
		this.currentClass = n.i.toString();
		for (final FieldDecl v : n.fields)
			v.accept(this);

		for (final MethodDecl m : n.methods)
			m.accept(this);
		this.currentClass = "";
		return null;
	}

	public LazyIRTree visit(MethodDecl n) {
		this.currentMethod = n.i.toString();
		ArrayList<Boolean> paramsEscape = new ArrayList<Boolean>();
		paramsEscape.add(false);

        for(int i = 0; i < n.fl.size(); i++)
        	paramsEscape.add(false);
        
        // for each function declaration, a new fragment
        // of Tree code will be kept for the functions body.
        NameOfLabel name = new NameOfLabel(this.currentClass, n.i.s);
        Frame newFrame = fact.newFrame(name, paramsEscape);

        this.currentFrame = newFrame;
        this.scope.addMethodAccess(this.currentClass, this.currentMethod, "this", newFrame.formals.get(0));
        // parameters are passed via registers
        for(int i = 0; i < n.fl.size(); i++)
        	this.scope.addMethodAccess(this.currentClass, this.currentMethod, n.fl.get(i).i.s, newFrame.formals.get(i+1));

        for(LocalDecl v : n.locals)
        	this.scope.addMethodAccess(this.currentClass, this.currentMethod, v.i.s, newFrame.allocLocal(true));

        Exp returnExp;
        if(n.sl.size() > 0){
            Stm stm = n.sl.get(0).accept(this).asStm();
            for(int i = 1; i < n.sl.size(); i++)
                stm = new SEQ(stm, n.sl.get(i).accept(this).asStm());
            returnExp = new RET(stm, n.e.accept(this).asExp());
        }
        else returnExp = n.e.accept(this).asExp();

        this.frags.add(new Frag(newFrame.procEntryExit1(
                new SEQ(new SEQ(new LABEL(name), new MOVE(new TEMP(newFrame.RV()), returnExp)), new JUMP(newFrame.temp))), newFrame));
        this.currentMethod = "";
        return null;
	}

	public LazyIRTree visit(LocalDecl n) {	
		return null;
	}
	
	public LazyIRTree visit(FieldDecl n) {
		return null;
	}

	public LazyIRTree visit(FormalDecl n) {	
		return null;
	}

	public LazyIRTree visit(IdentifierType n) {
		return null;
	}

	public LazyIRTree visit(IntArrayType n) {	
		return null;
	}

	public LazyIRTree visit(BooleanType n) {
		return null;
	}

	public LazyIRTree visit(IntegerType n) {
		return null;
	}

	public LazyIRTree visit(VoidType n) {
		return null;
	}

	public LazyIRTree visit(Block n) {
		Stm stm = new ExpLazyIRTree(CONST.ZERO).asStm();
		
		if (n.sl.size() > 0) {
			n.sl.get(0).accept(this).asStm();

			// Loop excluding the first one
			for (Statement s : n.sl.subList(1, n.sl.size())) {
				stm = new SEQ(stm, s.accept(this).asStm());
			}
		}
        return new StmLazyIRTree(stm);
	}

	public LazyIRTree visit(If n) {
		return new IfThenElseLazyIRTree(n.e.accept(this), n.s1.accept(this), n.s2.accept(this));
	}

	public LazyIRTree visit(While n) {
		NameOfLabel cond = NameOfLabel.generateLabel("cond");
        NameOfLabel body = NameOfLabel.generateLabel("body");
        NameOfLabel join = NameOfLabel.generateLabel("join");

        return new StmLazyIRTree(SEQ.fromList(
               new LABEL(cond), n.e.accept(this).asCond(body, join),
               new LABEL(body), n.s.accept(this).asStm(),
               new JUMP(cond),
               new LABEL(join)));
	}

	public LazyIRTree visit(Print n) {
		return new StmLazyIRTree(
			   new ExpLazyIRTree(
			   new CALL(print_int, n.e.accept(this).asExp())).asStm());
	}

	public LazyIRTree visit(Assign n) {
		return new StmLazyIRTree(
			   new MOVE(this.variableAccess(n.i.s), n.e.accept(this).asExp()));
	}
	
	public LazyIRTree visit(ArrayAssign n) {	
		return new StmLazyIRTree (
			   new MOVE( 
			   new MEM(
	           new BINOP(BINOP.PLUS,
	           new BINOP(BINOP.MUL,
	           new BINOP(BINOP.PLUS, CONST.ONE, n.indexInArray.accept(this).asExp()),
	           new CONST(fact.wordSize())), this.variableAccess(n.nameOfArray.s))), n.e.accept(this).asExp()));
	}

	public LazyIRTree visit(And n) {
		return new AndCondLazyIRTree(
               new ExpLazyIRTree(n.e1.accept(this).asExp()),
               new ExpLazyIRTree(n.e2.accept(this).asExp()));
	}

	public LazyIRTree visit(LessThan n) {
		return new ComparsionCondLazyIRTree(CJUMP.LT, n.e1.accept(this).asExp(), n.e2.accept(this).asExp());
	}

	public LazyIRTree visit(Plus n) {
		return new ExpLazyIRTree(
			   new BINOP(BINOP.PLUS, n.e1.accept(this).asExp(), n.e2.accept(this).asExp()));
	}

	public LazyIRTree visit(Minus n) {
		return new ExpLazyIRTree(
			   new BINOP(BINOP.MINUS, n.e1.accept(this).asExp(), n.e2.accept(this).asExp()));
	}

	public LazyIRTree visit(Times n) {
		return new ExpLazyIRTree(
			   new BINOP(BINOP.MUL, n.e1.accept(this).asExp(), n.e2.accept(this).asExp()));
	}

	public LazyIRTree visit(ArrayLookup n) {
		return new ExpLazyIRTree(
			   new MEM(
			   new BINOP(BINOP.PLUS, n.expressionForArray.accept(this).asExp(),
               new BINOP(BINOP.MUL,
               new CONST(fact.wordSize()),
               new BINOP(BINOP.PLUS, CONST.ONE, n.indexInArray.accept(this).asExp())))));
	}

	public LazyIRTree visit(ArrayLength n) {
		return new ExpLazyIRTree(
			   new MEM(n.expressionForArray.accept(this).asExp()));
	}

	public LazyIRTree visit(Call n) {
		ArrayList<Exp> par = new ArrayList<Exp>();
        par.add(n.e.accept(this).asExp());

        if (n.el.size() > 0) {
        	par.add(n.el.get(0).accept(this).asExp());

			// Loop over all actuals excluding the first one
			for (Expression e : n.el.subList(1, n.el.size())) {
				par.add(e.accept(this).asExp());
			}
	        return new ExpLazyIRTree(
	        	   new CALL(
	        	   new NAME(
	        	   new NameOfLabel(n.getReceiverClassName(), n.i.s)), par));
		}

        return new ExpLazyIRTree(
        	   new CALL(
        	   new NAME(
        	   new NameOfLabel(n.getReceiverClassName(), n.i.s)), par));
	}

	public LazyIRTree visit(IntegerLiteral n) {
		return new ExpLazyIRTree(
			   new CONST(n.i));
	}

	public LazyIRTree visit(True n) {
		return new ExpLazyIRTree(CONST.TRUE);
	}

	public LazyIRTree visit(False n) {
		return new ExpLazyIRTree(CONST.FALSE);
	}

	public LazyIRTree visit(IdentifierExp n) {	
		return new ExpLazyIRTree(this.variableAccess(n.s));
	}

	public LazyIRTree visit(This n) {
		return new ExpLazyIRTree(this.currentFrame.formals.get(0).exp(
			   new TEMP(this.currentFrame.FP())));
	}

	public LazyIRTree visit(NewArray n) {
		TEMP returnTemp = new TEMP(NameOfTemp.generateTemp());
		// Call an external function newArr to get space on the heap.
        return new ExpLazyIRTree( 
        	   new RET(
               new MOVE(returnTemp, 
               new CALL(new_arr, n.e.accept(this).asExp(),
               new CONST(fact.wordSize()))), returnTemp));
	}

	public LazyIRTree visit(NewObject n) {
		int size = this.scope.classSize(n.i.s) * fact.wordSize();
        TEMP returnTemp = new TEMP(NameOfTemp.generateTemp());
        // Call an external function newObj to get space on the heap.
        return new ExpLazyIRTree(
        	   new RET(
        	   new MOVE(returnTemp,
               new CALL(alloc_object, 
               new CONST(size))),
        	   returnTemp));
	}

	public LazyIRTree visit(Not n) {
		return new ExpLazyIRTree(
			   new BINOP(BINOP.MINUS, CONST.ONE, n.e.accept(this).asExp()));
	}

	public LazyIRTree visit(Identifier n) {
		return new ExpLazyIRTree(this.variableAccess(n.s));
	}
}
