JAVAC:= javac
JAVACC:= javacc
GCC:= sparc-linux-gcc

default:	compiler

compiler:	compile.jar shcomp

shcomp: 	assemble
	chmod u+x assemble

runtime.o : runtime.c
	$(GCC) -Wall -c runtime.c -o runtime.o

compile.jar: syntax runtime.o
	$(JAVAC) -classpath .:$(SUPPORT) -encoding US-ASCII */*.java
	jar cfm $@ manifest.txt frame/*.class parser/*.class symbol/*.class sparc/*.class main/*.class translate/*.class error/*.class

syntax:
	$(JAVACC) -debug_parser parser/parser.jj

clean:
	-/bin/rm -f *~ */*~
	-/bin/rm -f */*.class
	-/bin/rm -f *.jar
