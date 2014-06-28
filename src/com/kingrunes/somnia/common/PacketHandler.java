package com.kingrunes.somnia.common;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.kingrunes.somnia.Somnia;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
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
				handleGUIUpdate(in);
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
	
	private void handleGUIUpdate(DataInputStream in) throws IOException 
	{
		Somnia.proxy.handleGUIUpdatePacket(in);
	}
	//
	
	// BOTH
	private void handleGUIClosePacket(EntityPlayerMP player, DataInputStream in) throws IOException
	{
		Somnia.proxy.handleGUIClosePacket(player);
	}
	//
	
	/*
	 * Building
	 */
	private static ByteBufOutputStream unpooled()
	{
		return new ByteBufOutputStream(Unpooled.buffer());
	}
	
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
		ByteBufOutputStream bbos = unpooled();
		
        try
        {
        	bbos.writeByte(0x00);
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
	
	public static FMLProxyPacket buildGUIClosePacket()
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
        }
        finally
        {
        	close(bbos);
        }
        
        return null;
	}
	
	public static FMLProxyPacket buildGUIUpdatePacket(Object... fields)
	{
		ByteBufOutputStream bbos = unpooled();
		
        try
        {
        	bbos.writeByte(0x02);
        	bbos.writeInt(fields.length/2);
        	for (int i=0; i<fields.length; i++)
        	{
        		StreamUtils.writeString(fields[i].toString(), bbos);
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
}