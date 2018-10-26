package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.exceptions.RTIexception;

public class RegisterAtBusStopHomeEvent extends AbstractSimEventDelegator<Human>{

	protected RegisterAtBusStopHomeEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		human.setPosition(human.getHomeBusStop());
		human.setDestination(human.getWorkBusStop());
		m.registerHumanAtBusStop(human, human.getHomeBusStop(), human.getWorkBusStop());
		
		human.arriveAtBusStopWalkingTimePointLog();
		
		Utils.log(human, "Registers at bus Stop:" + human.getHomeBusStop().getName());
		
		if(HumanSimValues.USE_SPIN_WAIT){
		WaitForBusAtHomeEvent e = new WaitForBusAtHomeEvent(this.getModel(), "Waiting for bus at home event");
//		e.schedule(human, 0);
		m.getComponent().synchronisedAdvancedTime(1.0, e, human);
		return;
		} else {
			PickUpTimeoutEvent e = new PickUpTimeoutEvent(getModel(), "PickUpTimeoutAtBSH");
			
			m.getComponent().synchronisedAdvancedTime(Duration.minutes(50).toSeconds().value(), e, human);
		}
		
		
		
	
		
	}

}
