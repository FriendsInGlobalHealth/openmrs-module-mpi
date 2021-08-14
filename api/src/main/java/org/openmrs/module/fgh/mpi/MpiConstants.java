package org.openmrs.module.fgh.mpi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MpiConstants {
	
	public final static String MODULE_ID = "mpi";
	
	public final static String GP_MPI_BASE_URL = MODULE_ID + ".server.base.url";
	
	public final static String GP_KEYSTORE_PATH = MODULE_ID + ".keystore.path";
	
	public final static String GP_KEYSTORE_PASS = MODULE_ID + ".keystore.password";
	
	public final static String GP_KEYSTORE_TYPE = MODULE_ID + ".keystore.type";
	
	public final static String GP_PHONE_MOBILE = MODULE_ID + ".person.attribute.type.mobile.phone";
	
	public final static String GP_PHONE_HOME = MODULE_ID + ".person.attribute.type.home.phone";
	
	public final static String GP_HAPI_FHIR_BASE_URL = MODULE_ID + ".hapi.fhir.base.url";
	
	public final static String FIELD_RESOURCE_TYPE = "resourceType";
	
	public final static String FIELD_ID = "id";
	
	public final static String FIELD_IDENTIFIER = "identifier";
	
	public final static String FIELD_ACTIVE = "active";
	
	public final static String FIELD_NAME = "name";
	
	public final static String FIELD_GENDER = "gender";
	
	public final static String FIELD_BIRTHDATE = "birthDate";
	
	public final static String FIELD_DECEASED = "deceasedBoolean";
	
	public final static String FIELD_DECEASED_DATE = "deceasedDateTime";
	
	public final static String FIELD_SYSTEM = "system";
	
	public final static String FIELD_VALUE = "value";
	
	public final static String FIELD_PREFIX = "prefix";
	
	public final static String FIELD_FAMILY = "family";
	
	public final static String FIELD_GIVEN = "given";
	
	public final static String FIELD_ADDRESS = "address";
	
	public final static String FIELD_USE = "use";
	
	public final static String FIELD_LINE = "line";
	
	public final static String FIELD_DISTRICT = "district";
	
	public final static String FIELD_STATE = "state";
	
	public final static String FIELD_COUNTRY = "country";
	
	public final static String FIELD_PERIOD = "period";
	
	public final static String FIELD_START = "start";
	
	public final static String FIELD_END = "end";
	
	public final static String FIELD_TELECOM = "telecom";
	
	public final static String HOME = "home";
	
	public final static String MOBILE = "mobile";
	
	public final static String USE_OFFICIAL = "official";
	
	public final static String PHONE = "phone";
	
	public final static String SYSTEM_SOURCE_ID = "http://openclientregistry.org/fhir/sourceid";
	
	public final static String SYSTEM_PREFIX = "urn:uuid:";
	
	public final static DateFormat DATETIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	
	public final static DateFormat MYSQL_DATETIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
}
