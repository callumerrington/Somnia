package com.kingrunes.somnia.client.gui;

import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0BPacketEntityAction;

import org.lwjgl.opengl.GL11;

import com.kingrunes.somnia.common.StreamUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSomnia extends GuiSleepMP
{
	public static final String	SPEED_FORMAT = "%sx%s";
	
	public static final String 	COLOR = new String(new char[]{ (char)167 }),
								BLACK = COLOR+"0",
								WHITE = COLOR+"f",
								RED = COLOR+"c",
								DARK_RED = COLOR+"4",
								GOLD = COLOR+"6";
	
	public static final byte[]	BYTES_WHITE = new byte[]{ (byte) 255, (byte) 255, (byte) 255 },
								BYTES_DARK_RED = new byte[]{ (byte) 171, 0, 0 },
								BYTES_RED = new byte[]{ (byte) 255, 0, 0 },
								BYTES_GOLD = new byte[]{ (byte) 240, (byte) 200, 30 };
	
	private static RenderItem presetIconRenderer = new RenderItem();
	private static ItemStack clockItemStack = new ItemStack(Item.getItemById(347));
								
	
	public String status = "Waiting...";
	public double speed = 0;
	public int sleeping = 0, awake = 0;
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		this.mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(this.mc.thePlayer, 3));
	}
	
	@Override
    public void drawScreen(int par1, int par2, float par3)
    {
		super.drawScreen(par1, par2, par3);
		
		glPushMatrix();
		{
			glScalef(4f, 4f, 1f);
			presetIconRenderer.renderItemIntoGUI
			(
				fontRendererObj,
				this.mc.renderEngine,
				clockItemStack,
				(this.width - (this.width / 5))/4,
				((this.height / 2)-36)/4
			);
		}
		glPopMatrix();
		glDisable(GL11.GL_LIGHTING);
		glPushMatrix();
		{
			glScalef(2f, 2f, 1f);
			drawString
			(
				fontRendererObj,
				String.format(SPEED_FORMAT, getColorStringForSpeed(speed), speed),
//				Somnia.timeStringForWorldTime(mc.theWorld.getTotalWorldTime()), // Here for testing time sync
				5,
				5,
				Integer.MIN_VALUE
			);
		}
		glPopMatrix();
		boolean b = status.length() != 5; // cheap way of saying '!status.equals("Waiting...")'
		drawString(
				fontRendererObj,
				WHITE+status,
				b ? this.width / 2 : this.width - (this.width / 5) + 18,
				this.height / (b?4:2),
				Integer.MIN_VALUE
				);
    }
	
	public void updateField(String field, DataInputStream in) throws IOException
	{
		if (field.equalsIgnoreCase("status"))
			status = StreamUtils.readString(in);
		else if (field.equalsIgnoreCase("speed"))
			speed = in.readDouble();
		else if (field.equalsIgnoreCase("sleeping"))
			sleeping = in.readInt();
		else if (field.equalsIgnoreCase("awake"))
			awake = in.readInt();
	}
	
	public static byte[] getColorForSpeed(double speed)
	{
		if (speed < 8)
			return BYTES_WHITE;
		else if (speed < 20)
			return BYTES_DARK_RED;
		else if (speed < 30)
			return BYTES_RED;
		else
			return BYTES_GOLD;
	}
	
	public static String getColorStringForSpeed(double speed)
	{
		if (speed < 8)
			return WHITE;
		else if (speed < 20)
			return DARK_RED;
		else if (speed < 30)
			return RED;
		else
			return GOLD;
	}
}