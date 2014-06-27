package com.kingrunes.somnia.client;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.client.gui.GuiSelectWakeTime;
import com.kingrunes.somnia.client.gui.GuiSomnia;
import com.kingrunes.somnia.common.CommonProxy;
import com.kingrunes.somnia.common.StreamUtils;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void register()
	{
		super.register();
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(new ClientTickHandler());
	}

	@SubscribeEvent
	public void interactHook(PlayerInteractEvent event)
	{
		if (!event.world.isRemote)
			return;
		
		if (event.action.equals(Action.RIGHT_CLICK_BLOCK) && event.entity.worldObj.getBlock(event.x, event.y, event.z).getUnlocalizedName().equals("tile.bed"))
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
			
			if (Math.abs(event.entityPlayer.posX - (double)x) < 3.0D && Math.abs(event.entityPlayer.posY - (double)event.y) < 2.0D && Math.abs(event.entityPlayer.posZ - (double)z) < 3.0D)
			{
				ItemStack currentItem = event.entityPlayer.inventory.getCurrentItem();
				if (currentItem != null && currentItem.getItem().getUnlocalizedName().equals("item.clock"))
				{
					event.setCanceled(true);
					Minecraft.getMinecraft().displayGuiScreen(new GuiSelectWakeTime());
				}
				else
				{
					// Wake at next sunrise/sunset (whichever comes first)
					long totalWorldTime = event.world.getTotalWorldTime();
					Somnia.clientAutoWakeTime = Somnia.calculateWakeTime(totalWorldTime, totalWorldTime % 24000 > 12000 ? 0 : 12000);
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
	public void handleGUIUpdatePacket(DataInputStream in) throws IOException
	{
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		if (currentScreen != null && currentScreen instanceof GuiSomnia)
		{
			GuiSomnia gui = (GuiSomnia)currentScreen;
			
			String fieldName;
			int b = in.readInt();
			for (int a=0; a<b; a++)
			{
				fieldName = StreamUtils.readString(in);
				gui.updateField(fieldName, in);
			}
		}
	}

	@Override
	public void handleGUIClosePacket(EntityPlayerMP player)
	{
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
}