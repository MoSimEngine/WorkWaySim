package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.exceptions.RTIexception;

public class RegisterAtBusStopWorkEvent extends AbstractSimEventDelegator<Human>{

	protected RegisterAtBusStopWorkEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		try {
			m.registerHumanAtBusStop(human, human.getWorkBusStop());
			m.getComponent().changeDestinationAttribute(human, human.getHomeBusStop());
		} catch (RTIexception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		human.arriveAtBusStopWalkingTimePointLog();
		
		if(HumanSimValues.USE_SPIN_WAIT){
		WaitForBusAtWorkEvent e = new WaitForBusAtWorkEvent(getModel(), "WaitForBusStopWork");
//		e.schedule(human, 0);
		m.getComponent().synchronisedAdvancedTime(1.0, e, human);
		return;
		} else {
			m.startScanningForHLAEvents();
		}
		
		
		
	}

}
