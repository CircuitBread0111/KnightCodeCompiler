package compiler;
/**
 * This class encapsulates a basic grammar test.
 */

import java.io.IOException;
import java.io.FileOutputStream;
//ANTLR packages
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.Trees;

import lexparse.*;

public class kcc {
	
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
            writeFile(output, v.output.programName + ".class");
            
            //System.out.println(tree.toStringTree(parser));
        }
        catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

}//end class
