package com.kingrunes.somnia.client;

import org.lwjgl.opengl.GL11;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.client.gui.GuiSomnia;
import com.kingrunes.somnia.client.util.GLUtils;
import com.kingrunes.somnia.common.util.ClassUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.RenderWorldLastEvent;

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
	
	
	/*
	 * Rendering values
	 */
	private static final float rMin = 0.0F, gMin = 1.0F, bMin = 0.0F;
	private static final float rMax = 1.0F, gMax = 0.0F, bMax = 0.0F;
	private static final float rDiff = rMax-rMin, gDiff = gMax-gMin, bDiff = bMax-bMin;
	
	@SubscribeEvent
	public void on3dRenderTick(RenderWorldLastEvent event)
	{
		if (!ClientProxy.renderPR)
			return;
		
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
//        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        
		Minecraft mc = Minecraft.getMinecraft();
		long tickTime;
		float ratio;
		TileEntity te;
		for (Object teObj : mc.theWorld.loadedTileEntityList)
		{
			te = (TileEntity)teObj;
			tickTime = ClassUtils.getTileEntityTime(te);
			if (tickTime == 0L)
				continue;
			ratio = (float) (2*Math.min(1-(ClientProxy.meanPR/tickTime), 1.0D));
//			ratio = 1/(float)((te.xCoord%10)+1); // For viewing color interpolation per 10 blocks on the x-axis
			GL11.glLineWidth(1.5F);
			GL11.glColor4f(rMin+(ratio*rDiff), gMin+(ratio*gDiff), bMin+(ratio*bDiff), 1.0F);
			GLUtils.renderBoundingBox(te.xCoord+.1d, te.yCoord+.1d, te.zCoord+.1d, .8d, .8d, .8d);
		}
		
//		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
	}
}