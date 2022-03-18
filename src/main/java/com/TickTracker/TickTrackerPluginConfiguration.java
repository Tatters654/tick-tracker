
package com.TickTracker;

import com.TickTracker.config.SmallOverlayStyle;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(TickTrackerPluginConfiguration.GROUP)
public interface TickTrackerPluginConfiguration extends Config
{
	String GROUP = "TickTracker";

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
			keyName = "colorSmallOverlay",
			name = "Color Small overlay",
			description = "Off for yellow, other options use the text color thresholds",
			position = 6
	)
	default SmallOverlayStyle smallOverlayColorStyle()
	{
		return SmallOverlayStyle.BOTH;
	}

	@Range(
			max = 100
	)
	@ConfigItem(
		keyName = "warningText",
		name = "Color % threshold upper",
		description = "Above threshold upper = Green, between threshold upper and lower = Yellow, below threshold lower = Red",
		position = 7
	)
	default int warningColorThresholdUpper()
	{
		return 90;
	}

	@Range(
			max = 100
	)
	@ConfigItem(
		keyName = "warningTextLower",
		name = "Color % threshold lower",
		description = "Above threshold upper = Green, between threshold upper and lower = Yellow, below threshold lower = Red",
		position = 8
	)
	default int warningColorThresholdLower()
	{
		return 88;
	}

	@ConfigItem(
		keyName = "warnLargeTickDiff",
		name = "Warn in chat about large tick lags",
		description = "",
		position = 9
	)
	default boolean warnLargeTickDiff()
	{
		return false;
	}

	@ConfigItem(
			keyName = "warnLargeTickDiffValue",
			name = "Warn of ticks over: ",
			description = "",
			position = 10
	)
	default int warnLargeTickDiffValue() { return 1000; }

	@ConfigItem(
		keyName = "disregardCounter",
		name = "Disregard ticks on login",
		description = "Ticks on login are very inconsistent. This just disregards x many ticks starting from login to make the plugin more accurate.",
		position = 11
	)
	default int disregardCounter()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "Y_Offset",
		name = "Height selector",
		description = "Modify height of small overlay",
		position = 12
	)
	default int Y_Offset()
	{
		return 1;
	}
}
