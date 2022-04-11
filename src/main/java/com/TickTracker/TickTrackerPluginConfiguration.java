
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
		keyName = "Y_Offset",
		name = "Height selector",
		description = "Modify height of small overlay",
		position = 1
	)
	default int Y_Offset()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "drawLargeOverlay",
		name = "Show extra information",
		description = "Show set thresholds and each category's quantity of ticks and percentage of total ticks",
		position = 2
	)
	default boolean drawLargeOverlay()
	{
		return false;
	}

	@ConfigItem(
		keyName = "varianceHigh",
		name = "Variance High",
		description = "Variance for high tick latency +/-",
		position = 3
	)
	default int getThresholdHigh()
	{
		return 150;
	}

	@ConfigItem(
		keyName = "varianceMedium",
		name = "Variance Medium",
		description = "Variance for medium tick latency +/-",
		position = 4
	)
	default int getThresholdMedium()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "varianceLow",
		name = "Variance Low",
		description = "Variance for low tick latency +/-",
		position = 5
	)
	default int getThresholdLow()
	{
		return 50;
	}



	@ConfigItem(
		keyName = "drawSmallOverlay",
		name = "Small overlay",
		description = "Whether to draw a small overlay",
		position = 6
	)
	default SmallOverlayStyle drawSmallOverlay()
	{
		return SmallOverlayStyle.PERCENTAGE;
	}

	@ConfigItem(
			keyName = "colorSmallOverlay",
			name = "Color options",
			description = "Off for yellow, other options use the text color thresholds",
			position = 7
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
		name = "High threshold (%)",
		description = "Decides the color of the overlay. Above high threshold = Green, between high threshold and low = Yellow, below low threshold = Red",
		position = 8
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
		name = "Low threshold (%)",
		description = "Decides the color of the overlay. Above high threshold = Green, between high threshold and low = Yellow, below low threshold = Red",
		position = 9
	)
	default int warningColorThresholdLower()
	{
		return 88;
	}

	@ConfigItem(
		keyName = "warnLargeTickDiff",
		name = "Warn in chat about lag (ms)",
		description = "Prints a warning in the chatbox if a tick is too long or short",
		position = 10
	)
	default boolean warnLargeTickDiff()
	{
		return false;
	}

	@ConfigItem(
			keyName = "warnLargeTickDiffValue",
			name = "Warn of lag +/-: (ms)",
			description = "Warn in chat about server ticks being too fast or too slow (ms)",
			position = 11
	)
	default int warnLargeTickDiffValue() { return 150; }

	@ConfigItem(
		keyName = "disregardTickCounter",
		name = "Disregard ticks on login",
		description = "Ticks on login are very inconsistent. Having this too low can lead to inaccurate measurements.",
		position = 12
	)
	default int disregardTickCounter()
	{
		return 15;
	}


}
