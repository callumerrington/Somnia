package com.kingrunes.somnia.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.PlayerSleepTickHandler;
import com.kingrunes.somnia.common.util.SomniaEntityPlayerProperties;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class ForgeEventHandler
{
	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event)
	{
		if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
			SomniaEntityPlayerProperties.register((EntityPlayer) event.entity);
	}
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase != Phase.START || event.player.worldObj.isRemote)
			return;
		
		EntityPlayer player = event.player;
		SomniaEntityPlayerProperties props = SomniaEntityPlayerProperties.get(player);
		if (props == null)
			props = SomniaEntityPlayerProperties.register(player);
		double fatigue = props.getFatigue();
		
		boolean isSleeping = PlayerSleepTickHandler.serverState.sleepOverride;
		
		if (isSleeping)
			fatigue += .07;
		else
			fatigue -= .0035;
		
		if (fatigue > SomniaEntityPlayerProperties.FATIGUE_MAX)
			fatigue = SomniaEntityPlayerProperties.FATIGUE_MAX;
		else if (fatigue < .0d)
			fatigue = .0d;
		
		props.setFatigue(fatigue);
		
		if (++props.fatigueUpdateCounter >= 20)
		{
			props.fatigueUpdateCounter = 0;
			Somnia.channel.sendTo(PacketHandler.buildPropUpdatePacket(0x01, 0x00, fatigue), (EntityPlayerMP) player);
		}
	}
}
