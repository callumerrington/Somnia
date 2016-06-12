package com.kingrunes.somnia.client;

import com.kingrunes.somnia.common.util.ClassUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

public class ClientInterationHandler
{
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (ClientProxy.renderPR && event.entityPlayer != null && event.action == Action.RIGHT_CLICK_BLOCK && event.entityPlayer.isSneaking())
		{
			event.setCanceled(true);
			TileEntity te = event.entityPlayer.worldObj.getTileEntity(event.x, event.y, event.z);
			if (te != null)
			{
				long tickTime = ClassUtils.getTileEntityTime(te);
				event.entityPlayer.addChatMessage(new ChatComponentText(""));
				event.entityPlayer.addChatMessage(new ChatComponentText("Tile info:"));
				event.entityPlayer.addChatMessage(new ChatComponentText(String.format("    Class: %s", te.getClass().getCanonicalName())));
				event.entityPlayer.addChatMessage(new ChatComponentText(String.format("    Avg. Time: %s", tickTime)));
			}
		}
	}
}
