package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanArrivesHomeEvent extends AbstractSimEventDelegator<Human>{

	protected HumanArrivesHomeEvent(ISimulationModel model, String name) {
		super(model, name);

	}

	@Override
	public void eventRoutine(Human human) {
		human.arriveAtHome();
//		Utils.log(human, human.getName() + " arrives at home. Afterwork Party!");
		HumanLivingHisLifeEvent e = new HumanLivingHisLifeEvent(human.getModel(), "Human is living his life");
		e.schedule(human, 0);
	}
}
