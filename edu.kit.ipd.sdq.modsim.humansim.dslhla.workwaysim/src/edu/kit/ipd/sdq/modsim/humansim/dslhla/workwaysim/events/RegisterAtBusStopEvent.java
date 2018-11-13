package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.RegisterToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;


public class RegisterAtBusStopEvent extends AbstractSimEventDelegator<Human>{

	protected RegisterAtBusStopEvent(ISimulationModel model, String name) {
		super(model, name);
	}

	@Override
	public void eventRoutine(Human human) {
		
		WorkwayModel m = (WorkwayModel)this.getModel();
		human.arriveAtBusStopWalkingTimePointLog();
		RegisterToken regTok = new RegisterToken(null, human, 1.0, 0.0, (BusStop)human.getPosition() , (BusStop)human.getDestination());
		m.getTimelineSynchronizer().putToken(regTok);
		
		Utils.log(human, "Registers at bus Stop:" + human.getPosition().getName() + " with Destination" + human.getDestination().getName());
		PickUpTimeoutEvent e = new PickUpTimeoutEvent(getModel(), "PickUpTimeoutAtBSH");
		TimeAdvanceToken token = new TimeAdvanceToken(e, human, Duration.minutes(20).toSeconds().value());
		Utils.log(human, "Register Event Return determined: " + (getModel().getSimulationControl().getCurrentSimulationTime() + Duration.minutes(20).toSeconds().value()));
		m.getTimelineSynchronizer().putToken(token);
		return;
	}

}