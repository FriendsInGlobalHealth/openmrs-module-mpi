package org.openmrs.module.fgh.mpi;

import static java.lang.System.currentTimeMillis;

import java.util.Map;

import org.openmrs.api.APIException;
import org.openmrs.module.debezium.DatabaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes incremental events, one at a time
 */
public class IncrementalEventProcessor extends BaseEventProcessor {
	
	private static final Logger log = LoggerFactory.getLogger(IncrementalEventProcessor.class);
	
	private MpiHttpClient mpiHttpClient;
	
	public IncrementalEventProcessor(PatientAndPersonEventHandler patientHandler, AssociationEventHandler assocHandler,
	    MpiHttpClient mpiHttpClient) {
		super(patientHandler, assocHandler);
		this.mpiHttpClient = mpiHttpClient;
	}
	
	@Override
	public void process(DatabaseEvent event) {
		
		try {
			log.info("Processing database event -> " + event);
			
			final long start = System.currentTimeMillis();
			
			Map<String, Object> fhirPatient = createFhirResource(event);
			if (fhirPatient != null) {
				mpiHttpClient.submitPatient(mapper.writeValueAsString(fhirPatient));
			}
			
			log.info("Done processing database event -> " + event);
			
			if (log.isDebugEnabled()) {
				log.debug("Duration: " + (currentTimeMillis() - start) + "ms");
			}
		}
		catch (Throwable t) {
			log.error("An error occurred while processing event -> " + event, t);
			throw new APIException(t);
		}
		
	}
	
}
