package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.RTIinternalError;

public class HumanExitsBusEvent extends AbstractSimEventDelegator<Human>{

	public HumanExitsBusEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		try {
			m.getComponent().getRTIAmb().evokeCallback(0.5);
		} catch (CallNotAllowedFromWithinCallback | RTIinternalError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(human.getDestination().equals(human.getHomeBusStop())){
			HumanArriveByBustBusStopHomeEvent e = new HumanArriveByBustBusStopHomeEvent(getModel(), "ArriveAtBSHomeByBus");
//			e.schedule(human, 0);
			m.getComponent().synchronisedAdvancedTime(0, e, human);
		} else if (human.getDestination().equals(human.getWorkBusStop())){
			Utils.log(human, "Scheduling Arrive Event");
			HumanArriveByBusAtBusStopWorkEvent e = new HumanArriveByBusAtBusStopWorkEvent(getModel(), "ArriveAtBSWorkbyBus");
//			e.schedule(human, 0);
			m.getComponent().synchronisedAdvancedTime(0, e, human);
		}
		
	}

}
