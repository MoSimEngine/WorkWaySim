package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanWalkFromBusStopHomeToHomeEvent  extends AbstractSimEventDelegator<Human>{

	protected HumanWalkFromBusStopHomeToHomeEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		// TODO Auto-generated method stub
		human.walkHomeFromBusStop();
		Utils.log(human, human.getName() + " walks home. Only a few steps now!");
		double walkHomeDuration = human.HOME_TO_STATION.toSeconds().value();
		
		HumanArrivesHomeEvent e = new HumanArrivesHomeEvent(human.getModel(), "Human arrives at home");
//		e.schedule(human, walkHomeDuration);
		m.getComponent().synchronisedAdvancedTime(walkHomeDuration, e, human);
	}

}
