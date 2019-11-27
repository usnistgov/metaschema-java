package gov.nist.secauto.metaschema.codegen.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import net.openhft.compiler.CompilerUtils;

public class DynamicClassLoader extends ClassLoader {
	private final File classDir;

	public DynamicClassLoader(File classDir) {
		this(null, classDir, getSystemClassLoader());
	}

	public DynamicClassLoader(File classDir, ClassLoader parent) {
		this(null, classDir, parent);
	}

	public DynamicClassLoader(String name, File classDir, ClassLoader parent) {
		super(name, parent);
		Objects.requireNonNull(classDir, "classDir must not be null");
		this.classDir = classDir;
		CompilerUtils.addClassPath(classDir.getPath());
	}

	protected File getClassDir() {
		return classDir;
	}

	private static String readFile(Path filePath) throws IOException {
		StringBuilder contentBuilder = new StringBuilder();
		try (Stream<String> stream = Files.lines(filePath, StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		}
		return contentBuilder.toString();
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> retval = null;
		try {
			retval = super.findClass(name);
		} catch (ClassNotFoundException ex) {
			
			File classFile = new File(getClassDir(), name.replace(".", "/") + ".java");
			try {
				retval = CompilerUtils.CACHED_COMPILER.loadFromJava(this, name, readFile(classFile.toPath()));
			} catch (IOException e) {
				throw new ClassNotFoundException("An IO error occured while loading java class code",e);
			}
		}
		if (retval == null) {
			throw new ClassNotFoundException(name);
		}
		return retval;
	}
}
