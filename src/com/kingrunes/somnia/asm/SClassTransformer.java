package com.kingrunes.somnia.asm;

import java.util.ArrayList;
import java.util.List;

import com.kingrunes.somnia.asm.api.Patcher;
import com.kingrunes.somnia.asm.patchers.PatcherChunk;
import com.kingrunes.somnia.asm.patchers.PatcherClassUtils;
import com.kingrunes.somnia.asm.patchers.PatcherEntityRenderer;
import com.kingrunes.somnia.asm.patchers.PatcherFMLCommonHandler;
import com.kingrunes.somnia.asm.patchers.PatcherTileEntity;
import com.kingrunes.somnia.asm.patchers.PatcherWorld;
import com.kingrunes.somnia.asm.patchers.PatcherWorldServer;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

public class SClassTransformer implements IClassTransformer 
{
	private static List<Patcher> patchers;
	
	static
	{
		SClassTransformer.patchers = new ArrayList<Patcher>(7);
		patchers.add(new PatcherChunk());
		patchers.add(new PatcherClassUtils());
		patchers.add(new PatcherEntityRenderer());
		patchers.add(new PatcherFMLCommonHandler());
		patchers.add(new PatcherTileEntity());
		patchers.add(new PatcherWorld());
		patchers.add(new PatcherWorldServer());
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		for (Patcher patcher : SClassTransformer.patchers)
		{
			if (patcher.matches(transformedName))
			{
				System.out.println("[Somnia] Patching " + transformedName + " with " + patcher.getClass().getSimpleName());
				return patcher.patch(bytes, !((Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment")));
			}
		}
		return bytes;
	}
}