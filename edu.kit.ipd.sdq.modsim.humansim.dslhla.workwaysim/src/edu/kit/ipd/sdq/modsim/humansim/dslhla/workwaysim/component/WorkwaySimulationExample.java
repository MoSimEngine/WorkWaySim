package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;


public class WorkwaySimulationExample implements IApplication {

	private WorkwaySimConfig config;
	private WorkwayModel model;
	private ISimulationControl simControl;
	private WorkwayFederate component;
	
	 private static final Duration MAX_SIMULATION_TIME = HumanSimValues.MAX_SIM_TIME;
	
	
	public WorkwaySimulationExample() {
		this.config = new WorkwaySimConfig();
		this.model = WorkwayModel.create(config);
		this.simControl = model.getSimulationControl();
		this.simControl.setMaxSimTime((long) MAX_SIMULATION_TIME.toSeconds().value());
		this.component = new WorkwayFederate(model);
		this.model.setComponent(component);
	}
	public Object start(IApplicationContext context) throws Exception {
		
		BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);

        // run the simulation
        model.getSimulationControl().start();

        return EXIT_OK;
	}


	public void stop() {
		// nothing to do;
	}

}
