package com.kingrunes.somnia.common;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.EnumStatus;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.WorldEvent;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.util.ClassUtils;
import com.kingrunes.somnia.common.util.TimePeriod;
import com.kingrunes.somnia.server.ForgeEventHandler;
import com.kingrunes.somnia.server.ServerTickHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CommonProxy
{
	private static final int CONFIG_VERSION = 2;
	
	public TimePeriod 	enterSleepPeriod;
	public TimePeriod 	validSleepPeriod;
	
	public long 		maxSleepTimePeriod;
	
	public double 		fatigueRate,
						fatigueReplenishRate,
						minimumFatigueToSleep,
						baseMultiplier,
						multiplierCap;
	
	public boolean 		fatigueSideEffects,
						tpsGraph,
						secondsOnGraph,
						sleepWithArmor,
						vanillaBugFixes,
						fading,
						somniaGui,
						muteSoundWhenSleeping,
						ignoreMonsters,
						disableCreatureSpawning,
						disableRendering,
						disableMoodSoundAndLightCheck;
	
	public String		displayFatigue;
	
	public static ForgeEventHandler forgeEventHandler;
	
	public void configure(File file)
	{
		Configuration config = new Configuration(file);
		config.load();
		Property property = config.get(Configuration.CATEGORY_GENERAL, "configVersion", 0);
		if (property.getInt() != CONFIG_VERSION)
			file.delete();
		config = new Configuration(file);
		config.load();
		
		config.get(Configuration.CATEGORY_GENERAL, "configVersion", CONFIG_VERSION);

		/*
		 * Timings
		 */
		enterSleepPeriod =
				new TimePeriod(
						config.get("timings", "enterSleepStart", 0).getInt(),
						config.get("timings", "enterSleepEnd", 24000).getInt()
						);
		validSleepPeriod =
				new TimePeriod(
						config.get("timings", "validSleepStart", 0).getInt(),
						config.get("timings", "validSleepEnd", 24000).getInt()
						);
		
		/*
		 * Fatigue
		 */
		fatigueSideEffects = config.get("fatigue", "fatigueSideEffects", true).getBoolean(true);
		displayFatigue = config.get("fatigue", "displayFatigue", "br").getString();
		fatigueRate = config.get("fatigue", "fatigueRate", 0.00417d).getDouble(0.00417d);
		fatigueReplenishRate = config.get("fatigue", "fatigueReplenishRate", 0.00833d).getDouble(0.00833d);
		minimumFatigueToSleep = config.get("fatigue", "minimumFatigueToSleep", 40.0d).getDouble(40.0d);
		
		/*
		 * Logic
		 */
		baseMultiplier = config.get("logic", "baseMultiplier", 1.0d).getDouble(1.0d);
		multiplierCap = config.get("logic", "multiplierCap", 100.0d).getDouble(100.0d);
		
		/*
		 * Profiling (Not implemented)
		secondsOnGraph = config.get("profiling", "secondsOnGraph", 30).getInt();
		tpsGraph = config.get("profiling", "tpsGraph", false).getBoolean(false);
		*/
		
		/*
		 * Options
		 */
		sleepWithArmor = config.get("options", "sleepWithArmor", false).getBoolean(false);
		vanillaBugFixes = config.get("options", "vanillaBugFixes", true).getBoolean(true);
		fading = config.get("options", "fading", true).getBoolean(true);
		somniaGui = config.get("options", "somniaGui", true).getBoolean(true);
		muteSoundWhenSleeping = config.get("options", "muteSoundWhenSleeping", false).getBoolean(false);
		ignoreMonsters = config.get("options", "ignoreMonsters", false).getBoolean(false);

		/*
		 * Performance
		 */
		disableCreatureSpawning = config.get("performance", "disableCreatureSpawning", false).getBoolean(false);
		disableRendering = config.get("performance", "disableRendering", false).getBoolean(false);
		disableMoodSoundAndLightCheck = config.get("performance", "disableMoodSoundAndLightCheck", false).getBoolean(false);
		
		config.save();
	}
	
	public void register()
	{
		MinecraftForge.EVENT_BUS.register(this);
		
		FMLCommonHandler.instance().bus().register(new PlayerSleepTickHandler());
		
		forgeEventHandler = new ForgeEventHandler();
		MinecraftForge.EVENT_BUS.register(forgeEventHandler);
		FMLCommonHandler.instance().bus().register(forgeEventHandler);
	}
	
	@SubscribeEvent
	public void worldLoadHook(WorldEvent.Load event)
	{
		if (event.world instanceof WorldServer)
		{
			WorldServer worldServer = (WorldServer)event.world;
			Somnia.instance.tickHandlers.add(new ServerTickHandler(worldServer));
			System.out.println("[Somnia] Registering tick handler for loading world!");
		}
	}
	
	@SubscribeEvent
	public void worldUnloadHook(WorldEvent.Unload event)
	{
		if (event.world instanceof WorldServer)
		{
			WorldServer worldServer = (WorldServer)event.world;
			Iterator<ServerTickHandler> iter = Somnia.instance.tickHandlers.iterator();
			ServerTickHandler serverTickHandler;
			while (iter.hasNext())
			{
				serverTickHandler = (ServerTickHandler) iter.next();
				if (serverTickHandler.worldServer == worldServer)
				{
					System.out.println("[Somnia] Removing tick handler for unloading world!");
					iter.remove();
					break;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerDamage(LivingHurtEvent event)
	{
		if (event.entityLiving instanceof EntityPlayerMP)
		{
			if (!((EntityPlayer)event.entityLiving).isPlayerSleeping())
				return;
			
	        Somnia.channel.sendTo(PacketHandler.buildGUIClosePacket(), (EntityPlayerMP) event.entityLiving);
		}
	}
	
	@SubscribeEvent
	public void sleepHook(PlayerSleepInBedEvent event)
	{
		onSleep(event);
	}
	
	/*
	 * This method reimplements the entire sleep checking logic, look away
	 */
	public void onSleep(PlayerSleepInBedEvent event)
	{
		if (event.result != null && event.result != EnumStatus.OK)
			return;
		
		if (!event.entityPlayer.worldObj.isRemote)
        {
			if (!enterSleepPeriod.isTimeWithin(event.entityPlayer.worldObj.getWorldTime() % 24000))
			{
				event.result = EnumStatus.NOT_POSSIBLE_NOW;
				return;
			}
			
			if (!sleepWithArmor && Somnia.doesPlayHaveAnyArmor(event.entityPlayer))
			{
				event.result = EnumStatus.OTHER_PROBLEM;
				event.entityPlayer.addChatMessage(new ChatComponentText("That armor looks uncomfortable!"));
				return;
			}
			
            if (event.entityPlayer.isPlayerSleeping() || !event.entityPlayer.isEntityAlive())
            {
            	event.result = EnumStatus.OTHER_PROBLEM;
            	return;
            }

            if (!event.entityPlayer.worldObj.provider.isSurfaceWorld())
            {
            	event.result = EnumStatus.NOT_POSSIBLE_HERE;
                return;
            }

            if (Math.abs(event.entityPlayer.posX - (double)event.x) > 3.0d || Math.abs(event.entityPlayer.posY - (double)event.y) > 2.0d || Math.abs(event.entityPlayer.posZ - (double)event.z) > 3.0d)
            {
                event.result = EnumStatus.TOO_FAR_AWAY;
                return;
            }

            if (!this.ignoreMonsters)
            {
	            double d0 = 8.0D;
	            double d1 = 5.0D;
	            
				List<?> list = event.entityPlayer.worldObj.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.getBoundingBox((double)event.x - d0, (double)event.y - d1, (double)event.z - d0, (double)event.x + d0, (double)event.y + d1, (double)event.z + d0));
	
	            if (!list.isEmpty())
	            {
	                event.result = EnumStatus.NOT_SAFE;
	                return;
	            }
            }
        }

        if (event.entityPlayer.isRiding())
        {
        	event.entityPlayer.mountEntity((Entity)null);
        }

        ClassUtils.setSize(event.entityPlayer, 0.2F, 0.2F);
        event.entityPlayer.yOffset = 0.2F;

        if (event.entityPlayer.worldObj.blockExists(event.x, event.y, event.z))
        {
        	int l = event.entityPlayer.worldObj.getBlock(event.x, event.y, event.z).getBedDirection(event.entityPlayer.worldObj, event.x, event.y, event.z);
            float f1 = 0.5F;
            float f = 0.5F;

            switch (l)
            {
                case 0:
                    f1 = 0.9F;
                    break;
                case 1:
                    f = 0.1F;
                    break;
                case 2:
                    f1 = 0.1F;
                    break;
                case 3:
                    f = 0.9F;
            }

            ClassUtils.call_func_71013_b(event.entityPlayer, l);
            event.entityPlayer.setPosition((double)((float)event.x + f), (double)((float)event.y + 0.9375F), (double)((float)event.z + f1));
        }
        else
        	event.entityPlayer.setPosition((double)((float)event.x + 0.5F), (double)((float)event.y + 0.9375F), (double)((float)event.z + 0.5F));

        ClassUtils.setSleeping(event.entityPlayer, true);
        ClassUtils.setSleepTimer(event.entityPlayer, 0);
        event.entityPlayer.playerLocation = new ChunkCoordinates(event.x, event.y, event.z);
        event.entityPlayer.motionX = event.entityPlayer.motionZ = event.entityPlayer.motionY = 0.0D;

        if (!event.entityPlayer.worldObj.isRemote)
        {
            event.entityPlayer.worldObj.updateAllPlayersSleepingFlag();
        }
		
        event.result = EnumStatus.OK;
        
        if (event.entityPlayer.worldObj.isRemote)
	        return;
        
        Somnia.channel.sendTo(PacketHandler.buildGUIOpenPacket(), (EntityPlayerMP) event.entityPlayer);
	}
	
	/*
	 * The following methods are implemented client-side only
	 */
	
	public void handleGUIOpenPacket() throws IOException
	{}

	public void handlePropUpdatePacket(DataInputStream in) throws IOException
	{}

	public void handleGUIClosePacket(EntityPlayerMP player)
	{}
}