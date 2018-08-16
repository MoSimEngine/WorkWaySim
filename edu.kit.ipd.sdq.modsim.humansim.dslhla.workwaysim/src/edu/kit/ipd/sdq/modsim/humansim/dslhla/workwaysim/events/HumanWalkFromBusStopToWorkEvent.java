package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanWalkFromBusStopToWorkEvent extends AbstractSimEventDelegator<Human>{

	public HumanWalkFromBusStopToWorkEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		// TODO Auto-generated method stub
		human.walkToWorkFromBusStop();
		Utils.log(human, human.getName() + " is walking to work.");
		double walkingToWork = human.WORK_TO_STATION.toSeconds().value();
		HumanArrivesAtWorkEvent e = new HumanArrivesAtWorkEvent(this.getModel(), "Human arrives at work");
//		e.schedule(human, walkingToWork);
		m.getComponent().synchronisedAdvancedTime(walkingToWork, e, human);
	}

}
