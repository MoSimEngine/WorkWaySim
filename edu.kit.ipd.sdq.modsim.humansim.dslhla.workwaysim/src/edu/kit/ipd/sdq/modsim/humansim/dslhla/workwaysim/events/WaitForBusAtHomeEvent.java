package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class WaitForBusAtHomeEvent extends AbstractSimEventDelegator<Human> {

	protected WaitForBusAtHomeEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		
	
		if(!human.isCollected()){
			WaitForBusAtHomeEvent e = new WaitForBusAtHomeEvent(getModel(), "redscheduled wait for bus at home event");
//			e.schedule(human, HumanSimValues.BUSY_WAITING_TIME_STEP.toSeconds().value());
			m.getComponent().synchronisedAdvancedTime(HumanSimValues.BUSY_WAITING_TIME_STEP.toSeconds().value(), e, human);
			return;
		}
		
		human.calculateWaitedTime();
		
		
		Utils.log(human, human.getName() + " entered bus at " + human.getHomeBusStop().getName() );
		human.driveToBusStopAtWork();
		human.humanIsCollected();
		DrivingBusToWorkEvent e = new DrivingBusToWorkEvent(getModel(), "DrivingBusToWorkEvent");
//		e.schedule(human, 0);
		m.getComponent().synchronisedAdvancedTime(0, e, human);
		
	}

}
