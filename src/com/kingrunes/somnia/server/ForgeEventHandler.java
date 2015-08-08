package com.kingrunes.somnia.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.PlayerSleepTickHandler;
import com.kingrunes.somnia.common.util.SomniaEntityPlayerProperties;

public class ForgeEventHandler
{
	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event)
	{
		if (event.entity != null && event.entity.worldObj != null && !event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
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
		
		boolean isSleeping = PlayerSleepTickHandler.serverState.sleepOverride || player.isPlayerSleeping();
		
		if (isSleeping)
			fatigue -= Somnia.proxy.fatigueReplenishRate;
		else
			fatigue += Somnia.proxy.fatigueRate;
		
		if (fatigue > 100.0d)
			fatigue = 100.0d;
		else if (fatigue < .0d)
			fatigue = .0d;
//		fatigue = 69.8d;
		props.setFatigue(fatigue);
		
		if (++props.fatigueUpdateCounter >= 100)
		{
			props.fatigueUpdateCounter = 0;
			Somnia.channel.sendTo(PacketHandler.buildPropUpdatePacket(0x01, 0x00, fatigue), (EntityPlayerMP) player);
			
			// Side effects
			if (Somnia.proxy.fatigueSideEffects)
			{
				if (fatigue > 70.0d && props.lastSideEffectStage < 70)
				{
					props.lastSideEffectStage = 70;
					player.addPotionEffect(new PotionEffect(Potion.confusion.id, 150, 0));
				}
				else if (fatigue > 80.0d && props.lastSideEffectStage < 80)
				{
					props.lastSideEffectStage = 80;
					player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 300, 2));
				}
				else if (fatigue > 90.0d && props.lastSideEffectStage < 90)
				{
					props.lastSideEffectStage = 90;
					player.addPotionEffect(new PotionEffect(Potion.poison.id, 200, 1));
				}
				else if (fatigue > 95.0d)
					player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 150, 3));
				else if (fatigue < 70.0d)
					props.lastSideEffectStage = -1;
			}
		}
	}
}
