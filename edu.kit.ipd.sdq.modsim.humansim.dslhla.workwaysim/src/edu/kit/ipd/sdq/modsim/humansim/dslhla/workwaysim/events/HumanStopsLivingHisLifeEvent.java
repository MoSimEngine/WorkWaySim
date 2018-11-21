package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanStopsLivingHisLifeEvent extends AbstractSimEventDelegator<Human>{

	protected HumanStopsLivingHisLifeEvent(ISimulationModel model, String name) {
		super(model, name);
	}

	@Override
	public void eventRoutine(Human human) {
//		Utils.log(human, "Oh boy, time flies by... " + human.getName() + " stops living his life.");
		TravelToNextEvent e = new TravelToNextEvent(getModel(), "Travel from Home");
		e.schedule(human, 0);
	}
}
