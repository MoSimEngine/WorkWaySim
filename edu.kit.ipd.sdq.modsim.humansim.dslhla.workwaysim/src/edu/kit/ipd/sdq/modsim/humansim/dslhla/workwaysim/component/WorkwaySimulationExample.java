package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import java.util.LinkedList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration.TimeUnit;


public class WorkwaySimulationExample implements IApplication {

	private WorkwaySimConfig config;
	private WorkwayModel model;
	private ISimulationControl simControl;
	private WorkwayFederate component;
	
	 private static final Duration MAX_SIMULATION_TIME = HumanSimValues.MAX_SIM_TIME;
	
	
	public WorkwaySimulationExample() {
		

			config = new WorkwaySimConfig();
			model = WorkwayModel.create(config);
			simControl = model.getSimulationControl();
			simControl.setMaxSimTime((long) MAX_SIMULATION_TIME.toSeconds().value());
			component = new WorkwayFederate(model);
			model.setComponent(component);
			model.setId(0);
		
	}
	public synchronized Object start(IApplicationContext context) throws Exception {
		
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
