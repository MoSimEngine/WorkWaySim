package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
public class ArriveByBusAtBusStopHomeWithWaitingEvent extends AbstractSimEventDelegator<Human>{

	protected ArriveByBusAtBusStopHomeWithWaitingEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		WorkwayModel m = (WorkwayModel)this.getModel();
		m.hasToKeepAlive = false;
		Utils.log(human, human.getName() + "Arrived at Home BusStop at " + human.getHomeBusStop().getName() +" by bus");
		human.arriveAtBusStopHomeByDriving();
		HumanWalkFromBusStopHomeToHomeEvent e = new HumanWalkFromBusStopHomeToHomeEvent(getModel(), "WalkFromBusStopHomeToHome");
//		e.schedule(human, 0);
		m.getComponent().synchronisedAdvancedTime(0, e, human);
	}

}
