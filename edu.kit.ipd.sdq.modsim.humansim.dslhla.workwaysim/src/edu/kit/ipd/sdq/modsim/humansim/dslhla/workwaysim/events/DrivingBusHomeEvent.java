package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class DrivingBusHomeEvent extends AbstractSimEventDelegator<Human>{

	protected DrivingBusHomeEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub

		WorkwayModel m = (WorkwayModel)this.getModel();
		
		
		if(human.isCollected()){
			
			DrivingBusHomeEvent e = new DrivingBusHomeEvent(getModel(), "reschedulingDrivingBusHome");
//			e.schedule(human, HumanSimValues.BUSY_WAITING_TIME_STEP.toSeconds().value());
			m.getComponent().synchronisedAdvancedTime(HumanSimValues.BUSY_WAITING_TIME_STEP.toSeconds().value(), e, human);
			return;
		}
		Utils.log(human, human.getName() + " left bus at " + human.getHomeBusStop().getName() );
		human.calculateDrivingTime();
		
		ArriveByBusAtBusStopHomeWithWaitingEvent e = new ArriveByBusAtBusStopHomeWithWaitingEvent(getModel(), "ArriveAtHomeByBusWaiting");
//		e.schedule(human, 0);
		m.getComponent().synchronisedAdvancedTime(0, e, human);
	}

}
