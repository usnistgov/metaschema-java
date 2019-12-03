package gov.nist.secauto.metaschema.codegen;

import java.io.File;
import java.io.IOException;

public interface ClassGenerator {

	/**
	 * Generates the associated Java class.
	 * 
	 * @param dir the directory to generate the class in
	 * @return the qualified class name for the generated class
	 * @throws IOException if an error IO occurred while generating the class
	 */
	String generateClass(File dir) throws IOException;

	String getQualifiedClassName();

}
