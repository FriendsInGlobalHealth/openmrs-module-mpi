package org.openmrs.module.fgh.mpi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
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
	
	public final static String FIELD_RESOURCE_TYPE = "resourceType";
	
	public final static String FIELD_IDENTIFIER = "identifier";
	
	public final static String FIELD_ACTIVE = "active";
	
	public final static String FIELD_NAME = "name";
	
	public final static String FIELD_GENDER = "gender";
	
	public final static String FIELD_BIRTHDATE = "birthDate";
	
	public final static String FIELD_SYSTEM = "system";
	
	public final static String FIELD_VALUE = "value";
	
	public final static String FIELD_PREFIX = "prefix";
	
	public final static String FIELD_FAMILY = "family";
	
	public final static String FIELD_GIVEN = "given";
	
	public final static String FIELD_USE = "use";
	
	public final static String USE_OFFICIAL = "official";
	
	public final static String SYSTEM_SOURCE_ID = "http://openclientregistry.org/fhir/sourceid";
	
	public final static DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private MpiHttpClient mpiHttpClient;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	public void process(Patient patient) throws Exception {
		log.info("Processing patient -> " + patient);
		
		Map<String, Object> fhirRes = new HashMap();
		fhirRes.put(FIELD_RESOURCE_TYPE, "Patient");
		
		//TODO Add all identifiers
		List<Map<String, Object>> identifiers = new ArrayList(patient.getActiveIdentifiers().size());
		Map<String, Object> identifier = new HashMap();
		identifier.put(FIELD_SYSTEM, SYSTEM_SOURCE_ID);
		identifier.put(FIELD_VALUE, patient.getUuid());
		identifiers.add(identifier);
		fhirRes.put(FIELD_IDENTIFIER, identifiers);
		fhirRes.put(FIELD_ACTIVE, !patient.getVoided());
		
		List<Map<String, Object>> names = new ArrayList(patient.getNames().size());
		patient.getNames().stream().filter(name -> !name.getVoided()).forEach(name -> {
			Map<String, Object> nameRes = new HashMap();
			if (StringUtils.isNotBlank(name.getPrefix())) {
				nameRes.put(FIELD_PREFIX, name.getPrefix());
			}
			nameRes.put(FIELD_FAMILY, name.getFamilyName());
			List<String> givenNames = new ArrayList(2);
			givenNames.add(name.getGivenName());
			if (StringUtils.isNotBlank(name.getMiddleName())) {
				givenNames.add(name.getMiddleName());
			}
			nameRes.put(FIELD_GIVEN, givenNames);
			nameRes.put(FIELD_USE, USE_OFFICIAL);
			names.add(nameRes);
		});
		
		fhirRes.put(FIELD_NAME, names);
		String gender = null;
		if ("M".equalsIgnoreCase(patient.getGender())) {
			gender = "male";
		} else if ("F".equalsIgnoreCase(patient.getGender())) {
			gender = "female";
		} else if ("O".equalsIgnoreCase(patient.getGender())) {
			gender = "other";
		} else if (patient.getGender() != null) {
			throw new APIException("Don't know how to represent in fhir the gender: " + patient.getGender());
		}
		
		if (gender != null) {
			fhirRes.put(FIELD_GENDER, gender);
		}
		
		if (patient.getBirthdate() != null) {
			fhirRes.put(FIELD_BIRTHDATE, DATE_FORMATTER.format(patient.getBirthdate()));
		}
		
		//TODO add death info and addresses
		
		//TODO Add person attributes, user a GP to list attribute types to include
		
		mpiHttpClient.submitPatient(mapper.writeValueAsString(fhirRes));
		
		//TODO Add the MPI id to list of the patient's identifiers
	}
	
}
