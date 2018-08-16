package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanWalksFromWorkToBusStopWorkEvent extends AbstractSimEventDelegator<Human>{

	protected HumanWalksFromWorkToBusStopWorkEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		human.walkToBusStopAtWork();
		
		Utils.log(human, human.getName() + " walks to work busstop:" + human.getWorkBusStop().getName() + ".  Oh happy day!");

		double walkingToBusStopWork = human.WORK_TO_STATION.toSeconds().value();
		
		HumanArriveAtBusStopAtWorkEvent e = new HumanArriveAtBusStopAtWorkEvent(this.getModel(), "Arrives at busstop");
//		e.schedule(human, walkingToBusStopWork);
		m.getComponent().synchronisedAdvancedTime(walkingToBusStopWork, e, human);
	}

}
