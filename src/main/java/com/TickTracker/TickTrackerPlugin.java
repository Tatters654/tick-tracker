
package com.TickTracker;

import com.TickTracker.config.LogFormatStyle;
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

import static net.runelite.api.GameState.HOPPING;
import static net.runelite.api.GameState.LOGGING_IN;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import org.slf4j.LoggerFactory;


@PluginDescriptor(
	name = "Tick tracker",
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

	private static final String BASE_DIRECTORY = System.getProperty("user.home") + "/.runelite/ticklogs/";
	private Logger tickLogger;

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
	private int worldNumber;
	private long tickVarianceFromIdealMS;


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
		tickLogger = setupLogger("ticklogger", "/");
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		long tickTimeNS = System.nanoTime();
		tickDiffNS = tickTimeNS - lastTickTimeNS;
		lastTickTimeNS = tickTimeNS;

		if (disregardCounter < config.disregardCounter())
		{
			disregardCounter += 1; //ticks upon login or hopping are very inconsistent, thus the need for the disregard of the first ones
			return;
		}

		long tickVarianceFromIdealMS = (IDEAL_TICK_LENGTH_NS - tickDiffNS) / NANOS_PER_MILLIS;
		long tickVarianceFromIdealAbsoluteMS = Math.abs(tickVarianceFromIdealMS);

		if (tickVarianceFromIdealAbsoluteMS > config.warnLargeTickDiffValue())
		{
			if (config.warnLargeTickDiff() &&  allTickCounter > config.disregardCounter())
			{
				sendChatMessage("Tick was " + tickDiffNS / NANOS_PER_MILLIS + "ms long");
			}
		}

		if (tickVarianceFromIdealAbsoluteMS > config.getThresholdHigh())
		{
			tickOverThresholdHigh += 1;
		}
		else if (tickVarianceFromIdealAbsoluteMS > config.getThresholdMedium())
		{
			tickOverThresholdMedium += 1;
		}
		else if (tickVarianceFromIdealAbsoluteMS > config.getThresholdLow())
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

		if (config.logEnabled() && config.logFormatStyle() == LogFormatStyle.EACH_TICK)
		{
			LogInfo();
		}

	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == HOPPING || event.getGameState() == LOGGING_IN)
		{
			if (config.logEnabled()) {
				worldNumber = client.getWorld();
				Object date = new java.util.Date(); //spaghetti?
				tickLogger.info("World: " + String.valueOf(worldNumber) + ", Time " + String.valueOf(date));
			}
			if (config.logFormatStyle() == LogFormatStyle.SESSION) {
				LogInfo();
			}
			//possibly add alltickcounter > 15 check to these conditions
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
	private Logger setupLogger(String loggerName, String subFolder) {
		//borrowed from
		//https://github.com/hex-agon/chat-logger/blob/cc4845de0587009f7d93a2ce3e6a4a1d8a296aae/src/main/java/fking/work/chatlogger/ChatLoggerPlugin.java#L154
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(context);
		encoder.setPattern("%msg %n"); //%date{ISO8601}
		encoder.start();

		String directory = BASE_DIRECTORY + subFolder + "/";

		RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
		appender.setFile(directory + "latest.log");
		appender.setAppend(true);
		appender.setEncoder(encoder);
		appender.setContext(context);

		TimeBasedRollingPolicy<ILoggingEvent> logFilePolicy = new TimeBasedRollingPolicy<>();
		logFilePolicy.setContext(context);
		logFilePolicy.setParent(appender);
		logFilePolicy.setFileNamePattern(directory + "ticklog_%d{yyyy-MM-dd}.log");
		logFilePolicy.setMaxHistory(30); //TODO make configurable?
		logFilePolicy.start();

		appender.setRollingPolicy(logFilePolicy);
		appender.start();

		Logger logger = context.getLogger(loggerName);
		logger.detachAndStopAllAppenders();
		logger.setAdditive(false);
		logger.setLevel(Level.INFO);
		logger.addAppender(appender);

		return logger;
	}
	private void LogInfo() {
		if (config.logFormatStyle() == LogFormatStyle.SESSION) {
			tickLogger.info(
				"TicksOverThresholdHigh: " + String.valueOf(tickOverThresholdHigh) + ","
				+ "TicksOverThresholdMedium: " + String.valueOf(tickOverThresholdMedium) + ","
				+ "TicksOverThresholdLow: " + String.valueOf(tickOverThresholdLow) + ","
				+ "TicksWithinRange: " + String.valueOf(tickWithinRange) + ","
				+ "TotalTicks: " + String.valueOf(allTickCounter) + ",");
		}
		if (config.logFormatStyle() == LogFormatStyle.EACH_TICK) {
			tickLogger.info(String.valueOf(tickVarianceFromIdealMS)); //cant access local variable
		}
		else {
			sendChatMessage("debug loginfo, goes to else branch");
		}
	}
}
