package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Random;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.SimulationElement;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.adaption.ByteArrayToInteger32BEConversion;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.adaption.ByteArrayToStringConversion;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.adaption.DataMarker;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.adaption.DataMarkerMapping;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.adaption.HLAAdapter;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.adaption.HLAByteArrayAdaption;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.adaption.HLAByteArrayDerivedElement;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human.HumanBehaviour;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAASCIIstring;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InTimeAdvancingState;
import hla.rti1516e.exceptions.InvalidLookahead;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.TimeRegulationIsNotEnabled;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

public class WorkwayFederate {

	private RTIambassador rtiamb;
	public WorkwayFederateAmbassador fedamb; // created when we connect
	public HLAfloat64TimeFactory timeFactory; // set when we join
	protected EncoderFactory encoderFactory; // set when we join

	protected InteractionClassHandle registerAtBusStopHandle;
	protected ParameterHandle humanNameRegisterHandle;
	protected ParameterHandle busStopNameRegisterHandle;
	protected ParameterHandle destinationNameRegisterHandle;

	protected InteractionClassHandle unregisterAtBusStopHandle;
	protected ParameterHandle humanNameUnregisterHandle;
	protected ParameterHandle busStopNameUnregisterHandle;

	protected InteractionClassHandle humanEntersBusHandle;
	protected ParameterHandle humanNameEnterBusHandle;
	protected ParameterHandle busStopNameEnterHandle;

	protected InteractionClassHandle humanExitsBusHandle;
	protected ParameterHandle humanNameExitBusHandle;
	protected ParameterHandle busStopNameExitHandle;
	protected ParameterHandle humanExitsPassedTimeHandle;

	protected ObjectClassHandle humanObjectClassHandle;
	protected AttributeHandle humanNameAttributeHandle;
	protected AttributeHandle destinationHandle;
	protected AttributeHandle movementTypeHandle;

	protected ObjectClassHandle busStopObjectClassHandle;
	protected AttributeHandle busStopNameAttributeHandle;

	protected ObjectInstanceHandle busObjectHandles;

	private final String fedInfoStr = "!Workway Federate-Info!: ";
	private String federateName;

	private WorkwayModel simulation;
	public HLAAdapter adapterService;

	protected int busStopInitialised = 0;

	private boolean regulateTime = true;
	private boolean constrainTime = true;

	public LinkedList<ObjectInstanceHandle> busStopHandles;

	public WorkwayFederate(WorkwayModel simulation) {
		this.simulation = simulation;

		busStopHandles = new LinkedList<ObjectInstanceHandle>();
	}

	public void runFederate(String fedName) throws Exception {

		this.federateName = fedName;

		rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

		fedamb = new WorkwayFederateAmbassador(this);

		rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);

		URL[] joinModules = new URL[] { (new File("FOMS/HumanSimFOM.xml")).toURI().toURL() };
		rtiamb.joinFederationExecution(federateName, "HumanSim1", "HumanSim1", joinModules);

		this.timeFactory = (HLAfloat64TimeFactory) rtiamb.getTimeFactory();

		rtiamb.registerFederationSynchronizationPoint(HumanSimValues.READY_TO_RUN, null);

		while (fedamb.isAnnounced == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		}

		setUpAdaptation();

		rtiamb.synchronizationPointAchieved(HumanSimValues.READY_TO_RUN);

		runTimePolicyEnabling();

		rtiamb.enableCallbacks();

		while (fedamb.isReadyToRun == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		}

		publishAndSubscribe();

		//Wait until all bus stops are received
		while (simulation.getStops().size() != HumanSimValues.NUM_BUSSTOPS) {
			advanceTime(1.0);
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		}

		initialiseHuman();

		//TODO Hardcoded step to be on same time with BusSim due to exchange of busstops and humans
		//Maybe find more elegant solution?
		advanceTime(1.0);
		simulation.startSimulation();

	}

	public void endExecution() throws Exception {

		resignFromExecution();

		destroyExecution();

	}

	public void resignFromExecution() {
		try {
			rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
		} catch (InvalidResignAction e) {
			e.printStackTrace();
		} catch (OwnershipAcquisitionPending e) {
			e.printStackTrace();
		} catch (FederateOwnsAttributes e) {
			e.printStackTrace();
		} catch (FederateNotExecutionMember e) {
			Utils.log("Federate not execution member anymore");
		} catch (NotConnected e) {
			e.printStackTrace();
		} catch (CallNotAllowedFromWithinCallback e) {
			e.printStackTrace();
		} catch (RTIinternalError e) {
			e.printStackTrace();
		}
		Utils.log("Resigned from Federation");
	}

	public void destroyExecution() {

		try {
			rtiamb.destroyFederationExecution("HumanSim1");
		} catch (FederatesCurrentlyJoined e) {
			Utils.log("There are still other simulations joined");
		} catch (FederationExecutionDoesNotExist e) {
			Utils.log("Federation Exectuion does not exist anymore");
		} catch (NotConnected e) {
			e.printStackTrace();
		} catch (RTIinternalError e) {
			e.printStackTrace();
		}
		Utils.log("Destroyed HumanSim federation");

	}

	private void enableTimePolicy() throws Exception {

		if (regulateTime) {
			HLAfloat64Interval lookahead = timeFactory.makeInterval(fedamb.federateLookahead);
			this.rtiamb.enableTimeRegulation(lookahead);

			while (fedamb.isRegulating == false) {
				rtiamb.evokeMultipleCallbacks(0.1, 0.2);
			}
			
			Utils.log(fedInfoStr + "Timeregulating: " + fedamb.isRegulating);
		}

		if (constrainTime) {
			this.rtiamb.enableTimeConstrained();

			while (fedamb.isConstrained == false) {
				rtiamb.evokeMultipleCallbacks(0.1, 0.2);
			}
			
			Utils.log(fedInfoStr + "Timeconstrained: " + fedamb.isConstrained);
		}
	}

	private void runTimePolicyEnabling() throws Exception {

		if (regulateTime && constrainTime) {
			while (!fedamb.isConstrained && !fedamb.isRegulating) {
				try {
					enableTimePolicy();
				} catch (Exception e) {
					Utils.log(e.getMessage());
				}
			}
		} else if (regulateTime) {
			while (!fedamb.isRegulating) {
				try {
					enableTimePolicy();
				} catch (Exception e) {
					Utils.log(e.getMessage());
				}
			}

		} else if (constrainTime) {
			while (!fedamb.isConstrained) {
				try {
					enableTimePolicy();
				} catch (Exception e) {
					Utils.log(e.getMessage());
				}
			}
		} else {
			Utils.log("No time policy to enable");
		}

	}

	private void publishAndSubscribe() throws RTIexception {
		registerAtBusStopHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.HumanRegistersAtBusStop");
		rtiamb.publishInteractionClass(registerAtBusStopHandle);
		humanNameRegisterHandle = rtiamb.getParameterHandle(registerAtBusStopHandle, "HumanName");
		busStopNameRegisterHandle = rtiamb.getParameterHandle(registerAtBusStopHandle, "BusStopName");
		destinationNameRegisterHandle = rtiamb.getParameterHandle(registerAtBusStopHandle, "DestinationName");

		unregisterAtBusStopHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.HumanUnRegistersAtBusStop");
		rtiamb.publishInteractionClass(unregisterAtBusStopHandle);
		humanNameUnregisterHandle = rtiamb.getParameterHandle(unregisterAtBusStopHandle, "HumanName");
		busStopNameUnregisterHandle = rtiamb.getParameterHandle(unregisterAtBusStopHandle, "BusStopName");

		humanEntersBusHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.HumanEntersBus");
		rtiamb.subscribeInteractionClass(humanEntersBusHandle);
		humanNameEnterBusHandle = rtiamb.getParameterHandle(humanEntersBusHandle, "HumanName");
		busStopNameEnterHandle = rtiamb.getParameterHandle(humanEntersBusHandle, "BusStopName");

		humanExitsBusHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.HumanExitsBus");
		rtiamb.subscribeInteractionClass(humanExitsBusHandle);
		humanNameExitBusHandle = rtiamb.getParameterHandle(humanExitsBusHandle, "HumanName");
		busStopNameExitHandle = rtiamb.getParameterHandle(humanExitsBusHandle, "BusStopName");
		humanExitsPassedTimeHandle = rtiamb.getParameterHandle(humanExitsBusHandle, "PassedTime");

		humanObjectClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Human");
		humanNameAttributeHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "HumanName");
		destinationHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "Destination");
		movementTypeHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "Movementtype");

		busStopObjectClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.BusStop");
		busStopNameAttributeHandle = rtiamb.getAttributeHandle(busStopObjectClassHandle, "BusStopName");

		AttributeHandleSet humanPublishedAttributes = rtiamb.getAttributeHandleSetFactory().create();
		humanPublishedAttributes.add(humanNameAttributeHandle);
		humanPublishedAttributes.add(destinationHandle);
		humanPublishedAttributes.add(movementTypeHandle);

		rtiamb.publishObjectClassAttributes(humanObjectClassHandle, humanPublishedAttributes);

		busStopObjectClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.BusStop");
		busStopNameAttributeHandle = rtiamb.getAttributeHandle(busStopObjectClassHandle, "BusStopName");

		AttributeHandleSet busStopAttributes = rtiamb.getAttributeHandleSetFactory().create();
		busStopAttributes.add(busStopNameAttributeHandle);
		rtiamb.subscribeObjectClassAttributes(busStopObjectClassHandle, busStopAttributes);

	}

	private ObjectInstanceHandle registerHumanObject() throws RTIexception {
		return rtiamb.registerObjectInstance(humanObjectClassHandle);
	}

	private byte[] generateTag() {
		return ("(timestamp) " + System.currentTimeMillis()).getBytes();
	}

	/**
	 * This method will request a time advance to the current time, plus the given
	 * timestep. It will then wait until a notification of the time advance grant
	 * has been received.
	 */
	public synchronized boolean advanceTime(double timestep) throws RTIexception {

		double advancingTo = fedamb.federateTime + timestep;
		
		if (!(advancingTo <= HumanSimValues.MAX_SIM_TIME.toSeconds().value())) {
			return false;
		}

		fedamb.isAdvancing = true;
		HLAfloat64Time time = timeFactory.makeTime(advancingTo);
		try {
			rtiamb.timeAdvanceRequest(time);
		} catch (Exception e) {
			Utils.log(e.getMessage() + rtiamb.queryLogicalTime());
			return false;
		}

		// wait for the time advance to be granted. ticking will tell the
		// LRC to start delivering callbacks to the federate
		while (fedamb.isAdvancing) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		}
		return true;
	}

	public synchronized void synchronisedAdvancedTime(double timestep) {

		double advanceStep = 0.0;
		
		if(-0.000000001 < timestep && timestep < 0.00000001) {
			return;
		}
		
		if (getCurrentFedTime() < simulation.getSimulationControl().getCurrentSimulationTime()) {
			double diff = 0.0;
			diff = simulation.getSimulationControl().getCurrentSimulationTime() - getCurrentFedTime();
			advanceStep = timestep + diff;
		} else {
			advanceStep = timestep;
		}
		
		if (advanceStep > fedamb.federateLookahead) {
			try {
				if (!advanceTime(advanceStep)) {
					return;
				}
			} catch (RTIexception e) {
				e.printStackTrace();
			}

		}
	}

	public void sendRegisterInteraction(Human human, String busStop, String destination, double timestep) throws RTIexception {

		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(3);
		parameters.put(humanNameRegisterHandle, adapterService.filter(human.getName()));
		parameters.put(busStopNameRegisterHandle, adapterService.filter(busStop));
		parameters.put(destinationNameRegisterHandle, adapterService.filter(destination));
		HLAfloat64Time time = timeFactory.makeTime(simulation.getSimulationControl().getCurrentSimulationTime() + timestep);
		rtiamb.sendInteraction(registerAtBusStopHandle, parameters, generateTag(), time);
	}

	public void initialiseHuman() throws Exception {

		simulation.initialiseHumans();
		
		for (Human human : simulation.getHumans()) {
			
		ObjectInstanceHandle oih = registerHumanObject();

		human.setOih(oih);
		human.setOch(humanObjectClassHandle);
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(3);
		attributes.put(humanNameAttributeHandle, adapterService.filter(human.getName()));
		attributes.put(destinationHandle, adapterService.filter(human.getDestination().getName()));
		attributes.put(movementTypeHandle, adapterService.filter(human.getBehaviour().toString()));
		HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + 1.0);
		rtiamb.updateAttributeValues(human.getOih(), attributes, generateTag(), time);
		}

	}

	public double getCurrentFedTime() {
		return fedamb.federateTime;
	}

	public void handleBusStopAttributeUpdates(BusStop busStop, AttributeHandleValueMap attributes,
			ObjectInstanceHandle oih) {
		System.out.println("BusStopAttr");
		String busStopName = "";

		for (AttributeHandle handle : attributes.keySet()) {
			if (handle.equals(busStopNameAttributeHandle)) {
				busStopName = (String) adapterService.filter(String.class.getTypeName(),
						attributes.get(busStopNameAttributeHandle));
			} else {
				Utils.log("Got more than expected");
			}
		}

		if (busStopName.equals("")) {
			Utils.log(fedInfoStr + " ERROR: got empty name");
		} else {
			Utils.log("Got new busstop name, dont know what to do");
			return;
		}

		Utils.log(fedInfoStr + "No corresponding busStop to handle found");
	}

	public void waitForUser() {
		Utils.log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			reader.readLine();
		} catch (Exception e) {
			Utils.log("Error while waiting for user input: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void handleAttributeUpdate(ObjectInstanceHandle oih, AttributeHandleValueMap attributes) throws Exception {

		for (BusStop busStop : simulation.getStops()) {
			if (busStop.getOih().equals(oih)) {
				Utils.log("Handle busStop attribute" + oih.toString());
				handleBusStopAttributeUpdates(busStop, attributes, oih);
				return;
			}
		}

		for (ObjectInstanceHandle bsh : busStopHandles) {
			if (bsh.equals(oih)) {
				addBusStop(oih, attributes);
			}
		}

	}

	public void addBusStop(ObjectInstanceHandle oih, AttributeHandleValueMap attributes) {

		String busStopName = "";

		for (AttributeHandle handle : attributes.keySet()) {
			if (handle.equals(busStopNameAttributeHandle)) {
				busStopName = (String) adapterService.filter(String.class.getTypeName(),
						attributes.get(busStopNameAttributeHandle));
			} else {
				Utils.log("Got more than expected");
			}
		}

		BusStop bs = new BusStop(simulation, busStopName);
		bs.setOih(oih);
		bs.setOch(busStopObjectClassHandle);
		simulation.addBusStop(bs);
		busStopInitialised++;
	}

	public void addBusStopHandle(ObjectInstanceHandle oih) {
		busStopHandles.add(oih);
	}

	public RTIambassador getRTIAmb() {
		return rtiamb;
	}

	public void handleEnterInteraction(String humanName, String busStopName, double passedTime) {
		simulation.scheduleHumanEntersEvent(humanName, busStopName, passedTime);
	}

	public void handleExitInteraction(String humanName, String busStopName, double passedTime) {
		simulation.scheduleHumanExitsEvent(humanName, busStopName, passedTime);
	}

	private void setUpAdaptation() {

		adapterService = new HLAAdapter();

		DataMarker byteArray = new DataMarker("byteArray");
		DataMarker stringMarker = new DataMarker("string");
		DataMarker intMarker = new DataMarker("int");

		DataMarkerMapping mappingByteArray = new DataMarkerMapping(byteArray, byte[].class.getTypeName());
		DataMarkerMapping mappingHLAString = new DataMarkerMapping(stringMarker, String.class.getTypeName());
		DataMarkerMapping mappingHLAInt32 = new DataMarkerMapping(intMarker, Integer.class.getTypeName());

		HLAByteArrayAdaption byteArrayDesription = new HLAByteArrayAdaption(mappingByteArray);

		HLAByteArrayDerivedElement HLAStringElement = new HLAByteArrayDerivedElement(mappingHLAString,
				new ByteArrayToStringConversion(encoderFactory));
		HLAByteArrayDerivedElement HLAInt32Element = new HLAByteArrayDerivedElement(mappingHLAInt32,
				new ByteArrayToInteger32BEConversion(encoderFactory));
		byteArrayDesription.addDerivedElement(HLAStringElement);
		byteArrayDesription.addDerivedElement(HLAInt32Element);

		adapterService.addDescription(byteArrayDesription);
	}
	public WorkwayFederateAmbassador getFedAmb() {
		return fedamb;
	}
	
	public boolean isAdvancingTime() {
		return fedamb.isAdvancing;
	}

}


