package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanWalksDirectlyToWorkEvent extends AbstractSimEventDelegator<Human>{

	public HumanWalksDirectlyToWorkEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		// TODO Auto-generated method stub
		human.walkToWorkDirectly();
		Utils.log(human, human.getName() + " walking to work. Wow, its a long way.");
		
		double walkingTime = human.WALK_DIRECTLY.toSeconds().value();
		
		HumanArrivesAtWorkEvent e = new HumanArrivesAtWorkEvent(this.getModel(), "Human arrives at work walking");
//		e.schedule(human, walkingTime);
		m.getComponent().synchronisedAdvancedTime(walkingTime, e, human);
	}

}
