/* Author: Emran Kebede
 * Spring 2021
 */

package symbol;

import syntax.And;
import syntax.ArrayAssign;
import syntax.ArrayLength;
import syntax.ArrayLookup;
import syntax.Assign;
import syntax.Block;
import syntax.BooleanType;
import syntax.Call;
import syntax.ClassDecl;
import syntax.Expression;
import syntax.ExtendingClassDecl;
import syntax.False;
import syntax.FieldDecl;
import syntax.FormalDecl;
import syntax.Identifier;
import syntax.IdentifierExp;
import syntax.IdentifierType;
import syntax.If;
import syntax.IntArrayType;
import syntax.IntegerLiteral;
import syntax.IntegerType;
import syntax.LessThan;
import syntax.LocalDecl;
import syntax.MainClass;
import syntax.MethodDecl;
import syntax.Minus;
import syntax.NewArray;
import syntax.NewObject;
import syntax.Not;
import syntax.Plus;
import syntax.Print;
import syntax.Program;
import syntax.SimpleClassDecl;
import syntax.Statement;
import syntax.SyntaxTreeVisitor;
import syntax.This;
import syntax.Times;
import syntax.True;
import syntax.Type;
import syntax.VoidType;
import syntax.While;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import error.ErrorHandler;

@SuppressWarnings("rawtypes")
public class TypeChecker implements SyntaxTreeVisitor {
	private Table table;
	private String currentClass;
	private String currentMethod;
	private int errorsDetected;

	public TypeChecker(Table t) {
		this.table = t;
		this.currentClass = "";
		this.currentMethod = "";
		this.errorsDetected = t.errors;
	}

	public int getErrNum() {
		return this.errorsDetected;
	}

	@SuppressWarnings("unchecked")
	private void printT(final Statement s) {
		if (s instanceof Block) {
			for (Statement ss : ((Block) s).sl)
				ss.accept(this);
		} else {
			s.accept(this);
		}
	}

	@SuppressWarnings("unchecked")
	private void printC(final Statement s) {
		if (s instanceof Block) {
			for (Statement ss : ((Block) s).sl)
				ss.accept(this);
		} else {
			s.accept(this);
		}
	}

	@SuppressWarnings("unchecked")
	public String visit(Program n) {
		if (n == null) {

		} else if (n.m == null) {

		} else {
			n.m.accept(this);
			for (ClassDecl c : n.cl)
				c.accept(this);
		}
		return null;
	}

	// Subcomponents of MainClass: Identifier i1, i2; Statement s;
	@SuppressWarnings("unchecked")
	public String visit(MainClass n) {

		this.currentClass = n.i1.toString();
		n.i1.accept(this); // identifier: name of class

		n.i2.accept(this); // identifier: name of arguments

		n.s.accept(this); // statement: body of main

		return null;
	}

	// Subcomponents of SimpleClassDecl: Identifier i; List<FieldDecl> vl;
	// List<MethodDecl> ml;
	@SuppressWarnings("unchecked")
	public String visit(final SimpleClassDecl n) {

		this.currentClass = n.i.toString();
		n.i.accept(this);
		for (FieldDecl v : n.fields)
			v.accept(this);
		for (MethodDecl m : n.methods)
			m.accept(this);
		// Does end with a newline
		return null;
	}

	// Subcomponents of ExtendingClassDecl: Identifier i, j; List<FieldDecl> vl;
	// List<MethodDecl> ml;
	@SuppressWarnings("unchecked")
	public String visit(final ExtendingClassDecl n) {
		this.currentClass = n.i.toString();
		n.i.accept(this);
		n.j.accept(this);
		for (final FieldDecl v : n.fields)
			v.accept(this);
		for (final MethodDecl m : n.methods)
			m.accept(this);
		// Does end with a newline
		return null;
	}

	// Subcomponents of MethodDecl:
	// Type t; Identifier i; List<FormalDecl> fl; List<LocalDecl> locals;
	// List<Statement>t sl; Expression e;
	@SuppressWarnings("unchecked")
	public String visit(final MethodDecl n) {
		this.currentMethod = n.i.toString();
		if (n.fl.size() > 0) {
			n.fl.get(0).accept(this);
			// Loop over all actuals excluding the first one
			for (final FormalDecl f : n.fl.subList(1, n.fl.size())) {
				f.accept(this);
			}
		}
		for (final LocalDecl v : n.locals)
			v.accept(this);
		for (final Statement s : n.sl)
			s.accept(this);
		HashMap<String, String> classMethods;
		String returnType = "";
		classMethods = this.table.listOfClassMethods(this.currentClass);
		returnType = classMethods.get(this.currentMethod);
		// Return statement
		if(n.e.accept(this).toString().equals(returnType))
			return null;
		// return type mismatch from function return type
		System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e.lineNumber, n.e.columnNumber, "Error : return type mismatch from function return type."));
		this.errorsDetected++;	
		return null;
	}

	public String visit(FieldDecl n) {
		return null;
	}

	public String visit(LocalDecl n) {
		return null;
	}

	// Subcomponents of FormalDecl: Type t; Identifier i;
	public String visit(FormalDecl n) {
		return null;
	}

	public String visit(IntArrayType n) {
		return null;
	}

	public String visit(BooleanType n) {
		return null;
	}

	public String visit(IntegerType n) {
		return null;
	}

	public String visit(VoidType n) {
		return null;
	}

	// String s;
	public String visit(IdentifierType n) {
		return null;
	}

	// Subcomponents of Block statement: StatementList sl;
	@SuppressWarnings("unchecked")
	public String visit(final Block n) {
		for (Statement s : n.sl)
			s.accept(this);
		return null;
	}

	// Subcomponents of If statement: Expression e; Statement s1,s2;
	@SuppressWarnings("unchecked")
	public String visit(final If n) {

		if (!(n.e.accept(this) instanceof BooleanType)) {
			//System.out.println(
					//"Type mismatch: cannot convert the given expression to boolean type at Line: " + n.e.lineNumber);
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e.lineNumber, n.e.columnNumber, "Error : cannot convert the given expression to boolean type."));
			this.errorsDetected++;
		}

		printT(n.s1);
		printC(n.s2);
		return null;
	}

	// Subcomponents of While statement: Expression e, Statement s
	@SuppressWarnings("unchecked")
	public String visit(final While n) {
		if (!(n.e.accept(this) instanceof BooleanType)) {
			//System.out.println(
					//"Type mismatch: cannot convert the given expression to boolean type at Line: " + n.e.lineNumber);
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e.lineNumber, n.e.columnNumber, "Error : cannot convert the given expression to boolean type."));
			this.errorsDetected++;
		}
		printC(n.s);
		return null;
	}

	// Subcomponents of Print statement: Expression e;
	@SuppressWarnings("unchecked")
	public Type visit(final Print n) {
		if (!(n.e.accept(this) instanceof IntegerType)) {
			//System.out.println("Only int value can be printed at Line: " + n.e.lineNumber);
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e.lineNumber, n.e.columnNumber, "Error : only int value can be printed."));
			this.errorsDetected++;
			return null;
		}
		return Type.THE_INTEGER_TYPE;
	}

	// subcomponents of Assignment statement: Identifier i; Expression e;
	@SuppressWarnings("unchecked")
	public String visit(final Assign n) {
		String mapKey = n.i.toString();
		String keyType = "";
		String expType = "";
		boolean keyFound = false;
		// System.out.println("Assign");
		// look for the identifier in the symbol table
		List<HashMap<String, String>> currTable;
		// what do i want ?
		// I need maps of all legally available variable names
		// this.table.PrintClassSymbolTable(this.currentClass);
		// this.table.PrintMethodSymbolTable(this.currentClass, this.currentMethod);
		currTable = this.table.ListOfTablesForMethod(this.currentClass, this.currentMethod);
		// System.out.println(currTable);
		// System.out.println(currTable.size());
		// System.out.println(currTable.get(2));

		if (currTable.size() > 0) {
			for (int i = currTable.size() - 1; i >= 0; i--) {
				keyFound = currTable.get(i).containsKey(mapKey);
				if (keyFound) {
					// System.out.println("Key " + mapKey + " Found!");
					keyType = currTable.get(i).get(mapKey);
					// System.out.println("The type of it is " + keyType);
					break;
				}
			}
		}
		if (!keyFound) {
			//System.out.println(mapKey + " is never declared at line:" + n.i.lineNumber);
			this.errorsDetected++;
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : " + mapKey + " is never declared."));
			n.i.accept(this);
			n.e.accept(this);
		} else {
			n.i.accept(this);
			// System.out.println(n.i.toString());
			Object a = n.e.accept(this);
			if (a instanceof IntegerType) {
				expType = Type.THE_INTEGER_TYPE.getName();
			} else if (a instanceof BooleanType) {
				expType = Type.THE_BOOLEAN_TYPE.getName();
			} else if (a instanceof IdentifierType) {
				expType = a.toString();
				// this.table.checkSubType( , );
				// System.out.println(mapKey);
				if (this.table.checkSubType(keyType, expType)) {
					// System.out.println(keyType + " = " + expType);
				} else {
					//System.out.println("Type mismatch: cannot convert from " + expType + " to " + keyType + " at Line: "
							//+ n.lineNumber);
					System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.lineNumber, n.columnNumber, "Error : cannot convert from " + expType + " to " + keyType));
					this.errorsDetected++;
				}

			} else if (a instanceof IntArrayType) {
				expType = Type.THE_INT_ARRAY_TYPE.getName();
			}

			if (!keyType.equals(expType)) {
				// System.out.println(keyType.length() + " " + expType.length());
				//System.out.println("Type mismatch: cannot convert from " + expType + " to " + keyType + " at Line: "
						//+ n.lineNumber);
				System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.lineNumber, n.columnNumber, "Error : cannot convert from " + expType + " to " + keyType));
				this.errorsDetected++;
			}
		}

		return null;
	}

	// Subcomponents of ArrayAssign: Identifier nameOfArray; Expression
	// indexInArray, Expression e;
	@SuppressWarnings("unchecked")
	public String visit(ArrayAssign n) {
		String mapKey = n.nameOfArray.toString();
		String keyType = "";
		boolean keyFound = false;
		List<HashMap<String, String>> currTable;
		currTable = this.table.ListOfTablesForMethod(this.currentClass, this.currentMethod);
		if (currTable.size() > 0) {
			for (int i = currTable.size() - 1; i >= 0; i--) {
				keyFound = currTable.get(i).containsKey(mapKey);
				if (keyFound) {
					// System.out.println("Key " + mapKey + " Found!");
					keyType = currTable.get(i).get(mapKey);
					// System.out.println("The type of it is " + keyType);
					break;
				}
			}
		}

		if (!keyFound) {
			//System.out.println(mapKey + " is never declared at line:" + n.nameOfArray.lineNumber);
			this.errorsDetected++;
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.nameOfArray.lineNumber, n.nameOfArray.columnNumber, "Error : " + mapKey + " is never declared."));
			n.nameOfArray.accept(this);
			n.indexInArray.accept(this);
			n.e.accept(this);

		} else {
			n.nameOfArray.accept(this);
			if (keyType != "int[]") {
				//System.out.println(mapKey + " is not int[] type" + n.nameOfArray.lineNumber);
				this.errorsDetected++;
				System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.nameOfArray.lineNumber, n.nameOfArray.columnNumber, "Error : " + mapKey + " is not int[] type."));
				n.indexInArray.accept(this);
				n.e.accept(this);
			} else {
				if (!(n.indexInArray.accept(this) instanceof IntegerType)) {
					//System.out.println(mapKey + " Type mismatch: array index must be int type at Line: "
							//+ n.indexInArray.lineNumber);
					this.errorsDetected++;
					System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.indexInArray.lineNumber, n.indexInArray.columnNumber, "Error : in " + mapKey + " array index must be int type."));
					n.e.accept(this);
				} else {
					if (!(n.e.accept(this) instanceof IntegerType)) {
						//System.out.println(mapKey
							//	+ " Type mismatch: can not assign anything otherthan int value to an array at Line: "
							//	+ n.nameOfArray.lineNumber);
						System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.nameOfArray.lineNumber, n.nameOfArray.columnNumber, "Error : in " + mapKey + " can not assign anything otherthan int value to an array."));
						this.errorsDetected++;
					} else {

					}
				}
			}
		}
		return null;
	}

	// Expression e1,e2;
	@SuppressWarnings("unchecked")
	public Type visit(final And n) {
		if (!(n.e1.accept(this) instanceof BooleanType)) {
			//System.out.println("at line " + n.e1.lineNumber + " operand needs to be a boolean type");
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e1.lineNumber, n.e1.columnNumber, "Error : operand needs to be a boolean type."));
			this.errorsDetected++;
		}

		if (!(n.e2.accept(this) instanceof BooleanType)) {
			//System.out.println("at line " + n.e2.lineNumber + " operand needs to be a boolean type");
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e2.lineNumber, n.e2.columnNumber, "Error : operand needs to be a boolean type."));
			this.errorsDetected++;
		}

		return Type.THE_BOOLEAN_TYPE;
	}

	// Expression e1,e2;
	@SuppressWarnings("unchecked")
	public Type visit(final LessThan n) {
		if (!(n.e1.accept(this) instanceof IntegerType)) {
			//System.out.println("at line " + n.e1.lineNumber + " operand needs to be a number");
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e1.lineNumber, n.e1.columnNumber, "Error : operand needs to be a number."));
			this.errorsDetected++;
		}

		if (!(n.e2.accept(this) instanceof IntegerType)) {
			//System.out.println("at line " + n.e2.lineNumber + " operand needs to be a number");
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e2.lineNumber, n.e2.columnNumber, "Error : operand needs to be a number."));
			this.errorsDetected++;
		}

		return Type.THE_BOOLEAN_TYPE;
	}

	// Expression e1,e2;
	@SuppressWarnings("unchecked")
	public Type visit(final Plus n) {
		if (!(n.e1.accept(this) instanceof IntegerType)) {
			//System.out.println("at line " + n.e1.lineNumber + " operand needs to be a number");
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e1.lineNumber, n.e1.columnNumber, "Error : operand needs to be a number"));
			this.errorsDetected++;
		}

		if (!(n.e2.accept(this) instanceof IntegerType)) {
			//System.out.println("at line " + n.e2.lineNumber + " operand needs to be a number");
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e2.lineNumber, n.e2.columnNumber, "Error : operand needs to be a number"));
			this.errorsDetected++;
		}

		return Type.THE_INTEGER_TYPE;
	}

	@SuppressWarnings("unchecked")
	public Type visit(final Minus n) {
		if (!(n.e1.accept(this) instanceof IntegerType)) {
			//System.out.println("at line " + n.e1.lineNumber + " operand needs to be a number");
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e1.lineNumber, n.e1.columnNumber, "Error : operand needs to be a number"));
			this.errorsDetected++;
		}

		if (!(n.e2.accept(this) instanceof IntegerType)) {
			//System.out.println("at line " + n.e2.lineNumber + " operand needs to be a number");
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e2.lineNumber, n.e2.columnNumber, "Error : operand needs to be a number"));
			this.errorsDetected++;
		}

		return Type.THE_INTEGER_TYPE;
	}

	// Expression e1,e2;
	@SuppressWarnings("unchecked")
	public Type visit(final Times n) {
		if (!(n.e1.accept(this) instanceof IntegerType)) {
			//System.out.println("at line " + n.e1.lineNumber + " operand needs to be a number");
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e1.lineNumber, n.e1.columnNumber, "Error : operand needs to be a number"));
			this.errorsDetected++;
		}

		if (!(n.e2.accept(this) instanceof IntegerType)) {
			//System.out.println("at line " + n.e2.lineNumber + " operand needs to be a number");
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e2.lineNumber, n.e2.columnNumber, "Error : operand needs to be a number"));
			this.errorsDetected++;
		}

		return Type.THE_INTEGER_TYPE;
	}

	// Expression expressionForArray, indexInArray;
	@SuppressWarnings("unchecked")
	public Type visit(final ArrayLookup n) {
		// make sure n.expressionForArray is intarraytype -> a[1]

		if (!(n.expressionForArray.accept(this) instanceof IntArrayType)) {
			//System.out.println("at line " + n.expressionForArray.lineNumber + " must be of type int[]");
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.expressionForArray.lineNumber, n.expressionForArray.columnNumber, "Error : must be of type int[]"));
			this.errorsDetected++;
			n.indexInArray.accept(this);
			return null;
		}

		if (!(n.indexInArray.accept(this) instanceof IntegerType)) {
			//System.out.println("at line " + n.indexInArray.lineNumber + " an array can only be indexed with int value");
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.indexInArray.lineNumber, n.indexInArray.columnNumber, "Error : an array can only be indexed with int value"));
			this.errorsDetected++;
			return null;
		}

		return Type.THE_INTEGER_TYPE;
	}

	// Expression expressionForArray;
	@SuppressWarnings("unchecked")
	public Type visit(final ArrayLength n) {
		// n.expressionForArray
		if (!(n.expressionForArray.accept(this) instanceof IntArrayType)) {
			//System.out.println(
					//"e.length only applies to expressions of type int[] at Line: " + n.expressionForArray.lineNumber);
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.expressionForArray.lineNumber, n.expressionForArray.columnNumber, "Error : e.length only applies to expressions of type int[]"));
			this.errorsDetected++;
		}
		return Type.THE_INTEGER_TYPE;
	}

	// Subcomponents of Call: Expression e; Identifier i; ExpressionList el;
	@SuppressWarnings("unchecked")
	public Type visit(Call n) {
		boolean validEx = true;
		List<Object> holder = new ArrayList<Object>();
		Object tempE = n.e.accept(this);
		// question should i include intarraytype also ?
		String calledMethod = n.i.toString();
		if (!(tempE instanceof IdentifierType)) {
			//System.out.println("The left-hand side of an assignment must be a variable at Line: " + n.lineNumber);
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.lineNumber, n.columnNumber, "Error : The left-hand side of an assignment must be a variable"));
			this.errorsDetected++;
			validEx = false;
		}
		if (validEx) {
			if (n.e instanceof NewObject) {

				String classKey = n.e.accept(this).toString();
				n.setReceiverClassName(classKey);
				// System.out.println(classKey);
				boolean validMethod = false;

				HashMap<String, String> avlMethods;
				avlMethods = this.table.listOfClassMethods(classKey);

				if (avlMethods.containsKey(calledMethod)) {
					validMethod = true;
				} else {
					//System.out.println("The method " + calledMethod + " is undefined for the type " + classKey
						//	+ " at Line: " + n.i.lineNumber);
					System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : The method " + calledMethod + " is undefined for the type " + classKey));
					this.errorsDetected++;
				}

				if (validMethod) {
					int userGivenParamSIze = 0;
					boolean validParamSIze = false;
					n.i.accept(this);

					if (n.el.size() > 0) {
						holder.add(n.el.get(0).accept(this));
						// Loop over all actuals excluding the first one
						for (Expression e : n.el.subList(1, n.el.size())) {
							holder.add(e.accept(this));
						}
					}

					int parSize = 0;

					parSize = this.table.getNumOfMethodPar(classKey, calledMethod);
					// System.out.println(parSize);
					userGivenParamSIze = n.el.size();

					if (parSize == userGivenParamSIze) {
						validParamSIze = true;
					}

					if (validParamSIze) {

						boolean typeMatches = false;
						if (parSize == 0) {
							typeMatches = true;
						} else {
							List<String> legitParamTypes = this.table.paramTypes(classKey, calledMethod);
							// System.out.println(legitParamTypes + " by " + classKey + "." + calledMethod);
							List<String> userGivenParamTypes = new ArrayList<String>();
							int verytemp = 0;
							for (Expression e : n.el) {

								if (holder.get(verytemp) instanceof IntegerType)
									userGivenParamTypes.add("int");
								else if (holder.get(verytemp) instanceof BooleanType) {
									userGivenParamTypes.add("boolean");
								} else if (holder.get(verytemp) instanceof IdentifierType) {
									IdentifierType tempId = (IdentifierType) e.accept(this);
									userGivenParamTypes.add(tempId.getName());
								} else if (holder.get(verytemp) instanceof IntArrayType) {
									userGivenParamTypes.add("int[]");
								} else {
									userGivenParamTypes.add("Undefined");
								}
								verytemp++;
							}

							for (int i = 0; i < userGivenParamTypes.size(); i++) {
								if (this.table.checkSubType(legitParamTypes.get(i), userGivenParamTypes.get(i))) {
									typeMatches = true;
								} else {
									//System.out.print("The method " + calledMethod + "(");
									for (int j = 0; j < legitParamTypes.size(); j++) {
										//System.out.print(legitParamTypes.get(j));
										if (j == userGivenParamTypes.size() - 1) {
											//System.out.print("");
										} else {
											//System.out.print(", ");
										}
									}

								//	System.out.print(") in the type " + classKey + " is not applicable "
										//	+ "for the arguments (");

									for (int j = 0; j < userGivenParamTypes.size(); j++) {
										//System.out.print(userGivenParamTypes.get(j));

										if (j == userGivenParamTypes.size() - 1) {
											//System.out.print("");
										} else {
											//System.out.print(", ");
										}

									}
									//System.out.println(") at Line: " + n.i.lineNumber);
									typeMatches = false;
									break;
								}
							}

						}

						if (typeMatches) {
							HashMap<String, String> classMethods;
							String returnType = "";
							classMethods = this.table.listOfClassMethods(classKey);
							returnType = classMethods.get(calledMethod);

							if (returnType.equals(Type.THE_INTEGER_TYPE.getName())) {
								return Type.THE_INTEGER_TYPE;
							} else if (returnType.equals(Type.THE_BOOLEAN_TYPE.getName())) {
								return Type.THE_BOOLEAN_TYPE;
							} else if (returnType.equals(Type.THE_INT_ARRAY_TYPE.getName())) {
								return Type.THE_INT_ARRAY_TYPE;
							} else {
								return new IdentifierType(n.i.lineNumber, n.i.columnNumber, returnType);
							}
						} else {
							this.errorsDetected++;
							System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : Invalid parameter type passed."));
							return null;
						}
					} else {
						this.errorsDetected++;
						//System.out.println("^^^^The method " + calledMethod + " <<<in the type " + classKey
							//	+ " is not applicable for the arguments given at Line: " + n.i.lineNumber);
						System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : The method " + calledMethod + 
								" in the type " + classKey + " is not applicable for the arguments"));
						return null;
					}

				} else {
					if (n.el.size() > 0) {
						holder.add(n.el.get(0).accept(this));

						// Loop over all actuals excluding the first one
						for (Expression e : n.el.subList(1, n.el.size())) {
							holder.add(e.accept(this));
						}
					}
					return null;
				}

			} else {
				// System.out.println(calledMethod);
				String mapKey = (n.e).toString();
				// System.out.println(mapKey);
				String classType = "";
				boolean keyFound = false;

				if (n.e instanceof This) {
					classType = this.currentClass;
					keyFound = true;
				} else if (n.e instanceof Call) {
					if (!(tempE instanceof IdentifierType)) {
						//System.out.println(
								//"The left-hand side of an assignment must be a variable at Line: " + n.e.lineNumber);
						System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e.lineNumber, n.e.columnNumber, "Error : The left-hand side of an assignment must be a variable"));
						this.errorsDetected++;
					} else {
						keyFound = true;
						classType = ((IdentifierType) tempE).toString();
					}
				} else {

					List<HashMap<String, String>> currTable;
					currTable = this.table.ListOfTablesForMethod(this.currentClass, this.currentMethod);

					if (currTable.size() > 0) {
						for (int i = currTable.size() - 1; i >= 0; i--) {
							keyFound = currTable.get(i).containsKey(mapKey);
							if (keyFound) {
								// System.out.println("Key " + mapKey + " Found!");
								classType = currTable.get(i).get(mapKey);
								// System.out.println("The type of it is " + keyType);
								break;
							}
						}
					}
				}

				boolean validMethod = false;

				if (!keyFound) {
					// System.out.println("here");
					//System.out.println(mapKey + " can not be resolved at line:" + n.e.lineNumber);
					System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e.lineNumber, n.e.columnNumber, "Error : " + mapKey + " can not be resolved"));
					this.errorsDetected++;

				} else {

					HashMap<String, String> classMethods;
					// System.out.println(classType);
					classMethods = this.table.listOfClassMethods(classType);
					n.setReceiverClassName(classType);
					if (classMethods.containsKey(calledMethod)) {
						validMethod = true;
					} else {
						//System.out.println("The method " + calledMethod + " is undefined for the type " + classType
							//	+ " at Line: " + n.i.lineNumber);
						System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : The method " + calledMethod + 
								" is undefined for the type " + classType));
						this.errorsDetected++;
					}
				}

				if (validMethod) {
					n.i.accept(this);
					if (n.el.size() > 0) {
						holder.add(n.el.get(0).accept(this));

						// Loop over all actuals excluding the first one
						for (Expression e : n.el.subList(1, n.el.size())) {
							holder.add(e.accept(this));
						}
					}

					int userGivenParamSIze = 0;
					boolean validParamSIze = false;

					int parSize = 0;

					parSize = this.table.getNumOfMethodPar(classType, calledMethod);

					userGivenParamSIze = n.el.size();

					if (parSize == userGivenParamSIze) {
						validParamSIze = true;
					}

					if (validParamSIze) {
						boolean typeMatches = false;
						if (parSize == 0) {
							typeMatches = true;
						} else {
							List<String> legitParamTypes = this.table.paramTypes(classType, calledMethod);
							// System.out.println(legitParamTypes + " by " + classType + "." +
							// calledMethod);
							List<String> userGivenParamTypes = new ArrayList<String>();
							int verytemp = 0;
							for (Expression e : n.el) {
								if (holder.get(verytemp) instanceof IntegerType)
									userGivenParamTypes.add("int");
								else if (holder.get(verytemp) instanceof BooleanType) {
									userGivenParamTypes.add("boolean");
								} else if (holder.get(verytemp) instanceof IdentifierType) {
									IdentifierType temp = (IdentifierType) e.accept(this);
									userGivenParamTypes.add(temp.getName());
								} else if (holder.get(verytemp) instanceof IntArrayType) {
									userGivenParamTypes.add("int[]");
								}
								verytemp++;
							}

							for (int i = 0; i < userGivenParamTypes.size(); i++) {

								if (this.table.checkSubType(legitParamTypes.get(i), userGivenParamTypes.get(i))) {
									typeMatches = true;
								} else {
									//System.out.print("The method " + calledMethod + "(");
									for (int j = 0; j < legitParamTypes.size(); j++) {
										//System.out.print(legitParamTypes.get(j));
										if (j == userGivenParamTypes.size() - 1) {
											//System.out.print("");
										} else {
											//System.out.print(", ");
										}
									}

									//System.out.print(") in the type " + classType + " is not applicable "
											//+ "for the arguments (");

									for (int j = 0; j < userGivenParamTypes.size(); j++) {
										//System.out.print(userGivenParamTypes.get(j));
										if (j == userGivenParamTypes.size() - 1) {
											//System.out.print("");
										} else {
											//System.out.print(", ");
										}
									}
									//System.out.println(") at Line: " + n.i.lineNumber);
									typeMatches = false;
									break;
								}
							}

						}

						if (typeMatches) {
							HashMap<String, String> classMethods;
							String returnType = "";
							classMethods = this.table.listOfClassMethods(classType);
							returnType = classMethods.get(calledMethod);

							if (returnType.equals(Type.THE_INTEGER_TYPE.getName())) {
								return Type.THE_INTEGER_TYPE;
							} else if (returnType.equals(Type.THE_BOOLEAN_TYPE.getName())) {
								return Type.THE_BOOLEAN_TYPE;
							} else if (returnType.equals(Type.THE_INT_ARRAY_TYPE.getName())) {
								return Type.THE_INT_ARRAY_TYPE;
							} else {
								return new IdentifierType(n.i.lineNumber, n.i.columnNumber, returnType);
							}
						} else {
							return null;
						}
					} else {
						//System.out.println("The method " + calledMethod + " in the type " + classType
								//+ " >> is not applicable for the arguments given at Line: " + n.i.lineNumber);
						System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : The method " + calledMethod + 
								" in the type " + classType + " is not applicable for the arguments given"));			
						this.errorsDetected++;
						return null;
					}

				} else {
					n.i.accept(this);

					if (n.el.size() > 0) {
						holder.add(n.el.get(0).accept(this));

						// Loop over all actuals excluding the first one
						for (Expression e : n.el.subList(1, n.el.size())) {
							holder.add(e.accept(this));
						}
					}
					return null;
				}
			}
		} else {
			n.i.accept(this);

			if (n.el.size() > 0) {
				holder.add(n.el.get(0).accept(this));

				// Loop over all actuals excluding the first one
				for (Expression e : n.el.subList(1, n.el.size())) {
					holder.add(e.accept(this));
				}
			}
			return null;
		}
	}

	public Type visit(True n) {
		return Type.THE_BOOLEAN_TYPE;
	}

	public Type visit(False n) {
		return Type.THE_BOOLEAN_TYPE;
	}

	public Type visit(IntegerLiteral n) {
		return Type.THE_INTEGER_TYPE;
	}

	// Subcompoents of identifier statement: String:s
	public Type visit(IdentifierExp n) {
		String mapKey = n.s;
		String keyType = "";
		boolean keyFound = false;
		// System.out.println("Came here yes");
		// look for n.s in symbol table in current class current method
		List<HashMap<String, String>> currTable;
		currTable = this.table.ListOfTablesForMethod(this.currentClass, this.currentMethod);
		// System.out.println(currTable);
		// System.out.println(this.currentClass + " " + this.currentMethod);
		if (currTable.size() > 0) {
			for (int i = currTable.size() - 1; i >= 0; i--) {
				keyFound = currTable.get(i).containsKey(mapKey);
				if (keyFound) {
					// System.out.println("Key " + mapKey + " Found!");
					keyType = currTable.get(i).get(mapKey);
					// System.out.println("The type of it is " + keyType);
					break;
				}
			}
		}

		if (!keyFound) {
			//System.out.println(mapKey + " is never declared. at Line:" + n.lineNumber);
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.lineNumber, n.columnNumber, "Error : " + mapKey + " is never declared."));
			this.errorsDetected++;
			return null;
		} else {
			if (keyType.equals(Type.THE_INTEGER_TYPE.getName())) {
				return Type.THE_INTEGER_TYPE;
			} else if (keyType.equals(Type.THE_BOOLEAN_TYPE.getName())) {
				return Type.THE_BOOLEAN_TYPE;
			} else if (keyType.equals(Type.THE_INT_ARRAY_TYPE.getName())) {
				return Type.THE_INT_ARRAY_TYPE;
			} else
				return new IdentifierType(n.lineNumber, n.columnNumber, keyType);
		}
	}

	public Type visit(This n) {
		return new IdentifierType(n.lineNumber, n.columnNumber, this.currentClass);
	}

	// Expression e;
	@SuppressWarnings("unchecked")
	public Type visit(NewArray n) {

		if (!(n.e.accept(this) instanceof IntegerType)) {
			//System.out.println("Can only use int value for array size at Line: " + n.e.lineNumber);
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e.lineNumber, n.e.columnNumber, "Error : Can only use int value for array size."));
			this.errorsDetected++;
			return null;
		}

		return Type.THE_INT_ARRAY_TYPE;
	}

	// Identifier i;
	public Type visit(NewObject n) {
		// look in to table for n.i
		boolean validClass = false;
		String objType = n.i.toString();

		validClass = this.table.classValidity(objType);
		// question what line number and column number should i use when i return
		if (validClass) {
			return new IdentifierType(n.lineNumber, n.columnNumber, objType);

		} else {
			//System.out.println(objType + " cannot be resolved to a type at Line: " + n.i.lineNumber);
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.i.lineNumber, n.i.columnNumber, "Error : " + objType + " cannot be resolved to a type."));
			this.errorsDetected++;
			return null;
		}
	}

	// Expression e;
	@SuppressWarnings("unchecked")
	public Type visit(Not n) {
		if (!(n.e.accept(this) instanceof BooleanType)) {
			//System.out.println("at line " + n.e.lineNumber + " operand needs to be a boolean type");
			System.err.println(String.format("%s:%d.%d: %s", ErrorHandler.filename, n.e.lineNumber, n.e.columnNumber, "Error : operand needs to be a boolean type."));
			this.errorsDetected++;
		}

		return Type.THE_BOOLEAN_TYPE;
	}

	// String s;
	// question do i need to do anything with this method
	public Type visit(Identifier n) {
		return null;
	}
}
