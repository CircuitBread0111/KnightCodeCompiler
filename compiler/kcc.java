package compiler;

import java.io.IOException;
import java.io.FileOutputStream;
//ANTLR packages
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.Trees;
import lexparse.*;

/**
 * KnightCode Compiler
 * @author Jerrin Redmon
 * @version 1.0
 * Compiler Project 4
 * CS322 - Compiler Construction
 * Spring 2023
 */ 
public class kcc {
	
	/**
	 * Writes an array of bytes to a file
	 * @param bytearray the array to write
	 * @param fileName name of the file
	 */
	private static void writeFile(byte[] bytearray, String fileName){
        try{
            FileOutputStream out = new FileOutputStream(fileName);
            out.write(bytearray);
            out.close();
        }
        catch(IOException e){
        System.out.println(e.getMessage());
        }
        
    }//end writeFile

    public static void main(String[] args){
        CharStream input;
        KnightCodeLexer lexer;
        CommonTokenStream tokens;
        KnightCodeParser parser;
        try {
            input = CharStreams.fromFileName(args[0]);  //get the input
            lexer = new KnightCodeLexer(input); //create the lexer
            tokens = new CommonTokenStream(lexer); //create the token stream
            parser = new KnightCodeParser(tokens); //create the parser
       
            ParseTree tree = parser.file();  //set the start location of the parser
            Visitor v = new Visitor();
            v.visit(tree);
            if (v.getErrorEncountered()) {
            	System.out.println("Compilation failed.");
            	System.exit(1);
            }
            byte[] output = v.output.finish();
            writeFile(output, "./output/" + v.output.programName + ".class");
        }
        catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

}//end class
