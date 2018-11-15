package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import java.util.Random;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human.HumanState;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class PickUpTimeoutEvent extends AbstractSimEventDelegator<Human>{

	protected PickUpTimeoutEvent(ISimulationModel model, String name) {
		super(model, name);
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		boolean changeToWalking = false;
		
		if(!human.isCollected() && (human.getState().equals(HumanState.AT_BUSSTOP))){
			
			//TODO Insert here random assertion of change to walking and also a handling
			if(changeToWalking){
				new HumanArrivesAtWorkEvent(this.getModel(), "Human Arrives At Work Walking after waiting at BS").schedule(human, human.WALK_DIRECTLY.toSeconds().value());
				return;
			} else {
				PickUpTimeoutEvent e = new PickUpTimeoutEvent(getModel(), getName());
				TimeAdvanceToken token = new TimeAdvanceToken(e, human, Duration.minutes(20).toSeconds().value());
				m.getTimelineSynchronizer().putToken(token, false);
				return;
			}
		}
	}
}
