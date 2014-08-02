package com.kingrunes.somnia.common.util;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.server.ServerTickHandler;

public enum SomniaState
{
	IDLE,
	ACTIVE,
	WAITING_PLAYERS,
	EXPIRED,
	NOT_NOW,
	COOLDOWN;
	
	public static SomniaState getState(ServerTickHandler handler)
	{
		if (handler.currentSleepPeriod > Somnia.proxy.maxSleepTimePeriod)
			return EXPIRED;
		
		long totalWorldTime = handler.worldServer.getTotalWorldTime();
		
		if (handler.currentState != ACTIVE && handler.lastSleepStart > 0 && totalWorldTime-handler.lastSleepStart < Somnia.proxy.sleepCooldown)
			return COOLDOWN;
		
		if (!Somnia.proxy.validSleepPeriod.isTimeWithin(totalWorldTime % 24000))
			return NOT_NOW;
		
		if (handler.worldServer.playerEntities.isEmpty())
			return IDLE;
		
		@SuppressWarnings("unchecked")
		List<EntityPlayerMP> players = handler.worldServer.playerEntities;
		
		boolean sleeping, anySleeping = false, allSleeping = true;
		
		Iterator<EntityPlayerMP> iter = players.iterator();
		while (iter.hasNext())
		{
			EntityPlayerMP player = iter.next();
			sleeping = player.isPlayerSleeping() || ListUtils.<EntityPlayerMP>containsRef(player, Somnia.instance.ignoreList);
			anySleeping |= sleeping;
			allSleeping &= sleeping;
		}
		
		if (allSleeping)
			return ACTIVE;
		else if (anySleeping)
			return WAITING_PLAYERS;
		else
			return IDLE;
	}
}
