package com.kingrunes.somnia.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.util.ClassUtils;

public class PlayerSleepTickHandler
{
	/*
	 * A sided state for caching player data 
	 */
	public static class State
	{
		public boolean sleepOverride = false;
	}
	
	public static State clientState = new State(), serverState = new State();
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		State state = event.side == Side.CLIENT ? clientState : serverState;
		if (event.phase == Phase.START)
			tickStart(state, event.player);
		else
			tickEnd(state, event.player);
	}
	
	public void tickStart(State state, EntityPlayer player)
	{
		if (player.isPlayerSleeping())
		{
			state.sleepOverride = true;
			ClassUtils.setSleeping(player, false);
			
			if (player.worldObj.isRemote && Somnia.proxy.fading)
			{
				int sleepTimer = player.getSleepTimer()+1;
				if (sleepTimer >= 99)
					sleepTimer = 98;
				ClassUtils.setSleepTimer(player, sleepTimer);
			}
		}
	}

	public void tickEnd(State state, EntityPlayer player)
	{
		if (state.sleepOverride)
		{
			ClassUtils.setSleeping(player, true);
			state.sleepOverride = false;
		}
	}
}