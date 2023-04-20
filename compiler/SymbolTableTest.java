package compiler;

public class SymbolTableTest {

	public static void main(String[] args) {
		SymbolTable st = new SymbolTable();
		Variable vi = new Variable(Variable.Type.INTEGER, 1);
		Variable vs = new Variable(Variable.Type.STRING, 2);
		st.add("foo", vi);
		st.add("bar", vs);
		System.out.println(st);
		
		System.out.printf("foo is vi: %b\n", vi == st.lookup("foo"));
		System.out.printf("bar is vs: %b\n", vs == st.lookup("bar"));
	}
}
