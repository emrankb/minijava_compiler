package main;

import java.util.List;
import java.io.*;
import java.util.ArrayList;

import assem.Instruction;
import error.ErrorHandler;
import frame.Access;
import parser.MiniJavaParser;
import parser.ParseException;
import sparc.Codegen;
import sparc.MyFlowGraph;
import sparc.MyInterferenceGraph;
import sparc.RegisterAllocation;
import sparc.SparcFrame;
import symbol.Table;
import symbol.TypeChecker;
import translate.Frag;
import translate.Translate;
import tree.LABEL;
import tree.NameOfLabel;
import tree.Stm;
//import canon.Main;

public class Compile {
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws ParseException, IOException {		
		final boolean verbose;
		final String fileName;
		InputStream input = null;
		
		File file = new File("err.txt");
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		System.setErr(ps);
		
		if(System.getProperty("verbose") != null) {
			verbose = true;
		} else {
			verbose = false;
		}
		
		if (args.length < 1) {
			System.out.println("Please, run me with a file name argument!");
			System.exit(0);
		}
		
		fileName = args[0];
		ErrorHandler.filename = fileName;
        
		try {
			input = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			System.out.println("File " + fileName + " not found.");
			System.exit(0);
		}

		boolean parseError = false;

		int semanticErr = 0;

		final MiniJavaParser parser = new MiniJavaParser(input);
		parser.disable_tracing();
		syntax.Program p = null;
		
		try {
			p = parser.Goal();
			//syntax.PrettyPrint printer = new syntax.PrettyPrint();
			Table symTable = new Table();

			// printer.visit(p);
			try {
				symTable.visit(p);
			} catch (Throwable t) {
				
			}
			//symTable.PrintClassSymbolTable("MyVisitor");
			//System.out.println(symTable.fieldOffsetGetter("MyVisitor", "c"));
			//symTable.PrintMethodSymbolTable("Visitor", "visit");
			try {
				TypeChecker checker = new TypeChecker(symTable);
				checker.visit(p);
				semanticErr = checker.getErrNum();
			} catch (Throwable t) {
				
			}
			// symTable.displayClass();
		} catch (ParseException e) {
			parseError = true;
		}

		if (parseError)
			System.out.println("filename=" + fileName + ", errors=1");
		else
			System.out.println("filename=" + fileName + ", errors=" + semanticErr);
		
		if(semanticErr != 0) {
			System.exit(0);
		}
		
		Table symTable = new Table();

		symTable.visit(p);
		
		Translate t = new Translate(symTable,
	                  new SparcFrame(
	                  new NameOfLabel("factory"), 
	                  new ArrayList<Access>()));
		try {
	    	t.visit(p);
		} catch (Throwable tr) {
			
		}
		
		String prefix = "";
		String[] str = fileName.split("\\.");
		if(str.length == 2)
			prefix = str[0];
		else if (str.length == 3)
			prefix = "." + str[1];
		
	    FileWriter verboseFileWriter = null;
	    if(verbose) verboseFileWriter = new FileWriter(prefix + "-12.txt");
	    final FileWriter SparcFileWriter = new FileWriter(prefix + ".s");
	    SparcFileWriter.write("        .global start\n");

        for(Frag f : t.frags){
            ArrayList<Instruction> instList = new ArrayList<>();

            List<Stm> stmList = canon.Main.transform(f.body);
            Codegen cg = null;
            try {
            	cg = new Codegen((SparcFrame) f.frame);
            } catch (Throwable tr) {
			}
            if (((LABEL)stmList.get(0)).label.toString().contains("main")) {
            	try {
                	SparcFileWriter.write("start:\n");
                } catch (NullPointerException e) {
                }
            }
            if(verbose) {
            	try {
                	verboseFileWriter.write( "!  Procedure fragment " + ((LABEL)stmList.get(0)).label.toString() + "\n");
                } catch (Throwable tr) {}
            }
            
            for(Stm s : stmList) {	
                if(verbose) {
                    if (s instanceof LABEL) verboseFileWriter.write("\n");
                    verboseFileWriter.write(s.toString());
                }
                
                instList = cg.codegen(s);
            }
            instList = f.frame.procEntryExit3(instList);
            RegisterAllocation ra = new RegisterAllocation((SparcFrame) f.frame, new MyInterferenceGraph(new MyFlowGraph(instList)));
            ra.allocRegs();
            
            for(Instruction inst : instList){
                // Don't write mov instructions with the same dest and src register
            	
                if(inst.isMove() && (inst.use().get(0) != null && inst.def().get(0) != null)
                    && (f.frame.tempMap.get(inst.use().get(0)) == f.frame.tempMap.get(inst.def().get(0))))
                            continue;
                try {
                	SparcFileWriter.write(inst.format(f.frame.tempMap) + "\n");
                } catch (NullPointerException e) {
                }
            }
            
            if(verbose) {
            	verboseFileWriter.write("!  End fragment\n\n\n");
            }
        }
        try { SparcFileWriter.close(); if(verbose) verboseFileWriter.close();}
        catch (IOException e) { System.err.println("ERROR:main: IOException: " + e.toString()); }    
	}
}
