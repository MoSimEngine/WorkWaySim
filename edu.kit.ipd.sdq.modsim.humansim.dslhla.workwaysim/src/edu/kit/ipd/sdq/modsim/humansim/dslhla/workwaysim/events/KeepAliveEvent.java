package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.RTIinternalError;

public class KeepAliveEvent extends AbstractSimEventDelegator<Human>{

	protected KeepAliveEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		if(m.hasToKeepAlive){
			KeepAliveEvent e = new KeepAliveEvent(getModel(), getName());
			try {
				
				m.getComponent().modifyLookahead(1);
				m.getComponent().getRTIAmb().evokeCallback(0.5);
			} catch (CallNotAllowedFromWithinCallback | RTIinternalError e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.schedule(human, 0.0);
		}
	}

}
