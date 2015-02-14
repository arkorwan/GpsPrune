package tim.prune.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import tim.prune.I18nManager;
import tim.prune.config.ColourScheme;
import tim.prune.config.Config;
import tim.prune.data.Altitude;
import tim.prune.data.AltitudeRange;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Chart component for the profile display
 */
public class ProfileChart extends GenericChart
{
	/** Current scale factor in x direction*/
	private double _xScaleFactor = 0.0;
	/** Possible altitude scales to use */
	private static final int[] ALTITUDE_SCALES = {10000, 5000, 2000, 1000, 500, 200, 100, 50};


	/**
	 * Constructor
	 * @param inTrackInfo Track info object
	 */
	public ProfileChart(TrackInfo inTrackInfo)
	{
		super(inTrackInfo);
		MINIMUM_SIZE = new Dimension(200, 100);
		addMouseListener(this);
	}


	/**
	 * Override paint method to draw map
	 * @param g Graphics object
	 */
	public void paint(Graphics g)
	{
		super.paint(g);
		if (_track != null && _track.getNumPoints() > 0)
		{
			int width = getWidth();
			int height = getHeight();

			// Set up colours
			final Color barColour = Config.getColourScheme().getColour(ColourScheme.IDX_POINT);
			final Color rangeColour = Config.getColourScheme().getColour(ColourScheme.IDX_SELECTION);
			final Color currentColour = Config.getColourScheme().getColour(ColourScheme.IDX_PRIMARY);
			final Color secondColour = Config.getColourScheme().getColour(ColourScheme.IDX_SECONDARY);
			final Color lineColour = Config.getColourScheme().getColour(ColourScheme.IDX_LINES);

			// message if no altitudes in track
			if (!_track.hasAltitudeData())
			{
				g.setColor(lineColour);
				g.drawString(I18nManager.getText("display.noaltitudes"), 50, height/2);
				return;
			}

			// altitude profile
			AltitudeRange altitudeRange = _track.getAltitudeRange();
			int minAltitude = altitudeRange.getMinimum();
			int maxAltitude = altitudeRange.getMaximum();
			int numPoints = _track.getNumPoints();
			_xScaleFactor = 1.0 * (width - 2 * BORDER_WIDTH - 1) / numPoints;
			double yScaleFactor = 1.0 * (height - 2 * BORDER_WIDTH) / (maxAltitude - minAltitude);
			int barWidth = (int) (_xScaleFactor + 1.0);
			int selectedPoint = _trackInfo.getSelection().getCurrentPointIndex();
			// selection start, end
			int selectionStart = -1, selectionEnd = -1;
			if (_trackInfo.getSelection().hasRangeSelected()) {
				selectionStart = _trackInfo.getSelection().getStart();
				selectionEnd = _trackInfo.getSelection().getEnd();
			}

			// horizontal lines for scale - set to round numbers eg 500m
			int lineScale = getLineScale(minAltitude, maxAltitude);
			int altitude = 0;
			int x = 0, y = 0;
			if (lineScale > 1)
			{
				g.setColor(lineColour);
				while (altitude < maxAltitude)
				{
					if (altitude > minAltitude)
					{
						y = height - BORDER_WIDTH - (int) (yScaleFactor * (altitude - minAltitude));
						g.drawLine(BORDER_WIDTH + 1, y, width - BORDER_WIDTH - 1, y);
					}
					altitude += lineScale;
				}
			}

			try
			{
				// loop through points
				Altitude.Format chartFormat = altitudeRange.getFormat();
				g.setColor(barColour);
				for (int p = 0; p < numPoints; p++)
				{
					x = (int) (_xScaleFactor * p) + 1;
					if (p == selectionStart)
						g.setColor(rangeColour);
					else if (p == (selectionEnd+1))
						g.setColor(barColour);
					if (_track.getPoint(p).getAltitude().isValid())
					{
						altitude = _track.getPoint(p).getAltitude().getValue(chartFormat);
						y = (int) (yScaleFactor * (altitude - minAltitude));
						g.fillRect(BORDER_WIDTH+x, height-BORDER_WIDTH - y, barWidth, y);
					}
				}
				// current point (make sure it's drawn last)
				if (selectedPoint > -1)
				{
					Altitude alt = _track.getPoint(selectedPoint).getAltitude();
					if (alt.isValid())
					{
						x = (int) (_xScaleFactor * selectedPoint) + 1;
						g.setColor(secondColour);
						g.fillRect(BORDER_WIDTH + x, BORDER_WIDTH+1, barWidth, height-2*BORDER_WIDTH-2);
						g.setColor(currentColour);
						altitude = alt.getValue(chartFormat);
						y = (int) (yScaleFactor * (altitude - minAltitude));
						g.fillRect(BORDER_WIDTH + x, height-BORDER_WIDTH - y, barWidth, y);
					}
				}
			}
			catch (NullPointerException npe) { // ignore, probably due to data being changed
			}
			// Draw numbers on top of the graph to mark scale
			if (lineScale > 1)
			{
				int textHeight = g.getFontMetrics().getHeight();
				altitude = 0;
				y = 0;
				g.setColor(currentColour);
				while (altitude < maxAltitude)
				{
					if (altitude > minAltitude)
					{
						y = height - BORDER_WIDTH - (int) (yScaleFactor * (altitude - minAltitude));
						// Limit y so String isn't above border
						if (y < (BORDER_WIDTH + textHeight))
						{
							y = BORDER_WIDTH + textHeight;
						}
						g.drawString(""+altitude, BORDER_WIDTH + 5, y);
					}
					altitude += lineScale;
				}
			}
		}
	}


	/**
	 * Work out the scale for the horizontal lines
	 * @param inMin min altitude of data
	 * @param inMax max altitude of data
	 * @return scale separation, or -1 for no scale
	 */
	private int getLineScale(int inMin, int inMax)
	{
		if ((inMax - inMin) < 50 || inMax < 0)
		{
			return -1;
		}
		int numScales = ALTITUDE_SCALES.length;
		int scale = 0;
		int numLines = 0;
		int altitude = 0;
		for (int i=0; i<numScales; i++)
		{
			scale = ALTITUDE_SCALES[i];
			if (scale < inMax)
			{
				numLines = 0;
				altitude = 0;
				while (altitude < inMax)
				{
					altitude += scale;
					if (altitude > inMin)
					{
						numLines++;
					}
				}
				if (numLines > 2)
				{
					return scale;
				}
			}
		}
		// no suitable scale found so just use minimum
		return ALTITUDE_SCALES[numScales-1];
	}


	/**
	 * Method to inform map that data has changed
	 * @param inTrack track object
	 */
	public void dataUpdated(Track inTrack)
	{
		_track = inTrack;
		repaint();
	}


	/**
	 * React to click on profile display
	 */
	public void mouseClicked(MouseEvent e)
	{
		// ignore right clicks
		if (_track != null && !e.isMetaDown())
		{
			int xClick = e.getX();
			int yClick = e.getY();
			// Check click is within main area (not in border)
			if (xClick > BORDER_WIDTH && yClick > BORDER_WIDTH && xClick < (getWidth() - BORDER_WIDTH)
				&& yClick < (getHeight() - BORDER_WIDTH))
			{
				// work out which data point is nearest and select it
				int pointNum = (int) ((e.getX() - BORDER_WIDTH) / _xScaleFactor);
				// If shift clicked, then extend selection
				if (e.isShiftDown()) {
					_trackInfo.extendSelection(pointNum);
				}
				else {
					_trackInfo.selectPoint(pointNum);
				}
			}
		}
	}
}
