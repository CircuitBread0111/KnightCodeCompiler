package compiler;

import org.objectweb.asm.*;
import org.objectweb.asm.Opcodes;

public class ASM {

	private ClassWriter cw;
	public MethodVisitor mv;
	public final String programName;

	public ASM(String programName) {
		this.programName = programName;
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, programName, null, "java/lang/Object",null);
		
		{
			MethodVisitor init=cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
			init.visitCode();
			init.visitVarInsn(Opcodes.ALOAD, 0);
			init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V",false);
			init.visitInsn(Opcodes.RETURN);
			init.visitMaxs(1,1);
			init.visitEnd();
		}
		
		mv=cw.visitMethod(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
	}
	
	public byte[] finish() {
		//Return and End Visit
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0,0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
	}
}
