# KnightCodeCompiler
### Author: Jerrin C. Redmon
### Professor: Dr.Kelley
### CS-322 Compiler Construction

**Description:**
- 
The KnightCodeCompiler is a final CS322 project to create a compiler of the KnightCode programming language to Java Bytecode. All information of the language can be found in the KnightCodeSkeleton which was forked for this project.
	**How it works?**
> It works by parsing a tree and traversing it and generates appropriate Bytecode for each function as it traverses the tree which is then fully complied to bytecode then ran by the JVM


**KnightCode**
-
Knight code is a basic toy programming language with limitations. It only accepts Integers and Strings as variables. Most operations are not available!

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
This example adds two number and prints the results

**Requirements:**
-
- Antlr4
- Java ObjectWeb ASM Library
- Java 11 or newer


**How to use:**
-
*To compile Knight Code*
1. Build the Xml file and or generate the grammer file 
``KnightCode.g4``
2. Compile the KnightCodeComplier
``$ javac complier/kcc.java``
3. Use kcc with your given *~.kc* file
``$ java compiler.kcc tests/"Program.kc"``


**Other notes**
-
String literals are very buggy!







