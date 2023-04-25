package compiler;
/**
 * Represents the variable
 * @author Jerrin Redmon
 * @version 1.0
 * Compiler Project 4
 * CS322 - Compiler Construction
 * Spring 2023
 */ 
public class Variable {
	public enum Type { INTEGER, STRING }
	public final Type type;   // type of the variable
	public final int loc;   // location of the variable
	
	/**
	 * Constuctor
	 * @param type the type of the variable
	 * @param loc the location of the variable
	 */
	public Variable(Type type, int loc) {
		this.type = type;
		this.loc = loc;
	}
	
	/**
	 * Generates type from string
	 * @param type the Type to get
	 * @return the type given in the string
	 */
	public static Type typeFromString(String type) {
		switch (type) {
			case "INTEGER": return Type.INTEGER;
			case "STRING": return Type.STRING;
			default: return null;
		}
	}
	
	public String toString() {
		return String.format("type: %s, location: 0x%x", type, loc);
	}
}
