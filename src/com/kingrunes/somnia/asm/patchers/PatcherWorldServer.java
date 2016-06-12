package com.kingrunes.somnia.asm.patchers;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.kingrunes.somnia.asm.api.AbstractPatcher;

public class PatcherWorldServer extends AbstractPatcher
{
	public PatcherWorldServer()
	{
		super("net.minecraft.world.WorldServer");
	}

	@Override
	public byte[] patch(byte[] bytes, boolean obf)
	{
		String 	methodTick = obf ? "b" : "tick",
				methodGetGameRule = obf ? "b" : "getGameRuleBooleanValue";
		
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        Iterator<MethodNode> methods = classNode.methods.iterator();
        AbstractInsnNode ain;
        while(methods.hasNext())
        {
        	MethodNode m = methods.next();
        	if (m.name.equals(methodTick) && m.desc.equals("()V"))
        	{
        		Iterator<AbstractInsnNode> iter = m.instructions.iterator();
        		MethodInsnNode min;
        		while (iter.hasNext())
        		{
        			ain = iter.next();
        			if (ain instanceof MethodInsnNode)
        			{
        				min = (MethodInsnNode)ain;
        				if (min.name.equals(methodGetGameRule) && min.desc.equals("(Ljava/lang/String;)Z"))
        				{
        					int index = m.instructions.indexOf(min);
        					
        					LdcInsnNode lin = (LdcInsnNode)m.instructions.get(index-1);
        					if (lin.cst.equals("doMobSpawning"))
        					{
	        					min.setOpcode(Opcodes.INVOKESTATIC);
	        					min.desc = "(Lnet/minecraft/world/WorldServer;)Z";
	        					min.name = "doMobSpawning";
	        					min.owner = "com/kingrunes/somnia/Somnia";
	        					
	        					m.instructions.remove(lin);
	        					m.instructions.remove(m.instructions.get(index-2));
	        					break;
        					}
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
