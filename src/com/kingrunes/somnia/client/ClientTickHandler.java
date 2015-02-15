package com.kingrunes.somnia.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.play.client.C0BPacketEntityAction;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.client.gui.GuiSomnia;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class ClientTickHandler
{
	private static final String FATIGUE_FORMAT = GuiSomnia.WHITE + "Fatigue: %.2f";
	
	private boolean moddedFOV = false;
	private float fov = -1;
	
	private boolean muted = false;
	private float defVol;
	
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
			if (fov > .0f)
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
		}
		else
		{
			if (muted)
			{
				muted = false;
				gameSettings.setSoundLevel(SoundCategory.MASTER, defVol);
			}
		}
		
		/*
		 * Note the isPlayerSleeping() check. Without this, the mod exploits a bug which exists in vanilla Minecraft which
		 * allows the player to teleport back to there bed from anywhere in the world at any time.
		 */
		
		if (Somnia.clientAutoWakeTime > -1 && mc.thePlayer.isPlayerSleeping() && mc.theWorld.getTotalWorldTime() >= Somnia.clientAutoWakeTime)
		{
			Somnia.clientAutoWakeTime = -1;
			mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, 3));
		}
	}
	
	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		if (event.phase != Phase.END || ClientProxy.playerFatigue == -1 || (mc.currentScreen != null && !(mc.currentScreen instanceof GuiIngameMenu) && !(mc.currentScreen instanceof GuiSomnia)))
			return;
		
		FontRenderer fontRenderer = mc.fontRenderer;
		ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		String str = String.format(FATIGUE_FORMAT, ClientProxy.playerFatigue);
		int x, y, stringWidth = fontRenderer.getStringWidth(str);
		String param = Somnia.proxy.displayFatigue.toLowerCase();
		if (param.equals("tl"))
		{
			x = 10;
			y = 10;
		}
		else if (param.equals("tr"))
		{
			x = scaledResolution.getScaledWidth()-stringWidth-10;
			y = 10;
		}
		else if (param.equals("bl"))
		{
			x = 10;
			y = scaledResolution.getScaledHeight()-fontRenderer.FONT_HEIGHT-10;
		}
		else if (param.equals("br"))
		{
			x = scaledResolution.getScaledWidth()-stringWidth-10;
			y = scaledResolution.getScaledHeight()-fontRenderer.FONT_HEIGHT-10;
		}
		else
			return;
		
		fontRenderer.drawString(str, x, y, Integer.MIN_VALUE);
	}
}