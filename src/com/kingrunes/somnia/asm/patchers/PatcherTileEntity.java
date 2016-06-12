package com.kingrunes.somnia.asm.patchers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import com.kingrunes.somnia.asm.api.AbstractPatcher;

public class PatcherTileEntity extends AbstractPatcher
{

	public PatcherTileEntity()
	{
		super("net.minecraft.tileentity.TileEntity");
	}

	@Override
	public byte[] patch(byte[] bytes, boolean obf)
	{
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        classNode.visitField(Opcodes.ACC_PUBLIC, "tickCount", "I", null, null).visitEnd();
        classNode.visitField(Opcodes.ACC_PUBLIC, "tickTime", "J", null, null).visitEnd();
        
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
	}
}
