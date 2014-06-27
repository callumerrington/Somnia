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
	public WorldServer worldServer;
	private int checkTimer = 0;
	public boolean mbCheck = false;
	private int miCheck = -2;
	private double overflow = 0d;
	private int tps;
	private int currentSleepPeriod;
	private double multiplier = 1.0d;
	
	public ServerTickHandler(WorldServer worldServer)
	{
		this.worldServer = worldServer;
	}
	
	public void tickStart()
	{
		liTPS++;
		currentSleepPeriod++;
	
		if (Somnia.instance.serverTicking)
			return;
		
		checkTimer++;
		
		if (checkTimer == 10)
		{
			checkTimer = 0;
			boolean lbCheck = mbCheck;
			miCheck = Somnia.instance.allPlayersSleeping(worldServer);
			mbCheck = miCheck >= 0;
			
			if (lbCheck != mbCheck)
				currentSleepPeriod = 0;
			
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
				
				IChatComponent chatComponent = new ChatComponentText(miCheck == 1 ? "Something is keeping you awake!" : "You can't sleep forever!");
				synchronized (sleepingPlayers)
				{
					Iterator<EntityPlayer> iter = sleepingPlayers.iterator();
					EntityPlayer ep;
					while (iter.hasNext())
					{
						ep = iter.next();
						Somnia.channel.sendTo(packet, (EntityPlayerMP) ep);
						ep.addChatMessage(chatComponent);
					}
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
	
	private long mlMs = 0l, mlMs1 = -1;
	private int liTPS = 0;
	private void doMultipliedTicking()
	{
		if (mlMs1 < 0)
			mlMs1 = System.currentTimeMillis();
		
		Somnia.instance.serverTicking = true;
		
		long ms = 0, ms1 = System.currentTimeMillis();
		
		int a = 1;
		int b = (int) multiplier + (int) overflow;
		overflow -= (int) overflow;
		for (; a<b; a++)
		{
			doMultipliedServerTicking();
			ms += System.currentTimeMillis() - ms1;
			ms1 = System.currentTimeMillis();
		}
		overflow += b-a;
		
		if (ms > 50/Somnia.instance.countMultipliedTickHandlers())
		{
			multiplier -= .1d;
		}
		else
		{
			multiplier += .1d;
		}
		
		if (multiplier > Somnia.proxy.multiplierCap)
			multiplier = Somnia.proxy.multiplierCap;
		
		if (multiplier < Somnia.proxy.baseMultiplier)
			multiplier = Somnia.proxy.baseMultiplier;
		
		Somnia.instance.serverTicking = false;
		
		mlMs += System.currentTimeMillis()-mlMs1;
		mlMs1 = System.currentTimeMillis();
		
		if (mlMs > 1000)
		{
			mlMs -= 1000;
			tps = liTPS;
			liTPS = 0;
		}
		
	}
	
	private void doMultipliedServerTicking()
	{
		worldServer.tick();
		worldServer.updateEntities();
		worldServer.getEntityTracker().updateTrackedEntities();
		MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayersInDimension(new S03PacketTimeUpdate(worldServer.getTotalWorldTime(), worldServer.getWorldTime(), worldServer.getGameRules().getGameRuleBooleanValue("doDaylightCycle")), worldServer.provider.dimensionId);
		tickStart();
	}
}