# KnightCodeCompiler
### Author: Jerrin C. Redmon
### Professor: Dr.Kelley
### CS-322 Compiler Construction

**Description:**
- 
	The KnightCodeCompiler is a final CS322 project to create a compiler of the KnightCode programming language to Java Bytecode. All information of the language can be found in the KnightCodeSkeleton which was forked for this project.

**KnightCode**
-
Knight code is a basic toy programming language. It only accepts Integers and Strings as variables. It does not have subroutines.

*Example of what KnightCode looks like*
```
PROGRAM Example
DECLARE
	INTEGER x
	INTEGER y
	INTEGER z
BEGIN
	SET x := 10
	SET y := 12
	SET z := x + y
	PRINT z
END
```
This example adds two number and prints the result

**Requirements:**
-
- Antlr4
- Java ObjectWeb ASM Library
- Java 11 or newer


**How to use:**
-
*To compile Knight Code*
1. Build the Xml file and or generate the grammer file 
``$ antlr4 KnightCode.g4 -no-listener -visitor -o lexparse``
2. Compile the KnightCodeComplier
``$ javac complier/kcc.java``
3. Use kcc with your given *~.kc* file
``$ java compiler.kcc [path]``

**Other notes**
-
The grammar file was changed to improve the semantics of IF statements and WHILE loops. Rather than accepting a specific comparison statement, any expression is accepted. The result is treated like it is in the C programming language. That is, a value of zero is treated as false, and any nonzero value is treated as true. This behavior is more flexible and easier to implement.