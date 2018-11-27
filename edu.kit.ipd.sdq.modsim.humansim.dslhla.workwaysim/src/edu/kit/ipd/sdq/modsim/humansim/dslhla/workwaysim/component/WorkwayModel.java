package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.rmi.CORBA.Util;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimulationModel;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimEngineFactory;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationConfig;
import de.uka.ipd.sdq.simulation.preferences.SimulationPreferencesHelper;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Queue;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Token;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position;

import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position.PositionType;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.HumanEntersBusEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.HumanExitsBusEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.TravelToNextEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.WorkloadGenerationEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.RTITimelineSynchronizer;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.TimeAdvanceSynchronisationEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.CSVHandler;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.exceptions.RTIexception;

public class WorkwayModel extends AbstractSimulationModel {

	private double startTime;
	private LinkedList<Token> tokens;
	private LinkedList<Queue> queues;

	private WorkwayFederate component;
	private int processedTokens = 0;
	private RTITimelineSynchronizer timelineSynchronizer;

	private int receivedEventCounter = 0;
	private int sendEventCounter = 0;

	public WorkwayModel(ISimulationConfig config, ISimEngineFactory factory) {
		super(config, factory);
		tokens = new LinkedList<Token>();
		queues = new LinkedList<Queue>();
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
		String fileHeader = "";
		String csvActive = "";
		String csvEnqueued = "";
		String csvProcessed = "";

		for (int j = 0; j < tokens.size(); j++) {

			Token human = tokens.get(j);

			if (HumanSimValues.WORKLOAD_OPEN) {
				double active = human.calculatePositiveMean(human.getActiveTimes());
				double processing = human.calculatePositiveMean(human.getProcessedTimes());
				double enqueued = human.calculatePositiveMean(human.getEnqueuedTimes());

				fileHeader += human.getName();
				csvActive += active;
				csvProcessed += processing;
				csvEnqueued += enqueued;

			} else {
				LinkedList<Duration> active = human.getActiveTimes();
				LinkedList<Duration> processing = human.getProcessedTimes();
				LinkedList<Duration> enqueued = human.getEnqueuedTimes();

				if (active.size() == 0) {
					fileHeader += human.getName();
					csvActive += ("" + -1);
					csvEnqueued += ("" + -1);
					csvProcessed += ("" + -1);

				} else {

					for (int i = 0; i < active.size(); i++) {
						fileHeader += human.getName();
						csvActive += active.get(i).toSeconds().value();
						csvProcessed += processing.get(i).toSeconds().value();
						csvEnqueued += enqueued.get(i).toSeconds().value();

						if (i < active.size() - 1) {
							fileHeader += CSVHandler.CSV_DELIMITER;
							csvActive += CSVHandler.CSV_DELIMITER;
							csvProcessed += CSVHandler.CSV_DELIMITER;
							csvEnqueued += CSVHandler.CSV_DELIMITER;
						}
					}
				}
			}

			if (j < tokens.size() - 1) {
				fileHeader += CSVHandler.CSV_DELIMITER;
				csvActive += CSVHandler.CSV_DELIMITER;
				csvProcessed += CSVHandler.CSV_DELIMITER;
				csvEnqueued += CSVHandler.CSV_DELIMITER;
			}

		}


		fileHeader += CSVHandler.NEWLINE;

		CSVHandler.writeCSVFile("ResponseTimes", fileHeader + csvActive.replace('.', ','));
		CSVHandler.writeCSVFile("ProcessingTimes", fileHeader + csvProcessed.replace('.', ','));
		CSVHandler.writeCSVFile("WaitingTimes", fileHeader + csvEnqueued.replace('.', ','));

		Double d = BigDecimal.valueOf(finalTime).round(new MathContext(2, RoundingMode.CEILING)).doubleValue();
		String s = d.toString();
		
		
		String countHeader = "TimeAdvances" + CSVHandler.CSV_DELIMITER +
				"SentEvents" + CSVHandler.CSV_DELIMITER +
				"ReceivedEvents" + CSVHandler.NEWLINE;
		
		String counts = component.getTimeAdvanceCounter() + CSVHandler.CSV_DELIMITER + 
				sendEventCounter + CSVHandler.CSV_DELIMITER + 
				receivedEventCounter + CSVHandler.NEWLINE;
				
		

		s = s.replace('.', ',');

		CSVHandler.readCSVAndAppendHeaderDependent("ExecutionTimes", "ExecutionTime" + CSVHandler.NEWLINE, s);
		CSVHandler.readCSVAndAppendHeaderDependent("CommunicationCounts", countHeader, counts);

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
		List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
		loggers.add(LogManager.getRootLogger());
		for (Logger logger : loggers) {
			logger.setLevel(Level.OFF);
		}
		System.out.println("Start simulation at " + component.getCurrentFedTime());

		if (HumanSimValues.WORKLOAD_OPEN) {

			Utils.log("Starting to generate open workload for #tokens " + tokens.size());
			Token dummy = new Token(null, null, this, "DummyToken");
			new WorkloadGenerationEvent(this, "WorkloadGeneration").schedule(dummy,
					component.getCurrentFedTime() + HumanSimValues.interarrivalTime.toSeconds().value());
		} else {

			Utils.log("Starting to generate closed workload for #tokens " + tokens.size());

			for (Token human : tokens) {
				new TravelToNextEvent(this, human.getName() + "travels to queue").schedule(human,
						human.getEnqueuingDelayInSeconds() + component.getCurrentFedTime());
			}
		}

	}

	public WorkwayFederate getComponent() {
		return component;
	}

	public void setComponent(WorkwayFederate component) {
		this.component = component;
	}

	public LinkedList<Queue> getQueues() {
		return queues;
	}

	public void setQueues(LinkedList<Queue> queues) {
		this.queues = queues;
	}

	public LinkedList<Token> getTokens() {
		return tokens;
	}

	public void addBusStop(Queue stop) {
		for (Queue bs : queues) {
			if (stop.getOih().equals(bs.getOih())) {
				System.out.println("Stop already there");
				return;
			}
		}
		queues.add(stop);
	}

	public void scheduleHumanEntersEvent(String humanName, String busStopName, double passedTime) {
		incrementReceivedEventCounter();
		for (Token human : tokens) {
			if (human.getName().equals(humanName)) {
				for (Queue busStop : queues) {
					if (busStop.getName().equals(busStopName)) {

						BigDecimal st = BigDecimal.valueOf(getSimulationControl().getCurrentSimulationTime());
						BigDecimal pt = BigDecimal.valueOf(passedTime);
						BigDecimal timeDiff = pt.subtract(st);

//						Utils.log(human, "Received EntersEvent for " + passedTime);

						HumanEntersBusEvent e = new HumanEntersBusEvent(this, "HumanEntersBus");
						TimeAdvanceToken ta = new TimeAdvanceToken(e, human, timeDiff.doubleValue());
						((WorkwayModel) human.getModel()).getTimelineSynchronizer().putToken(ta, true);

				

						return;
					}
				}
			}
		}
	}

	public void scheduleHumanExitsEvent(String humanName, String busStopName, double passedTime) {
		incrementReceivedEventCounter();
		for (Token human : tokens) {
			if (human.getName().equals(humanName)) {
				for (Queue busStop : queues) {
					if (busStop.getName().equals(busStopName)) {

						BigDecimal st = BigDecimal.valueOf(getSimulationControl().getCurrentSimulationTime());
						BigDecimal pt = BigDecimal.valueOf(passedTime);
						BigDecimal timeDiff = pt.subtract(st);

//						Utils.log(human, "Received ExitsEvent for " + passedTime);

						HumanExitsBusEvent e = new HumanExitsBusEvent(this, "HumanExitsBus");
						TimeAdvanceToken ta = new TimeAdvanceToken(e, human, timeDiff.doubleValue());
						((WorkwayModel) human.getModel()).getTimelineSynchronizer().putToken(ta, true);

					
						return;
					}
				}
			}
		}

	}

	public void initialiseHumans() {

		Collections.sort(queues);

		ArrayList<ArrayList<Position>> routes = getRoutes();

		if (!HumanSimValues.WORKLOAD_OPEN) {
			// schedule a process for each human
			for (int i = 0; i < HumanSimValues.NUM_HUMANS; i++) {
				ArrayList<Position> usedRoute = new ArrayList<Position>();
				int homeBS = 0;
				int workBS = 0;

				if (HumanSimValues.STOCHASTIC) {
					homeBS = 0;
					workBS = homeBS + 1;

					usedRoute.add(queues.get(homeBS));
					usedRoute.add(queues.get(workBS));
				} else {
					usedRoute = routes.get(0);
				}

				Token hu = new Token(usedRoute, this, "Hugo" + i);
				tokens.add(hu);

			}
		}
	}

	public void registerHumanAtBusStop(Token human, Queue busStop, Queue destination, double timestep) {
		try {
			this.component.sendRegisterInteraction(human, busStop.getName(), destination.getName(), timestep);
		} catch (RTIexception e) {
			e.printStackTrace();
		}
	}

	public RTITimelineSynchronizer getTimelineSynchronizer() {
		return timelineSynchronizer;
	}

	public ArrayList<ArrayList<Position>> getRoutes() {
		ArrayList<ArrayList<Position>> routes = new ArrayList<ArrayList<Position>>();

		ArrayList<Position> routeOne = new ArrayList<Position>();

		routeOne.add(queues.get(0));
		routeOne.add(queues.get(1));

//		ArrayList<Position> routeTwo = new ArrayList<Position>();
//
//		routeTwo.add(queues.get(2));
//		routeTwo.add(queues.get(3));
//
//		ArrayList<Position> routeThree = new ArrayList<Position>();
//
//		routeThree.add(queues.get(4));
//		routeThree.add(queues.get(5));
//
		routes.add(routeOne);
//		routes.add(routeTwo);
//		routes.add(routeThree);

		return routes;
	}

	public void cutOff20PercentValues(LinkedList<Duration> list) {

		BigDecimal cutOffPos = BigDecimal.valueOf(list.size()).multiply(BigDecimal.valueOf(0.20),
				new MathContext(0, RoundingMode.CEILING));

		for (int i = 0; i < cutOffPos.intValue(); i++) {
			list.pop();
		}

	}

	public Token generateToken() {

		ArrayList<Position> usedRoute = new ArrayList<Position>();
		int homeBS = 0;
		int workBS = 0;

		if (HumanSimValues.STOCHASTIC) {
			homeBS = 0;
			workBS = homeBS + 1;

			usedRoute.add(queues.get(homeBS));
			usedRoute.add(queues.get(workBS));
		} else {
			usedRoute = getRoutes().get(0);
		}

		Token hu = new Token(usedRoute, this, "Hugo" + tokens.size());
		tokens.add(hu);

		new TravelToNextEvent(this, hu.getName() + "travels to queue").schedule(hu, 0);

		return hu;
	}

	public void tokenProcessed() {
		processedTokens++;

		if (processedTokens == HumanSimValues.NUM_HUMANS) {
			getSimulationControl().stop();
		}
	}

	public int getProcessedTokens() {
		return this.processedTokens;
	}

	public int calculateRunningTokens() {
		return tokens.size() - processedTokens;
	}

	public int getTokensCount() {
		return tokens.size();
	}

	public void incrementSendEventCounter() {
		sendEventCounter++;
	}

	public void incrementReceivedEventCounter() {
		receivedEventCounter++;
	}

}
