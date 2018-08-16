package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class ArriveAtBusStopAtHomeEvent  extends AbstractSimEventDelegator<Human>{

	protected ArriveAtBusStopAtHomeEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		human.arriveAtBusStopHome();
		Utils.log(human, human.getName() + " arrives at bus stop "+ human.getPosition().getName());
		
		RegisterAtBusStopHomeEvent e = new RegisterAtBusStopHomeEvent(this.getModel(), "Registers at BusStop home");
		m.getComponent().synchronisedAdvancedTime(0, e, human);
//		e.schedule(human, 0);
		//WaitForBusEvent e = new WaitForBusEvent(this.getModel(), "WaitForBus");
		
		//e.schedule(human, 0);
		
		
	}

}
