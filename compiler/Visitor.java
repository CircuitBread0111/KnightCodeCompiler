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

/**
 * Body Visitor 
 * @author Jerrin Redmon
 * @version 1.0
 * Compiler Project 4
 * CS322 - Compiler Construction
 * Spring 2023
 */ 
public class Visitor extends KnightCodeBaseVisitor<Object> {
	public ASM output;
	private int addressCounter = 1;
	private SymbolTable st = new SymbolTable();
	private boolean errorEncountered = false;
	public boolean getErrorEncountered() {
		return errorEncountered;
	}

	/**
	 * Reports any error found and print out the error
	 * @param ctx the parse tree
	 * @param error The error to report
	 */
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
        if (ctx.declare() != null) visit(ctx.declare());
        if (ctx.body() != null) visit(ctx.body());
        else reportError(ctx, "No body in program.");
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
		String symbol = ctx.ID().getText();
		Variable var = st.lookup(symbol);
		if (var == null) reportError(ctx, String.format("There is no variable '%s'.", symbol));
		// make that scanner
		output.mv.visitTypeInsn(Opcodes.NEW, "java/util/Scanner");
        output.mv.visitInsn(Opcodes.DUP);
        output.mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        output.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);
        if (var.type == Variable.Type.INTEGER) {
        	output.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false);
        	output.mv.visitVarInsn(Opcodes.ISTORE, var.loc);
        } else if (var.type == Variable.Type.STRING) {
        	output.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "next", "()Ljava/lang/String;", false);
        	output.mv.visitVarInsn(Opcodes.ASTORE, var.loc);
        }
        return null;
    }

    @Override
    public Object visitDecision(DecisionContext ctx) {
        // execute the expression in if
        visit(ctx.expr());
        Label ifLabel = new Label();
        Label endIfLabel = new Label();

        // get token positions
        int elsePosition = -1, endIfPosition = -1;
        for (int i = 3; i < ctx.getChildCount(); i++) {
        	if (ctx.getChild(i).getText().equals("ELSE")) elsePosition = i;
        	else if (ctx.getChild(i).getText().equals("ENDIF")) {
        		endIfPosition = i;
        		break;
        	}
        }

        // value is nonzero, so it is true.
        output.mv.visitJumpInsn(Opcodes.IFNE, ifLabel);
        // else
        if (elsePosition != -1) {
        	for (int i = elsePosition + 1; i < endIfPosition; i++) {
        		visit(ctx.getChild(i));
        	}
        }
        output.mv.visitJumpInsn(Opcodes.GOTO, endIfLabel);
        output.mv.visitLabel(ifLabel);
    	for (int i = 3; elsePosition == -1 && i < endIfPosition || i < elsePosition; i++) {
    		visit(ctx.getChild(i));
    	}
        output.mv.visitLabel(endIfLabel);

        return null;
    }

    @Override
    public Object visitLoop(LoopContext ctx) {
        Label beginLoop = new Label();
        Label endLoop = new Label();
        output.mv.visitLabel(beginLoop);
        visit(ctx.expr());
        output.mv.visitJumpInsn(Opcodes.IFEQ, endLoop);
        for (StatContext stat : ctx.stat()) visit(stat);
        output.mv.visitJumpInsn(Opcodes.GOTO, beginLoop);
        output.mv.visitLabel(endLoop);
        return null;
    }

}
