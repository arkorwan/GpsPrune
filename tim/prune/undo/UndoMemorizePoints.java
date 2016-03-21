package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

public class UndoMemorizePoints implements UndoOperation {

	App _app;
	DataPoint[] _oldData;

	public UndoMemorizePoints(App inApp) {
		_app = inApp;
		_oldData = _app.getTemporaryPoints();
	}

	@Override
	public String getDescription() {
		return I18nManager.getText("undo.memorize");
	}

	@Override
	public void performUndo(TrackInfo inTrackInfo) throws UndoException {
		_app.saveTemporaryPoints(_oldData);
	}

}
