package com.kingrunes.somnia.server;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.util.ClassUtils;
import com.kingrunes.somnia.common.util.ListUtils;

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
	
	public static void _tileTickStart(TileEntity te)
	{
		INSTANCE.tileTickStart(te);
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
			Somnia.channel.sendTo(PacketHandler.buildProfilerResultPacket(0x01, null), player);
		}
	}
	
	/*
	 * 
	 */
	
	private int usersProfiling;
	private List<WeakReference<ICommandSender>> userList;
	private long nTime;
	
	public Profiler()
	{
		this.usersProfiling = 0;
		this.userList = new ArrayList<WeakReference<ICommandSender>>();
	}
	
	public boolean enabled()
	{
		return usersProfiling > 0;
	}
	
	public boolean disable(ICommandSender sender)
	{
		if (enabled())
		{
			if (sender instanceof EntityPlayerMP)
			{
				EntityPlayerMP player = (EntityPlayerMP)sender;
				Somnia.channel.sendTo(PacketHandler.buildProfilerResultPacket(0x00, player.worldObj), player);
			}
			else
				return false;
		}
		else
			return false;
		userList.remove(ListUtils.getWeakRef(sender, userList));
		usersProfiling--;
		return true;
	}
	
	public boolean enable(ICommandSender sender)
	{
		if (!(sender instanceof EntityPlayerMP))
			return false;
		EntityPlayerMP player = (EntityPlayerMP)sender;
		
		if (ListUtils.containsRef(sender, userList))
			return false;
		
		System.out.println("users: " + usersProfiling);
		usersProfiling++;
		if (usersProfiling == 1)
		{
			TileEntity te;
			for (Object teObj : player.worldObj.loadedTileEntityList)
			{
				te = (TileEntity)teObj;
				ClassUtils.setTileEntityTime(te, 0l);
				ClassUtils.setTileEntityTickCount(te, 0);
			}
		}
		userList.add(new WeakReference<ICommandSender>(sender));
		return true;
	}
	
	public void tileTickStart(TileEntity te)
	{
		if (!te.getWorldObj().isRemote && enabled())
			nTime = System.nanoTime();
	}
	
	public void tileTickEnd(TileEntity te)
	{
		if (!te.getWorldObj().isRemote && enabled())
		{
			nTime = System.nanoTime() - nTime;
			ClassUtils.setTileEntityTime(te, ClassUtils.getTileEntityTime(te)+nTime);
			ClassUtils.setTileEntityTickCount(te, ClassUtils.getTileEntityTickCount(te)+1);
		}
	}

	public void writeResultTo(int type, ByteBufOutputStream bbos, World world) throws IOException
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
