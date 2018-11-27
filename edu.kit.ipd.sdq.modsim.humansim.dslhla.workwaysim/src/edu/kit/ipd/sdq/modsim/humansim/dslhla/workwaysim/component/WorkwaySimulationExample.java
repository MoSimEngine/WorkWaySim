package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.Configurator;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration.TimeUnit;



public class WorkwaySimulationExample implements IApplication {

	private WorkwaySimConfig config;
	private WorkwayModel model;
	private ISimulationControl simControl;
	private WorkwayFederate component;
	
	public static Duration MAX_SIMULATION_TIME = HumanSimValues.MAX_SIM_TIME;
	
	
	public WorkwaySimulationExample() {
		
			this.config = new WorkwaySimConfig();
			this.model = WorkwayModel.create(config);
			this.simControl = model.getSimulationControl();
			if(HumanSimValues.WORKLOAD_OPEN) {
				MAX_SIMULATION_TIME = Duration.hours(Double.MAX_VALUE);
			}
			this.simControl.setMaxSimTime((long) MAX_SIMULATION_TIME.toSeconds().value());
			this.component = new WorkwayFederate(model);
			this.model.setComponent(component);
	}
	public synchronized Object start(IApplicationContext context) throws Exception {
		
		BasicConfigurator.configure();
	
        // run the simulation
        model.getSimulationControl().start();

        return EXIT_OK;
	}


	public void stop() {
		// nothing to do;
	}

}
