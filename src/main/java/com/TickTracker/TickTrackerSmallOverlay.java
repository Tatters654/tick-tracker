package com.TickTracker;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;

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


public class TickTrackerSmallOverlay extends OverlayPanel
{
	private static final int Y_OFFSET = 1;
	private static final int X_OFFSET = 1;

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

		StringBuilder overlayText = new StringBuilder();
		if (config.drawSmallOverlay() == SmallOverlayStyle.PERCENTAGE || config.drawSmallOverlay() == SmallOverlayStyle.BOTH)
		{
			overlayText.append(String.format("%.2f%%", plugin.getTickWithinRangePercent()));
			if (config.drawSmallOverlay() == SmallOverlayStyle.BOTH)
			{
				overlayText.append(" / ");
			}
		}
		if (config.drawSmallOverlay() == SmallOverlayStyle.LAST_DIFF || config.drawSmallOverlay() == SmallOverlayStyle.BOTH)
		{
			overlayText.append(String.format("%dms", plugin.getTickDiffNS() / plugin.getNANOS_PER_MILLIS()));
		}

		final int textWidth = graphics.getFontMetrics().stringWidth(overlayText.toString());
		final int textHeight = graphics.getFontMetrics().getAscent() - graphics.getFontMetrics().getDescent();

		final int width = (int) client.getRealDimensions().getWidth();
		final Point point = new Point(width - textWidth - xOffset, textHeight + Y_OFFSET);
		OverlayUtil.renderTextLocation(graphics, point, overlayText.toString(), plugin.colorSelection());
	}

}
