package org.openmrs.module.fgh.mpi;

import static java.lang.Boolean.valueOf;
import static org.openmrs.module.debezium.DatabaseOperation.CREATE;
import static org.openmrs.module.debezium.DatabaseOperation.DELETE;
import static org.openmrs.module.fgh.mpi.MpiConstants.FIELD_ACTIVE;

import java.util.List;
import java.util.Map;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.debezium.DatabaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An instance of this class takes a patient uuid, loads the patient record, generates the fhir json
 * payload and calls the http client to post the patient to the MPI.
 */
@Component("mpiIntegrationProcessor")
public class MpiIntegrationProcessor {
	
	private static final Logger log = LoggerFactory.getLogger(MpiIntegrationProcessor.class);
	
	public final static String ID_PLACEHOLDER = "{PATIENT_ID}";
	
	public final static String PATIENT_QUERY = "SELECT voided FROM patient WHERE patient_id = " + ID_PLACEHOLDER;
	
	public final static String PERSON_QUERY = "SELECT gender, birthdate, dead, death_date, uuid FROM person WHERE "
	        + "person_id = " + ID_PLACEHOLDER;
	
	@Autowired
	private MpiHttpClient mpiHttpClient;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Adds or updates the patient with the specified patient id in the MPI
	 * 
	 * @param patientId the patient id
	 * @param e DatabaseEvent object
	 * @throws Exception
	 */
	public void process(Integer patientId, DatabaseEvent e) throws Exception {
		log.info("Processing patient with id: " + patientId);
		
		if ("person".equalsIgnoreCase(e.getTableName()) && e.getOperation() == CREATE) {
			log.info("Ignoring person insert event");
			return;
		}
		
		String id = patientId.toString();
		AdministrationService adminService = Context.getAdministrationService();
		boolean isPersonDeletedEvent = "person".equalsIgnoreCase(e.getTableName()) && e.getOperation() == DELETE;
		List<List<Object>> person = null;
		String patientUud;
		if (isPersonDeletedEvent) {
			patientUud = e.getPreviousState().get("uuid").toString();
		} else {
			person = adminService.executeSQL(PERSON_QUERY.replace(ID_PLACEHOLDER, id), true);
			if (person.isEmpty()) {
				log.info("Ignoring event because no person was found with id: " + id);
				return;
			} else {
				patientUud = person.get(0).get(4).toString();
			}
		}
		
		Map<String, Object> mpiPatient = mpiHttpClient.getPatient(patientUud);
		if (mpiPatient != null) {
			log.info("Found existing patient record in the MPI");
		} else {
			if (log.isDebugEnabled()) {
				log.debug("No patient record found in the MPI");
			}
		}
		
		boolean isMpiPatientActive = mpiPatient == null ? false : valueOf(mpiPatient.get(FIELD_ACTIVE).toString());
		boolean isPatientDeletedEvent = "patient".equalsIgnoreCase(e.getTableName()) && e.getOperation() == DELETE;
		
		if ((mpiPatient == null || !isMpiPatientActive) && (isPatientDeletedEvent || isPersonDeletedEvent)) {
			if (mpiPatient == null) {
				log.info("Ignoring event because there is no record in the MPI to update for deleted "
				        + (isPatientDeletedEvent ? "patient" : "person"));
			} else {
				log.info("Ignoring event because the record in the MPI is already marked as inactive for deleted "
				        + (isPatientDeletedEvent ? "patient" : "person"));
			}
			
			return;
		}
		
		Map<String, Object> fhirResource;
		if (isPatientDeletedEvent || isPersonDeletedEvent) {
			fhirResource = mpiPatient;
			fhirResource.put(FIELD_ACTIVE, false);
		} else {
			List<List<Object>> patient = adminService.executeSQL(PATIENT_QUERY.replace(ID_PLACEHOLDER, id), true);
			if (patient.isEmpty()) {
				if (mpiPatient == null || !isMpiPatientActive) {
					log.info("Ignoring event because there is no patient record both in OpenMRS and MPI");
					if (mpiPatient == null) {
						log.info("Ignoring event because there is no record in the MPI to update");
					} else {
						log.info("Ignoring event because the record in the MPI is already marked as inactive");
					}
					
					return;
				}
				
				fhirResource = mpiPatient;
				fhirResource.put(FIELD_ACTIVE, false);
			} else {
				//TODO May be we should not build a new resource and instead update the mpiPatient if one exists
				fhirResource = MpiUtils.buildFhirPatient(id, patient.get(0), person.get(0), mpiPatient);
			}
		}
		
		List<Map<String, Object>> mpiIdsResp = mpiHttpClient.submitPatient(mapper.writeValueAsString(fhirResource));
		
		if (log.isDebugEnabled()) {
			log.debug("Response from MPI submission: " + mpiIdsResp);
		}
		
		log.info("Successfully " + (mpiPatient == null ? "created" : "updated") + " the patient record in the MPI");
	}
	
}
