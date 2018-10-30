package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class RegisterAtBusStopEvent extends AbstractSimEventDelegator<Human>{

	protected RegisterAtBusStopEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		m.registerHumanAtBusStop(human, (BusStop)human.getPosition(), (BusStop)human.getDestination());
		
		human.arriveAtBusStopWalkingTimePointLog();
		
		Utils.log(human, "Registers at bus Stop:" + human.getPosition().getName() + " with Destination" + human.getDestination().getName());
		
		if(HumanSimValues.USE_SPIN_WAIT){
		WaitForBusAtHomeEvent e = new WaitForBusAtHomeEvent(this.getModel(), "Waiting for bus at home event");
		m.getComponent().synchronisedAdvancedTime(HumanSimValues.BUSY_WAITING_TIME_STEP.toSeconds().value(), e, human);
		return;
		} else {
			PickUpTimeoutEvent e = new PickUpTimeoutEvent(getModel(), "PickUpTimeoutAtBSH");
//			e.schedule(human, Duration.minutes(20).toSeconds().value());
			m.getComponent().synchronisedAdvancedTime(Duration.minutes(20).toSeconds().value(), e, human);
		}
		
		
		
	
		
	}

}