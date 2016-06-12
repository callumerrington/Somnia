package com.kingrunes.somnia.asm.api;

public abstract class AbstractPatcher implements Patcher
{
	public final String className;
	
	public AbstractPatcher(String className)
	{
		this.className = className;
	}
	
	@Override
	public boolean matches(String name)
	{
		return className.equals(name);
	}
}
