package io.mosip.kernel.auditmanager.constant;

/**
 * Error code constants for Hibernate implementation of Dao Manager
 */
public enum HibernateErrorCode {
	ERR_DATABASE("KER-AUD-DAH-001"), HIBERNATE_EXCEPTION("KER-AUD-DAH-002"), NO_RESULT_EXCEPTION("KER-AUD-DAH-003");

	/**
	 * Field for error code
	 */
	private final String errorCode;

	/**
	 * Function to set errorcode
	 * 
	 * @param errorCode The errorcode
	 */
	private HibernateErrorCode(final String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Function to get errorcode
	 * 
	 * @return The errorcode
	 */
	public String getErrorCode() {
		return errorCode;
	}

}