package com.kingrunes.somnia.server;

import java.io.IOException;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.util.ClassUtils;

import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class Profiler
{
	private static Profiler INSTANCE;
	
	static
	{
		Profiler.INSTANCE = new Profiler();
	}
	
	public static Profiler instance()
	{
		return Profiler.INSTANCE;
	}
	
	public static void _tileTickStart()
	{
		INSTANCE.tileTickStart();
	}
	
	public static void _tileTickEnd(TileEntity te)
	{
		INSTANCE.tileTickEnd(te);
	}
	
	public static void clearClient(ICommandSender sender)
	{
		if (sender instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)sender;
			Somnia.channel.sendTo(PacketHandler.buildProfilerResultPacket(0x01, true), player);
		}
	}
	
	/*
	 * 
	 */
	
	private boolean enabled;
	private World world;
	private long nTime;
	
	public void disable(ICommandSender sender)
	{
		if (enabled)
		{
			if (sender instanceof EntityPlayerMP)
			{
				EntityPlayerMP player = (EntityPlayerMP)sender;
				Somnia.channel.sendTo(PacketHandler.buildProfilerResultPacket(0x00, false), player);
			}
		}
		enabled = false;
	}
	
	public void enable(World world)
	{
		enabled = true;
		TileEntity te;
		for (Object teObj : world.loadedTileEntityList)
		{
			te = (TileEntity)teObj;
			ClassUtils.setTileEntityTime(te, 0l);
			ClassUtils.setTileEntityTickCount(te, 0);
		}
		this.world = world;
	}
	
	public void tileTickStart()
	{
		if (enabled)
			nTime = System.nanoTime();
	}
	
	public void tileTickEnd(TileEntity te)
	{
		if (enabled && te.getWorldObj() == world)
		{
			nTime = System.nanoTime() - nTime;
			ClassUtils.setTileEntityTime(te, ClassUtils.getTileEntityTime(te)+nTime);
			ClassUtils.setTileEntityTickCount(te, ClassUtils.getTileEntityTickCount(te)+1);
		}
	}

	public void writeResultTo(int type, ByteBufOutputStream bbos) throws IOException
	{
		switch (type)
		{
		case 0x00:
			int tiles = world.loadedTileEntityList.size();
			bbos.writeInt(tiles);
			TileEntity te;
			for (Object teObj : world.loadedTileEntityList)
			{
				te = (TileEntity)teObj;
				bbos.writeInt(te.xCoord);
				bbos.writeInt(te.yCoord);
				bbos.writeInt(te.zCoord);
				bbos.writeLong(ClassUtils.getTileEntityTime(te)/ClassUtils.getTileEntityTickCount(te));
			}
			break;
		}
	}
}
