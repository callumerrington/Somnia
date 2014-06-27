package com.kingrunes.somnia.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.play.client.C0BPacketEntityAction;

import com.kingrunes.somnia.Somnia;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class ClientTickHandler
{
	private boolean moddedFOV = false;
	private float fov = -1;
	
	private boolean muted = false;
	private float defVol;
	
	private boolean disabledWorldRendering = true;
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event)
	{
		if (event.phase == Phase.END)
			tickEnd();
	}
	
	public void tickEnd()
	{
		Minecraft mc = Minecraft.getMinecraft();
		if (mc == null || mc.thePlayer == null)
			return;
		
		GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
		
		/*
		 * Fixes some rendering issues with high FOVs when the GUIs are open during sleep
		 */
		
		if (mc.currentScreen != null && mc.currentScreen instanceof GuiSleepMP)
		{
			if (Somnia.proxy.vanillaBugFixes)
			{
				if (!moddedFOV)
				{
					moddedFOV = true;
					if (gameSettings.fovSetting >= 0.75352114)
					{
						fov = gameSettings.fovSetting;
						gameSettings.fovSetting = 0.7253521f;
					}
				}
			}
		}
		else if (moddedFOV)
		{
			moddedFOV = false;
			if (fov > 0)
				Minecraft.getMinecraft().gameSettings.fovSetting = fov;
		}
		
		/*
		 * If the player is sleeping and the player has chosen the 'muteSoundWhenSleeping' option in the config,
		 * set the master volume to 0
		 */
		
		if (mc.thePlayer.isPlayerSleeping())
		{
			if (Somnia.proxy.muteSoundWhenSleeping)
			{
				if (!muted)
				{
					muted = true;
					defVol = gameSettings.getSoundLevel(SoundCategory.MASTER);
					gameSettings.setSoundLevel(SoundCategory.MASTER, .0f);
				}
			}
			
			if (Somnia.proxy.disableRendering)
				disabledWorldRendering = true;
		}
		else
		{
			if (muted)
			{
				muted = false;
				gameSettings.setSoundLevel(SoundCategory.MASTER, defVol);
			}
			
			if (disabledWorldRendering)
				disabledWorldRendering = false;
		}
		
		/*
		 * Auto wakes when the time is matched but wasn't matched on a previous tick. Otherwise we wouldn't be able to set wake times for the next day
		 * as it'd trigger the auto wake immediately.
		 */
		
		if (Somnia.clientAutoWakeTime > -1 && mc.theWorld.getTotalWorldTime() >= Somnia.clientAutoWakeTime)
		{
//			System.out.println("AUTO WAKING @ " + Somnia.clientAutoWakeTime % 24000);
			Somnia.clientAutoWakeTime = -1;
			mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, 3));
		}
	}
}