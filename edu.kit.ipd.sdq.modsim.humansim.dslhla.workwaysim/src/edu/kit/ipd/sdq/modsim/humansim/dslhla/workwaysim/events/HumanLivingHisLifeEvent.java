package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanLivingHisLifeEvent extends AbstractSimEventDelegator<Human>{

	protected HumanLivingHisLifeEvent(ISimulationModel model, String name) {
		super(model, name);

	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		Utils.log(human, human.getName() + " lives his life. Black Jack and Hookers baby.");
		human.calculateFreeTime();
		double livingHisLife = human.FREETIME.toSeconds().value();
		
		HumanStopsLivingHisLifeEvent e = new HumanStopsLivingHisLifeEvent(human.getModel(), "Human stops living his life");
//		
		if(HumanSimValues.FULL_SYNC) {
			m.getComponent().synchronisedAdvancedTime(livingHisLife, e, human);
		} else {
			e.schedule(human, livingHisLife);
		}
		
		
	}

}
