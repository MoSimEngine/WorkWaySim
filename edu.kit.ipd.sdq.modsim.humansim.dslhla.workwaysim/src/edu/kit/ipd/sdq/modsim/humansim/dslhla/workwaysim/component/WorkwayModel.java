package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimulationModel;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimEngineFactory;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationConfig;
import de.uka.ipd.sdq.simulation.preferences.SimulationPreferencesHelper;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.HumanEntersBusEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.HumanExitsBusEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.TravelToNextEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.CSVHandler;
import hla.rti1516e.exceptions.RTIexception;


public class WorkwayModel extends AbstractSimulationModel implements Runnable {

	private LinkedList<BusStop> stops;

	private double startTime;

	public int modelRun;
	public LinkedList<Double> durations;
	private LinkedList<Human> humans;
	private WorkwayFederate component;

	private LinkedList<WorkwayModel> models;
	private Human human;
	private int id;

	private boolean finished = false;

	public WorkwayModel(ISimulationConfig config, ISimEngineFactory factory) {
		super(config, factory);
		humans = new LinkedList<Human>();
		stops = new LinkedList<BusStop>();
	}

	public void init() {

		startTime = System.nanoTime();

		try {
			component.runFederate("WorkwayFed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void finalise() {

		component.resignFromExecution();

		finished = true;

		int countFinished = 0;

			Double finalTime = (System.nanoTime() - startTime) / Math.pow(10, 9);
			System.out.println("Time taken");
			String file_header = "";
			String csvAway = "";
			String csvWaitingAtStation = "";
			String csvDrivingTimes = "";
			String behaviourMarker = "";
			String csvFreeTimes = "";

			int getMaxNumValues = 0;

			for (WorkwayModel model : models) {
				Human human = model.getHuman();
				file_header += human.getName() + CSVHandler.CSV_DELIMITER;
				behaviourMarker += human.getBehaviour().toString() + CSVHandler.CSV_DELIMITER;
				System.out.println("Human " + human.getName() + " is in State " + human.getState() + " and is "
						+ human.getBehaviour().toString());
				if (getMaxNumValues < human.getAwayFromHomeTimes().size()) {
					getMaxNumValues = human.getAwayFromHomeTimes().size();
				}
			}

			file_header += CSVHandler.NEWLINE;
			behaviourMarker += CSVHandler.NEWLINE;

			for (int i = 0; i < getMaxNumValues; i++) {

				for (int j = 0; j < models.size(); j++) {

					Human human = models.get(j).getHuman();
					ArrayList<Duration> away = human.getAwayFromHomeTimes();
					ArrayList<Duration> driven = human.getDrivingTimes();
					ArrayList<Duration> waited = human.getBusWaitingTimes();
					ArrayList<Duration> free = human.getFreeTimes();

					if (away.size() > i) {
						csvAway += away.get(i).toSeconds().value();
						csvDrivingTimes += driven.get(i).toSeconds().value();
						csvWaitingAtStation += waited.get(i).toSeconds().value();
						csvFreeTimes += free.get(i).toSeconds().value();
					} else {
						csvAway += "-1";
						csvDrivingTimes += "-1";
						csvWaitingAtStation += "-1";
						csvFreeTimes += "-1";
					}

					if (j < models.size() - 1) {
						csvAway += CSVHandler.CSV_DELIMITER;
						csvDrivingTimes += CSVHandler.CSV_DELIMITER;
						csvWaitingAtStation += CSVHandler.CSV_DELIMITER;
						csvFreeTimes += CSVHandler.CSV_DELIMITER;
					}

				}

				csvAway += CSVHandler.NEWLINE;
				csvDrivingTimes += CSVHandler.NEWLINE;
				csvWaitingAtStation += CSVHandler.NEWLINE;
				csvFreeTimes += CSVHandler.NEWLINE;
			}

			String[] csvs = { csvAway, csvDrivingTimes, csvWaitingAtStation, csvFreeTimes };

			for (int i = 0; i < csvs.length; i++) {
				String s = "";

				switch (i) {
				case 0:
					s = "AwayTimes";
					break;
				case 1:
					s = "DrivingTimes";
					break;
				case 2:
					s = "BusStationWaitingTimes";
					break;
				case 3:
					s = "FreeTimes";
					break;

				default:
					throw new IllegalStateException("More than expected files");
				}

				csvs[i] = csvs[i].replace('.', ',');

				CSVHandler.writeCSVFile(s, file_header + csvs[i]);

			}

			CSVHandler.writeCSVFile("HumanBehaviour", behaviourMarker);

			Double d = Math.round(finalTime * 100.00) / 100.00;
			String s = d.toString();

			s = s.replace('.', ',');

			CSVHandler.readCSVAndAppend("ExecutionTimes", s + CSVHandler.CSV_DELIMITER);

			System.out.println("Done writing data");

			component.destroyExecution();

		
		System.out.println("Finalized in " + getId());

	}

	/**
	 * Creates the simulation model for the specified configuration.
	 * 
	 * @param config the simulation configuration
	 * @return the created simulation model
	 */
	public static WorkwayModel create(final WorkwaySimConfig config) {
		// load factory for the preferred simulation engine
		ISimEngineFactory factory = SimulationPreferencesHelper.getPreferredSimulationEngine();
		if (factory == null) {
			throw new RuntimeException("There is no simulation engine available. Install at least one engine.");
		}

		// create and return simulation model
		final WorkwayModel model = new WorkwayModel(config, factory);

		return model;
	}

	public void startSimulation() {
		
		for (Human human : humans) {
			new TravelToNextEvent(this, human.getName() + "starts travelling").schedule(human,
					component.getCurrentFedTime());
		}
	}

	public WorkwayFederate getComponent() {
		return component;
	}

	public void setComponent(WorkwayFederate component) {
		this.component = component;
	}

	public LinkedList<BusStop> getStops() {
		return stops;
	}

	public void setStops(LinkedList<BusStop> stops) {
		this.stops = stops;
	}

	public LinkedList<Human> getHumans() {
		return humans;
	}

	public void addBusStop(BusStop stop) {

		for (BusStop bs : stops) {
			if (stop.getOih().equals(bs.getOih())) {
				System.out.println("Stop already there");
				return;
			}

		}
		stops.add(stop);

	}

	public void scheduleHumanEntersEvent(String humanName, String busStopName, double passedTime) {

		if (human.getName().equals(humanName)) {
			for (BusStop busStop : stops) {
				if (busStop.getName().equals(busStopName)) {
					HumanEntersBusEvent e = new HumanEntersBusEvent(this, "HumanEntersBus");

					BigDecimal st = BigDecimal.valueOf(getSimulationControl().getCurrentSimulationTime());
					BigDecimal pt = BigDecimal.valueOf(passedTime);
					BigDecimal timeDiff = pt.subtract(st);

					e.schedule(human, timeDiff.doubleValue());
					return;
				}
			}
		}
	}

	public void scheduleHumanExitsEvent(String humanName, String busStopName, double passedTime) {

		if (human.getName().equals(humanName)) {
			for (BusStop busStop : stops) {
				if (busStop.getName().equals(busStopName)) {
					BigDecimal st = BigDecimal.valueOf(getSimulationControl().getCurrentSimulationTime());
					BigDecimal pt = BigDecimal.valueOf(passedTime);
					BigDecimal timeDiff = pt.subtract(st);

					HumanExitsBusEvent e = new HumanExitsBusEvent(this, "HumanExitsBus");
					e.schedule(human, timeDiff.doubleValue());
					return;
				}
			}

		}
	}

	public void initialiseHumans() {
		BusStop[] tmpStops = new BusStop[3];

		for (BusStop stop : stops) {

			switch (stop.getName()) {
			case "Stop1":
				tmpStops[0] = stop;
				break;
			case "Stop2":
				tmpStops[1] = stop;
				break;
			case "Stop3":
				tmpStops[2] = stop;
				break;
			default:
				break;
			}

		}

		stops.clear();
		stops.add(tmpStops[0]);
		stops.add(tmpStops[1]);
		stops.add(tmpStops[2]);

		// new HumanProcess(new Human(stop1, stop3, this, "Bob" + i),
		// bus).scheduleAt(0);
		int homeBS = 0;
		int workBS = 0;

		for(int i = 0; i < HumanSimValues.NUM_HUMANS; i++){
		
		if (HumanSimValues.STOCHASTIC) {
			while (homeBS == workBS) {
				homeBS = new Random().nextInt(HumanSimValues.NUM_BUSSTOPS);
				workBS = new Random().nextInt(HumanSimValues.NUM_BUSSTOPS);
			}
		} else {
			homeBS = this.id % 2;
			workBS = (this.id % 2) + 1;
		}

		humans.add(new Human(stops.get(homeBS), stops.get(workBS), this, "Bob" + i));
		}
	}

	@Override
	public void run() {
		getSimulationControl().start();
	}

	public LinkedList<WorkwayModel> getModels() {
		return models;
	}

	public void setModels(LinkedList<WorkwayModel> models) {
		this.models = models;
	}

	public Human getHuman() {
		return human;
	}

	public void setHuman(Human human) {
		this.human = human;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void registerHumanAtBusStop(Human human, BusStop busStop, BusStop destination) {
		try {
			this.component.sendRegisterInteraction(human, busStop.getName(), destination.getName());
		} catch (RTIexception e) {
			e.printStackTrace();
		}
	}

}
