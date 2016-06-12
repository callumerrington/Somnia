package com.kingrunes.somnia.asm.patchers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.kingrunes.somnia.asm.api.AbstractPatcher;

public class PatcherClassUtils extends AbstractPatcher
{
	private List<String> asmInjectedMethods;
	
	public PatcherClassUtils()
	{
		super("com.kingrunes.somnia.common.util.ClassUtils");
		
		this.asmInjectedMethods = new ArrayList<String>(4);
		asmInjectedMethods.add("getTileEntityTime");
		asmInjectedMethods.add("setTileEntityTime");
		asmInjectedMethods.add("getTileEntityTickCount");
		asmInjectedMethods.add("setTileEntityTickCount");
	}

	@Override
	public byte[] patch(byte[] bytes, boolean obf)
	{
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
        	MethodNode m = methods.next();
        	if (this.asmInjectedMethods.contains(m.name))
        		methods.remove();
        }
        
        /*
         * getTileEntityTime
         */
        MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "getTileEntityTime", "(Lnet/minecraft/tileentity/TileEntity;)J", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "tickTime", "J");
        mv.visitInsn(Opcodes.LRETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("te", "Lnet/minecraft/tileentity/TileEntity;", null, l0, l1, 0);
        mv.visitMaxs(2, 1);
        mv.visitEnd();
        
        /*
         * setTileEntityTime
         */
    	mv = classNode.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "setTileEntityTime", "(Lnet/minecraft/tileentity/TileEntity;J)V", null, null);
    	mv.visitCode();
    	Label l2 = new Label();
    	mv.visitLabel(l2);
    	mv.visitVarInsn(Opcodes.ALOAD, 0);
    	mv.visitVarInsn(Opcodes.LLOAD, 1);
    	mv.visitFieldInsn(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "tickTime", "J");
    	mv.visitInsn(Opcodes.RETURN);
    	Label l3 = new Label();
    	mv.visitLabel(l1);
    	mv.visitLocalVariable("te", "Lnet/minecraft/tileentity/TileEntity;", null, l2, l3, 0);
    	mv.visitLocalVariable("tickTime", "J", null, l2, l3, 1);
    	mv.visitMaxs(2, 2);
    	mv.visitEnd();
    	
    	/*
         * getTileEntityTickCount
         */
        mv = classNode.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "getTileEntityTickCount", "(Lnet/minecraft/tileentity/TileEntity;)I", null, null);
        mv.visitCode();
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "tickCount", "I");
        mv.visitInsn(Opcodes.IRETURN);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLocalVariable("te", "Lnet/minecraft/tileentity/TileEntity;", null, l4, l5, 0);
        mv.visitMaxs(2, 1);
        mv.visitEnd();
        
        /*
         * setTileEntityTickCount
         */
    	mv = classNode.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "setTileEntityTickCount", "(Lnet/minecraft/tileentity/TileEntity;I)V", null, null);
    	mv.visitCode();
    	Label l6 = new Label();
    	mv.visitLabel(l6);
    	mv.visitVarInsn(Opcodes.ALOAD, 0);
    	mv.visitVarInsn(Opcodes.ILOAD, 1);
    	mv.visitFieldInsn(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "tickCount", "I");
    	mv.visitInsn(Opcodes.RETURN);
    	Label l7 = new Label();
    	mv.visitLabel(l1);
    	mv.visitLocalVariable("te", "Lnet/minecraft/tileentity/TileEntity;", null, l6, l7, 0);
    	mv.visitLocalVariable("tickTime", "J", null, l2, l7, 1);
    	mv.visitMaxs(2, 2);
    	mv.visitEnd();
    	
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
	}

}
