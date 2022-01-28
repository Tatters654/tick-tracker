package com.TickTracker;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import static java.util.Arrays.asList;

public class TickTrackerOverlay extends OverlayPanel
{

	private final Client client;
	private final TickTrackerPlugin plugin;
	private final TickTrackerPluginConfiguration config;

	@Inject
	private TickTrackerOverlay(Client client, TickTrackerPlugin plugin, TickTrackerPluginConfiguration config)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		this.plugin = plugin;
		this.client = client;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.drawLargeOverlay())
		{
			drawExtraInformation(graphics);
		}
		return super.render(graphics);
	}


	private LineComponent tickOverThresholdLineComponent(int tickOverThreshold, String configThreshold)
	{
		return LineComponent.builder()
			.right(String.format("%d (%.2f %%)", tickOverThreshold, (tickOverThreshold * 100.0) / plugin.getAllTickCounter()))
			.left(">" + configThreshold)
			.build();
	}

	private void drawExtraInformation(Graphics2D graphics)
	{
		panelComponent.getChildren().addAll(asList(TitleComponent.builder().text("Ticks").build(),
			tickOverThresholdLineComponent(plugin.getTickOverThresholdHigh(), String.valueOf(config.getThresholdHigh())),
			tickOverThresholdLineComponent(plugin.getTickOverThresholdMedium(), String.valueOf(config.getThresholdMedium())),
			tickOverThresholdLineComponent(plugin.getTickOverThresholdLow(), String.valueOf(config.getThresholdLow())),
			LineComponent.builder()
				.right(String.format("%d (%.2f %%)", plugin.getTickWithinRange(), plugin.getTickWithinRangePercent()))
				.left("Good")
				.build(),
			LineComponent.builder().right(String.valueOf(plugin.getAllTickCounter())).left("Total").build(),
			LineComponent.builder().right(String.valueOf(plugin.getTickDiffNS() / plugin.getMillisPerNanosecond())).left("Last Tick ms").build(),
			LineComponent.builder().right(String.valueOf(plugin.getRunningTickAverageNS() / plugin.getMillisPerNanosecond())).left("Tick Average ms").build()));
		panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth("average tick ms+extra fill600"), 0));
	}
}
