package com.kingrunes.somnia.asm;

import java.util.Iterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

public class InsnListPrinter
{
	public static void printInsnList(InsnList instructions)
	{
		int i = 0;
		AbstractInsnNode ain;
		Iterator<AbstractInsnNode> iter = instructions.iterator();
		while (iter.hasNext())
		{
			ain = iter.next();
			if (ain instanceof MethodInsnNode)
				System.out.println("[DEBUG] " + ((MethodInsnNode)ain).name + " @ " + i);
			else
				System.out.println("[DEBUG] " + ain.toString() + " @ " + i);
			i++;
		}
	}
}