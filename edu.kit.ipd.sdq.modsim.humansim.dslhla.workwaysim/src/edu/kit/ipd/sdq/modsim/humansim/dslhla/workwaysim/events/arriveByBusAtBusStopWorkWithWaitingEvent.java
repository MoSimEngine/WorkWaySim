package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class arriveByBusAtBusStopWorkWithWaitingEvent extends AbstractSimEventDelegator<Human>{

	protected arriveByBusAtBusStopWorkWithWaitingEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		m.hasToKeepAlive = false;
		
		//System.out.println("ArrivedAtBusStop");
		
		human.arriveAtBusStopWorkByDriving();
		Utils.log(human, human.getName() + "arrived at BusStop at Work - by bus");
		
		HumanWalkFromBusStopToWorkEvent e = new HumanWalkFromBusStopToWorkEvent(getModel(),"WalkFromBusStopWorkToWork");
//		e.schedule(human, 0);
		m.getComponent().synchronisedAdvancedTime(0, e, human);
	}

}
