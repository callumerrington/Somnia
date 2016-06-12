package com.kingrunes.somnia.client.util;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;

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
	
	public static void renderBoundingBox(double x, double y, double z, double width, double height, double depth)
	{
		double minX, maxX;
		double minY, maxY;
		double minZ, maxZ;
		
		minX = x - RenderManager.renderPosX;
		minY = y - RenderManager.renderPosY;
		minZ = z - RenderManager.renderPosZ;
		
		maxX = minX+width;
		maxY = minY+height;
		maxZ = minZ+depth;
		
		int color = -1;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawing(3);
		
		if (color != -1)
		    tessellator.setColorOpaque_I(color);
		
		tessellator.addVertex(minX, minY, minZ);
		tessellator.addVertex(maxX, minY, minZ);
		tessellator.addVertex(maxX, minY, maxZ);
		tessellator.addVertex(minX, minY, maxZ);
		tessellator.addVertex(minX, minY, minZ);
		tessellator.draw();
		tessellator.startDrawing(3);
		
		if (color != -1)
		    tessellator.setColorOpaque_I(color);
		
		tessellator.addVertex(minX, maxY, minZ);
		tessellator.addVertex(maxX, maxY, minZ);
		tessellator.addVertex(maxX, maxY, maxZ);
		tessellator.addVertex(minX, maxY, maxZ);
		tessellator.addVertex(minX, maxY, minZ);
		tessellator.draw();
		tessellator.startDrawing(1);
		
		if (color != -1)
		    tessellator.setColorOpaque_I(color);
		
		tessellator.addVertex(minX, minY, minZ);
		tessellator.addVertex(minX, maxY, minZ);
		tessellator.addVertex(maxX, minY, minZ);
		tessellator.addVertex(maxX, maxY, minZ);
		tessellator.addVertex(maxX, minY, maxZ);
		tessellator.addVertex(maxX, maxY, maxZ);
		tessellator.addVertex(minX, minY, maxZ);
		tessellator.addVertex(minX, maxY, maxZ);
		tessellator.draw();
	}
}