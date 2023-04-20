package compiler;

import java.io.IOException;
//ANTLR packages
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.*;

import org.objectweb.asm.*;
import org.objectweb.asm.Opcodes;

import lexparse.*;
import lexparse.KnightCodeParser.*;

public class Visitor extends KnightCodeBaseVisitor<Object> {

	public ASM output;
	private int addressCounter = 1;
	private SymbolTable st = new SymbolTable();
	private boolean errorEncountered = false;
	
	public boolean getErrorEncountered() {
		return errorEncountered;
	}
	
	private void reportError(ParserRuleContext ctx, String error) {
		int lineNumber = ctx.getStart().getLine();
		int charPos = ctx.getStart().getCharPositionInLine();
		System.out.printf("%d:%d: error: %s\n", lineNumber, charPos, error);
		errorEncountered = true;
	}

    @Override
    public Object visitFile(FileContext ctx) {
        String programName = ctx.getChild(1).getText();
        output = new ASM(programName);
        visit(ctx.declare());
        visit(ctx.body());
        return null;
    }

    @Override
    public Object visitDeclare(DeclareContext ctx) {
    	visitChildren(ctx);
    	return null;
    }

    @Override
    public Object visitVariable(VariableContext ctx) {
        Variable.Type type = (Variable.Type) visit(ctx.vartype());
        String identifier = (String) visit(ctx.identifier());
        int loc = addressCounter++;
        Variable var = new Variable(type, loc);
        st.add(identifier, var);
        return null;
    }
    
    @Override
    public Object visitIdentifier(IdentifierContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitVartype(VartypeContext ctx) {
        return Variable.typeFromString(ctx.getText());
    }
    

    @Override
    public Object visitBody(BodyContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitStat(StatContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitSetvar(SetvarContext ctx) {
        String symbol = ctx.getChild(1).getText();
        Variable var = st.lookup(symbol);
        if (var == null) {
        	reportError(ctx, String.format("There is no variable '%s'.", symbol));
        }
        
        // jank because you don't have string literals
        String expr = ctx.getChild(3).getText();
        if (expr.charAt(0) == '"') {
        	output.mv.visitLdcInsn(expr.substring(1, expr.length() - 1));
        } else {
		    visit(ctx.getChild(3));
        }
        
        if (var.type == Variable.Type.INTEGER) {
		    output.mv.visitVarInsn(Opcodes.ISTORE, var.loc);
        } else if (var.type == Variable.Type.STRING) {
        	output.mv.visitVarInsn(Opcodes.ASTORE, var.loc);
        }
        return null;
    }

    @Override
    public Object visitMultiplication(MultiplicationContext ctx) {
        String symbol1 = (String) visit(ctx.getChild(0));
        String symbol2 = (String) visit(ctx.getChild(2));
    	output.mv.visitInsn(Opcodes.IMUL);
    	return null;
    }

    @Override
    public Object visitAddition(AdditionContext ctx) {
        String symbol1 = (String) visit(ctx.getChild(0));
        String symbol2 = (String) visit(ctx.getChild(2));
    	output.mv.visitInsn(Opcodes.IADD);
    	return null;
    }

    @Override
    public Object visitSubtraction(SubtractionContext ctx) {
        String symbol1 = (String) visit(ctx.getChild(0));
        String symbol2 = (String) visit(ctx.getChild(2));
    	output.mv.visitInsn(Opcodes.ISUB);
    	return null;
    }

    @Override
    public Object visitNumber(NumberContext ctx) {
        int num = Integer.parseInt(ctx.getText());
        output.mv.visitLdcInsn(num);
        return null;
    }

    @Override
    public Object visitComparison(ComparisonContext ctx) {
        visit(ctx.getChild(0));
        visit(ctx.getChild(2));
        visit(ctx.comp());
        return null;
    }

    @Override
    public Object visitDivision(DivisionContext ctx) {
        String symbol1 = (String) visit(ctx.getChild(0));
        String symbol2 = (String) visit(ctx.getChild(2));
    	output.mv.visitInsn(Opcodes.IDIV);
    	return null;
    }

    @Override
    public Object visitId(IdContext ctx) {
        String id = ctx.getText();
        Variable var = st.lookup(id);
        if (var == null) {
        	reportError(ctx, String.format("There is no variable '%s'.", id));
        }
        
        if (var.type == Variable.Type.INTEGER) {
		    output.mv.visitVarInsn(Opcodes.ILOAD, var.loc);
        } else if (var.type == Variable.Type.STRING) {
        	output.mv.visitVarInsn(Opcodes.ALOAD, var.loc);
        }
        return null;
    }

    @Override
    public Object visitComp(CompContext ctx) {
        String op = ctx.getText();
        Label cmpTrue = new Label();
        Label cmpTrueEnd = new Label();
        switch (op) {
        	case ">":
        		output.mv.visitJumpInsn(Opcodes.IF_ICMPGT, cmpTrue);
        		break;
        	case "<":
        		output.mv.visitJumpInsn(Opcodes.IF_ICMPLT, cmpTrue);
				break;
        	case "=":
        		output.mv.visitJumpInsn(Opcodes.IF_ICMPEQ, cmpTrue);
        		break;
        	case "<>":
        		output.mv.visitJumpInsn(Opcodes.IF_ICMPNE, cmpTrue);
        }
        output.mv.visitLdcInsn(0);
        output.mv.visitJumpInsn(Opcodes.GOTO, cmpTrueEnd);
        output.mv.visitLabel(cmpTrue);
        output.mv.visitLdcInsn(1);
        output.mv.visitLabel(cmpTrueEnd);
        return null;
    }

    @Override
    public Object visitPrint(PrintContext ctx) {
        output.mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    	String text = ctx.getChild(1).getText();
        Variable var = st.lookup(text);
        if (var == null) { // it is a literal
        	if (text.charAt(0) >= '0' && text.charAt(0) <= '9') { // integer literal
        		output.mv.visitLdcInsn(Integer.parseInt(text));
        		output.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
        	} else { // string literal
        		output.mv.visitLdcInsn(text.substring(1, text.length() - 1));
        		output.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        	}
        } else {
		    if (var.type == Variable.Type.INTEGER) {
				output.mv.visitVarInsn(Opcodes.ILOAD, var.loc);
		    	output.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
		    } else if (var.type == Variable.Type.STRING) {
		    	output.mv.visitVarInsn(Opcodes.ALOAD, var.loc);
		    	output.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		    }        
        }
        return null;
    }

    @Override
    public Object visitRead(ReadContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitRead'");
    }

    @Override
    public Object visitDecision(DecisionContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDecision'");
    }

    @Override
    public Object visitLoop(LoopContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitLoop'");
    }

}
