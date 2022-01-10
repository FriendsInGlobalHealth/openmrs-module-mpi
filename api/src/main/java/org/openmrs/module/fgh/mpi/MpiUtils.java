package org.openmrs.module.fgh.mpi;

import static org.openmrs.module.fgh.mpi.MpiConstants.MODULE_ID;
import static org.openmrs.module.fgh.mpi.MpiConstants.PATIENT_ID_OFFSET_FILE;
import static org.openmrs.util.OpenmrsUtil.getApplicationDataDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionFactoryImpl;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utility methods
 */
public class MpiUtils {
	
	private static final Logger log = LoggerFactory.getLogger(MpiUtils.class);
	
	private static File patientIdOffsetFile;
	
	/**
	 * Gets the file used to store the id of the patient that was last submitted to the MPI
	 *
	 * @return File object
	 */
	private static File getPatientIdOffsetFile() {
		if (patientIdOffsetFile == null) {
			Path path = Paths.get(getApplicationDataDirectory(), MODULE_ID, PATIENT_ID_OFFSET_FILE);
			log.info("Patient Id off set file -> " + path);
			patientIdOffsetFile = path.toFile();
		}
		
		return patientIdOffsetFile;
	}
	
	/**
	 * Gets the patient id of the last submitted patient id
	 *
	 * @return patient id
	 */
	public static Integer getLastSubmittedPatientId() {
		log.info("Loading the patient id of the patient that was last submitted to the MPI");
		
		try {
			File file = getPatientIdOffsetFile();
			String patientId = null;
			if (file.exists()) {
				patientId = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
			}
			
			if (StringUtils.isBlank(patientId)) {
				log.info("No patient id found that was previously saved");
				return null;
			}
			
			log.info("Found id of the patient that was last submitted to the MPI: " + patientId);
			
			return Integer.valueOf(patientId);
		}
		catch (IOException e) {
			throw new APIException("Failed to read the id of the patient that was last submitted to the MPI", e);
		}
	}
	
	/**
	 * Gets the patient id of the last submitted patient id
	 *
	 * @return patient id
	 */
	public static void saveLastSubmittedPatientId(Integer patientId) {
		log.info("Saving the id of the patient that was last submitted to the MPI as: " + patientId);
		
		try {
			FileUtils.writeStringToFile(getPatientIdOffsetFile(), patientId.toString(), StandardCharsets.UTF_8);
			
			log.info("Successfully saved the id of the patient that was last submitted to the MPI as: " + patientId);
		}
		catch (IOException e) {
			log.error("Failed to save the id of the patient that was last submitted to the MPI as: " + patientId, e);
		}
	}
	
	/**
	 * Deletes the file used to store the id of the patient that was last submitted to the MPI
	 */
	public static void deletePatientIdOffsetFile() {
		log.info("Deleting the patient id off set file");
		
		File file = getPatientIdOffsetFile();
		if (!file.exists()) {
			log.info("No patient id off set file found to delete");
			return;
		}
		
		try {
			FileUtils.forceDelete(file);
			
			log.info("Successfully deleted the patient id off set file");
		}
		catch (IOException e) {
			log.error("Failed to delete the patient id off set file", e);
		}
	}
	
	/**
	 * Executes the specified query against the specified {@link DataSource}
	 * 
	 * @param query the query to execute
	 * @return results
	 * @throws SQLException
	 */
	public static List<List<Object>> executeQuery(String query) {
		List<List<Object>> results = new ArrayList();
		
		try (Connection conn = getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
			try (ResultSet resultSet = stmt.executeQuery()) {
				ResultSetMetaData rmd = resultSet.getMetaData();
				int columnCount = rmd.getColumnCount();
				
				while (resultSet.next()) {
					List<Object> rowObjects = new ArrayList<>();
					for (int x = 1; x <= columnCount; x++) {
						rowObjects.add(resultSet.getObject(x));
					}
					results.add(rowObjects);
				}
			}
		}
		catch (SQLException e) {
			throw new DAOException(e);
		}
		
		return results;
	}
	
	/**
	 * Gets the DataSource object
	 * 
	 * @return javax.sql.DataSource object
	 */
	public static DataSource getDataSource() {
		SessionFactory sf = Context.getRegisteredComponents(SessionFactory.class).get(0);
		return ((SessionFactoryImpl) sf).getConnectionProvider().unwrap(DataSource.class);
	}
	
}
