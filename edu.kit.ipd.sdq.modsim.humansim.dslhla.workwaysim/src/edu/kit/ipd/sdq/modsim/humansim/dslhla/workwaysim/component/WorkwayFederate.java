package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InTimeAdvancingState;
import hla.rti1516e.exceptions.InvalidLookahead;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.TimeRegulationIsNotEnabled;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

public class WorkwayFederate{

	

	
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
	protected AttributeHandle collectedHandle;
	protected AttributeHandle humanNameAttributeHandle;
	protected AttributeHandle destinationHandle;
	
	protected ObjectClassHandle busStopObjectClassHandle;
	protected AttributeHandle busStopNameAttributeHandle;
	
	protected ObjectInstanceHandle busObjectHandles;
	
	
	
	String fedName;
	String humanName;
	
	private final String fedInfoStr =  "!Workway Federate-Info!: ";
	private String federateName;
	
	
	public double executionTime = 0.0;
	public boolean finished = false;
	public boolean finishedCounted = false;
	private boolean isFedInit = false;
	private WorkwayModel simulation;
	public HLAAdapter adapterService;
	
	double startTime;

	protected InteractionClassHandle humanReadyHandle;

	protected InteractionClassHandle busSimReadyHandle;
	
	protected int busStopInitialised = 0;
	
	protected boolean busReady = false;
	
	private boolean regulateTime = false;
	private boolean constrainTime = false;
	
	public LinkedList<ObjectInstanceHandle> busStopHandles;
	
	
	public WorkwayFederate(WorkwayModel simulation){
		this.simulation = simulation;
		
		busStopHandles = new LinkedList<ObjectInstanceHandle>();
	}
	 
	
	public void runFederate(String fedName) throws Exception{

		this.federateName = fedName;
//		log(fedInfoStr + "Creating RTIambassador");
		rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

	
		
//		log(fedInfoStr + "Connecting");
		fedamb = new WorkwayFederateAmbassador(this);
		if(HumanSimValues.EVOKE){
		rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);
		} else {
			rtiamb.connect(fedamb, CallbackModel.HLA_IMMEDIATE);
		}
//		log(fedInfoStr + "Creating Federation");
//		try {
//			URL[] modules = new URL[] { (new File("FOMS/HumanSimFOM.xml")).toURI().toURL() };
//
//			rtiamb.createFederationExecution("HumanSim", modules);
////			log(fedInfoStr + "Created Federation");
//		} catch (FederationExecutionAlreadyExists exists) {
////			log(fedInfoStr + "Didn't create federation, it already existed");
//		} catch (MalformedURLException urle) {
//			log(fedInfoStr + "Exception loading one of the FOM modules from disk: " + urle.getMessage());
//			urle.printStackTrace();
//			return;
//		}
//		
		URL[] joinModules = new URL[] { (new File("FOMS/HumanSimFOM.xml")).toURI().toURL() };
		rtiamb.joinFederationExecution(federateName, "HumanSim1", "HumanSim1", joinModules);
		
		
//		log(fedInfoStr + "Joined fedration as " + federateName);
		

		this.timeFactory = (HLAfloat64TimeFactory) rtiamb.getTimeFactory();

		rtiamb.registerFederationSynchronizationPoint(HumanSimValues.READY_TO_RUN, null);
	
		
		while (fedamb.isAnnounced == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		}
		
		adapterService = new HLAAdapter();
		

		DataMarker byteArray = new DataMarker("byteArray");
		DataMarker stringMarker = new DataMarker("string");
		DataMarker intMarker = new DataMarker("int");
		
		
		DataMarkerMapping mappingByteArray = new DataMarkerMapping(byteArray, byte[].class.getTypeName());
		DataMarkerMapping mappingHLAString = new DataMarkerMapping(stringMarker, String.class.getTypeName());
		DataMarkerMapping mappingHLAInt32 = new DataMarkerMapping(intMarker, Integer.class.getTypeName());
		
		
		HLAByteArrayAdaption byteArrayDesription = new HLAByteArrayAdaption(mappingByteArray);
		
	
		HLAByteArrayDerivedElement HLAStringElement = new HLAByteArrayDerivedElement(mappingHLAString, new ByteArrayToStringConversion(encoderFactory));
		HLAByteArrayDerivedElement HLAInt32Element = new HLAByteArrayDerivedElement(mappingHLAInt32, new ByteArrayToInteger32BEConversion(encoderFactory));
		byteArrayDesription.addDerivedElement(HLAStringElement);
		byteArrayDesription.addDerivedElement(HLAInt32Element);
		
		adapterService.addDescription(byteArrayDesription);
		
		rtiamb.synchronizationPointAchieved(HumanSimValues.READY_TO_RUN);

//		log(fedInfoStr + "Before Time Policy Enable");

				
		
		
		
		regulateTime = true;
		constrainTime = true;
		runTimePolicyEnabling();
		
		rtiamb.enableCallbacks();
		
		while (fedamb.isReadyToRun == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		}

		publishAndSubscribe();
	

		while(simulation.getStops().size() != HumanSimValues.NUM_BUSSTOPS){
			if(HumanSimValues.EVOKE){
				advanceTime(1.0);
				rtiamb.evokeMultipleCallbacks(0.1, 0.2);
			} else {
				System.out.print("");
			}
		}
		
		
		initialiseHuman();
		
			//divestCollectedOwnership();
		//TODO Hardcoded Timeadvance to be on equal starting time with BusSim 
		//not nice but cleaner
		advanceTime(1.0);
		simulation.startSimulation();
			
		}
	
	
	public void endExecution() throws Exception{
		
		rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
		log("Resigned from Federatin");
		
		try{
			rtiamb.destroyFederationExecution("HumanSim1");
			log("Destroyed HumanSim federation");
		} catch (FederationExecutionDoesNotExist fedne){
			log(" Federation does not exist");
		} catch (FederatesCurrentlyJoined fcj) {
			log("Federates still joined at HumanSim");
		}
	}

	
	private void enableTimePolicy() throws Exception {
		
		if(regulateTime){
		HLAfloat64Interval lookahead = timeFactory.makeInterval(fedamb.federateLookahead);
		this.rtiamb.enableTimeRegulation(lookahead);

		while (fedamb.isRegulating == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
			}
		
		//log(fedInfoStr + "activated time regulation");
		}
		
		if(constrainTime){
		this.rtiamb.enableTimeConstrained();
			
		while (fedamb.isConstrained == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
			}
		//log(fedInfoStr + "activated time contrained");
		}
	}
	
	
	private void runTimePolicyEnabling() throws Exception{
		
		if(regulateTime && constrainTime){
			while(!fedamb.isConstrained && !fedamb.isRegulating){
				try{
				enableTimePolicy();
				} catch (Exception e){
					log(e.getMessage());
				}
			}
		} else if (regulateTime){
			while(!fedamb.isRegulating){
				try{
					enableTimePolicy();
					} catch (Exception e){
						log(e.getMessage());
					}
				}
			
		} else if (constrainTime){
			while(!fedamb.isConstrained){
				try{
					enableTimePolicy();
					} catch (Exception e){
						log(e.getMessage());
					}
				}
		} else {
			log("No time policy to enable");
		}
		
		
	}


	private void divestCollectedOwnership() throws Exception{
		
		AttributeHandleSet handles = rtiamb.getAttributeHandleSetFactory().create();
		handles.add(collectedHandle);
		for (Human human : simulation.getHumans()) {
			rtiamb.unconditionalAttributeOwnershipDivestiture(human.getOih(), handles);
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
		collectedHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "HumanCollected");
		destinationHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "Destination");
		
		busStopObjectClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.BusStop");
		busStopNameAttributeHandle = rtiamb.getAttributeHandle(busStopObjectClassHandle, "BusStopName");
		
	
		AttributeHandleSet humanSubscribedAttributes = rtiamb.getAttributeHandleSetFactory().create();
		humanSubscribedAttributes.add(collectedHandle);
	
		
		AttributeHandleSet humanPublishedAttributes = rtiamb.getAttributeHandleSetFactory().create();
		humanPublishedAttributes.add(humanNameAttributeHandle);
		humanPublishedAttributes.add(destinationHandle);

		
		
		
		rtiamb.publishObjectClassAttributes(humanObjectClassHandle, humanPublishedAttributes);
		rtiamb.subscribeObjectClassAttributes(humanObjectClassHandle, humanSubscribedAttributes);
		
		busStopObjectClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.BusStop");
		busStopNameAttributeHandle = rtiamb.getAttributeHandle(busStopObjectClassHandle, "BusStopName");
		
		AttributeHandleSet busStopAttributes = rtiamb.getAttributeHandleSetFactory().create();
		busStopAttributes.add(busStopNameAttributeHandle);
		rtiamb.subscribeObjectClassAttributes(busStopObjectClassHandle, busStopAttributes);
		
	}
	
	private ObjectInstanceHandle registerHumanObject() throws RTIexception{
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
	public synchronized boolean advanceTime( double timestep ) throws RTIexception
	{
		
		double advancingTo = 0;
		double miniStep = 0.000000001;
		if(fedamb.federateTime + timestep <= HumanSimValues.MAX_SIM_TIME.toSeconds().value()){
			advancingTo = fedamb.federateTime + timestep;
		} else {
			Utils.log(simulation.getHuman(), "Sim overtime - wants to advance to: " + fedamb.federateTime + timestep + " current time: " + fedamb.federateTime);
			advancingTo =  HumanSimValues.MAX_SIM_TIME.toSeconds().value() + miniStep;
			return false;
		}
		// request the advance
		fedamb.isAdvancing = true;
		HLAfloat64Time time = timeFactory.makeTime( advancingTo );
		if(HumanSimValues.MESSAGE){
			try{
			rtiamb.nextMessageRequest( time );
			} catch (Exception e){
				log(e.getMessage());
				return false;
			}
			} else {
				try{
					rtiamb.timeAdvanceRequest( time );
					} catch (Exception e){
						log(e.getMessage());
						return false;
					}
			}
			
	
		// wait for the time advance to be granted. ticking will tell the
		// LRC to start delivering callbacks to the federate
		while( fedamb.isAdvancing )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}
//		System.out.println("New Fed Time: " + fedamb.federateTime);
		return true;
	}
	
	
	public synchronized void  synchronisedAdvancedTime(double timestep, AbstractSimEventDelegator simevent, AbstractSimEntityDelegator simentity ){

		if(timestep != 0.0){
			
			try {
				if(!advanceTime(timestep)){
					
					System.out.println("Not Advancing Time");
				simulation.getSimulationControl().stop();
					return;
				}
			} catch (RTIexception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	
		}
		simevent.schedule(simentity, timestep);
	}
	
	public void sendRegisterInteraction(String human, String busStop, String destination) throws RTIexception{
		
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(3);
		parameters.put(humanNameRegisterHandle, adapterService.filter(human));
		parameters.put(busStopNameRegisterHandle, adapterService.filter(busStop));
		parameters.put(destinationNameRegisterHandle, adapterService.filter(destination));
		HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + 1.0);

		//System.out.println("Human: " + human + " registers at" + busStop + " for " + destination);
		rtiamb.sendInteraction( registerAtBusStopHandle, parameters, generateTag(), time);
	}
	
	public void initialiseHuman() throws Exception{

		simulation.initialiseHumans();
		
		Human human = simulation.getHuman();

//		System.out.println("Initialising: " + human.getName());
		ObjectInstanceHandle oih = registerHumanObject();
		
		human.setOih(oih);
		human.setOch(humanObjectClassHandle);
//		System.out.println("Set Handles for: " + human.getName());
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
		attributes.put(humanNameAttributeHandle, adapterService.filter(human.getName()));
		attributes.put(destinationHandle, adapterService.filter(human.getDestination().getName()));
		HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + 1.0);
//		System.out.print("Updating Values");
		rtiamb.updateAttributeValues(human.getOih(), attributes, generateTag(), time);
		
		
	}
	
	public void changeDestinationAttribute(Human human, BusStop destination) throws RTIexception{
		
		//log(human, "Changing destination to: " + destination.getName());
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(1);
		
		attributes.put(destinationHandle, adapterService.filter(destination.getName()));
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + 1.0);
	
		rtiamb.updateAttributeValues(human.getOih(), attributes, generateTag(), time);
		
	}
	
	public double getCurrentFedTime(){
		return fedamb.federateTime;
	}
	
	public boolean timeOver(){
		
		if(fedamb == null){
			return false;
		}
		
		//log("Current FedTime:" + fedamb.federateTime);
		//log("Max sim time:" + maxSimTime.toSeconds().value());	
		if(fedamb.federateTime > HumanSimValues.MAX_SIM_TIME.toSeconds().value())
			return true;
		else
			return false;
	}
	
	public void log(String msg){
		 StringBuilder s = new StringBuilder();
    	 s.append(msg);
    	 System.out.println(s);
	}
	
	public void handleBusStopAttributeUpdates(BusStop busStop, AttributeHandleValueMap attributes, ObjectInstanceHandle oih){
		System.out.println("BusStopAttr");
		String busStopName = "";
	
		//log("Received BusStop attribute updates");
		
		
		for(AttributeHandle handle : attributes.keySet()){
			if(handle.equals(busStopNameAttributeHandle)){
				busStopName = (String)adapterService.filter(String.class.getTypeName(), attributes.get(busStopNameAttributeHandle));
			} else {
				log("Got more than expected");
			}
		}
		
		
		if(busStopName.equals("")){
			log(fedInfoStr + " ERROR: got empty name");
		} else {
			
			
			System.out.println("Got new busstop name, dont know what to do" );
			return;
		}
		
		
		
		log(fedInfoStr + "No corresponding busStop to handle found");
	
	}
	
	public void waitForUser() {
		log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			reader.readLine();
		} catch (Exception e) {
			log("Error while waiting for user input: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void handleHumanAttributesUpdate(Human human, AttributeHandleValueMap attributes) throws Exception{

		String collected = "";
	
		for(AttributeHandle handle : attributes.keySet()){
			if(handle.equals(collectedHandle)){
				collected = fedamb.decodeBoolean(attributes.get(collectedHandle));
				//System.out.println(collected);
			} else {
				log("Got more attributes than expected");
			}
		}
		//System.out.println("Received Collected at " + "Current time:" + fedamb.federateTime + "SimTime: " + simulation.getSimulationControl().getCurrentSimulationTime());
		//Utils.log(human, "Setting Collected to " + collected);
		switch (collected) {
		case "True":
			human.setCollected(true);
			//log(human, "setting achieved");
			break;
		case "False":
			human.setCollected(false);
			//log(human, "setting achieved");
			break;
		default:
			log(fedInfoStr + " ERROR: got faulty boolean");
		}
		
		
		//log(fedInfoStr + "No corresponding busStop to handle found");
	}
	
	
	public void handleAttributeUpdate(ObjectInstanceHandle oih, AttributeHandleValueMap attributes) throws Exception{

		
		//log("Got Update Handle:" + oih.toString());
		if(simulation.getHuman() != null && simulation.getHuman().getOih().equals(oih)) {
				handleHumanAttributesUpdate(simulation.getHuman(), attributes);
				return;
			}

		
		for (BusStop busStop : simulation.getStops()) {
			if(busStop.getOih().equals(oih)){
				//log("Handle busStop attribute" + oih.toString());
				handleBusStopAttributeUpdates(busStop, attributes, oih);
				return;
			}
		}
		
		for (ObjectInstanceHandle bsh : busStopHandles){
			if(bsh.equals(oih)){
				addBusStop(oih, attributes);
			}
		}
		
	}
	
	public void addBusStop(ObjectInstanceHandle oih, AttributeHandleValueMap attributes){
		
		String busStopName = "";
		
		//log("Received BusStop attribute updates");
		
		
		for(AttributeHandle handle : attributes.keySet()){
			if(handle.equals(busStopNameAttributeHandle)){
				busStopName = fedamb.decodeStringValues(attributes.get(busStopNameAttributeHandle));
			} else {
				log("Got more than expected");
			}
		}
		
		BusStop bs = new BusStop(simulation, busStopName);
		bs.setOih(oih);
		bs.setOch(busStopObjectClassHandle);
		simulation.addBusStop(bs);
		//log("Created BusStop: " + bs.getName());
		busStopInitialised++;
	}
	
	public void addBusStopHandle(ObjectInstanceHandle oih){
		busStopHandles.add(oih);
	}
	
	public RTIambassador getRTIAmb(){
		return rtiamb;
	}
	
	public void handleEnterInteraction(String humanName, String busStopName){
		simulation.scheduleHumanEntersEvent(humanName, busStopName);
	}
	
	public void handleExitInteraction(String humanName, String busStopName, double passedTime){
		simulation.scheduleHumanExitsEvent(humanName, busStopName, passedTime);
	}
	
	public void modifyLookahead(double d){
		try {
			rtiamb.modifyLookahead(timeFactory.makeInterval(d));
		} catch (InvalidLookahead | InTimeAdvancingState | TimeRegulationIsNotEnabled | SaveInProgress
				| RestoreInProgress | FederateNotExecutionMember | NotConnected | RTIinternalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		} catch (CallNotAllowedFromWithinCallback | RTIinternalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
 