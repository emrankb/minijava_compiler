/* Author: Emran Kebede
 * Spring 2021
 */

package frame;

import tree.Exp;
import tree.NameOfLabel;
import tree.NameOfTemp;
import tree.Stm;
import java.util.ArrayList;
import java.util.HashMap;

import assem.Instruction;


public abstract class Frame {
	public abstract Frame newFrame(NameOfLabel name, ArrayList<Boolean> formals);
	public final NameOfLabel name;
	public final ArrayList<Access> formals;
	public NameOfLabel temp;
	protected int frameResidentVar = 0;
	protected int registerVar = 0;
	
	public Frame(NameOfLabel n, ArrayList<Access> form) {
		name = n;
		formals = form;
	}
	
	public abstract Access allocLocal(boolean escape);
	
	public abstract NameOfTemp FP();
	
	public abstract int wordSize();
	
	public abstract NameOfTemp RV();
	
	public abstract ArrayList<Instruction> procEntryExit3(ArrayList<Instruction> body);
	
	public abstract Stm procEntryExit1(Stm body);
	
	public abstract ArrayList<Instruction> procEntryExit2(ArrayList<Instruction> body);
	
	public abstract Exp externalCall(String func, ArrayList<Exp> args);
	
	// register assignment (or perhaps just the name) of every temp
	public final HashMap<NameOfTemp,String> tempMap = new HashMap<NameOfTemp,String>() {
        @java.lang.Override
        public String get(Object t) {
            if (containsKey(t)) 
            	return super.get(t); 
            else
            	return t.toString();
        }
    };
}
