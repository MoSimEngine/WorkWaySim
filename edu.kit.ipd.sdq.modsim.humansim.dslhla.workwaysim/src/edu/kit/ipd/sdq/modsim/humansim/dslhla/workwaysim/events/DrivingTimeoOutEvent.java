package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class DrivingTimeoOutEvent extends AbstractSimEventDelegator<Human> {

	protected DrivingTimeoOutEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		if(human.isCollected()){
			boolean panik = false;
			
			if(panik){
				Utils.log(human, "Human is still in the bus! Panik!!!!!!");
			} else {
				Utils.log(human, "Human waits again");
				DrivingTimeoOutEvent e = new DrivingTimeoOutEvent(getModel(), getName());
				m.getComponent().synchronisedAdvancedTime(Duration.minutes(20).toSeconds().value(), e, human);
			}
		}
		
	}

}
