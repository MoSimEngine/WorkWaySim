package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class WaitForBusEvent extends AbstractSimEventDelegator<Human>{

	protected WaitForBusEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		
		if(!human.isCollected()){
			WaitForBusEvent e = new WaitForBusEvent(getModel(), "redscheduled wait for bus at home event");
//			e.schedule(human, HumanSimValues.BUSY_WAITING_TIME_STEP.toSeconds().value());
			m.getComponent().synchronisedAdvancedTime(HumanSimValues.BUSY_WAITING_TIME_STEP.toSeconds().value(), e, human);
			return;
		}
		
		human.calculateWaitedTime();
		
		
		Utils.log(human, human.getName() + " entered bus at " + human.getPosition().getName() );
		human.travellingToNext();
		human.humanIsCollected();
		DrivingBusEvent e = new DrivingBusEvent(getModel(), "DrivingBusToWorkEvent");
//		
		if(HumanSimValues.FULL_SYNC) {
			m.getComponent().synchronisedAdvancedTime(0, e, human);
		} else {
			e.schedule(human, 0);
		}
		
	}

}
