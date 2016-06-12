package com.kingrunes.somnia.asm.patchers;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.kingrunes.somnia.asm.api.AbstractPatcher;

public class PatcherWorld extends AbstractPatcher
{
	public PatcherWorld()
	{
		super("net.minecraft.world.World");
	}

	@Override
	public byte[] patch(byte[] bytes, boolean obf)
	{
		String methodName = obf ? "h" : "updateEntities";
		String methodDesc = "()V";
		String methodName2 = obf ? "h" : "updateEntity";
		String methodDesc2 = "()V";
		String methodOwner2 = "aor";
		
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        boolean found = false;
        
        Iterator<MethodNode> methods = classNode.methods.iterator();
        MethodNode m = null;
        AbstractInsnNode ain;
        MethodInsnNode min = null;
        while(methods.hasNext())
        {
        	m = methods.next();
        	if (m.name.equals(methodName) && m.desc.equals(methodDesc))
        	{
        		Iterator<AbstractInsnNode> iter = m.instructions.iterator();
        		while (iter.hasNext())
        		{
        			ain = iter.next();
        			if (ain instanceof MethodInsnNode)
        			{
        				min = (MethodInsnNode)ain;
        				if (min.name.equals(methodName2) && min.desc.equals(methodDesc2) && (!obf || min.owner.equals(methodOwner2)))
        				{
        					found = true;
        					break;
        				}
        			}
        		}
        		break;
        	}
        }
        
        if (found)
        {
        	System.out.println("[Somnia] [PatcherWorld] Found patching index in World");
        	int idx = m.instructions.indexOf(min);
        	InsnList il1 = new InsnList();
        	il1.add(new VarInsnNode(Opcodes.ALOAD, 8));
        	il1.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/kingrunes/somnia/server/Profiler", "_tileTickStart", "(Lnet/minecraft/tileentity/TileEntity;)V", false));
        	m.instructions.insertBefore(m.instructions.get(idx-1), il1);
        	il1 = new InsnList();
        	il1.add(new VarInsnNode(Opcodes.ALOAD, 8));
        	il1.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/kingrunes/somnia/server/Profiler", "_tileTickEnd", "(Lnet/minecraft/tileentity/TileEntity;)V", false));
        	m.instructions.insert(min, il1);
        }
        
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
	}
}
