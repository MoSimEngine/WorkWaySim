package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.SynchroniseToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;

public class Token extends AbstractSimEntityDelegator {

	public static enum TokenState {
		ENQUEUING, 
		ENQUEUED, 
		IN_PROCESSING
	}

	private TokenState state;
	private Position position;
	private Position destination;
	private volatile boolean collected;
	private Duration enqueuingDelay = Duration.minutes(21).toSeconds();
	private int numTimesProcessed = 0;
	private ArrayList<Position> workway = new ArrayList<Position>();
	private int positionIndex = 1;
	// Regulates the index direction for traversal of the workway list
	// Values: 1 -> forward; -1 -> backward;
	private int direction = (-1);

	private SynchroniseToken currentTAToken;
	private LinkedList<SynchroniseToken> regTokens;
	private int taTokenIndex = -1;

	private ObjectInstanceHandle oih;
	private ObjectClassHandle och;

	private LinkedList<Duration> activeTimes;
	private LinkedList<Duration> enqueuedTimes;
	private LinkedList<Duration> processedTimes;
	private Duration enqueuedTime = Duration.seconds(0);
	private Duration timeProcessed = Duration.seconds(0);
	private double timePointEnqueued = 0.0;
	private double timePointProcessing = 0.0;

	public Token(Queue home, Queue work, ISimulationModel model, String name) {
		super(model, name);

		state = TokenState.IN_PROCESSING;

		workway.add(home);
		workway.add(work);

		position = workway.get(positionIndex);
		destination = workway.get(positionIndex - 1);

//		if (HumanSimValues.STOCHASTIC) {
//			enqueuingDelay = Duration.minutes(0);
//		} else {
//			enqueuingDelay = Duration.minutes(0);
//		}

		activeTimes = new LinkedList<Duration>();
		enqueuedTimes = new LinkedList<Duration>();
		processedTimes = new LinkedList<Duration>();
		regTokens = new LinkedList<SynchroniseToken>();
	}

	public Token(Queue home, Queue work, ISimulationModel model, String name, ObjectInstanceHandle oih,
			ObjectClassHandle och) {
		super(model, name);

		state = TokenState.IN_PROCESSING;

		workway.add(home);
		workway.add(work);

		position = workway.get(positionIndex);
		destination = workway.get(positionIndex - 1);

//		if (HumanSimValues.STOCHASTIC) {
//			enqueuingDelay = Duration.minutes(0);
//		} else {
//			enqueuingDelay = Duration.minutes(0);
//		}

		activeTimes = new LinkedList<Duration>();
		enqueuedTimes = new LinkedList<Duration>();
		processedTimes = new LinkedList<Duration>();
		regTokens = new LinkedList<SynchroniseToken>();

		this.oih = oih;
		this.och = och;

	}

	public Token(ArrayList<Position> route, ISimulationModel model, String name) {
		super(model, name);

		// start at home
		state = TokenState.IN_PROCESSING;

		this.workway = route;

		position = workway.get(positionIndex);
		destination = workway.get(positionIndex - 1);

//		if (HumanSimValues.STOCHASTIC) {
//			enqueuingDelay = Duration.minutes(0);
//		} else {
//			enqueuingDelay = Duration.minutes(0);
//		}

		activeTimes = new LinkedList<Duration>();
		enqueuedTimes = new LinkedList<Duration>();
		processedTimes = new LinkedList<Duration>();
		regTokens = new LinkedList<SynchroniseToken>();

	}

	//Token state changes
		public void enqueuedInQueue() {
			if(state.equals(TokenState.ENQUEUING)) {
				state = TokenState.ENQUEUED;
			} else {
				throw new IllegalStateException("There is no teleportation to a BusStop!" + "CurrentState: " + this.state.toString());
			}
		}
		
		public void processing() {
			if(state.equals(TokenState.ENQUEUED)) {
				state = TokenState.IN_PROCESSING;
			} else {
				throw new IllegalStateException("Human cannot walk!" + "CurrentState: " + this.state.toString());
			}
		}
		
		public void enqueueAgain() {
			if(state.equals(TokenState.IN_PROCESSING)) {
				state = TokenState.ENQUEUING;
			} else {
				throw new IllegalStateException("How to drive by bus when not at BusStop???" + "CurrentState: " + this.state.toString());
			}
		}

	

	public Position getPosition() {
		return this.position;
	}

	public Position getDestination() {
		return this.destination;
	}

	public boolean isCollected() {
		return collected;
	}

	public void setCollected(boolean collected) {
		this.collected = collected;
	}
	
	public TokenState getState() {
		return this.state;
	}
	
	public double getEnqueuingDelayInSeconds() {
		return enqueuingDelay.toSeconds().value();
	}

	public int getDirection() {
		return direction;
	}

	public LinkedList<Duration> getEnqueuedTimes(){
		return enqueuedTimes;
	}
	
	public LinkedList<Duration> getProcessedTimes(){
		return processedTimes;
	}
	
	public LinkedList<Duration> getActiveTimes(){
		return activeTimes;
	}
	
	public void tokenEqueued(){
		
		if (timePointEnqueued != 0.0)
			throw new IllegalStateException("time point arrived at bus stop was not zero");
		
		timePointEnqueued = getModel().getSimulationControl().getCurrentSimulationTime();
	}
	
	public void calculateTimeEnqueued(){
		enqueuedTime = Duration.seconds(enqueuedTime.toSeconds().value() + Duration.seconds(getModel().getSimulationControl().getCurrentSimulationTime() - timePointEnqueued).value());
		timePointEnqueued = 0.0;
	}
	
	public void tokenprocessingStarted(){
		if (timePointProcessing != 0.0)
			throw new IllegalStateException("time point arrived at bus stop was not zero, was:" + timePointProcessing);
		
		timePointProcessing = this.getModel().getSimulationControl().getCurrentSimulationTime();
	}
	
	public void calculateTimeProcessed(){
		numTimesProcessed++;
		timeProcessed = Duration.seconds(timeProcessed.toSeconds().value() + Duration.seconds(getModel().getSimulationControl().getCurrentSimulationTime() - timePointProcessing).value());
		timePointProcessing = 0.0;
	}
	
	public void calculateActiveTime(){

		enqueuedTimes.add(enqueuedTime);
		processedTimes.add(timeProcessed);
		activeTimes.add(Duration.seconds(enqueuedTime.toSeconds().value()+timeProcessed.toSeconds().value()));

		timeProcessed = Duration.seconds(0);
		timePointEnqueued = 0;
		timePointProcessing = 0;
		
		enqueuedTime = Duration.seconds(0);
	}

	public ObjectInstanceHandle getOih() {
		return oih;
	}

	public void setOih(ObjectInstanceHandle oih) {
		this.oih = oih;
	}

	public ObjectClassHandle getOch() {
		return och;
	}

	public void setOch(ObjectClassHandle och) {
		this.och = och;
	}

	public ArrayList<Position> getWorkway() {
		return workway;
	}

	public Position nextPosition() {

		this.position = this.destination;
		this.positionIndex += direction;

		if (positionIndex + direction >= workway.size()) {
			direction = (-1);
		} else if (positionIndex + direction < 0) {
			direction = 1;
		}

		this.destination = this.workway.get(positionIndex + direction);

		return this.position;

	}

	public SynchroniseToken getTaToken() {
		return currentTAToken;
	}

	public void setTaToken(SynchroniseToken token) {
		this.currentTAToken = token;
	}

	public LinkedList<SynchroniseToken> getRegTokens() {
		return regTokens;
	}

	public void addRegToken(SynchroniseToken regToken) {
		regTokens.add(regToken);
	}

	public void removeRegToken(SynchroniseToken regToken) {
		regTokens.remove(regToken);
	}

	public int getTaTokenIndex() {
		return taTokenIndex;
	}

	public void setTaTokenIndex(int taTokenIndex) {
		this.taTokenIndex = taTokenIndex;
	}
	
	public double calculatePositiveMean(LinkedList<Duration> durations) {
		if(durations.size() == 0)
			return -1;
		
		BigDecimal result = BigDecimal.ZERO;
		
		for (Duration duration : durations) {
			result = result.add(BigDecimal.valueOf(duration.toSeconds().value()));
		}
		
		result = result.divide(BigDecimal.valueOf(durations.size()), 0, BigDecimal.ROUND_CEILING);
		return result.doubleValue();
	}
}