package com.TickTracker;

import com.TickTracker.config.SmallOverlayStyle;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;


public class TickTrackerSmallOverlay extends OverlayPanel
{
	private static final int X_OFFSET = 1;
	private static final String SEPARATOR_STRING = " / ";
	private static final String WAITING = "Waiting...";

	private final Client client;
	private final TickTrackerPlugin plugin;
	private final TickTrackerPluginConfiguration config;

	@Inject
	private TickTrackerSmallOverlay overlay;

	@Inject
	private TickTrackerSmallOverlay(Client client, TickTrackerPlugin plugin, TickTrackerPluginConfiguration config)
	{
		super(plugin);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGH);
		this.plugin = plugin;
		this.client = client;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.drawSmallOverlay() != SmallOverlayStyle.NONE)
		{
			drawSmallOverlay(graphics);
		}
		return null;
	}

	private void drawSmallOverlay(Graphics2D graphics)
	{
		// taken from the fps plugin
		Widget logoutButton = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_LOGOUT_BUTTON);
		int xOffset = X_OFFSET;
		if (logoutButton != null && !logoutButton.isHidden())
		{
			xOffset += logoutButton.getWidth();
		}

		final int clientWidth = (int) client.getRealDimensions().getWidth();
		final int textHeight = graphics.getFontMetrics().getAscent() - graphics.getFontMetrics().getDescent();

		// Draw waiting indicator if inside disregard period, then exit method
		if (plugin.getDisregardCounter() < config.disregardTickCounter())
		{
			drawSmallOverlaySubsection(graphics, WAITING, clientWidth, xOffset, textHeight, true);
			return;
		}

		// As we draw sections, modify xOffset based on the width of the text we've drawn so far

		// Draw tick length (600ms) part, if configured to, with correct coloring
		if (config.drawSmallOverlay() == SmallOverlayStyle.LAST_DIFF || config.drawSmallOverlay() == SmallOverlayStyle.BOTH)
		{
			xOffset += drawSmallOverlaySubsection(graphics,
					String.format("%dms", plugin.getTickDiffNS() / plugin.getNANOS_PER_MILLIS()),
					clientWidth, xOffset, textHeight,
					config.smallOverlayColorStyle() == SmallOverlayStyle.PERCENTAGE);
		}

		// Draw separator part if required, with correct coloring
		if (config.drawSmallOverlay() == SmallOverlayStyle.BOTH)
		{
			xOffset += drawSmallOverlaySubsection(graphics, SEPARATOR_STRING, clientWidth, xOffset, textHeight,
					config.smallOverlayColorStyle() != SmallOverlayStyle.BOTH);
		}

		// Draw percentage part, if configured to, with correct coloring
		if (config.drawSmallOverlay() == SmallOverlayStyle.PERCENTAGE || config.drawSmallOverlay() == SmallOverlayStyle.BOTH)
		{
			xOffset += drawSmallOverlaySubsection(graphics,
					String.format("%.2f%%", plugin.getTickWithinRangePercent()),
					clientWidth, xOffset, textHeight,
					config.smallOverlayColorStyle() == SmallOverlayStyle.LAST_DIFF);
		}
	}

	private int drawSmallOverlaySubsection(Graphics2D graphics, String toDraw, int clientWidth, int xOffset, int textHeight, boolean offForSection)
	{
		final int textWidth = graphics.getFontMetrics().stringWidth(toDraw);
		final Point point = new Point(clientWidth - textWidth - xOffset, textHeight + config.Y_Offset());
		OverlayUtil.renderTextLocation(graphics, point, toDraw, colorSelection(offForSection));
		return textWidth;
	}

	public Color colorSelection(boolean offForSection)
	{
		if (offForSection || config.smallOverlayColorStyle() == SmallOverlayStyle.NONE)
		{
			return Color.YELLOW;
		}

		if (plugin.getTickWithinRangePercent() >= config.warningColorThresholdUpper())
		{
			return Color.GREEN;
		}
		else if (plugin.getTickWithinRangePercent() >= config.warningColorThresholdLower())
		{
			return Color.YELLOW;
		}
		else
		{
			return Color.RED;
		}
	}
}
