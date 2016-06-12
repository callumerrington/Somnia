package com.kingrunes.somnia.asm.patchers;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.kingrunes.somnia.asm.api.AbstractPatcher;

public class PatcherEntityRenderer extends AbstractPatcher
{
	public PatcherEntityRenderer()
	{
		super("net.minecraft.renderer.EntityRenderer");
	}

	@Override
	public byte[] patch(byte[] bytes, boolean obf)
	{
		String methodName = obf ? "b" : "updateCameraAndRender";
		String methodName2 = obf ? "a" : "renderWorld";
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        boolean f = true;
        
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
        	MethodNode m = methods.next();
        	if (m.name.equals(methodName) && m.desc.equals("(F)V"))
        	{
	    		AbstractInsnNode ain;
	    		MethodInsnNode min;
	    		VarInsnNode vin;
	    		Iterator<AbstractInsnNode> iter = m.instructions.iterator();
	     		while (iter.hasNext())
	     		{
	     			ain = iter.next();
	     			if (ain instanceof MethodInsnNode)
	     			{
	     				min = (MethodInsnNode)ain;
	     				if (min.name.equals(methodName2) && min.desc.equalsIgnoreCase("(FJ)V") && min.getOpcode() == Opcodes.INVOKEVIRTUAL)
	     				{
	     					min.setOpcode(Opcodes.INVOKESTATIC);
	     					min.name = "renderWorld";
	     					min.owner = "com/kingrunes/somnia/Somnia";
	     					
	     					vin = (VarInsnNode) m.instructions.get(m.instructions.indexOf(min)-(f ? 9 : 3));
	     					m.instructions.remove(vin);
	     					
	     					f = false;
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
