package tim.prune.function;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.undo.UndoRedistributePoints;

/**
 * 
 * Redistribute points in memory equally (by speed) over the selected range. The
 * endpoints of the range are not affected.
 * 
 * @author arkorwan
 *
 */
public class RedistributePointsFunction extends GenericFunction {

	App _app;

	public RedistributePointsFunction(App inApp) {
		super(inApp);
		_app = inApp;
	}

	@Override
	public void begin() {
		int startIndex = _app.getTrackInfo().getSelection().getStart();
		int endIndex = _app.getTrackInfo().getSelection().getEnd();
		Track track = _app.getTrackInfo().getTrack();
		DataPoint[] temp = _app.getTemporaryPoints();
		int guideSize = endIndex - startIndex - 1;
		DataPoint[] oldPoints = track.cloneContents();

		double[] distances = new double[guideSize + 1];
		for (int i = 0; i <= guideSize; i++) {
			DataPoint prev = oldPoints[startIndex + i];
			DataPoint current = oldPoints[startIndex + i + 1];
			distances[i] = DataPoint.calculateRadiansBetween(prev, current);
		}

		double[] accDistances = new double[guideSize + 1];
		accDistances[0] = distances[0];
		for (int i = 1; i <= guideSize; i++) {
			accDistances[i] = accDistances[i - 1] + distances[i];
		}

		Timestamp endTime = oldPoints[endIndex].getTimestamp();

		DataPoint[] newPoints = new DataPoint[oldPoints.length - guideSize
				+ temp.length];
		DataPoint[] replacedPoints = new DataPoint[guideSize];
		System.arraycopy(oldPoints, 0, newPoints, 0, startIndex + 1);
		System.arraycopy(oldPoints, startIndex + 1, replacedPoints, 0,
				guideSize);
		System.arraycopy(oldPoints, endIndex, newPoints,
				startIndex + 1 + temp.length, oldPoints.length - endIndex);

		int lastUsedTemp = -1;
		int lastNewIndex = startIndex;
		DataPoint prev = oldPoints[startIndex];
		for (int i = 0; i < guideSize; i++) {
			double leftD = distances[i];
			double rightD = accDistances[guideSize] - accDistances[i];

			// find best point
			int g = binarySearch(temp, leftD, rightD, prev.getTimestamp(),
					endTime, lastUsedTemp + 1, temp.length - 1);
			DataPoint current = temp[g];
			copyLocationData(oldPoints[startIndex + i + 1], current);
			int count = g - lastUsedTemp - 1;
			// interpolate from last known point to this point
			fillPoints(prev, current, count, newPoints, temp, lastNewIndex + 1,
					lastUsedTemp + 1);
			lastNewIndex += count + 1;
			lastUsedTemp += count + 1;
			newPoints[lastNewIndex] = temp[g];
			prev = current;
		}

		// interpolate the last segment
		fillPoints(prev, oldPoints[endIndex], temp.length - lastUsedTemp - 1,
				newPoints, temp, lastNewIndex + 1, lastUsedTemp + 1);

		UndoRedistributePoints undo = new UndoRedistributePoints(_app,
				replacedPoints, startIndex, endIndex - guideSize + temp.length);

		_app.saveTemporaryPoints(null);
		track.replaceContents(newPoints);
		_app.getTrackInfo().getSelection().clearAll();
		_app.completeFunction(undo,
				I18nManager.getText("confirm.redistribute"));

	}

	private void fillPoints(DataPoint from, DataPoint to, int count,
			DataPoint[] newPoints, DataPoint[] temp, int startingNewIndex,
			int startingTempIndex) {
		DataPoint[] interpolated = from.interpolate(to, count);
		for (int j = 0; j < interpolated.length; j++) {
			newPoints[startingNewIndex + j] = temp[startingTempIndex + j];
			copyLocationData(interpolated[j], newPoints[startingNewIndex + j]);
		}
	}

	/**
	 * Copy latitude, longitude, and altitude from source to dest.
	 * 
	 * @param source
	 * @param dest
	 */
	static void copyLocationData(DataPoint source, DataPoint dest) {
		dest.setFieldValue(Field.LATITUDE, source.getFieldValue(Field.LATITUDE),
				false);
		dest.setFieldValue(Field.LONGITUDE,
				source.getFieldValue(Field.LONGITUDE), false);
		dest.setFieldValue(Field.ALTITUDE, source.getFieldValue(Field.ALTITUDE),
				false);

	}

	// find the index of point in data, that yield the least difference in
	// velocity
	// coming to and going from the point.
	private int binarySearch(DataPoint[] data, double leftDistance,
			double rightDistance, Timestamp startTime, Timestamp endTime,
			int leftIndex, int rightIndex) {

		if (leftIndex == rightIndex) {
			return leftIndex;
		} else {
			int mid = leftIndex + (rightIndex - leftIndex) / 2;
			double leftV = leftDistance
					/ data[mid].getTimestamp().getSecondsSince(startTime);
			double rightV = rightDistance
					/ endTime.getSecondsSince(data[mid].getTimestamp());

			// return mid if it's close enough
			if (Math.abs(leftV / rightV - 1.0) < 0.01) {
				return mid;
			}
			// less velocity -> denser points -> the optimal must be on this
			// side
			if (leftV < rightV) {
				return binarySearch(data, leftDistance, rightDistance,
						startTime, endTime, leftIndex, mid);
			} else {
				return binarySearch(data, leftDistance, rightDistance,
						startTime, endTime, mid + 1, rightIndex);
			}

		}
	}

	@Override
	public String getNameKey() {
		return "function.redistribute";
	}

}
