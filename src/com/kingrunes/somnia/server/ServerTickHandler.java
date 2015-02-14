package com.kingrunes.somnia.server;

import static com.kingrunes.somnia.common.util.SomniaState.ACTIVE;
import static com.kingrunes.somnia.common.util.SomniaState.COOLDOWN;
import static com.kingrunes.somnia.common.util.SomniaState.EXPIRED;
import static com.kingrunes.somnia.common.util.SomniaState.NOT_NOW;
import static com.kingrunes.somnia.common.util.SomniaState.WAITING_PLAYERS;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.CommonProxy;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.util.SomniaState;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

public class ServerTickHandler
{
	public static final String TRANSLATION_FORMAT = "somnia.status.%s";
			
	private static int activeTickHandlers = 0;
	
	public WorldServer worldServer;
	public SomniaState currentState;
	
	public long 	lastSleepStart,
					currentSleepPeriod; 			// Incremented while mbCheck is true, reset when state is changed
	public long		checkTimer 			= 0, 		// Used to schedule GUI update packets and sleep state checks
					lastTpsMillis		= 0,
					liTps 				= 0, 		// Counts ticks
					tps					= 0;		// Set per second to liTPS, used to work out actual multiplier to send to clients
	
	private double 	multiplier 			= Somnia.proxy.baseMultiplier;
	
	public ServerTickHandler(WorldServer worldServer)
	{
		this.worldServer = worldServer;
	}
	
	public void tickStart()
	{
		if (++checkTimer == 10)
		{
			checkTimer = 0;
			
			SomniaState prevState = currentState;
			currentState = SomniaState.getState(this);
			
			
			if (prevState != currentState)
			{
				currentSleepPeriod = 0;
				if (currentState == ACTIVE) // acceleration started
				{
					lastSleepStart = worldServer.getTotalWorldTime();
					activeTickHandlers++;
				}
				else if (prevState == ACTIVE) // acceleration stopped
				{
					activeTickHandlers--;
					
					if (currentState == EXPIRED || currentState == NOT_NOW)
						closeGuiWithMessage(currentState.toString());
				}
			}
			
			if (currentState == ACTIVE || currentState == WAITING_PLAYERS || currentState == COOLDOWN)
			{
				FMLProxyPacket packet = PacketHandler.buildPropUpdatePacket
				(
					0x00,
					0x00, currentState == ACTIVE ? (double)tps/20d : .0d,
					0x01, currentState == ACTIVE ? Somnia.timeStringForWorldTime(worldServer.getWorldTime()) : "f:"+currentState.toString()
				);
				
				Somnia.channel.sendToDimension(packet, worldServer.provider.dimensionId);
			}
		}
		
		if (currentState == ACTIVE)
			doMultipliedTicking();
	}
	
	private void closeGuiWithMessage(String key)
	{
		FMLProxyPacket packet = PacketHandler.buildGUIClosePacket();
		
		IChatComponent chatComponent = new ChatComponentTranslation(String.format(TRANSLATION_FORMAT, key), new Object[0]);
		@SuppressWarnings("unchecked")
		Iterator<EntityPlayer> iter = ((List<EntityPlayer>)worldServer.playerEntities).iterator();
		EntityPlayer ep;
		while (iter.hasNext())
		{
			ep = iter.next();
			if (ep.isPlayerSleeping())
			{
				Somnia.channel.sendTo(packet, (EntityPlayerMP) ep);
				if (ep.isPlayerSleeping()) // this if might stop random teleporting when players have already woken
					ep.wakeUpPlayer(false, true, true); // Stop clients ignoring GUI close packets (major hax)
				ep.addChatMessage(chatComponent);
			}
		}
	}

	private void incrementCounters()
	{
		liTps++;
		currentSleepPeriod++;
	}
	
	private double overflow = .0d;
	private void doMultipliedTicking()
	{
		/*
		 * We can't run 0.5 of a tick,
		 * so we floor the multiplier and store the difference as overflow to be ran on the next tick
		 */
//		int liMultiplier = (int) Math.floor(multiplier);
		double target = multiplier + overflow;
		int liTarget = (int) Math.floor(target);
		overflow = target - liTarget;
		
		long delta = System.currentTimeMillis();
		for (int i=0; i<liTarget; i++)
			doMultipliedServerTicking();
		delta = System.currentTimeMillis() - delta;
		
		MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayersInDimension(new S03PacketTimeUpdate(worldServer.getTotalWorldTime(), worldServer.getWorldTime(), worldServer.getGameRules().getGameRuleBooleanValue("doDaylightCycle")), worldServer.provider.dimensionId);
		
		if (delta > (50.0d/activeTickHandlers))
			multiplier -= .1d;
		else
			multiplier += .1d;
		
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
		FMLCommonHandler.instance().onPreWorldTick(worldServer);
		worldServer.tick();
		worldServer.updateEntities();
		worldServer.getEntityTracker().updateTrackedEntities();
		FMLCommonHandler.instance().onPostWorldTick(worldServer);
		
		/*
		 * Work around for making sure fatigue is updated with every tick (including Somnia ticks)
		 */
		for (Object obj : worldServer.playerEntities)
			CommonProxy.forgeEventHandler.onPlayerTick(new TickEvent.PlayerTickEvent(Phase.START, (EntityPlayer) obj));
		
		incrementCounters();
	}
}