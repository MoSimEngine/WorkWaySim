package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanExitsBusEvent extends AbstractSimEventDelegator<Human>{

	public HumanExitsBusEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		
		WorkwayModel m = (WorkwayModel)this.getModel();
		System.out.println("In Exit Bus Event");
		if(human.getDestination().equals(human.getHomeBusStop())){
			HumanArriveByBustBusStopHomeEvent e = new HumanArriveByBustBusStopHomeEvent(getModel(), "ArriveAtBSHomeByBus");
//			e.schedule(human, 0);
			m.getComponent().synchronisedAdvancedTime(0, e, human);
		} else if (human.getDestination().equals(human.getWorkBusStop())){
			HumanArriveByBusAtBusStopWorkEvent e = new HumanArriveByBusAtBusStopWorkEvent(getModel(), "ArriveAtBSWorkbyBus");
//			e.schedule(human, 0);
			m.getComponent().synchronisedAdvancedTime(0, e, human);
		}
		
		m.setScanning(false);
		
	}

}
