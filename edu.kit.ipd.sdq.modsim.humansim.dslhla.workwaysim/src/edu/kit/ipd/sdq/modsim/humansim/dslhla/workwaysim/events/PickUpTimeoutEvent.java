package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import java.util.Random;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Token;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Token.TokenState;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class PickUpTimeoutEvent extends AbstractSimEventDelegator<Token> {

	protected PickUpTimeoutEvent(ISimulationModel model, String name) {
		super(model, name);
	}

	@Override
	public void eventRoutine(Token human) {
		WorkwayModel m = (WorkwayModel) this.getModel();

		if (!human.isCollected()) {
			PickUpTimeoutEvent e = new PickUpTimeoutEvent(getModel(), getName());
			TimeAdvanceToken token = new TimeAdvanceToken(e, human, Duration.minutes(20).toSeconds().value());
			m.getTimelineSynchronizer().putToken(token, false);
			return;
		}
	}
}
