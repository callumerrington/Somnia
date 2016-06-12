package com.kingrunes.somnia.asm.api;

public interface Patcher
{
	public boolean matches(String name);
	public byte[] patch(byte[] bytes, boolean obf);
}
