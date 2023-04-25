package compiler;

import java.util.HashMap;

/**
 * Creates a SymbolTable stored in a HashMap
 * @author Jerrin Redmon
 * @version 1.0
 * Compiler Project 4
 * CS322 - Compiler Construction
 * Spring 2023
 */ 
public class SymbolTable {

	private HashMap<String, Variable> table;
	
	/**
	 * Constuctor
	 */
	public SymbolTable(){
		table = new HashMap();
	}
		
	/**
	 * Lookup function for the variables
	 * @param symbol the symbol to look up
	 * @return the given variable
	 */
	public Variable lookup(String symbol) {
		return table.get(symbol);
	}
	
	/**
	 * Adds symbols to the table
	 * @param symbol the symbol to add
	 * @param var the given variable
	 */
	public void add(String symbol, Variable var) {
		table.put(symbol, var);
	}
	
	public String toString() {
		return table.toString();
	}
}
