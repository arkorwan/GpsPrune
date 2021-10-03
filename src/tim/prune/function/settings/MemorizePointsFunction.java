package tim.prune.function;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.undo.UndoMemorizePoints;

/**
 * Commit the points in the selected range to memory. The memorized points can
 * be used later from the Redistribute function.
 * 
 * @author arkorwan
 *
 */
public class MemorizePointsFunction extends GenericFunction {

	App _app;

	public MemorizePointsFunction(App inApp) {
		super(inApp);
		_app = inApp;
	}

	@Override
	public void begin() {
		int startIndex = _app.getTrackInfo().getSelection().getStart();
		int endIndex = _app.getTrackInfo().getSelection().getEnd();
		int tempSize = endIndex - startIndex + 1;
		if (tempSize > 0) {
			Track track = _app.getTrackInfo().getTrack();
			DataPoint[] oldPoints = track.cloneContents();
			DataPoint[] temp = new DataPoint[tempSize];
			System.arraycopy(oldPoints, startIndex, temp, 0, tempSize);

			UndoMemorizePoints undo = new UndoMemorizePoints(_app);
			_app.saveTemporaryPoints(temp);
			_app.completeFunction(undo,
					I18nManager.getText("confirm.memorize"));
		}

	}

	@Override
	public String getNameKey() {
		return "function.memorize";
	}

}
