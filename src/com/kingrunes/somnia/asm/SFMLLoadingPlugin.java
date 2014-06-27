package com.kingrunes.somnia.asm;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@MCVersion(value = "1.7.2")
@TransformerExclusions(value={"com.kingrunes.somnia.asm"})
public class SFMLLoadingPlugin implements IFMLLoadingPlugin
{
	@Override
	public String[] getASMTransformerClass()
	{
		return new String[] { SClassTransformer.class.getName() };
	}

	@Override
	public String getModContainerClass()
	{
		return SDummyContainer.class.getName();
	}

	@Override
	public void injectData(Map<String, Object> data)
	{

	}
	
	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}