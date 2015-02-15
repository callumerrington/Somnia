package com.kingrunes.somnia.common;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

import com.kingrunes.somnia.Somnia;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

public class PacketHandler
{
	/*
	 * Handling
	 */
	@SubscribeEvent
	public void onServerPacket(ServerCustomPacketEvent event)
	{
		if (event.packet.channel().equals(Somnia.MOD_ID))
			onPacket(event.packet, ((NetHandlerPlayServer)event.handler).playerEntity);
	}
	
	@SubscribeEvent
	public void onClientPacket(ClientCustomPacketEvent event)
	{
		if (event.packet.channel().equals(Somnia.MOD_ID))
			onPacket(event.packet, null);
	}
	
	public void onPacket(FMLProxyPacket packet, EntityPlayerMP player)
	{
		DataInputStream in = new DataInputStream(new ByteBufInputStream(packet.payload()));
		
		try
		{
			byte id = in.readByte();
			
			switch (id)
			{
			case 0x00:
				handleGUIOpenPacket();
				break;
			case 0x01:
				handleGUIClosePacket(player, in);
				break;
			case 0x02:
				handlePropUpdatePacket(in);
				break;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	// CLIENT
	private void handleGUIOpenPacket() throws IOException
	{
		Somnia.proxy.handleGUIOpenPacket();
	}
	
	private void handlePropUpdatePacket(DataInputStream in) throws IOException 
	{
		Somnia.proxy.handlePropUpdatePacket(in);
	}
	
	
	private void handleGUIClosePacket(EntityPlayerMP player, DataInputStream in) throws IOException
	{
		Somnia.proxy.handleGUIClosePacket(player);
	}
	//
	
	/*
	 * Building
	 */
	
	// Cache
	private static HashMap<Byte, FMLProxyPacket> cache;
	
	static
	{
		cache = new HashMap<Byte, FMLProxyPacket>();
	}
	//
	
	private static void close(OutputStream os)
	{
		try
		{
			os.close();
		}
		catch (IOException ioe){}
	}
	
	public static FMLProxyPacket buildGUIOpenPacket()
	{
		FMLProxyPacket packet = cache.get(byteOf(0x00));
		
		if (packet == null)
			cache.put(byteOf(0x00), packet=doBuildGUIOpenPacket());
		
		return packet;
	}
	
	private static FMLProxyPacket doBuildGUIOpenPacket()
	{
		ByteBufOutputStream bbos = unpooled();
		
        try
        {
        	bbos.writeByte(0x00);
        	return new FMLProxyPacket(bbos.buffer(), Somnia.MOD_ID);
        }
        catch (IOException ioe)
        {
        	ioe.printStackTrace();
        	throw new RuntimeException(ioe);
        }
        finally
        {
        	close(bbos);
        }
	}
	
	public static FMLProxyPacket buildGUIClosePacket()
	{
		FMLProxyPacket packet = cache.get(byteOf(0x01));
		
		if (packet == null)
			cache.put(byteOf(0x01), packet=doBuildGUIOpenPacket());
		
		return packet;
	}

	public static FMLProxyPacket doBuildGUIClosePacket()
	{
		ByteBufOutputStream bbos = unpooled();
		
        try
        {
        	bbos.writeByte(0x01);
        	return new FMLProxyPacket(bbos.buffer(), Somnia.MOD_ID);
        }
        catch (IOException ioe)
        {
        	ioe.printStackTrace();
        	throw new RuntimeException(ioe);
        }
        finally
        {
        	close(bbos);
        }
	}
	
	public static FMLProxyPacket buildPropUpdatePacket(int target, Object... fields)
	{
		ByteBufOutputStream bbos = unpooled();
		
        try
        {
        	bbos.writeByte(0x02);
        	bbos.writeByte(target);
        	bbos.writeInt(fields.length/2);
        	for (int i=0; i<fields.length; i++)
        	{
        		bbos.writeByte((Integer) fields[i]);
        		StreamUtils.writeObject(fields[++i], bbos);
        	}
        	
        	return new FMLProxyPacket(bbos.buffer(), Somnia.MOD_ID);
        }
        catch (IOException ioe)
        {
        	ioe.printStackTrace();
        }
        finally
        {
        	close(bbos);
        }
        
        return null;
	}
	
	/*
	 * Utils
	 */
	private static ByteBufOutputStream unpooled()
	{
		return new ByteBufOutputStream(Unpooled.buffer());
	}
	
	public static Byte byteOf(int i)
	{
		return Byte.valueOf((byte)i);
	}
}