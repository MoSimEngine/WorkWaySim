package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class DrivingBusToWorkEvent extends AbstractSimEventDelegator<Human>{

	protected DrivingBusToWorkEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		
		
		if(human.isCollected()){
			DrivingBusToWorkEvent e = new DrivingBusToWorkEvent(getModel(), "RescheduledDrivingToBusWorkEvent");
//			e.schedule(human, HumanSimValues.BUSY_WAITING_TIME_STEP.toSeconds().value());
			m.getComponent().synchronisedAdvancedTime(HumanSimValues.BUSY_WAITING_TIME_STEP.toSeconds().value(), e, human);
			return;
		}
		
		Utils.log(human, human.getName() + " left bus at " + human.getWorkBusStop().getName() );
		human.calculateDrivingTime();
		
		arriveByBusAtBusStopWorkWithWaitingEvent e = new arriveByBusAtBusStopWorkWithWaitingEvent(getModel(), "ArriveAtBusStopWorkByBus");
//		e.schedule(human, 0);
		m.getComponent().synchronisedAdvancedTime(0, e, human);
		
	}

}
