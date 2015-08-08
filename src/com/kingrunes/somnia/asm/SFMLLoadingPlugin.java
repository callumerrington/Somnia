package com.kingrunes.somnia.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@MCVersion(value = "1.8")
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