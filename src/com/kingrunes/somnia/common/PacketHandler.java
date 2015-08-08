package com.kingrunes.somnia.common;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import com.kingrunes.somnia.Somnia;

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
	
	// TODO: Make caching work on something other than Windows...
	/*/ Cache
	private static HashMap<Byte, FMLProxyPacket> cache;
	
	static
	{
		cache = new HashMap<Byte, FMLProxyPacket>();
	}
	/*/
	
	public static FMLProxyPacket buildGUIOpenPacket()
	{
		return doBuildGUIOpenPacket();
		/*
		FMLProxyPacket packet = cache.get(byteOf(0x00));
		
		if (packet == null)
			cache.put(byteOf(0x00), packet=doBuildGUIOpenPacket());
		
		return packet;
		*/
	}
	
	private static FMLProxyPacket doBuildGUIOpenPacket()
	{
		PacketBuffer buffer = unpooled();
		
    	buffer.writeByte(0x00);
    	return new FMLProxyPacket(buffer, Somnia.MOD_ID);
	}
	
	public static FMLProxyPacket buildGUIClosePacket()
	{
		return doBuildGUIClosePacket();
		/*
		FMLProxyPacket packet = cache.get(byteOf(0x01));
		
		if (packet == null)
			cache.put(byteOf(0x01), packet=doBuildGUIOpenPacket());
		
		return packet;
		*/
	}

	public static FMLProxyPacket doBuildGUIClosePacket()
	{
		PacketBuffer buffer = unpooled();
		
    	buffer.writeByte(0x01);
    	return new FMLProxyPacket(buffer, Somnia.MOD_ID);
	}
	
	public static FMLProxyPacket buildPropUpdatePacket(int target, Object... fields)
	{
		PacketBuffer buffer = unpooled();
		
    	buffer.writeByte(0x02);
    	buffer.writeByte(target);
    	buffer.writeInt(fields.length/2);
    	for (int i=0; i<fields.length; i++)
    	{
    		buffer.writeByte((Integer) fields[i]);
    		StreamUtils.writeObject(fields[++i], buffer);
    	}
    	
    	return new FMLProxyPacket(buffer, Somnia.MOD_ID);
	}
	
	/*
	 * Utils
	 */
	private static PacketBuffer unpooled()
	{
		return new PacketBuffer(Unpooled.buffer());
	}
	
	public static Byte byteOf(int i)
	{
		return Byte.valueOf((byte)i);
	}
}