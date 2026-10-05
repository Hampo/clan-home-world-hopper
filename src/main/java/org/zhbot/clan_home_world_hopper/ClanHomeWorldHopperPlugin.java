package org.zhbot.clan_home_world_hopper;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.WorldUtil;

@Slf4j
@PluginDescriptor(
	name = "Clan Home World Hopper",
	description = "A plugin to add a \"Hop-to\" option to the clan home world.",
	tags = {"clan", "home", "world", "hop", "hopper"}
)
public class ClanHomeWorldHopperPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private WorldService worldService;

	@Subscribe
	public void onPostMenuSort(PostMenuSort event)
	{
		final var clansHeader = client.getWidget(InterfaceID.ClansSidepanel.HEADER);
		if (clansHeader == null || clansHeader.isHidden())
			return;

		final var headerChildren = clansHeader.getDynamicChildren();
		if (headerChildren == null || headerChildren.length != 5)
			return;

		final var mousePosition = client.getMouseCanvasPosition();
		final var homeWorldIcon = headerChildren[3];
		final var homeWorldText = headerChildren[4];

		if (!homeWorldIcon.contains(mousePosition) && !homeWorldText.contains(mousePosition))
			return;

		final int homeWorldNumber;

		try
		{
			homeWorldNumber = Integer.parseInt(homeWorldText.getText());
		}
		catch (NumberFormatException ignored)
		{
			return;
		}

		if (client.getWorld() == homeWorldNumber)
			return;

		client.getMenu().createMenuEntry(-1)
				.setOption("Hop-to")
				.setTarget(ColorUtil.wrapWithColorTag(String.valueOf(homeWorldNumber), JagexColors.MENU_TARGET))
				.setType(MenuAction.RUNELITE)
				.onClick(e -> hop(homeWorldNumber));
	}

	private void hop(final int worldNum)
	{
		final var world = getWorld(worldNum);
		if (world == null)
			return;

		final net.runelite.api.World rsWorld = client.createWorld();
		rsWorld.setActivity(world.getActivity());
		rsWorld.setAddress(world.getAddress());
		rsWorld.setId(world.getId());
		rsWorld.setPlayerCount(world.getPlayers());
		rsWorld.setLocation(world.getLocation());
		rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));

		if (configManager.getConfiguration("worldhopper", "showMessage", boolean.class))
		{
			String chatMessage = new ChatMessageBuilder()
					.append(ChatColorType.NORMAL)
					.append("Quick-hopping to World ")
					.append(ChatColorType.HIGHLIGHT)
					.append(Integer.toString(world.getId()))
					.append(ChatColorType.NORMAL)
					.append("..")
					.build();

			chatMessageManager
					.queue(QueuedMessage.builder()
							.type(ChatMessageType.CONSOLE)
							.runeLiteFormattedMessage(chatMessage)
							.build());
		}

		client.hopToWorld(rsWorld);
	}

	private net.runelite.http.api.worlds.World getWorld(final int worldNum)
	{
		final var worlds = worldService.getWorlds();

		return worlds == null ? null : worlds.findWorld(worldNum);
	}
}
