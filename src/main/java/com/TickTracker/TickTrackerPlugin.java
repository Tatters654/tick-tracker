
package com.TickTracker;

import com.google.inject.Provides;
import java.awt.Color;
import javax.inject.Inject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import static net.runelite.api.GameState.HOPPING;
import static net.runelite.api.GameState.LOGGING_IN;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@EqualsAndHashCode(callSuper = false)
@PluginDescriptor(
	name = "Tick tracker",
	description = "Display tick timing variance in an overlay",
	tags = {"tick", "timers", "skill", "pvm", "lag"},
	enabledByDefault = false
)

@Data
public class TickTrackerPlugin extends Plugin
{

	private void sendChatMessage(String chatMessage)
	{
		final String message = new ChatMessageBuilder().append(ChatColorType.HIGHLIGHT).append(chatMessage).build();
		chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE).runeLiteFormattedMessage(message).build());
	}

	@Inject
	private Client client;

	@Inject
	private TickTrackerPluginConfiguration config;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TickTrackerOverlay overlay;

	@Inject
	private TickTrackerSmallOverlay SmallOverlay;

	private final long millisPerNanosecond = 1000000L;
	private long lastTickTimeNS = 0L;
	private long tickDiffNS = 0;
	private long tickTimePassedNS = 0;
	private int tickOverThresholdLow = 0;
	private int tickOverThresholdMedium = 0;
	private int tickOverThresholdHigh = 0;
	private int tickWithinRange = 0;
	private int allTickCounter = 0;
	private long runningTickAverageNS = 0;
	private int disregardCounter = 0;
	private double tickWithinRangePercent = 0;

	@Provides
	TickTrackerPluginConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TickTrackerPluginConfiguration.class);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(SmallOverlay);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		overlayManager.add(SmallOverlay);
	}

	public Color colorSelection()
	{
		if (getTickWithinRangePercent() > config.warningColorThreshold())
		{
			return Color.GREEN;
		}
		else if (getTickWithinRangePercent() > config.warningColorThreshold() - 2)
		{
			return Color.YELLOW;
		}
		else
		{
			return Color.RED;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (disregardCounter < config.disregardCounter())
		{
			disregardCounter += 1; // waiting 10 ticks, because ticks upon login or hopping are funky
			return;
		}

		long tickTimeNS = System.nanoTime();
		tickDiffNS = tickTimeNS - lastTickTimeNS;
		lastTickTimeNS = tickTimeNS;

		if (tickDiffNS > 2500 * millisPerNanosecond)
		{
			if (config.warnLargeTickDiff())
			{
				sendChatMessage("Disregarding tick because it was over too long");
			}
			return;
		}

		if (tickDiffNS > config.getThresholdHigh() * millisPerNanosecond)
		{
			tickOverThresholdHigh += 1;
		}
		else if (tickDiffNS > config.getThresholdMedium() * millisPerNanosecond)
		{
			tickOverThresholdMedium += 1;
		}
		else if (tickDiffNS > config.getThresholdLow() * millisPerNanosecond)
		{
			tickOverThresholdLow += 1;
		}
		else
		{
			tickWithinRange += 1;
		}

		allTickCounter += 1;
		tickTimePassedNS += tickDiffNS;
		runningTickAverageNS = tickTimePassedNS / allTickCounter;
		tickWithinRangePercent = (tickWithinRange * 100.0) / allTickCounter;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == HOPPING || event.getGameState() == LOGGING_IN)
		{
			lastTickTimeNS = 0;
			tickDiffNS = 0;
			tickTimePassedNS = 0;
			tickOverThresholdHigh = 0;
			tickOverThresholdMedium = 0;
			tickOverThresholdLow = 0;
			tickWithinRange = 0;
			allTickCounter = 0;
			runningTickAverageNS = 0;
			disregardCounter = 0;
			tickWithinRangePercent = 0;
		}
	}
}