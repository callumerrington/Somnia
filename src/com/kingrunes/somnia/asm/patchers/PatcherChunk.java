package com.kingrunes.somnia.asm.patchers;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.kingrunes.somnia.asm.api.AbstractPatcher;

public class PatcherChunk extends AbstractPatcher
{
	public PatcherChunk()
	{
		super("net.minecraft.world.chunk.Chunk");
	}

	@Override
	public byte[] patch(byte[] bytes, boolean obf)
	{
		String methodName = obf ? "b" : "func_150804_b";
		String methodName2 = obf ? "p" : "func_150809_p";
		
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        Iterator<MethodNode> methods = classNode.methods.iterator();
        AbstractInsnNode ain;
        while(methods.hasNext())
        {
        	MethodNode m = methods.next();
        	if (m.name.equals(methodName))
        	{
        		Iterator<AbstractInsnNode> iter = m.instructions.iterator();
        		while (iter.hasNext())
        		{
        			ain = iter.next();
        			if (ain instanceof MethodInsnNode)
        			{
        				MethodInsnNode min = (MethodInsnNode)ain;
        				if (min.name.equals(methodName2))
        				{
        					min.setOpcode(Opcodes.INVOKESTATIC);
        					min.desc = "(Lnet/minecraft/world/chunk/Chunk;)V";
        					min.name = "chunkLightCheck";
        					min.owner = "com/kingrunes/somnia/Somnia";
        				}
        			}
        		}
        		break;
        	}
        }
        
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
	}
}
