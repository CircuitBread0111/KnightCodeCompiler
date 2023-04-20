package compiler;

import java.util.HashMap;

public class SymbolTable {

	private HashMap<String, Variable> table;
	
	public SymbolTable(){
		table = new HashMap();
	}
		
	public Variable lookup(String symbol) {
		return table.get(symbol);
	}
	
	public void add(String symbol, Variable var) {
		table.put(symbol, var);
	}
	
	public String toString() {
		return table.toString();
	}
}
