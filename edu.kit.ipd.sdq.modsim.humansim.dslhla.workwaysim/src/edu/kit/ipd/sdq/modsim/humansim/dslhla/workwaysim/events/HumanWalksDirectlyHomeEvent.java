package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanWalksDirectlyHomeEvent extends AbstractSimEventDelegator<Human> {

	protected HumanWalksDirectlyHomeEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		human.walkHomeDirectly();
		Utils.log(human, human.getName() + " walking home. Dubidu human is walking");
		double walkingTime = human.WALK_DIRECTLY.toSeconds().value();
		
		HumanArrivesHomeEvent e = new HumanArrivesHomeEvent(this.getModel(), "Human arrives home walking directly");
//		e.schedule(human, walkingTime);
		m.getComponent().synchronisedAdvancedTime(walkingTime, e, human);
		
	}

}
