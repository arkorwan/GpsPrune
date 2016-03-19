package tim.prune.function;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;

/**
 * 
 * Redistribute cluttered trackpoints equally over a guided path. Intended to be
 * used with Interpolation tool.
 * 
 * @author arkorwan
 *
 */
public class RedistributePointFunction extends GenericFunction {

	App _app;

	public RedistributePointFunction(App inApp) {
		super(inApp);
		_app = inApp;
	}

	@Override
	public void begin() {
		int startIndex = _app.getTrackInfo().getSelection().getStart();
		int endIndex = _app.getTrackInfo().getSelection().getEnd();
		if (_app.getTemporaryPoints() == null) {
			deleteAndSavePoints(startIndex, endIndex);
		} else {
			redistributePoints(startIndex, endIndex);
		}

	}

	private void deleteAndSavePoints(int startIndex, int endIndex) {
		// ignoring waypoints for now

		int tempSize = endIndex - startIndex - 1;
		if (tempSize >= 0) {
			Track track = _app.getTrackInfo().getTrack();
			DataPoint[] oldPoints = track.cloneContents();
			DataPoint[] newPoints = new DataPoint[oldPoints.length - tempSize];
			DataPoint[] temp = new DataPoint[tempSize];
			System.arraycopy(oldPoints, 0, newPoints, 0, startIndex + 1);
			System.arraycopy(oldPoints, startIndex + 1, temp, 0, tempSize);
			System.arraycopy(oldPoints, endIndex, newPoints, startIndex + 1,
					oldPoints.length - endIndex);
			_app.saveTemporaryPoints(temp);
			track.replaceContents(newPoints);
			_app.getTrackInfo().getSelection().selectRange(startIndex,
					startIndex + 1);
		}

	}

	private void redistributePoints(int startIndex, int endIndex) {
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
		System.arraycopy(oldPoints, 0, newPoints, 0, startIndex + 1);
		System.arraycopy(oldPoints, endIndex, newPoints,
				startIndex + 1 + temp.length, oldPoints.length - endIndex);

		int lastUsedTemp = -1;
		int lastNewIndex = startIndex;
		DataPoint prev = oldPoints[startIndex];
		for (int i = 0; i < guideSize; i++) {
			double leftD = distances[i];
			double rightD = accDistances[guideSize] - accDistances[i];
			int g = binarySearch(temp, leftD, rightD, prev.getTimestamp(),
					endTime, lastUsedTemp + 1, temp.length - 1);
			DataPoint current = temp[g];
			copyLocationData(oldPoints[startIndex + i + 1], current);
			int count = g - lastUsedTemp - 1;
			fillPoints(prev, current, count, newPoints, temp, lastNewIndex + 1,
					lastUsedTemp + 1);
			lastNewIndex += count + 1;
			lastUsedTemp += count + 1;
			newPoints[lastNewIndex] = temp[g];
			prev = current;
		}

		fillPoints(prev, oldPoints[endIndex], temp.length - lastUsedTemp - 1,
				newPoints, temp, lastNewIndex + 1, lastUsedTemp);

		_app.saveTemporaryPoints(null);
		track.replaceContents(newPoints);
		_app.getTrackInfo().getSelection().clearAll();

	}

	void fillPoints(DataPoint from, DataPoint to, int count,
			DataPoint[] newPoints, DataPoint[] temp, int startingNewIndex,
			int startingTempIndex) {
		DataPoint[] interpolated = from.interpolate(to, count);
		for (int j = 0; j < interpolated.length; j++) {
			newPoints[startingNewIndex + j] = temp[startingTempIndex + j];
			copyLocationData(interpolated[j], newPoints[startingNewIndex + j]);
		}
	}

	static void copyLocationData(DataPoint source, DataPoint dest) {
		dest.setFieldValue(Field.LATITUDE, source.getFieldValue(Field.LATITUDE),
				false);
		dest.setFieldValue(Field.LONGITUDE,
				source.getFieldValue(Field.LONGITUDE), false);
		dest.setFieldValue(Field.ALTITUDE, source.getFieldValue(Field.ALTITUDE),
				false);

	}

	int binarySearch(DataPoint[] data, double leftDistance,
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
