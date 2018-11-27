package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Token;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.RTIinternalError;

public class HumanExitsBusEvent extends AbstractSimEventDelegator<Token>{

	public HumanExitsBusEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Token human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
	
//		Utils.log(human, human.getName() + " left queue at " + human.getDestination().getName());
		human.calculateTimeProcessed();
		human.calculateActiveTime();
		human.setCollected(false);
		
		
		if(HumanSimValues.WORKLOAD_OPEN) {
			m.tokenProcessed();
			m.getTimelineSynchronizer().checkAndExecute();
		} else {
			if(human.getTaToken() != null) {
				m.getTimelineSynchronizer().revokeToken(human.getTaToken());
				}
				
				ArriveAtNextEvent e = new ArriveAtNextEvent(getModel(), "ArriveAtHomeByBusWaiting");
				e.schedule(human, 0);
		}

	}
}
