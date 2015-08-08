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
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class SClassTransformer implements IClassTransformer 
{
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		if (name.equalsIgnoreCase("net.minecraft.client.renderer.EntityRenderer"))
			return patchEntityRenderer(bytes, false);
		else if (name.equalsIgnoreCase("cji"))
			return patchEntityRenderer(bytes, true);
		else if (name.equalsIgnoreCase("net.minecraft.world.WorldServer"))
			return patchWorldServer(bytes, false);
		else if (name.equalsIgnoreCase("qt"))
			return patchWorldServer(bytes, true);
		else if (name.equalsIgnoreCase("net.minecraft.world.chunk.Chunk"))
			return patchChunk(bytes, false);
		else if (name.equalsIgnoreCase("bfh"))
			return patchChunk(bytes, true);
		else if (name.equalsIgnoreCase("net.minecraft.server.MinecraftServer"))
			return patchMinecraftServer(bytes);
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
	
	
	private byte[] patchWorldServer(byte[] bytes, boolean obf)
	{
		String 	methodTick = obf ? "c" : "tick",
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
	
	private byte[] patchChunk(byte[] bytes, boolean obf)
	{
		String methodName = obf ? "b" : "func_150804_b";
		String methodName2 = obf ? "n" : "func_150809_p";
		
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
	
	private byte[] patchMinecraftServer(byte[] bytes)
	{
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        Iterator<MethodNode> methods = classNode.methods.iterator();
        AbstractInsnNode ain = null;
        while(methods.hasNext())
        {
        	MethodNode m = methods.next();
        	if ((m.name.equals("y") || m.name.equals("tick")) && m.desc.equals("()V"))
        	{
        		AbstractInsnNode lrin = null;
        		Iterator<AbstractInsnNode> iter = m.instructions.iterator();
        		while (iter.hasNext())
        		{
					ain = iter.next();
					if (ain instanceof InsnNode && ((InsnNode)ain).getOpcode() == Opcodes.RETURN)
						lrin = ain;
				}
        		
        		if (lrin != null)
        		{
        			InsnList toInject = new InsnList();
        			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/kingrunes/somnia/Somnia", "tick", "()V", false));
        			
        			m.instructions.insertBefore(lrin, toInject);
        		}
                break;
        	}
        }
        
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
	}
}