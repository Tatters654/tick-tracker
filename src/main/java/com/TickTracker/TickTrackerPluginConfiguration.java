
package com.TickTracker;

import com.TickTracker.config.SmallOverlayStyle;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("TickTracker")
public interface TickTrackerPluginConfiguration extends Config
{
	@ConfigItem(
		keyName = "varianceHigh",
		name = "Variance High",
		description = "Variance for high tick latency +/-",
		position = 2
	)
	default int getThresholdHigh()
	{
		return 150;
	}

	@ConfigItem(
		keyName = "varianceMedium",
		name = "Variance Medium",
		description = "Variance for medium tick latency +/-",
		position = 3
	)
	default int getThresholdMedium()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "varianceLow",
		name = "Variance Low",
		description = "Variance for low tick latency +/-",
		position = 4
	)
	default int getThresholdLow()
	{
		return 50;
	}

	@ConfigItem(
		keyName = "drawLargeOverlay",
		name = "Show extra information",
		description = "Show set thresholds and each category's quantity of ticks and percentage of total ticks",
		position = 1
	)
	default boolean drawLargeOverlay()
	{
		return false;
	}

	@ConfigItem(
		keyName = "drawSmallOverlay",
		name = "Draw Small overlay",
		description = "Whether to draw a small overlay",
		position = 5
	)
	default SmallOverlayStyle drawSmallOverlay()
	{
		return SmallOverlayStyle.PERCENTAGE;
	}

	@ConfigItem(
		keyName = "warningText",
		name = "Small overlay text color threshold",
		description = "Above threshold = Green, Above Threshold -2 = Yellow, Below that = Red",
		position = 6
	)
	default int warningColorThreshold()
	{
		return 90;
	}

	@ConfigItem(
		keyName = "warnLargeTickDiff",
		name = "Warn in chat about large tick lags",
		description = "Print notification in chat of ticks over 2500ms",
		position = 7
	)
	default boolean warnLargeTickDiff()
	{
		return false;
	}

	@ConfigItem(
		keyName = "disregardCounter",
		name = "Disregard ticks on login",
		description = "Ticks on login are very inconsistent. This just disregards x many ticks starting from login to make the plugin more accurate.",
		position = 8
	)
	default int disregardCounter()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "Y_Offset",
		name = "Height selector",
		description = "Modify height of small overlay",
		position = 9
	)
	default int Y_Offset()
	{
		return 1;
	}
}
