package com.kingrunes.somnia.server;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.util.ClassUtils;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;

public class ServerTickHandler
{
	private static int activeTickHandlers = 0;
	
	public WorldServer worldServer;

	private int 	miCheck 			= -2; 		// (See Somnia.allPlayersSleeping)
	public 	boolean mbCheck 			= false;	// All players sleeping? miCheck >= 0 (see tickStart)
	
	private long 	currentSleepPeriod, 			// Incremented while mbCheck is true, reset when state is changed
					checkTimer 			= 0, 		// Used to schedule GUI update packets and sleep state checks
					lastTpsMillis		= 0,
					liTps 				= 0, 		// Counts ticks
					tps					= 0;		// Set per second to liTPS, used to work out actual multiplier to send to clients
	
	private double multiplier = Somnia.proxy.baseMultiplier;
	
	public ServerTickHandler(WorldServer worldServer)
	{
		this.worldServer = worldServer;
	}
	
	public void tickStart()
	{
		incrementCounters();
	
		checkTimer++;
		
		if (checkTimer == 10)
		{
			checkTimer = 0;
			boolean lbCheck = mbCheck;
			miCheck = Somnia.instance.allPlayersSleeping(worldServer);
			mbCheck = miCheck >= 0;
			
			if (lbCheck != mbCheck)
			{
				currentSleepPeriod = 0;
				if (miCheck == 0)
					activeTickHandlers++;
				else
					activeTickHandlers--;
			}
			
			if (miCheck < 1)
			{
				FMLProxyPacket packet = PacketHandler.buildGUIUpdatePacket
				(
					"status", mbCheck ? Somnia.timeStringForWorldTime(worldServer.getWorldTime()) : "Waiting for everyone to sleep!", //TODO: Localization
					"speed", (double)tps/20d
				);
				
				Somnia.channel.sendToDimension(packet, worldServer.provider.dimensionId);
			}
		}
		
		if (mbCheck)
		{
			@SuppressWarnings("unchecked")
			List<EntityPlayer> sleepingPlayers = worldServer.playerEntities;
			if ((Somnia.proxy.maxSleepTimePeriod > 0 && currentSleepPeriod >= Somnia.proxy.maxSleepTimePeriod) || miCheck == 1)
			{
				FMLProxyPacket packet = PacketHandler.buildGUIClosePacket();
				
				IChatComponent chatComponent = new ChatComponentText(miCheck == 1 ? "Something is keeping you awake!" : "You can't sleep forever!"); //TODO: Localization
				Iterator<EntityPlayer> iter = sleepingPlayers.iterator();
				EntityPlayer ep;
				while (iter.hasNext())
				{
					ep = iter.next();
					Somnia.channel.sendTo(packet, (EntityPlayerMP) ep);
					ep.addChatMessage(chatComponent);
				}
				
				mbCheck = false;
				return;
			}
			
			for (EntityPlayer sleepingPlayer : sleepingPlayers)
				ClassUtils.setSleepTimer(sleepingPlayer, 0);
			
			if (miCheck == 1)
				miCheck = -2;

			doMultipliedTicking();
		}
	}
	
	private void incrementCounters()
	{
		liTps++;
		if (mbCheck)
			currentSleepPeriod++;
	}
	
	private double overflow = .0d;
	private void doMultipliedTicking()
	{
		// We can't run 0.5 of a tick,
		//so we floor the multiplier and store the difference as overflow to be ran on the next tick
		int liMultiplier = (int) Math.floor(multiplier);
		double target = liMultiplier + overflow;
		int liTarget = (int) Math.floor(target);
		overflow = target - liTarget;
		
		long nanoTime = System.nanoTime();
		for (int i=0; i<liTarget; i++)
			doMultipliedServerTicking();
		
		if (nanoTime > 50.0d/activeTickHandlers)
			multiplier += .1d;
		else
			multiplier -= .1d;
		
		if (multiplier > Somnia.proxy.multiplierCap)
			multiplier = Somnia.proxy.multiplierCap;
		
		if (multiplier < Somnia.proxy.baseMultiplier)
			multiplier = Somnia.proxy.baseMultiplier;
		
		long currentTimeMillis = System.currentTimeMillis();
		if (currentTimeMillis-lastTpsMillis > 1000)
		{
			tps = liTps;
			liTps = 0;
			lastTpsMillis = currentTimeMillis;
		}
	}
	
	private void doMultipliedServerTicking()
	{
		worldServer.tick();
		worldServer.updateEntities();
		worldServer.getEntityTracker().updateTrackedEntities();
		MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayersInDimension(new S03PacketTimeUpdate(worldServer.getTotalWorldTime(), worldServer.getWorldTime(), worldServer.getGameRules().getGameRuleBooleanValue("doDaylightCycle")), worldServer.provider.dimensionId);
		incrementCounters();
	}
}