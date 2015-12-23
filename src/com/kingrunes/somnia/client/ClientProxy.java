package com.kingrunes.somnia.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.client.gui.GuiSelectWakeTime;
import com.kingrunes.somnia.client.gui.GuiSomnia;
import com.kingrunes.somnia.common.CommonProxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	public static double playerFatigue = -1;
	
	@Override
	public void register()
	{
		super.register();
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(new ClientTickHandler());
	}

	@Override
	public void printMessage(final String message)
	{
		final Minecraft mc = Minecraft.getMinecraft();
		mc.func_152343_a(
				new Callable<Void>()
				{
					@Override
					public Void call() throws Exception
					{
						mc.thePlayer.addChatMessage(new ChatComponentText("[Somnia] " + message));
						return null;
					}
				});
	}

	@SubscribeEvent
	public void interactHook(PlayerInteractEvent event)
	{
		if (!event.world.isRemote)
			return;
		
		if (event.action.equals(Action.RIGHT_CLICK_BLOCK) && event.entity.worldObj.getBlock(event.x, event.y, event.z).equals(Blocks.bed))
		{
			int i1 = event.entity.worldObj.getBlockMetadata(event.x, event.y, event.z);
			int j1 = i1 & 3;
			
			int x = event.x;
			int z = event.z;
			
			if ((i1 & 8) == 0)
			{
				x += BlockBed.field_149981_a[j1][0]; // footBlockToHeadBlockMap
	            z += BlockBed.field_149981_a[j1][1]; //
			}
			
			if (Math.abs(event.entityPlayer.posX - (double)x) < 3.0D && Math.abs(event.entityPlayer.posY - (double)event.y) < 3.65D && Math.abs(event.entityPlayer.posZ - (double)z) < 3.0D)
			{
				ItemStack currentItem = event.entityPlayer.inventory.getCurrentItem();
				if (currentItem != null && currentItem.getItem().equals(Items.clock))
				{
					event.setCanceled(true);
					Minecraft.getMinecraft().displayGuiScreen(new GuiSelectWakeTime());
				}
				else
				{
					// Wake at next sunrise/sunset (whichever comes first)
					long totalWorldTime = event.world.getTotalWorldTime();
					Somnia.clientAutoWakeTime = Somnia.calculateWakeTime(totalWorldTime, totalWorldTime % Somnia.proxy.ticksPerDay > Somnia.proxy.halfTicksPerDay ? 0 : Somnia.proxy.halfTicksPerDay);
				}
			}
		}
		else if (Minecraft.getMinecraft().currentScreen instanceof GuiSelectWakeTime)
			event.setCanceled(true);
	}
	
	@Override
	public void handleGUIOpenPacket() throws IOException
	{
		if (somniaGui)
			Minecraft.getMinecraft().displayGuiScreen(new GuiSomnia());
	}

	@Override
	public void handlePropUpdatePacket(DataInputStream in) throws IOException
	{
		byte target = in.readByte();
		
		switch (target)
		{
		case 0x00:
			GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
			if (currentScreen != null && currentScreen instanceof GuiSomnia)
			{
				GuiSomnia gui = (GuiSomnia)currentScreen;
				
				int b = in.readInt();
				for (int a=0; a<b; a++)
					gui.readField(in);
			}
			break;
		case 0x01:
			int b = in.readInt();
			for (int a=0; a<b; a++)
			{
				switch (in.readByte())
				{
				case 0x00:
					playerFatigue = in.readDouble();
					break;
				}
			}
			break;
		}
	}

	@Override
	public void handleGUIClosePacket(EntityPlayerMP player)
	{
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
}