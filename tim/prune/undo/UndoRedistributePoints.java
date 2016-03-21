package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

public class UndoRedistributePoints implements UndoOperation {

	App _app;
	DataPoint[] _temp, _replacedPoints;
	int _startIndex, _endIndex;

	public UndoRedistributePoints(App inApp, DataPoint[] replacedPoints,
			int startIndex, int endIndex) {
		_app = inApp;
		_replacedPoints = replacedPoints;
		_startIndex = startIndex;
		_endIndex = endIndex;
		_temp = _app.getTemporaryPoints();
	}

	@Override
	public String getDescription() {
		return I18nManager.getText("undo.redistribute");
	}

	@Override
	public void performUndo(TrackInfo inTrackInfo) throws UndoException {

		Track track = inTrackInfo.getTrack();
		int size = track.getNumPoints() - (_endIndex - _startIndex - 1)
				+ _replacedPoints.length;
		DataPoint[] oldPoints = track.cloneContents();
		DataPoint[] newPoints = new DataPoint[size];
		System.arraycopy(oldPoints, 0, newPoints, 0, _startIndex + 1);
		System.arraycopy(_replacedPoints, 0, newPoints, _startIndex + 1,
				_replacedPoints.length);
		System.arraycopy(oldPoints, _endIndex, newPoints,
				_startIndex + 1 + _replacedPoints.length,
				oldPoints.length - _endIndex);
		track.replaceContents(newPoints);
		_app.saveTemporaryPoints(_temp);
	}

}
