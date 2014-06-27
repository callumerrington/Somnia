package com.kingrunes.somnia.client.util;

import org.lwjgl.opengl.GL11;

public class GLUtils 
{
	public static void glDisable()
	{
		glDisable(true);
	}
	
	public static void glDisable(boolean par1)
	{
		GL11.glDisable(GL11.GL_TEXTURE_2D); //disables 2d texture
		GL11.glDisable(GL11.GL_LIGHTING); //disables lighting
		GL11.glDisable(GL11.GL_DEPTH_TEST); //disables lighting
		if (par1) GL11.glBlendFunc(770, 771); //transparency
		GL11.glEnable(GL11.GL_LINE_SMOOTH); //make the lines smooth
		GL11.glEnable(GL11.GL_BLEND); //make the lines smooth
	}
	
	public static void glEnable(boolean par1)
	{
		GL11.glEnable(GL11.GL_TEXTURE_2D); //enables 2d texture
		if (par1) GL11.glEnable(GL11.GL_LIGHTING); //enables lighting
		GL11.glEnable(GL11.GL_DEPTH_TEST); //enables depth test
		GL11.glDisable(GL11.GL_LINE_SMOOTH);//disable the smoothness
		GL11.glDisable(GL11.GL_BLEND);//disable the smoothness
	}
}