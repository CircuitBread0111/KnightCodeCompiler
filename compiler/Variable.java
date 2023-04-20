package compiler;

public class Variable {
	/**
	 * All the types
	 */
	public enum Type { INTEGER, STRING }
	public final Type type;   // type of the variable
	public final int loc;   // location of the variable
	
	public Variable(Type type, int loc) {
		this.type = type;
		this.loc = loc;
	}
	
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
