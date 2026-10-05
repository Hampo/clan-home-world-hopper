package org.zhbot.clan_home_world_hopper;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ClanHomeWorldHopperPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ClanHomeWorldHopperPlugin.class);
		RuneLite.main(args);
	}
}