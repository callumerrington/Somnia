package com.kingrunes.somnia.asm;

import java.util.Arrays;

import com.google.common.eventbus.EventBus;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;

public class SDummyContainer extends DummyModContainer
{
	public SDummyContainer()
	{
		super(new ModMetadata());
		ModMetadata meta = super.getMetadata();
		meta.modId = "SomniaCore";
		meta.name = "SomniaCore";
		meta.version = "1.1.1";
		meta.authorList = Arrays.asList("Kingrunes");
		meta.description = "This mod modifies Minecraft to allow Somnia to hook in";
		meta.url = "";
		meta.updateUrl = "";
		meta.screenshots = new String[0];
		meta.logoFile = "";
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller)
	{
		bus.register(this);
		return true;
	}
}