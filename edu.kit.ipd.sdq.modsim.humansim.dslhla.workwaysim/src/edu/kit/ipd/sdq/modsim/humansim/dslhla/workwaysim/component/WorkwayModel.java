package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import javax.rmi.CORBA.Util;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimulationModel;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimEngineFactory;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationConfig;
import de.uka.ipd.sdq.simulation.preferences.SimulationPreferencesHelper;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human.HumanBehaviour;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position.PositionType;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.HumanEntersBusEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.HumanExitsBusEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.TravelToNextEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.RTITimelineSynchronizer;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.TimeAdvanceSynchronisationEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.CSVHandler;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.exceptions.RTIexception;

public class WorkwayModel extends AbstractSimulationModel{

	private LinkedList<BusStop> stops;

	private double startTime;

	private LinkedList<Human> humans;
	private WorkwayFederate component;

	private RTITimelineSynchronizer timelineSynchronizer;

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
			e.printStackTrace();
		}

	}

	public void finalise() {

		component.resignFromExecution();
		Double finalTime = (System.nanoTime() - startTime) / Math.pow(10, 9);
		String file_header = "";
		String csvAway = "";
		String csvWaitingAtStation = "";
		String csvDrivingTimes = "";
		String behaviourMarker = "";
		String csvFreeTimes = "";

		int getMaxNumValues = 0;

		for (Human h : humans) {

			file_header += h.getName() + CSVHandler.CSV_DELIMITER;
			behaviourMarker += h.getBehaviour().toString() + CSVHandler.CSV_DELIMITER;
			System.out.println("Human " + h.getName() + " is in State " + h.getState() + " and is "
					+ h.getBehaviour().toString()+ "starting from: " + h.getWorkway().get(2).getName());
			if (getMaxNumValues < h.getAwayFromHomeTimes().size()) {
				getMaxNumValues = h.getAwayFromHomeTimes().size();
			}
		}

		file_header += CSVHandler.NEWLINE;
		behaviourMarker += CSVHandler.NEWLINE;

		for (int i = 0; i < getMaxNumValues; i++) {

			for (int j = 0; j < humans.size(); j++) {

				Human human = humans.get(j);
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

				if (j < humans.size() - 1) {
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

		String[] csvs = {csvAway, csvDrivingTimes, csvWaitingAtStation, csvFreeTimes};

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
		component.destroyExecution();

		System.out.println("Finalized");

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

		this.timelineSynchronizer = new RTITimelineSynchronizer(this);

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
		
		for (Human human : humans) {
			if (human.getName().equals(humanName)) {
				for (BusStop busStop : stops) {
					if (busStop.getName().equals(busStopName)) {
						
						BigDecimal st = BigDecimal.valueOf(getSimulationControl().getCurrentSimulationTime());
						BigDecimal pt = BigDecimal.valueOf(passedTime);
						BigDecimal timeDiff = pt.subtract(st);
						
//						Utils.log(human, "Received EntersEvent for " + passedTime);
						
						HumanEntersBusEvent e = new HumanEntersBusEvent(this, "HumanEntersBus");
						TimeAdvanceToken ta = new TimeAdvanceToken(e, human, timeDiff.doubleValue());
						((WorkwayModel)human.getModel()).getTimelineSynchronizer().putToken(ta, true);
						return;
					}
				}
			}
		}
	}

	public void scheduleHumanExitsEvent(String humanName, String busStopName, double passedTime) {
		
		for (Human human : humans) {
			if (human.getName().equals(humanName)) {
				for (BusStop busStop : stops) {
					if (busStop.getName().equals(busStopName)) {
						
						BigDecimal st = BigDecimal.valueOf(getSimulationControl().getCurrentSimulationTime());
						BigDecimal pt = BigDecimal.valueOf(passedTime);
						BigDecimal timeDiff = pt.subtract(st);
						
//						Utils.log(human, "Received ExitsEvent for " + passedTime);
						
						HumanExitsBusEvent e = new HumanExitsBusEvent(this, "HumanExitsBus");
						TimeAdvanceToken ta = new TimeAdvanceToken(e, human, timeDiff.doubleValue());
						((WorkwayModel)human.getModel()).getTimelineSynchronizer().putToken(ta, true);
						return;
					}
				}
			}
		}
		
	}

	public void initialiseHumans() {
		
	
		
		Collections.sort(stops);
		
		ArrayList<ArrayList<Position>> routes = getRoutes();
		
		int homeBS = 0;
		int workBS = 0;

		for (int i = 0; i < HumanSimValues.NUM_HUMANS; i++) {
			ArrayList<Position> usedRoute = new ArrayList<Position>();
			if (HumanSimValues.STOCHASTIC) {
				
				
				homeBS = new Random().nextInt(HumanSimValues.NUM_BUSSTOPS);
				if(homeBS % 2 == 0) {
					workBS = homeBS + 1;
				} else {
					workBS = homeBS - 1;
				}
				
			} else {
				
				usedRoute = routes.get(i%3);
				
				
				
//				int route = i % 2;
//				
//				
//				homeBS = route * 3;
//				workBS = (route * 3) + 1;
			}

			humans.add(new Human(usedRoute, this, "Hugo" + i));
		}
	}

	public void registerHumanAtBusStop(Human human, BusStop busStop, BusStop destination, double timestep) {
		try {
			this.component.sendRegisterInteraction(human, busStop.getName(), destination.getName(), timestep);
		} catch (RTIexception e) {
			e.printStackTrace();
		}
	}

	public RTITimelineSynchronizer getTimelineSynchronizer() {
		return timelineSynchronizer;
	}
	
	
	public ArrayList<ArrayList<Position>> getRoutes(){
		ArrayList<ArrayList<Position>> routes = new ArrayList<ArrayList<Position>>();
		Position home = new Position(this, "Home", PositionType.HOME);
		Position work = new Position(this, "Work", PositionType.WORK);
		
		ArrayList<Position> routeOne = new ArrayList<Position>();
		routeOne.add(home);
		routeOne.add(stops.get(0));
		routeOne.add(stops.get(1));
		routeOne.add(work);
		
		ArrayList<Position> routeTwo = new ArrayList<Position>();
		routeTwo.add(home);
		routeTwo.add(stops.get(2));
		routeTwo.add(stops.get(3));
		routeTwo.add(work);

		ArrayList<Position> routeThree = new ArrayList<Position>();
		routeThree.add(home);
		routeThree.add(stops.get(4));
		routeThree.add(stops.get(5));
		routeThree.add(work);
	

		routes.add(routeOne);
		routes.add(routeTwo);
		routes.add(routeThree);
		
		return routes;
		
	}

}
