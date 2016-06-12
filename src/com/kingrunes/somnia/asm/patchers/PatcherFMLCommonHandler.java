package com.kingrunes.somnia.asm.patchers;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.kingrunes.somnia.asm.api.AbstractPatcher;

public class PatcherFMLCommonHandler extends AbstractPatcher
{
	public PatcherFMLCommonHandler()
	{
		super("cpw.mods.fml.common.FMLCommonHandler");
	}

	@Override
	public byte[] patch(byte[] bytes, boolean obf)
	{
		String methodName = "onPostServerTick";
		
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
        	MethodNode m = methods.next();
        	
        	if (m.name.equals(methodName))
        	{
        		AbstractInsnNode fain = m.instructions.getFirst();
        		
        		InsnList toInject = new InsnList();
    			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/kingrunes/somnia/Somnia", "instance", "Lcom/kingrunes/somnia/Somnia;"));
    			toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/kingrunes/somnia/Somnia", "tick", "()V", false));
        			
    			m.instructions.insertBefore(fain, toInject);
    			
                break;
            }
        }
        
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
	}
}
