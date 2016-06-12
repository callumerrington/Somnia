package com.kingrunes.somnia.common;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.SomniaVersion;
import com.kingrunes.somnia.SomniaVersion.UpdateCheckerEntry;
import com.kingrunes.somnia.common.util.ClassUtils;
import com.kingrunes.somnia.common.util.SomniaEntityPlayerProperties;
import com.kingrunes.somnia.common.util.TimePeriod;
import com.kingrunes.somnia.server.ForgeEventHandler;
import com.kingrunes.somnia.server.ServerTickHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer.EnumStatus;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.WorldEvent;

public class CommonProxy
{
	private static final int CONFIG_VERSION = 2;
	
	public TimePeriod 	enterSleepPeriod;
	public TimePeriod 	validSleepPeriod;
	
	public double 		fatigueRate,
						fatigueReplenishRate,
						minimumFatigueToSleep,
						baseMultiplier,
						multiplierCap;
	
	public boolean 		fatigueSideEffects,
						enableProfiling,
						playersCanProfile,
						sleepWithArmor,
						vanillaBugFixes,
						fading,
						somniaGui,
						muteSoundWhenSleeping,
						ignoreMonsters,
						checkForUpdates,
						disableCreatureSpawning,
						disableRendering,
						disableMoodSoundAndLightCheck;
	
	public String		displayFatigue;

	public int			ticksPerDay,
						halfTicksPerDay;
	
	public static ForgeEventHandler forgeEventHandler;
	
	private static long lastUpdateCheck = 0l;
	
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
		ticksPerDay = config.get("timings", "ticksPerDay", 24000).getInt();
		halfTicksPerDay = ticksPerDay/2;
		
		/*
		 * Fatigue
		 */
		fatigueSideEffects = config.get("fatigue", "fatigueSideEffects", true).getBoolean(true);
		displayFatigue = config.get("fatigue", "displayFatigue", "br").getString();
		fatigueRate = config.get("fatigue", "fatigueRate", 0.00208d).getDouble(0.00208d);
		fatigueReplenishRate = config.get("fatigue", "fatigueReplenishRate", 0.00833d).getDouble(0.00833d);
		minimumFatigueToSleep = config.get("fatigue", "minimumFatigueToSleep", 20.0d).getDouble(20.0d);
		
		/*
		 * Logic
		 */
		baseMultiplier = config.get("logic", "baseMultiplier", 1.0d).getDouble(1.0d);
		multiplierCap = config.get("logic", "multiplierCap", 100.0d).getDouble(100.0d);
		
		/*
		 * Profiling (Not implemented)
		*/
		enableProfiling = config.get("profiling", "enableProfiling", true).getBoolean(true);
		playersCanProfile = config.get("profiling", "playersCanProfile", false).getBoolean(false);
		
		/*
		 * Options
		 */
		sleepWithArmor = config.get("options", "sleepWithArmor", false).getBoolean(false);
		vanillaBugFixes = config.get("options", "vanillaBugFixes", true).getBoolean(true);
		fading = config.get("options", "fading", true).getBoolean(true);
		somniaGui = config.get("options", "somniaGui", true).getBoolean(true);
		muteSoundWhenSleeping = config.get("options", "muteSoundWhenSleeping", false).getBoolean(false);
		ignoreMonsters = config.get("options", "ignoreMonsters", false).getBoolean(false);
		checkForUpdates = config.get("options", "checkForUpdates", true).getBoolean(true);

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
	
	/*
	 * - Are updates enabled in the config?
	 * - Are we on the client thread? Yes - run, No - run _only_ if on dedicated server
	 * - Has it been an hour since we last checked for updates?
	 */
	public boolean shouldCheckForUpdates()
	{
		return checkForUpdates  && (FMLCommonHandler.instance().getEffectiveSide().isClient() != getClass().equals(CommonProxy.class)) && System.currentTimeMillis()-lastUpdateCheck > 3600000l;
	}
	
	private void checkForUpdates()
	{
		lastUpdateCheck = System.currentTimeMillis();
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				System.out.println("[Somnia] Checking for updates...");
				UpdateCheckerEntry result;
				try
				{
					result = SomniaVersion.checkForUpdates();
					
					if (result != null)
					{
						printMessage(String.format("Somnia %s is available for download from: %s", result.getVersionString(), result.getUrl()));
						if (result.getComment().length() > 0)
							printMessage("Comment: " + result.getComment());
					}
				}
				catch (IOException e) // All we can do is log it
				{
					e.printStackTrace();
				}
			}
		});
		thread.setName("Somnia-Updater");
		thread.start();
//		HashMap<String, UpdateCheckerEntry[]> map = new HashMap<String, SomniaVersion.UpdateCheckerEntry[]>();
//		map.put("1.7.10", new UpdateCheckerEntry[]{ new UpdateCheckerEntry("1.7.10", "memes", "lel") });
//		System.out.println(new Gson().toJson(new SomniaVersion.UpdateCheckerResponse(map)));
	}
	
	public void printMessage(String message)
	{
		System.out.println("[Somnia] " + message);
	}

	@SubscribeEvent
	public void worldLoadHook(WorldEvent.Load event)
	{
		if (shouldCheckForUpdates())
			checkForUpdates();
		
		if (event.world instanceof WorldServer)
		{
			WorldServer worldServer = (WorldServer)event.world;
			Somnia.instance.tickHandlers.add(new ServerTickHandler(worldServer));
			System.out.println("[Somnia] Registering tick handler for loading world [" + worldServer.provider.dimensionId + "]");
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
					System.out.println("[Somnia] Removing tick handler for unloading world [" + worldServer.provider.dimensionId + "]");
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
			if (!PlayerSleepTickHandler.serverState.sleepOverride)
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
			SomniaEntityPlayerProperties props = SomniaEntityPlayerProperties.get(event.entityPlayer);
			if (props != null && props.getFatigue() < minimumFatigueToSleep)
			{
				if (!enterSleepPeriod.isTimeWithin(event.entityPlayer.worldObj.getWorldTime() % Somnia.proxy.ticksPerDay))
				{
//					event.result = EnumStatus.NOT_POSSIBLE_NOW;
					event.result = EnumStatus.OTHER_PROBLEM;
					event.entityPlayer.addChatMessage(new ChatComponentTranslation("somnia.status.cooldown"));
					return;
				}
			}
			
			if (!sleepWithArmor && Somnia.doesPlayHaveAnyArmor(event.entityPlayer))
			{
				event.result = EnumStatus.OTHER_PROBLEM;
				event.entityPlayer.addChatMessage(new ChatComponentTranslation("somnia.status.armor"));
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
	
	public void handleGUIOpenPacket()
	{}

	public void handlePropUpdatePacket(DataInputStream in) throws IOException
	{}

	public void handleGUIClosePacket(EntityPlayerMP player)
	{}

	public void handleProfilerResultPacket(DataInputStream in) throws IOException
	{}
}