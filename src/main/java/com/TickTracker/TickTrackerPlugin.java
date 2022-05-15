
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
import lombok.extern.slf4j.Slf4j;


import javax.inject.Inject;

import static net.runelite.api.GameState.*;

@Slf4j
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
	//large overlay statistics
	private int ticksOverThresholdLow = 0;
	private int ticksOverThresholdMedium = 0;
	private int ticksOverThresholdHigh = 0;
	private int ticksWithinRange = 0;
	private int ticksPassed = 0;
	private int disregardCounter = 0;
	private double ticksWithinRangePercent = 100;
	private double timeDifferencePercentDouble = 100;
	private long runningTickAverageNS = 0;
	private long sumOfTimeVariationFromIdeal = 0;
	private long idealTimePassed = 0;
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
		if (isGameStateLoading || disregardCounter < config.disregardTickCounter())
		{
			disregardCounter += 1; //ticks upon login or hopping are very inconsistent, thus the need for the disregard of the first ones
			return;
		}

		long tickVarianceFromIdealMS = Math.abs(IDEAL_TICK_LENGTH_NS - tickDiffNS) / NANOS_PER_MILLIS;
		logTickLength(tickVarianceFromIdealMS);

		if (tickVarianceFromIdealMS > config.warnLargeTickDiffValue() && config.warnLargeTickDiff())
		{
			sendChatMessage("Tick was " + tickDiffNS / NANOS_PER_MILLIS + "ms long");
		}

		sumOfTimeVariationFromIdeal += tickVarianceFromIdealMS;
		ticksPassed += 1;
		tickTimePassedNS += tickDiffNS;
		runningTickAverageNS = tickTimePassedNS / ticksPassed;
		idealTimePassed = ticksPassed * 600L; //cast 600 to long, probably fine but double check before publishing
		timeDifferencePercentDouble = (((double)idealTimePassed - sumOfTimeVariationFromIdeal) / idealTimePassed) * 100; // *100 to make it a nice percent
		ticksWithinRangePercent = (ticksWithinRange * 100.0) / ticksPassed;


		log.debug("sumOfTimeVariationFromIdeal" + sumOfTimeVariationFromIdeal);
		log.debug("timeDifferencePercentDouble" + timeDifferencePercentDouble);
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
		//large display info
		ticksOverThresholdHigh = 0;
		ticksOverThresholdMedium = 0;
		ticksOverThresholdLow = 0;
		ticksWithinRange = 0;
		//plugin internals
		ticksPassed = 0;
		tickTimePassedNS = 0;
		timeDifferencePercentDouble = 100;
		ticksWithinRangePercent = 100;
		sumOfTimeVariationFromIdeal = 0;
		idealTimePassed = 0;
		if (onlyVarianceRelevantStats) {
			return;
		}
		lastTickTimeNS = 0;
		tickDiffNS = 0;
		runningTickAverageNS = 0;
		disregardCounter = 0;
	}
	private void logTickLength(long tick) {
		if (tick > config.getThresholdHigh())
		{
			ticksOverThresholdHigh += 1;
		}
		else if (tick > config.getThresholdMedium())
		{
			ticksOverThresholdMedium += 1;
		}
		else if (tick > config.getThresholdLow())
		{
			ticksOverThresholdLow += 1;
		}
		else
		{
			ticksWithinRange += 1;
		}
	}
}
