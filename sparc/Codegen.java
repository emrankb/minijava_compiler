package sparc;

import assem.Instruction;

import assem.LabelInstruction;
import assem.MoveInstruction;
import assem.OperationInstruction;
import tree.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Codegen {
    private SparcFrame frame;
    private ArrayList<Instruction> instList = new ArrayList<>();
    private ArrayList<NameOfTemp> tmpTemps = new ArrayList<>();
    private int tempsUsed;

    public Codegen(SparcFrame frame) {
        this.frame = frame;
    }

    public ArrayList<Instruction> codegen(Stm s){
        munchStm(s);
        return instList;
    }
    
    private NameOfTemp getTemp(){
        tempsUsed++;
        if(tempsUsed >= tmpTemps.size()){
            NameOfTemp newTemp = new NameOfTemp("tmp"+tempsUsed);
            tmpTemps.add(newTemp);
            return newTemp;
        }
        return tmpTemps.get(tempsUsed);
    }

    private void freeTemps(int numFreed){ tempsUsed-=numFreed; }

    private static boolean isImmediateSize(int v){
        return (v >= -4096 && v <= 4096);
    }

    private void munchStm(Stm stm){
        if(stm instanceof LABEL) munchStm((LABEL) stm);
        else if(stm instanceof EVAL) munchStm((EVAL) stm);
        else if(stm instanceof MOVE) munchStm((MOVE) stm);
        else if(stm instanceof JUMP) munchStm((JUMP) stm);
        else if(stm instanceof CJUMP) munchStm((CJUMP) stm);
        else System.err.println("ERROR:munchStm(Stm): Statement is not valid");
    }

    private void munchStm(LABEL s){
        instList.add(new LabelInstruction(s.label));
    }

    private void munchStm(EVAL s){
        munchExp(s.exp, getTemp());
        freeTemps(1);
    }

    private void munchStm(MOVE s){
        if(s.dst instanceof TEMP){
        	//System.err.println(s.dst);
            final TEMP dst = (TEMP) s.dst;

            if(s.src instanceof CONST) {
                final int val = ((CONST) s.src).value;

                if (isImmediateSize(val))
                    instList.add(new MoveInstruction("\tmov\t" + val + ", `d0", dst.temp, null));
                else {
                    instList.add(new OperationInstruction("\tsethi\t%hi(" + val + "), `d0", dst.temp, null));
                    instList.add(new OperationInstruction("\tor\t`s0, %lo(" + val + "), `d0", dst.temp, dst.temp));
                }
            }
            else if(s.src instanceof MEM){
                final MEM src = (MEM) s.src;

                if(src.exp instanceof BINOP &&
                        (((BINOP) src.exp).binop == BINOP.PLUS
                                || ((BINOP) src.exp).binop == BINOP.MINUS)){
                    final BINOP exp = (BINOP) src.exp;
                    final String op = (exp.binop == BINOP.PLUS)? "+" : "-";

                    if(exp.left instanceof  CONST && isImmediateSize(((CONST)exp.left).value)){
                        final int val = ((CONST)exp.left).value;
                        instList.add(new OperationInstruction("\tld\t[`s0 "+op+" "+val+"], `d0",
                                dst.temp, munchExp(exp.right, getTemp())));
                        freeTemps(1);
                    }
                    else if(exp.right instanceof  CONST && isImmediateSize(((CONST)exp.right).value)){
                        final int val = ((CONST)exp.right).value;
                        instList.add(new OperationInstruction("\tld\t[`s0 "+op+" "+val+"], `d0",
                                dst.temp, munchExp(exp.left, getTemp())));
                        freeTemps(1);
                    }
                    else {
                        instList.add(new OperationInstruction("\tld\t[`s0 " + op + " `s1], `d0",
                                dst.temp, munchExp(exp.left, getTemp()), munchExp(exp.right, getTemp())));
                        freeTemps(2);
                    }
                }
                else {
                    instList.add(new OperationInstruction("\tld\t[`s0], `d0",
                            dst.temp, munchExp(s.src, getTemp())));
                    freeTemps(1);
                }
            }
            else {
                instList.add(new MoveInstruction("\tmov\t`s0, `d0",
                        dst.temp, munchExp(s.src, getTemp())));
                freeTemps(1);
            }
        }
        else if(s.dst instanceof MEM) {
        	//System.err.println(s.dst);
            final MEM ptr = (MEM) s.dst;

            if (ptr.exp instanceof BINOP &&
                    (((BINOP) ptr.exp).binop == BINOP.PLUS
                            || ((BINOP) ptr.exp).binop == BINOP.MINUS)) {
                final BINOP exp = (BINOP) ptr.exp;
                final String op = (exp.binop == BINOP.PLUS) ? "+" : "-";

                if (exp.left instanceof CONST && isImmediateSize(((CONST) exp.left).value)) {
                    final int val = ((CONST) exp.left).value;
                    instList.add(new OperationInstruction("\tst\t`s1, [`s0 "+op+" "+val+"]",
                            (NameOfTemp) null, munchExp(exp.right, getTemp()), munchExp(s.src, getTemp())));
                    freeTemps(2);
                }
                else if (exp.right instanceof CONST && isImmediateSize(((CONST) exp.right).value)) {
                    final int val = ((CONST) exp.right).value;
                    instList.add(new OperationInstruction("\tst\t`s1, [`s0 "+op+" "+val+"]",
                            (NameOfTemp) null, munchExp(exp.left, getTemp()), munchExp(s.src, getTemp())));
                    freeTemps(2);
                }
                else {
                    final ArrayList<NameOfTemp> srcList = new ArrayList<>();
                    srcList.add(munchExp(exp.left, getTemp()));
                    srcList.add(munchExp(exp.right, getTemp()));
                    srcList.add(munchExp(s.src, getTemp()));
                    instList.add(new OperationInstruction("\tst\t`s2, [`s0 "+op+" `s1]",
                            new ArrayList<>(), srcList));
                    freeTemps(3);
                }
            }
            else {
                instList.add(new OperationInstruction("\tst\t`s1, [`s0]",
                        (NameOfTemp) null, munchExp(ptr.exp, getTemp()), munchExp(s.src, getTemp())));
                freeTemps(2);
            }
        }
        else {
        	System.err.println("ERROR:munchStm(MOVE): Invalid MOVE.");
        	System.err.println(s.dst);
        }
    }

    private void munchStm(JUMP s){
        instList.add(new OperationInstruction("\tba\t`j0",
                new ArrayList<>(), new ArrayList<>(), s.targets));
        instList.add(new OperationInstruction("\tnop"));
    }

    private void munchStm(CJUMP s){
        if(s.left instanceof CONST){
            final int val = ((CONST)s.left).value;

            if(isImmediateSize(val)){
                instList.add(new OperationInstruction("\tcmp\t`s0, "+val+"",
                        (NameOfTemp) null, munchExp(s.right, getTemp())));
                freeTemps(1);
            }
            else {
                final NameOfTemp tmp = getTemp();
                instList.add(new OperationInstruction("\tsethi\t%hi("+val+"), `d0", tmp, null));
                instList.add(new OperationInstruction("\tor\t`s0, %lo("+val+"), `d0", tmp, tmp));
                instList.add(new OperationInstruction("\tcmp\t`s1, `s0",
                        (NameOfTemp) null, tmp, munchExp(s.right, getTemp())));
                freeTemps(2);
            }
        }
        else if(s.right instanceof CONST){
            final int val = ((CONST)s.right).value;

            if(isImmediateSize(val)){
                instList.add(new OperationInstruction("\tcmp\t`s0, "+val+"",
                        (NameOfTemp) null, munchExp(s.left, getTemp())));
                freeTemps(1);
            }
            else {
                final NameOfTemp tmp = getTemp();
                instList.add(new OperationInstruction("\tsethi\t%hi("+val+"), `d0", tmp, null));
                instList.add(new OperationInstruction("\tor\t`s0, %lo("+val+"), `d0", tmp, tmp));
                instList.add(new OperationInstruction("\tcmp\t`s1, `s0",
                        (NameOfTemp) null, tmp, munchExp(s.left, getTemp())));
                freeTemps(2);
            }
        }
        else {
            instList.add(new OperationInstruction("\tcmp\t`s0, `s1",
                    (NameOfTemp) null, munchExp(s.left, getTemp()), munchExp(s.right, getTemp())));
            freeTemps(2);
        }
        final ArrayList<NameOfLabel> jumpList = new ArrayList<>();
        jumpList.add(s.iftrue);
        jumpList.add(s.iffalse);

        if(s.relop == CJUMP.EQ)
            instList.add(new OperationInstruction("\tbe\t`j0",
                    new ArrayList<>(), new ArrayList<>(), jumpList));
        else if(s.relop == CJUMP.NE)
            instList.add(new OperationInstruction("\tbne\t`j0",
                    new ArrayList<>(), new ArrayList<>(), jumpList));
        else if(s.relop == CJUMP.LT)
            instList.add(new OperationInstruction("\tbl\t`j0",
                    new ArrayList<>(), new ArrayList<>(), jumpList));
        else if(s.relop == CJUMP.GT)
            instList.add(new OperationInstruction("\tbg\t`j0",
                    new ArrayList<>(), new ArrayList<>(), jumpList));
        else if(s.relop == CJUMP.LE)
            instList.add(new OperationInstruction("\tble\t`j0",
                    new ArrayList<>(), new ArrayList<>(), jumpList));
        else if(s.relop == CJUMP.GE)
            instList.add(new OperationInstruction("\tbge\t`j0",
                    new ArrayList<>(), new ArrayList<>(), jumpList));

        instList.add(new OperationInstruction("\tnop"));
    }

    private NameOfTemp munchExp(Exp e, NameOfTemp r){
        if (e instanceof TEMP) return ((TEMP) e).temp;
        if (e instanceof NAME) return null;
        if (e instanceof CONST) return munchExp((CONST) e, r);
        if (e instanceof BINOP) return munchExp((BINOP) e, r);
        if (e instanceof MEM) return munchExp((MEM) e, r);
        if (e instanceof CALL) return munchExp((CALL) e, r);
        System.err.println("ERROR:munchExp(Exp, NameOfTemp): Bad expression.");
        return null;
    }

    private NameOfTemp munchExp(CONST e, NameOfTemp r){
        if(isImmediateSize(e.value))
            instList.add(new MoveInstruction("\tmov\t"+e.value+", `d0", r, null));
        else{
            instList.add(new OperationInstruction("\tsethi\t%hi("+e.value+"), `d0", r, null));
            instList.add(new OperationInstruction("\tor\t`s0, %lo("+e.value+"), `d0", r, r));
        }
        return r;
    }

    private NameOfTemp munchExp(BINOP e, NameOfTemp r){
        final String op;
        if(e.binop == BINOP.PLUS)           op = "\tand\t";
        else if(e.binop == BINOP.MINUS)     op = "\tsub\t";
        else if(e.binop == BINOP.MUL)       op = "\tsmul\t";
        else if(e.binop == BINOP.RSHIFT)    op = "\tsrl\t";
        else if(e.binop == BINOP.LSHIFT)    op = "\tsll\t";
        else if(e.binop == BINOP.AND)       op = "\tand\t";
        else if(e.binop == BINOP.OR)        op = "\tor\t";
        else if(e.binop == BINOP.XOR)       op = "\txor\t";
        else{
            System.err.println("ERROR:munchExp(BINOP, NameOfLabel): Invalid operation");
            return null;
        }

        if(e.left instanceof CONST){
            final int val = ((CONST) e.left).value;
            if(isImmediateSize(val))
                instList.add(new OperationInstruction(op+"`s0, "+val+", `d0",
                        r, munchExp(e.right, r)));
            else {
                instList.add(new OperationInstruction("\tsethi\t%hi("+val+"), `d0", r, null));
                instList.add(new OperationInstruction("\tor\t`s0, %lo("+val+"), `d0", r, r));
                instList.add(new OperationInstruction(op+"`s0, `s1, `d0", r, r, munchExp(e.right, getTemp())));
                freeTemps(1);
            }
        }
        else if(e.right instanceof CONST){
            final int val = ((CONST) e.right).value;
            if(isImmediateSize(val))
                instList.add(new OperationInstruction(op+"`s0, "+val+", `d0",
                        r, munchExp(e.left, r)));
            else {
                final NameOfTemp tmp = getTemp();
                instList.add(new OperationInstruction("\tsethi\t%hi("+val+"), `d0", r, null));
                instList.add(new OperationInstruction("\tor\t`s0, %lo("+val+"), `d0", r, tmp));
                instList.add(new OperationInstruction(op+"`s1, `s0, `d0", r, r, munchExp(e.left, tmp)));
                freeTemps(1);
            }
        }
        else {
            instList.add(new OperationInstruction(op+"`s0, `s1, `d0",
                    r, munchExp(e.left, getTemp()), munchExp(e.right, getTemp())));
            freeTemps(2);
        }
        return r;
    }

    private NameOfTemp munchExp(MEM e, NameOfTemp r){
        if(e.exp instanceof BINOP &&
                (((BINOP) e.exp).binop == BINOP.PLUS
                        || ((BINOP) e.exp).binop == BINOP.MINUS)) {
            final BINOP exp = (BINOP) e.exp;
            final String op = (exp.binop == BINOP.PLUS) ? "+" : "-";

            if(exp.left instanceof  CONST && isImmediateSize(((CONST)exp.left).value)){
                final int val = ((CONST) exp.left).value;
                instList.add(new OperationInstruction("\tld\t[`s0 "+op+" "+val+"], `d0",
                        r, munchExp(exp.right, r)));
            }
            else if(exp.right instanceof  CONST && isImmediateSize(((CONST)exp.right).value)){
                final int val = ((CONST) exp.right).value;
                instList.add(new OperationInstruction("\tld\t[`s0 "+op+" "+val+"], `d0",
                        r, munchExp(exp.left, r)));
            }
            else{
                instList.add(new OperationInstruction("\tld\t[`s0 "+op+" `s1], `d0",
                        r, munchExp(exp.left, getTemp()), munchExp(exp.right, getTemp())));
                freeTemps(2);
            }
        }
        else {
            instList.add(new OperationInstruction("\tld\t[`s0], `d0",
                    r, munchExp(e.exp, r)));
        }
        return r;
    }

    @SuppressWarnings("static-access")
	private NameOfTemp munchExp(CALL e, NameOfTemp r){
        for(int i = 0; i < e.UNUSEDargs.size() && i < 6; i++) {
            instList.add(new MoveInstruction("\tmov\t`s0, `d0",
                    frame.outgoingArgs[i], munchExp(e.UNUSEDargs.get(i), frame.outgoingArgs[i])));
        }
        instList.add(new OperationInstruction("\tcall `j0",
                new ArrayList<>(), new ArrayList<>(), Arrays.asList(((NAME)e.func).label)));
        instList.add(new OperationInstruction("\tnop"));
        instList.add(new OperationInstruction("\tmov\t`s0, `d0",
                r, frame.outgoingArgs[0]));
        return r;
    }
}