package com.kingrunes.somnia.server;

import java.lang.ref.WeakReference;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.util.ListUtils;
import com.kingrunes.somnia.common.util.SomniaEntityPlayerProperties;

public class SomniaCommand extends CommandBase
{
	private static final String	COMMAND_NAME 			= "somnia",
								COMMAND_USAGE			= "[override [add <player>|remove <player>|list]] [fatigue [set <player>]]",
								COMMAND_USAGE_CONSOLE	= "[override [add [player]|remove [player]|list]] [fatigue [set [player]]]",
								COMMAND_USAGE_FORMAT	= "/%s %s";
	
	@Override
	public String getCommandName()
	{
		return COMMAND_NAME;
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return String.format(COMMAND_USAGE_FORMAT, COMMAND_NAME, COMMAND_USAGE);
	}
	
	@Override
	public int getRequiredPermissionLevel()
	{
		return 3;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		if (args.length < 2)
			throw new WrongUsageException(getCommandUsage(sender));
		
		EntityPlayerMP player;
		if (args[0].equalsIgnoreCase("override"))
		{
			if (args.length > 2)
				player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[2]);
			else
			{
				if (sender instanceof EntityPlayerMP)
					player = (EntityPlayerMP)sender;
				else
					throw new WrongUsageException(String.format(COMMAND_USAGE_FORMAT, COMMAND_NAME, COMMAND_USAGE_CONSOLE));
			}
			
			if (args[1].equalsIgnoreCase("add"))
				Somnia.instance.ignoreList.add(new WeakReference<EntityPlayerMP>(player));
			else if (args[1].equalsIgnoreCase("remove"))
				Somnia.instance.ignoreList.remove(ListUtils.<EntityPlayerMP>getWeakRef(player, Somnia.instance.ignoreList));
			else if (args[1].equalsIgnoreCase("list"))
			{
				List<EntityPlayerMP> players = ListUtils.<EntityPlayerMP>extractRefs(Somnia.instance.ignoreList);
				String[] astring = ListUtils.playersToStringArray(players);
				ChatComponentText chatComponent = new ChatComponentText(astring.length > 0 ? joinNiceString(astring) : "Nothing to see here...");
				sender.addChatMessage(chatComponent);
			}
			else
				throw new WrongUsageException(getCommandUsage(sender));
		}
		else if (args[0].equalsIgnoreCase("fatigue"))
		{
			if (args.length > 3)
				player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[3]);
			else
			{
				if (sender instanceof EntityPlayerMP)
					player = (EntityPlayerMP)sender;
				else
					throw new WrongUsageException(String.format(COMMAND_USAGE_FORMAT, COMMAND_NAME, COMMAND_USAGE_CONSOLE));
			}
			
			if (args[1].equalsIgnoreCase("set"))
			{
				SomniaEntityPlayerProperties props = SomniaEntityPlayerProperties.get(player);
				if (props != null)
				{
					try
					{
						props.setFatigue(Double.parseDouble(args[2]));
						ForgeEventHandler.sendFatigueUpdate(player, props.getFatigue());
					}
					catch (NumberFormatException nfe)
					{
						sender.addChatMessage(new ChatComponentText("Invalid double!"));
					}
				}
				else
					sender.addChatMessage(new ChatComponentText("props = null! Weird..."));
			}
			else
				throw new WrongUsageException(getCommandUsage(sender));
			
		}
	}
}