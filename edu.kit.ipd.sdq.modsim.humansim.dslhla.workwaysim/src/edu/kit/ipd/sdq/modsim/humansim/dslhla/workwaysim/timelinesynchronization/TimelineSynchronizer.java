package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;

public interface TimelineSynchronizer {
	

	
	public boolean putToken(SynchroniseToken token);
	public void sortTokens();
	public void executeTimeorderedEvents();
	public void scheduleReturnEvent(SynchroniseToken token);
	public void handleEntityLeft(AbstractSimEntityDelegator entity);
	public boolean revokeToken(SynchroniseToken token);
	public void breakExecution(AbstractSimEntityDelegator entity);
	
}
