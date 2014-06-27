package com.kingrunes.somnia.asm;

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class SClassTransformer implements IClassTransformer 
{
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		
		if (name.equalsIgnoreCase("cpw.mods.fml.common.FMLCommonHandler"))
			return patchFMLCommonHandler(bytes);
		else if (name.equalsIgnoreCase("net.minecraft.client.renderer.EntityRenderer"))
			return patchEntityRenderer(bytes, false);
		else if (name.equalsIgnoreCase("bll"))
			return patchEntityRenderer(bytes, true);
		
		/*else if (name.equalsIgnoreCase("net.minecraft.world.WorldServer"))
			return patchWorldServer(bytes, false);
		else if (name.equalsIgnoreCase("js"))
			return patchWorldServer(bytes, true);
		else if (name.equalsIgnoreCase("abw"))
			return patchWorld(bytes);
		*/
		return bytes;
	}

	private byte[] patchFMLCommonHandler(byte[] bytes)
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
    			toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/kingrunes/somnia/Somnia", "tick", "()V"));
        			
    			m.instructions.insertBefore(fain, toInject);
    			
                break;
            }
        }
        
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
	}

	private byte[] patchEntityRenderer(byte[] bytes, boolean obf)
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
	
	/*
	private byte[] patchWorldServer(byte[] bytes, boolean obf)
	{
		String methodName = obf ? "b" : "tick";
		String methodName2 = obf ? "g" : "tickBlocksAndAmbiance";
		String methodName3 = obf ? "a" : "moodSoundAndLightCheck";
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        Iterator<MethodNode> methods = classNode.methods.iterator();
        AbstractInsnNode ain;
        MethodInsnNode min;
        while(methods.hasNext())
        {
        	MethodNode m = methods.next();
        	if (m.name.equals(methodName) && m.desc.equalsIgnoreCase("()V"))
        	{
        		Iterator<AbstractInsnNode> iter = m.instructions.iterator();
        		LdcInsnNode lin;
        		while (iter.hasNext())
        		{
        			ain = iter.next();
        			if (ain instanceof LdcInsnNode)
        			{
        				lin = (LdcInsnNode)ain;
        				if (lin.cst.equals("doMobSpawning"))
        				{
        					int index = m.instructions.indexOf(lin);
        					m.instructions.remove(ain);
        					m.instructions.remove(m.instructions.get(index-1));
        					
        					min = (MethodInsnNode)m.instructions.get(index-1);
        					
        					min.setOpcode(Opcodes.INVOKESTATIC);
        					min.desc = "(Lnet/minecraft/world/WorldServer;)Z";
        					min.name = "doMobSpawning";
        					min.owner = "com/kingrunes/somnia/Somnia";
        				}
        			}
        		}
            }
        	else if (m.name.equals(methodName2) && m.desc.equalsIgnoreCase("()V"))
        	{
        		Iterator<AbstractInsnNode> iter = m.instructions.iterator();
	     		while (iter.hasNext())
	     		{
	     			ain = iter.next();
	     			if (ain instanceof MethodInsnNode)
	     			{
	     				min = (MethodInsnNode)ain;
	     				if (min.name.equals(methodName3) && min.desc.startsWith("(IIL") && min.getOpcode() == Opcodes.INVOKEVIRTUAL)
	     				{
	     					min.setOpcode(Opcodes.INVOKESTATIC);
	     					min.name = "moodSoundAndLightCheck";
	     					min.owner = "com/kingrunes/somnia/Somnia";
	     					
	     					m.instructions.remove(m.instructions.get(m.instructions.indexOf(min)-4));
	     				}
	     			}
         		}
    		}
        }
        
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
	}
	
	private byte[] patchWorld(byte[] bytes)
	{
		String methodName = "a";
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
        	MethodNode m = methods.next();
        	if (m.name.equals(methodName))
        	{
        		m.access = Opcodes.ACC_PUBLIC;
        		break;
        	}
        }
        
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
	}
	*/
}