package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAASCIIstring;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;

public class WorkwayFederateAmbassador extends NullFederateAmbassador{
	private WorkwayFederate federate;
	
	public double federateTime = 0.0;
	protected double federateLookahead = 1.0;

	protected boolean isRegulating = false;
	protected boolean isConstrained = false;
	protected boolean isAdvancing = false;

	protected boolean isAnnounced = false;
	protected boolean isReadyToRun = false;
	
	
	
	// decode methods 
	
	public String decodeBoolean(byte[] bytes){
		HLAinteger32BE value = federate.encoderFactory.createHLAinteger32BE();
		// decode
		try
		{
			value.decode( bytes );
		}
		catch( DecoderException de )
		{
			return "Decoder Exception: "+de.getMessage();
		}

		
		switch( value.getValue() )
		{
			case 101:
				return "True";
			case 102:
				return "False";
			default:
				return "Unkown";
		}

	}
	


	public WorkwayFederateAmbassador(WorkwayFederate federate){
		this.federate = federate;
	}
	
	private void log(String message) {
		System.out.println("FederateAmbassador: " + message);
	}

	@Override
	public void synchronizationPointRegistrationFailed(String label, SynchronizationPointFailureReason reason) {
		log("Failed to register sync point: " + label + ", reason=" + reason);
	}

	@Override
	public void synchronizationPointRegistrationSucceeded(String label) {
		log("Successfully registered sync point: " + label);
	}

	@Override
	public void announceSynchronizationPoint(String label, byte[] tag) {
		log("Synchronization point announced: " + label);
		if (label.equals(HumanSimValues.READY_TO_RUN))
			this.isAnnounced = true;
	}

	@Override
	public void federationSynchronized(String label, FederateHandleSet failed) {
		log("Federation Synchronized: " + label);
		if (label.equals(HumanSimValues.READY_TO_RUN))
			this.isReadyToRun = true;
	}

	@Override
	public void timeRegulationEnabled(LogicalTime time) {
		this.federateTime = ((HLAfloat64Time) time).getValue();
		this.isRegulating = true;
	}

	@Override
	public void timeConstrainedEnabled(LogicalTime time) {
		this.federateTime = ((HLAfloat64Time) time).getValue();
		this.isConstrained = true;
	}

	@Override
	public void timeAdvanceGrant(LogicalTime time) {
		this.federateTime = ((HLAfloat64Time) time).getValue();
		this.isAdvancing = false;
	}

	@Override
	public void discoverObjectInstance(ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass,
			String objectName) throws FederateInternalError {
		log("Discoverd Object: handle=" + theObject + ", classHandle=" + theObjectClass + ", name=" + objectName);	
		
		
		if(theObjectClass.equals(federate.busStopObjectClassHandle)){
			federate.addBusStopHandle(theObject);
		}
		
	}

	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
			byte[] tag, OrderType sentOrder, TransportationTypeHandle transport, SupplementalReflectInfo reflectInfo)
			throws FederateInternalError {
		reflectAttributeValues(theObject, theAttributes, tag, sentOrder, transport, null, sentOrder, reflectInfo);
		//federate.log(federate.fedName + " got attributes");
	}

	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
			byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time,
			OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) throws FederateInternalError {
		//federate.log(federate.fedName + " got attributes");
		try {
			federate.handleAttributeUpdate(theObject, theAttributes);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters,
			byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport,
			SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
		this.receiveInteraction(interactionClass, theParameters, tag, sentOrdering, theTransport, null, sentOrdering,
				receiveInfo);
		
		//federate.log("Received Interaction");
	}

	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters,
			byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time,
			OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) throws FederateInternalError {

		//federate.log("Received Interaction");
		if(interactionClass.equals(federate.humanEntersBusHandle)){
			federate.log("Found enter interaction");
			String humanName = decodeStringValues(theParameters.get(federate.humanNameEnterBusHandle));
			String busStopName =  decodeStringValues(theParameters.get(federate.busStopNameEnterHandle));
			
			try {
				federate.handleHumanEntersBusInteraction(humanName, busStopName);
			} catch (RTIexception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (interactionClass.equals(federate.humanExitsBusHandle)){
			federate.log("Found exit interaction");
			String humanName = decodeStringValues(theParameters.get(federate.humanNameExitBusHandle));
			String busStopName =  decodeStringValues(theParameters.get(federate.busStopNameExitHandle));
			
			try {
				federate.handleHumanExitsBusInteraction(humanName, busStopName);
			} catch (RTIexception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		else {
			federate.log("Gotten more interactions than expected");
		}
		
	}

	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject, byte[] tag, OrderType sentOrdering,
			SupplementalRemoveInfo removeInfo) throws FederateInternalError {
		log("Object Removed: handle=" + theObject);
	}
	
	
	public String decodeStringValues(byte[] bytes){
		
		//federate.log(bytes.toString());
		HLAASCIIstring value = federate.encoderFactory.createHLAASCIIstring();
		
		
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			de.printStackTrace();
			return "";
		}
		
	}
}