package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import java.util.Random;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class DrivingTimeoOutEvent extends AbstractSimEventDelegator<Human> {

	protected DrivingTimeoOutEvent(ISimulationModel model, String name) {
		super(model, name);
	}

	@Override
	public void eventRoutine(Human human) {
		
		WorkwayModel m = (WorkwayModel)this.getModel();
		boolean panik = false;
				
		if(human.isCollected()){
			
			//TODO:Include here a stochastical assertion of panik
			
			
			if(panik){
				Utils.log(human, "Human is still in the bus! Panik!!!!!!");
			} else {
				DrivingTimeoOutEvent e = new DrivingTimeoOutEvent(getModel(), getName());
				m.getComponent().synchronisedAdvancedTime(Duration.minutes(20).toSeconds().value(), e, human);
			}
		}
		
	}

}
