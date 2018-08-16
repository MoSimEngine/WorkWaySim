package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanArriveAtBusStopAtWorkEvent extends AbstractSimEventDelegator<Human>{

	protected HumanArriveAtBusStopAtWorkEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		human.arriveAtBusStopWork();
		Utils.log(human, human.getName() + "is at bus stop and halfway home!");
		//human.getPosition().setHuman(human);
		
		RegisterAtBusStopWorkEvent e = new RegisterAtBusStopWorkEvent(getModel(), "Register at bus stop work");
//		e.schedule(human, 0);
		m.getComponent().synchronisedAdvancedTime(0, e, human);
	}

}
