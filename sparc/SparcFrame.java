/* Author: Emran Kebede
 * Spring 2021
 */

package sparc;

import java.util.ArrayList;
//import java.util.List;

import assem.Instruction;
import assem.LabelInstruction;
import assem.OperationInstruction;
import frame.Access;
import frame.Frame;
import tree.BINOP;
import tree.CALL;
import tree.CONST;
import tree.Exp;
import tree.JUMP;
import tree.LABEL;
import tree.MEM;
import tree.NAME;
import tree.NameOfLabel;
import tree.NameOfTemp;
import tree.SEQ;
import tree.Stm;
import tree.TEMP;

class InFrame extends Access{

	public int offset;
	
	public InFrame(int offset) {
		this.offset = offset;
	}
	
	public Exp exp(Exp framePtr) {
		return new MEM(new BINOP(BINOP.PLUS, framePtr, new CONST(offset)));
	}
}

class InReg extends Access{
	
	public NameOfTemp temp;
	
	public InReg(NameOfTemp temp) { 
		this.temp = temp; 
	}
	
	public Exp exp(Exp framePtr) {
		return new TEMP(temp);
	}
}

public class SparcFrame extends frame.Frame {
    
	
    // 32 registers available at any one time
 	static final NameOfTemp [] globalRegs = new NameOfTemp[8];
 	static final NameOfTemp [] localRegs = new NameOfTemp[8];
    static final NameOfTemp [] inputRegs = new NameOfTemp[8];
    static final NameOfTemp [] outputRegs = new NameOfTemp[8]; 
    // name of registers
    static {
    	for(int i = 0; i < 8; i++) {
    		globalRegs[i] = new NameOfTemp("%g"+i);
    		localRegs[i] = new NameOfTemp("%l"+i);
    		inputRegs[i] = new NameOfTemp("%i"+i);
    		outputRegs[i] = new NameOfTemp("%o"+i);
    	}
    }
    
    /*
	Do not use %o6, %o7, %i6, or %i7.
	Use %g1 - %g7 for your global variables.
	Use %l0 - %l7 for your local variables
	*/
    
    // Since all procedures have less than six parameters -> size = 6
 	static final NameOfTemp [] incomingArgs = new NameOfTemp[6];
    static final NameOfTemp [] outgoingArgs = new NameOfTemp[6];
    static {
    	for(int i = 0; i < 6; i++) {
    		incomingArgs[i] = inputRegs[i];
    		outgoingArgs[i] = outgoingArgs[i];
    	}
    }
    // A list of registers that the called procedure (callee) must pre-
    // serve unchanged (or save and restore);
    static final NameOfTemp [] calleeSaves = new NameOfTemp[7];
    // use global regs to preserve unchanged
	static {
		for(int i = 0; i < 7; i++) {
		    // %g0 is always zero -> globalRegs[0] 
			calleeSaves[i] = globalRegs[i+1];
		}
	}
	
	/* specialregs */
	
	// The zero register
    static final NameOfTemp ZERO = globalRegs[0];
    
    // The stack pointer
    static final NameOfTemp SP = new NameOfTemp("%sp");
    
    // The frame pointer
    static final NameOfTemp FP = new NameOfTemp("%fp");
    
	public SparcFrame(NameOfLabel n, ArrayList<Access> form) {
		super(n, form);
		temp = NameOfLabel.generateLabel("exit");
		
		for(int i = 0; i < 8; i++){
            this.tempMap.put(globalRegs[i], "%g" + i);
            this.tempMap.put(localRegs[i], "%l" + i);
            this.tempMap.put(inputRegs[i], "%i" + i);
            this.tempMap.put(outputRegs[i], "%o" + i);
        }
		this.tempMap.put(FP, "%fp");
		this.tempMap.put(SP, "%sp");
	}

	public Frame newFrame(NameOfLabel name, ArrayList<Boolean> formals) {
		ArrayList<Access> form = new ArrayList<Access>();
        for(int i = 0; i < formals.size(); i++){
            if(formals.get(i))
                System.err.println("ERROR: Minijava Parameters can't escape");
            if(i >= 6)
                System.err.println("ERROR: Only 6 or less Parameters allowed");
            else
            	form.add(new InReg(incomingArgs[i]));
        }
        return new SparcFrame(name, form);
	}

	public Access allocLocal(boolean escape) {
		// Assuming that every variable escapes 
		if(escape)
			// negative offset because memory grows downward 
            return new InFrame(-wordSize()*(++this.frameResidentVar));
        return new InReg(localRegs[this.registerVar++]);
	}

	public NameOfTemp FP() {
		return FP;
	}

	public int wordSize() {
		return 4;
	}

	public NameOfTemp RV() {
		return inputRegs[0];
	}

	public ArrayList<Instruction> procEntryExit3(ArrayList<Instruction> body) {
		int stackSize = (wordSize()*(frameResidentVar + 1)) + 96;
		stackSize += 8 - (stackSize % 8);
        body.add(1,
                new OperationInstruction("\tsave\t`s0, -"+stackSize+", `d0", SP, SP));
        body.add(new LabelInstruction(temp));
        if(name.toString().equals("main")) {
            body.add(new OperationInstruction("\tclr\t%o0"));
            body.add(new OperationInstruction("\tmov\t1, %g1"));
            body.add(new OperationInstruction("\tta\t0x90"));
        }
   
        body.add(new OperationInstruction("\tret"));
        body.add(new OperationInstruction("\trestore"));
        
        return body;
	}

	public Stm procEntryExit1(Stm body) {
		return new SEQ(new SEQ(new LABEL(name), body), new JUMP(temp));
	}

	public Exp externalCall(String func, ArrayList<Exp> args) {
		return new CALL(new NAME(new NameOfLabel(func)), args);
	}
	/*
	List<NameOfTemp> L(NameOfTemp h, List<NameOfTemp> t) {return new ArrayList<NameOfTemp>(h,t);}
	static List<NameOfTemp> returnSink = L(ZERO, L(RA, L(SP, calleeSaves)));
	
	static List<NameOfTemp> returnSink =
		new List<NameOfTemp>(ZERO, new List<NameOfTemp>(RAx, new List<NameOfTemp>(SP, calleeSaves)));

	static ArrayList<Instruction> append(ArrayList<Instruction> a, ArrayList<Instruction> b) {

		if (a==null) return b;
		else {
			ArrayList<Instruction> p;
			a.addAll(b);
			return a;
		}
	}
	
	public ArrayList<Instruction> procEntryExit2(ArrayList<Instruction> body) {
		return append(body, new ArrayList<Instruction>(new OperationInstruction("", null, returnSink),null));
	}*/

	@Override
	public ArrayList<Instruction> procEntryExit2(ArrayList<Instruction> body) {
		// TODO Auto-generated method stub
		return null;
	}
}
