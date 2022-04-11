
package com.TickTracker;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

import static net.runelite.api.GameState.*;


@PluginDescriptor(
	name = "Tick Tracker",
	description = "Display tick timing variance in an overlay",
	tags = {"tick", "timers", "skill", "pvm", "lag"},
	enabledByDefault = false
)

@Getter @Setter
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

	private final long NANOS_PER_MILLIS = 1000000L;
	private final long  IDEAL_TICK_LENGTH_NS = 600L * NANOS_PER_MILLIS;
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
	private double tickWithinRangePercent = 100;
	private boolean isGameStateLoading = false;

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
	protected void startUp()
	{
		overlayManager.add(overlay);
		overlayManager.add(SmallOverlay);
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		long tickTimeNS = System.nanoTime();
		tickDiffNS = tickTimeNS - lastTickTimeNS;
		lastTickTimeNS = tickTimeNS;

		//If the gameState is LOADING, then the server tick can be long for a valid reason
		if (isGameStateLoading || disregardCounter < config.disregardCounter())
		{
			disregardCounter += 1; //ticks upon login or hopping are very inconsistent, thus the need for the disregard of the first ones
			return;
		}

		long tickVarianceFromIdealMS = Math.abs(IDEAL_TICK_LENGTH_NS - tickDiffNS) / NANOS_PER_MILLIS;

		if (tickVarianceFromIdealMS > config.warnLargeTickDiffValue())
		{
			if (config.warnLargeTickDiff() &&  allTickCounter > config.disregardCounter())
			{
				sendChatMessage("Tick was " + tickDiffNS / NANOS_PER_MILLIS + "ms long");
			}
		}

		if (tickVarianceFromIdealMS > config.getThresholdHigh())
		{
			tickOverThresholdHigh += 1;
		}
		else if (tickVarianceFromIdealMS > config.getThresholdMedium())
		{
			tickOverThresholdMedium += 1;
		}
		else if (tickVarianceFromIdealMS > config.getThresholdLow())
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
		isGameStateLoading = event.getGameState() == LOADING;
		if (event.getGameState() == HOPPING || event.getGameState() == LOGGING_IN)
		{
			resetStats(false);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (TickTrackerPluginConfiguration.GROUP.equals(event.getGroup())) {
			String key = event.getKey();
			if ("varianceHigh".equals(key) || "varianceMedium".equals(key) || "varianceLow".equals(key)) {
				resetStats(true);
			}
		}
	}

	private void resetStats(boolean onlyVarianceRelevantStats) {
		tickOverThresholdHigh = 0;
		tickOverThresholdMedium = 0;
		tickOverThresholdLow = 0;
		tickWithinRange = 0;
		allTickCounter = 0;
		tickTimePassedNS = 0;
		tickWithinRangePercent = 100;

		if (onlyVarianceRelevantStats) {
			return;
		}

		lastTickTimeNS = 0;
		tickDiffNS = 0;
		runningTickAverageNS = 0;
		disregardCounter = 0;
	}
}
