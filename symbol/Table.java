/* Author: Emran Kebede
 * Spring 2021
 */

package symbol;

import syntax.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import error.ErrorHandler;
import frame.Access;

class ParamsLocals {
	private LinkedHashMap<String, String> par;
	private HashMap<String, String> loc;
	private boolean beenUsed;
	private List<String> ParTypeHolder;
	private HashMap<String, Access> accesses = new HashMap<String, Access>();
	
	
	public ParamsLocals() {
		this.par = new LinkedHashMap<String, String>();
		this.loc = new HashMap<String, String>();
		this.ParTypeHolder = new ArrayList<String>();
		this.beenUsed = false;
	}

	public List<String> getMethodParamTypes() {
		this.ParTypeHolder.clear();
		// System.out.println(" hashmap - > "+this.par);
		for (String value : this.par.values()) {
			// System.out.println(" -> "+value);
			this.ParTypeHolder.add(value);
		}
		return this.ParTypeHolder;
	}
	
	public boolean containsAccess(String key) {
		return this.accesses.containsKey(key);
	}
	
	public void duplicate(ParamsLocals toBeCleared) {
		for (String key : toBeCleared.getPar().keySet()) {
			String val = toBeCleared.getPar().get(key);
			this.addPar(key, val);
		}

		for (String key : toBeCleared.getLoc().keySet()) {
			String val = toBeCleared.getLoc().get(key);
			this.addLocVar(key, val);
		}
		this.beenUsed = true;
	}

	public int paramSize() {
		return this.par.size();
	}

	public boolean beenUsed() {
		return this.beenUsed;
	}

	public LinkedHashMap<String, String> getPar() {
		return this.par;
	}

	public HashMap<String, String> getLoc() {
		return this.loc;
	}

	public void addLocVar(String a, String b) {
		this.loc.put(a, b);
	}

	public boolean locVarExist(String str) {
		if (this.loc.containsKey(str)) {
			return true;
		} else
			return false;
	}

	public void addPar(String a, String b) {
		this.par.put(a, b);
	}
	
	public boolean ParExist(String str) {
		if (this.par.containsKey(str)) {
			return true;
		} else
			return false;
	}
	
	public void addAccess(String s, Access a) {
		this.accesses.put(s, a);
	}

	public void clearALL() {
		this.loc.clear();
		this.par.clear();
	}

	public void displayLocalsAndParams() {
		System.out.println("Formal Parameters = " + this.par);
		System.out.println("Local Variables = " + this.loc);
	}

	public HashMap<String, String> getLocalsMap() {
		return this.loc;
	}

	public LinkedHashMap<String, String> getParamsMap() {
		return this.par;
	}

	public Access getAccess(String s) {
		return this.accesses.get(s);
	}
}

class FieldsMethods {
	private LinkedHashMap<String, String> f;
	private HashMap<String, String> mNameType;
	private HashMap<String, ParamsLocals> mNameData;
	private int size = 0;
	private List<ParamsLocals> LPList;
	private boolean beenUsed;

	public FieldsMethods() {
		this.f = new LinkedHashMap<String, String>();
		this.mNameType = new HashMap<String, String>();
		this.mNameData = new HashMap<String, ParamsLocals>();
		this.LPList = new ArrayList<ParamsLocals>();
		for (int i = 0; i < 100; i++)
			this.LPList.add(new ParamsLocals());
		this.beenUsed = false;
	}
	
	public int findAndGetOffset(String FieldName) {
		int offset = 0;
		int counter = 0;
		for (String key : this.f.keySet()) {
			if(key.equals(FieldName))
				offset = counter;			
			counter++;
		}
		return offset;
	}
	
	public boolean beenUsed() {
		return this.beenUsed;
	}

	public int getParamSize(String MethodName) {
		return this.mNameData.get(MethodName).paramSize();
	}

	public void duplicate(FieldsMethods toBeCleared) {
		try { 
			for (String key : toBeCleared.getF().keySet()) {
				String val = toBeCleared.getF().get(key);
				this.setField(key, val);
			}
	
			for (String key : toBeCleared.getmNameType().keySet()) {
				String val = toBeCleared.getmNameType().get(key);
				this.setMnameT(key, val);
			}
	
			for (String key : toBeCleared.getmNameData().keySet()) {
				ParamsLocals val = toBeCleared.getmNameData().get(key);
				for (int i = 0; i < this.LPList.size(); i++) {
					if (this.LPList.get(i).beenUsed()) {
	
					} else {
						this.LPList.get(i).duplicate(val);
						break;
					}
				}
				this.setMnameD(key, val);
			}
			this.beenUsed = true;
		} catch (NullPointerException e) {
			
		}
	}

	public void extend(FieldsMethods extension) {
		this.duplicate(extension);
	}

	public void setField(String key, String T) {
		this.f.put(key, T);
	}

	public ParamsLocals getPL(String methodName) {
		return this.mNameData.get(methodName);
	}

	public boolean fieldExist(String str) {
		if (this.f.containsKey(str)) {
			return true;
		} else
			return false;
	}

	public void setMnameT(String key, String T) {
		this.mNameType.put(key, T);
	}

	public boolean MExist(String str) {
		if (this.mNameType.containsKey(str)) {
			return true;
		} else
			return false;
	}

	public void setMnameD(String key, ParamsLocals D) {
		this.mNameData.put(key, D);
	}

	public HashMap<String, String> getF() {
		return this.f;
	}

	public HashMap<String, String> getmNameType() {
		return this.mNameType;
	}

	public HashMap<String, ParamsLocals> getmNameData() {
		return this.mNameData;
	}

	public List<String> paramTypes(String methodName) {
		List<String> temp = this.mNameData.get(methodName).getMethodParamTypes();
		return temp;
	}
	
	public void putAccess(String MethodName, String s, Access a) {
		this.mNameData.get(MethodName).addAccess(s, a);
	}
	
	public boolean isAccessAvail(String MethodName, String s) {
		return this.mNameData.get(MethodName).containsAccess(s);
	}
	
	public Access getAcc(String MethodName, String s) {
		return this.mNameData.get(MethodName).getAccess(s);
	}
	
	public void clearALL() {
		this.f.clear();
		this.mNameType.clear();
		this.mNameData.clear();
	}

	public void displayFieldsAndMethods() {
		System.out.println("Fields = " + this.f);
		System.out.println("Methods = " + this.mNameType);
	}

	public HashMap<String, String> getFieldsMap() {
		return this.f;
	}

	public void displayFields() {
		System.out.println("Class Variables = " + this.f);
	}

	public void allocSize(int size) {
		this.size = size;		
	}
	
	public int getClassSize() {
		return this.size;		
	}
}

public class Table implements SyntaxTreeVisitor<Void> {

	private FieldsMethods currFM;
	private FieldsMethods childFM;
	private ParamsLocals currPL;
	private ParamsLocals childPL;
	private HashMap<String, Object> classToFM;
	private String currentClass;
	private String currentMethod;
	private List<FieldsMethods> fMList;
	private FieldsMethods FMForMainClass;
	private List<ParamsLocals> LPList;
	private List<HashMap<String, String>> mapHolder;
	private boolean ext = false;
	private List<String> classList;
	public int errors;
	
	public Table() {
		this.FMForMainClass = new FieldsMethods();
		this.currFM = new FieldsMethods();
		this.currPL = new ParamsLocals();
		this.childFM = new FieldsMethods();
		this.childPL = new ParamsLocals();
		this.classToFM = new HashMap<String, Object>();
		this.currentClass = "";
		this.currentMethod = "";
		this.errors = 0;
		this.fMList = new ArrayList<FieldsMethods>();
		this.LPList = new ArrayList<ParamsLocals>();

		this.classList = new ArrayList<String>();

		for (int i = 0; i < 100; i++)
			this.fMList.add(new FieldsMethods());
		for (int i = 0; i < 100; i++)
			this.LPList.add(new ParamsLocals());

		this.mapHolder = new ArrayList<HashMap<String, String>>();
	}
	
	public void addMethodAccess(String className, String methodName, String key, Access acc) {
		
		((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).putAccess(methodName, key, acc);
	}
	
	public void setClassSize(String className, int size) {
		
		((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).allocSize(size);
	}
	
	public int classSize(String className) {
		
		return ((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).getClassSize();
	}
	
	public boolean isAccess(String className, String methodName, String key) {
		
		return ((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).isAccessAvail(methodName, key);
	}

	public Access getAccess(String className, String methodName, String key) {
		return ((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).getAcc(methodName, key);
	}
	
	public int fieldOffsetGetter(String className, String fieldName) {
		return ((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).findAndGetOffset(fieldName);
	}
	
	public int getNumOfMethodPar(String className, String methodName) {
		return ((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).getParamSize(methodName);
	}

	public List<String> paramTypes(String className, String methodName) {
		List<String> temp = ((FieldsMethods) this.classToFM.get(this.classNameCorrector(className)))
				.paramTypes(methodName);
		return temp;
	}
	
	public boolean fieldExistInClass(String className, String fieldName) {
		return ((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).fieldExist(fieldName);
	}
	
	public void PrintClassSymbolTable(String className) {
		//System.out.println("Class -> " + className);
		((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).displayFieldsAndMethods();
		//System.out.println();
	}

	public HashMap<String, String> listOfClassMethods(String className) {
		// System.out.println(this.classNameCorrector(className));
		return ((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).getmNameType();
	}

	public void PrintMethodSymbolTable(String className, String MethodName) {
		System.out.println("Method - > " + className + "." + MethodName);
		((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).displayFields();
		((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).getmNameData().get(MethodName)
				.displayLocalsAndParams();
		System.out.println();
	}

	public List<HashMap<String, String>> ListOfTablesForMethod(String className, String MethodName) {
		// System.out.println("Method - > " + className + "." + MethodName);
		this.mapHolder.clear();
		try {
			this.mapHolder.add(((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).getFieldsMap());
			this.mapHolder.add(((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).getmNameData()
					.get(MethodName).getParamsMap());
			this.mapHolder.add(((FieldsMethods) this.classToFM.get(this.classNameCorrector(className))).getmNameData()
					.get(MethodName).getLocalsMap());
			return this.mapHolder;
		} catch (ClassCastException e) {
			return this.mapHolder;
		}
	}

	public String classNameCorrector(String className) {

		String altered = "";
		Set<String> keys = this.classToFM.keySet();
		for (String key : keys) {
			if (key.equals(className) || key.startsWith(className + "EXTENDS")) {
				altered = key;
				break;
			}

		}

		// System.out.println(altered);
		return altered;
	}

	private void printT(final Statement s) {
		if (s instanceof Block) {
			for (Statement ss : ((Block) s).sl)
				ss.accept(this);
		} else {
			s.accept(this);
		}
	}

	private void printC(final Statement s) {
		if (s instanceof Block) {
			for (Statement ss : ((Block) s).sl)
				ss.accept(this);
		} else {
			s.accept(this);
		}
	}

	public void displayClass() {
		System.out.println(this.classToFM);
	}

	public Void visit(Program n) {
		if (n == null) {

		} else if (n.m == null) {

		} else {
			n.m.accept(this);
			for (ClassDecl c : n.cl) {
				c.accept(this);
				this.currFM.clearALL();
			}
			// System.out.println(this.classToFM);
			// System.out.println(this.classList);
		}
		// System.out.println(((FieldsMethods) this.classToFM.get("List")).getF());
		return null;
	}

	// Subcomponents of MainClass: Identifier i1, i2; Statement s;
	public Void visit(MainClass n) {
		// create a 2 new hashmaps 1 <String, Type> for the field names and types of the
		// class
		// and 1 <String, Object> for name of functions and there return type
		this.currentClass = n.i1.toString();
		this.currentMethod = "main";
		this.classList.add(this.currentClass);
		this.FMForMainClass.setMnameT("main", "void");
		this.classToFM.put(this.currentClass, this.FMForMainClass);
		n.i1.accept(this); // identifier: name of class
		n.i2.accept(this); // identifier: name of arguments
		n.s.accept(this); // statement: body of main
		// System.out.println("Main class done!");
		return null;
	}

	// Subcomponents of SimpleClassDecl: Identifier i; List<FieldDecl> vl;
	// List<MethodDecl> ml;
	public Void visit(final SimpleClassDecl n) {

		this.currentClass = n.i.toString();
		// System.out.println("Class -> " + this.currentClass);

		if (this.classList.contains(this.currentClass)) {
			//System.out.println("Duplicate class definition at Line: " + n.i.lineNumber);
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : duplicate class definition."));
			this.errors++;
		} else {
			this.classList.add(this.currentClass);
		}

		this.currFM.clearALL();

		// System.out.println(this.currentClass + " up up up ");
		// this.classToFM.put(currentClass, currFM);
		n.i.accept(this);
		for (FieldDecl v : n.fields)
			v.accept(this);
		
		
		for (MethodDecl m : n.methods)
			m.accept(this);
		int indexUsed = 0;
		for (int i = 0; i < this.fMList.size(); i++) {
			if (this.fMList.get(i).beenUsed()) {

			} else {
				this.fMList.get(i).duplicate(this.currFM);
				indexUsed = i;
				break;
			}
		}
		// System.out.println("index used -> " + indexUsed);
		// this.fMList.get(indexUsed).displayFields();
		this.classToFM.put(this.currentClass, this.fMList.get(indexUsed));
		this.setClassSize(this.currentClass, n.fields.size());
		return null;
	}

	// Subcomponents of ExtendingClassDecl: Identifier i, j; List<FieldDecl> vl;
	// List<MethodDecl> ml;
	public Void visit(final ExtendingClassDecl n) {
		this.ext = true;
		String extension = this.classNameCorrector(n.j.toString());
		
		if (!this.classValidity(extension)) {
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.j.lineNumber, n.j.columnNumber, "Error : " + extension + " is invalid class."));
			this.errors++;
		}
		
		this.currentClass = n.i.toString() + "EXTENDS" + extension;
		this.currFM.clearALL();
		this.childFM.clearALL();

		if (this.classList.contains(n.i.toString())) {
			//System.out.println("Duplicate class definition at Line: " + n.i.lineNumber);
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : duplicate class definition."));
			this.errors++;
		} else {
			this.classList.add(n.i.toString());
		}

		this.currFM.extend((FieldsMethods) this.classToFM.get(extension));
		n.i.accept(this);

		n.j.accept(this);

		for (final FieldDecl v : n.fields)
			v.accept(this);
		
		
		for (final MethodDecl m : n.methods)
			m.accept(this);

		int indexUsed = 0;

		for (int i = 0; i < this.fMList.size(); i++) {
			if (this.fMList.get(i).beenUsed()) {

			} else {
				this.fMList.get(i).duplicate(this.currFM);
				indexUsed = i;
				break;
			}
		}

		this.classToFM.put(this.currentClass, this.fMList.get(indexUsed));	
		try {
			int totalSIze = n.fields.size() + ((FieldsMethods) this.classToFM.get(this.classNameCorrector(extension))).getClassSize();
			this.setClassSize(this.currentClass, totalSIze);
		}
		catch (NullPointerException e) {}
		
		this.ext = false;
		return null;
	}

	// Subcomponents of MethodDecl:
	// Type t; Identifier i; List<FormalDecl> fl; List<LocalDecl> locals;
	// List<Statement>t sl; Expression e;
	public Void visit(final MethodDecl n) {

		this.currPL.clearALL();
		this.childPL.clearALL();

		this.currentMethod = n.i.toString();
		String MName, MType;
		MName = n.i.toString();

		// System.out.println(this.currentClass + " up");
		MType = n.t.getName();

		if (ext) {
			if (this.childFM.MExist(MName)) {
				//System.out.println(
						//"Method Overloading/Overriding is not allowed in Minijava at Line: " + n.i.lineNumber);
				System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : method overloading/overriding is not allowed in Minijava."));
				this.errors++;
			} else {
				this.currFM.setMnameT(MName, MType);
				this.childFM.setMnameT(MName, MType);
			}
		} else {
			if (this.currFM.MExist(MName)) {
				//System.out.println(
						//"Method Overloading/Overriding is not " + "allowed in Minijava at Line: " + n.i.lineNumber);
				System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : method overloading/overriding is not allowed in Minijava."));
				this.errors++;
			} else
				this.currFM.setMnameT(MName, MType);
		}

		if (n.fl.size() > 0) {
			n.fl.get(0).accept(this);
			for (final FormalDecl f : n.fl.subList(1, n.fl.size())) {
				// System.out.println(itemp + " -> " + f.i.toString());
				f.accept(this);
			}
			// System.out.println(" -------------------- ");
		}

		for (final LocalDecl v : n.locals)
			v.accept(this);
		int indexUsed = 0;
		for (int i = 0; i < this.LPList.size(); i++) {
			if (this.LPList.get(i).beenUsed()) {

			} else {
				this.LPList.get(i).duplicate(this.currPL);
				indexUsed = i;
				break;
			}
		}

		this.currFM.setMnameD(this.currentMethod, this.LPList.get(indexUsed));
		for (final Statement s : n.sl)
			s.accept(this);

		n.e.accept(this); // Expression e: no new line

		// Does end with a newline
		return null;
	}

	public Void visit(FieldDecl n) {
		String fName, fType;
		fName = n.i.toString();
		fType = n.t.getName();

		if (this.ext) {
			if (this.childFM.fieldExist(fName)) {
				//System.out.println("Duplicate field! at Line: " + n.i.lineNumber);
				System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : duplicate field variable."));
				this.errors++;
			} else {
				this.childFM.setField(fName, fType);
				this.currFM.setField(fName, fType);
			}
		} else if (!this.ext) {
			if (this.currFM.fieldExist(fName)) {
				//System.out.println("Duplicate field! at Line: " + n.i.lineNumber);
				System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : duplicate field variable."));
				this.errors++;
			} else
				this.currFM.setField(fName, fType);
		}
		return null;
	}

	public Void visit(LocalDecl n) {
		String lName, lType;
		lName = n.i.toString();
		lType = n.t.getName();

		if (ext) {
			try {
				if (this.childPL.locVarExist(lName) || this.childPL.ParExist(lName)) {
					//System.out.println("Local Variable redeclared at Line: " + n.i.lineNumber);
					System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : redeclaration of local variable."));
					this.errors++;
				} else {
					this.currPL.addLocVar(lName, lType);
					this.childPL.addLocVar(lName, lType);
				}
			} catch (NullPointerException e) {
				
				this.currPL.addLocVar(lName, lType);
				this.childPL.addLocVar(lName, lType);
			}
		} else {
			try {
				if (this.currPL.locVarExist(lName) || this.currPL.ParExist(lName)) {
					//System.out.println("Local Variable redeclared at Line: " + n.i.lineNumber);
					System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : redeclaration of local variable."));
					this.errors++;
				} else
					this.currPL.addLocVar(lName, lType);
			} catch (NullPointerException e) {
				//System.out.println("here");
				this.currPL.addLocVar(lName, lType);
			}
		}

		return null;
	}

	// Subcomponents of FormalDecl: Type t; Identifier i;
	public Void visit(FormalDecl n) {

		String pName, pType;
		pName = n.i.toString();
		pType = n.t.getName();
		// System.out.println(this.currentMethod + " " + pName);
		// System.out.println("In formal -> " + pName);

		// System.out.println(this.currentMethod + " " + this.currentClass);
		/*
		 * try { this.currFM.displayFieldsAndMethods();
		 * this.currFM.getPL(this.currentMethod).displayLocalsAndParams(); } catch
		 * (NullPointerException e) {}
		 */
		try {
			if (this.currFM.getPL(this.currentMethod).ParExist(pName) && !this.ext) {
				// System.out.println("chula");
			} else
				// System.out.println(this.currentMethod + " " + pName);
				this.currPL.addPar(pName, pType);
		} catch (NullPointerException e) {
			// System.out.println("null");
			// System.out.println(this.currentMethod + " " + pName);
			this.currPL.addPar(pName, pType);
		}

		// System.out.println("key dawg-> " + this.currPL.getPar());

		return null;
	}

	public Void visit(IntArrayType n) {
		// Does not end with a newline
		return null;
	}

	public Void visit(BooleanType n) {

		return null;
	}

	public Void visit(IntegerType n) {
		return null;
	}

	public Void visit(VoidType n) {
		return null;
	}

	// String s;
	public Void visit(IdentifierType n) {
		return null;
	}

	// Subcomponents of Block statement: StatementList sl;
	public Void visit(final Block n) {

		for (Statement s : n.sl)
			s.accept(this);

		return null;
	}

	// Subcomponents of If statement: Expression e; Statement s1,s2;
	public Void visit(final If n) {
		n.e.accept(this);
		printT(n.s1);
		printC(n.s2);
		return null;
	}

	// Subcomponents of While statement: Expression e, Statement s
	public Void visit(final While n) {
		n.e.accept(this);
		printC(n.s);
		return null;
	}

	// Subcomponents of Print statement: Expression e;
	public Void visit(final Print n) {
		n.e.accept(this);
		return null;
	}

	// subcomponents of Assignment statement: Identifier i; Expression e;
	public Void visit(final Assign n) {
		n.i.accept(this);
		n.e.accept(this);
		return null;
	}

	// Subcomponents of ArrayAssign: Identifier nameOfArray; Expression
	// indexInArray, Expression e;
	public Void visit(ArrayAssign n) {
		n.nameOfArray.accept(this);
		n.indexInArray.accept(this);
		n.e.accept(this);
		return null;
	}

	// Expression e1,e2;
	public Void visit(final And n) {
		n.e1.accept(this);
		n.e2.accept(this);
		return null;
	}

	// Expression e1,e2;
	public Void visit(final LessThan n) {
		n.e1.accept(this);
		n.e2.accept(this);
		return null;
	}

	// Expression e1,e2;
	public Void visit(final Plus n) {
		n.e1.accept(this);
		n.e2.accept(this);
		return null;
	}

	// Expression e1,e2;
	public Void visit(final Minus n) {
		n.e1.accept(this);
		n.e2.accept(this);
		return null;
	}

	// Expression e1,e2;
	public Void visit(final Times n) {
		n.e1.accept(this);
		n.e2.accept(this);
		return null;
	}

	// Expression expressionForArray, indexInArray;
	public Void visit(final ArrayLookup n) {
		n.expressionForArray.accept(this);
		n.indexInArray.accept(this);
		return null;
	}

	// Expression expressionForArray;
	public Void visit(final ArrayLength n) {
		n.expressionForArray.accept(this);
		return null;
	}

	// Subcomponents of Call: Expression e; Identifier i; ExpressionList el;
	public Void visit(Call n) {
		n.e.accept(this);
		n.i.accept(this);

		if (n.el.size() > 0) {
			n.el.get(0).accept(this);

			// Loop over all actuals excluding the first one
			for (Expression e : n.el.subList(1, n.el.size())) {
				e.accept(this);
			}
		}
		return null;
	}

	public Void visit(True n) {
		return null;
	}

	public Void visit(False n) {
		return null;
	}

	public Void visit(IntegerLiteral n) {
		return null;
	}

	// Subcompoents of identifier statement: String:s
	public Void visit(IdentifierExp n) {
		return null;
	}

	public Void visit(This n) {
		return null;
	}

	// Expression e;
	public Void visit(NewArray n) {
		n.e.accept(this);
		return null;
	}

	// Identifier i;
	public Void visit(NewObject n) {
		return null;
	}

	// Expression e;
	public Void visit(Not n) {
		n.e.accept(this);
		return null;
	}

	// String s;
	public Void visit(Identifier n) {
		return null;
	}

	public boolean classValidity(String className) {
		return this.classList.contains(className);
	}

	public boolean checkSubType(String left, String right) {
		// System.out.println(left + " " + right);
		if (this.classNameCorrector(right).contains("EXTENDS" + left) || left.equals(right))
			return true;
		else
			return false;
	}
}
