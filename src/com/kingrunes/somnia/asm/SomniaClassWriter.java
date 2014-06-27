package com.kingrunes.somnia.asm;

import org.objectweb.asm.ClassWriter;

public class SomniaClassWriter extends ClassWriter
{
	private String precalculatedCommonSuperClass;
	
	public SomniaClassWriter(int flags, String precalculatedCommonSuperClass)
	{
		super(flags);
		this.precalculatedCommonSuperClass = precalculatedCommonSuperClass;
	}

	@Override
	protected String getCommonSuperClass(String a, String b)
	{
		System.out.println("[SOMNIA] [DEBUG] a = " + a + " b = " + b);
		if (precalculatedCommonSuperClass != null)
		{
			System.out.println("[Somnia] Overriding common superclass with: " + precalculatedCommonSuperClass);
		}
		else
		{
			precalculatedCommonSuperClass = super.getCommonSuperClass(a, b);
			System.out.println("[Somnia] [DEBUG] a = " + a + " b = " + b + " output = " + precalculatedCommonSuperClass);
		}
		return precalculatedCommonSuperClass;
	}
}