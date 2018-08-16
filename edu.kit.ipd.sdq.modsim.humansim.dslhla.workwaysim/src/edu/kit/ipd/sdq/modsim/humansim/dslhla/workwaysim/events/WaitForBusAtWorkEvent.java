package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class WaitForBusAtWorkEvent extends AbstractSimEventDelegator<Human>{

	protected WaitForBusAtWorkEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		
		
		if(!human.isCollected()){
			WaitForBusAtWorkEvent e = new WaitForBusAtWorkEvent(getModel(), "ReschedulingWaitForBusAtWork");
//			e.schedule(human, HumanSimValues.BUSY_WAITING_TIME_STEP.toSeconds().value());
			m.getComponent().synchronisedAdvancedTime(HumanSimValues.BUSY_WAITING_TIME_STEP.toSeconds().value(), e, human);
			return;
		} 
		
		human.calculateWaitedTime();
		Utils.log(human, human.getName() + " entered bus at " + human.getWorkBusStop().getName() );
		human.driveToBusStopAtHome();
		human.humanIsCollected();
		DrivingBusHomeEvent e = new DrivingBusHomeEvent(getModel(), "DrivingBusHome");
//		e.schedule(human, 0);
		m.getComponent().synchronisedAdvancedTime(0, e, human);
		
	}
	
	

}
