package com.kingrunes.somnia.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.client.gui.GuiSelectWakeTime;
import com.kingrunes.somnia.client.gui.GuiSomnia;
import com.kingrunes.somnia.common.CommonProxy;

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

	@SubscribeEvent
	public void interactHook(PlayerInteractEvent event)
	{
		if (!event.world.isRemote)
			return;

		World worldIn = event.world;
		BlockPos pos = event.pos;
		
		if (pos == null)
			return;
		
		IBlockState state = worldIn.getBlockState(pos);
		
		if (event.action.equals(Action.RIGHT_CLICK_BLOCK) && state.getBlock() == Blocks.bed)
		{
//			int i1 = event.entity.worldObj.getBlockMetadata(event.x, event.y, event.z);
//			int j1 = i1 & 3;
			
			if (state.getValue(BlockBed.PART) != BlockBed.EnumPartType.HEAD)
            {
                pos = pos.offset((EnumFacing)state.getValue(BlockBed.FACING));
                state = worldIn.getBlockState(pos);

                if (state.getBlock() != Blocks.bed)
                {
                    event.setCanceled(true);
                    return;
                }
            }
			
			if (Math.abs(event.entityPlayer.posX - (double)pos.getX()) < 3.0D && Math.abs(event.entityPlayer.posY - (double)pos.getY()) < 2.0D && Math.abs(event.entityPlayer.posZ - (double)pos.getZ()) < 3.0D)
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
		{
			final Minecraft mc = Minecraft.getMinecraft();
			
			mc.addScheduledTask(new Callable<Void>()
			{
				@Override
				public Void call() throws Exception
				{
					mc.displayGuiScreen(new GuiSomnia());
					return null;
				}
			});
		}
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