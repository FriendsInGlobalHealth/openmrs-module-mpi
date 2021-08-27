package org.openmrs.module.fgh.mpi;

import org.openmrs.module.debezium.DatabaseEvent;
import org.openmrs.module.debezium.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for table events of patient association entities i.e. patient_identifier, person_name and
 * person_address
 */
@Component("associationEventHandler")
public class AssociationEventHandler extends BaseEventHandler {
	
	private static final Logger log = LoggerFactory.getLogger(AssociationEventHandler.class);
	
	@Override
	public Integer getPatientId(DatabaseEvent event) {
		final String tableName = event.getTableName();
		if (log.isDebugEnabled()) {
			log.debug("Handling " + tableName + " event -> " + event);
		}
		
		final String columnName = "patient_identifier".equalsIgnoreCase(tableName) ? "patient_id" : "person_id";
		Object patientId;
		if (DatabaseOperation.DELETE == event.getOperation()) {
			patientId = event.getPreviousState().get(columnName);
		} else {
			patientId = event.getNewState().get(columnName);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Patient Id: " + patientId);
		}
		
		//TODO If person attribute and it's not one of the phone number attribute types ignore
		
		return Integer.valueOf(patientId.toString());
	}
}