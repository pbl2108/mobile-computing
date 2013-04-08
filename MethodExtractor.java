import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MethodExtractor {

	final static Charset ENCODING = StandardCharsets.UTF_8;
	/*Text file to write the output to*/
	final static String OUTPUT_FILE_NAME = "/home/peter/columbia/mob/smali-methods.txt";
	
	public static final HashMap<String, String> primitiveTypes = new HashMap<String, String>() {
		{
			put("void", "V");
			put("boolean", "Z");
			put("byte", "B");
			put("short", "S");
			put("char", "C");
			put("int", "I");
			put("long", "J");
			put("float", "F");
			put("double", "D");
		}
	};

	public String ReplacePeriods(String name) {
		return name.replace('.', '/');
	}

	public String GetSmaliFormattedClassName(String originalName) {
		/* When the parameters are arrays, getName() returns the smali name */
		String smaliName = ReplacePeriods(originalName);
		if (smaliName.startsWith("["))
			return smaliName;

		/* Primitive types are mapped to single capital letters */
		if (primitiveTypes.containsKey(originalName))
			smaliName = primitiveTypes.get(smaliName);
		else
			smaliName = "L" + smaliName + ";";

		return smaliName;
	}

	public void PrintMethods(Class<?> t) {
		ArrayList<String> list = GetMethods(t);
		for (String s : list) {
			System.out.print(s + "\n");
		}
	}

	public ArrayList<String> GetMethods(Class<?> t) {
		String className = t.getName();
		ArrayList<String> list = new ArrayList<String>();
		Method[] methods = t.getDeclaredMethods();

		for (Method method : methods) {
			/* Get only the public methods */
			if (Modifier.isPublic(method.getModifiers())) {
				list.add(BuildSmaliName(method, className));
			}
		}
		return list;
	}

	public String BuildSmaliName(Method method, String className) {
		StringBuilder sb = new StringBuilder();
		sb.append(GetSmaliFormattedClassName(className));
		sb.append("->");
		sb.append(method.getName());
		sb.append("(");
		Class<?>[] parameters = method.getParameterTypes();
		for (Class<?> parameter : parameters) {
			sb.append(GetSmaliFormattedClassName(parameter.getName()));
		}
		sb.append(")");
		sb.append(GetSmaliFormattedClassName(method.getReturnType().getName()));

		return sb.toString();
	}

	/*
	 * Copied from
	 * http://stackoverflow.com/questions/176527/how-can-i-enumerate-
	 * all-classes-in-a-package-and-add-them-to-a-list?lq=1
	 */
	public ArrayList<Class<?>> GetClassesForPackage(Package pkg) {
		String pkgname = pkg.getName();
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		// Get a File object for the package
		File directory = null;
		String fullPath;
		String relPath = pkgname.replace('.', '/');
		System.out.println("ClassDiscovery: Package: " + pkgname
				+ " becomes Path:" + relPath);
		URL resource = ClassLoader.getSystemClassLoader().getResource(relPath);
		System.out.println("ClassDiscovery: Resource = " + resource);
		if (resource == null) {
			throw new RuntimeException("No resource for " + relPath);
		}
		fullPath = resource.getFile();
		System.out.println("ClassDiscovery: FullPath = " + resource);

		try {
			directory = new File(resource.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(
					pkgname
							+ " ("
							+ resource
							+ ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...",
					e);
		} catch (IllegalArgumentException e) {
			directory = null;
		}
		System.out.println("ClassDiscovery: Directory = " + directory);

		if (directory != null && directory.exists()) {
			// Get the list of the files contained in the package
			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {
				// we are only interested in .class files
				if (files[i].endsWith(".class")) {
					// removes the .class extension
					String className = pkgname + '.'
							+ files[i].substring(0, files[i].length() - 6);
					System.out.println("ClassDiscovery: className = "
							+ className);
					try {
						classes.add(Class.forName(className));
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(
								"ClassNotFoundException loading " + className);
					}
				}
			}
		} else {
			try {
				String jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar")
						.replaceFirst("file:", "");
				JarFile jarFile = new JarFile(jarPath);
				Enumeration<JarEntry> entries = jarFile.entries();

				Path path = Paths.get(OUTPUT_FILE_NAME);
				try (BufferedWriter writer = Files.newBufferedWriter(path,
						ENCODING)) {

					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						String entryName = entry.getName();
						if (entryName.startsWith(relPath)
								&& entryName.length() > (relPath.length() + "/"
										.length())) {
							/*System.out.println("ClassDiscovery: JarEntry: "
									+ entryName);*/
							String className = entryName.replace('/', '.')
									.replace('\\', '.').replace(".class", "");
							/*System.out.println("ClassDiscovery: className = "
									+ className);*/

							Class<?> newClass = null;
							try {
								// classes.add(Class.forName(className));
								newClass = Class.forName(className);
							} catch (ClassNotFoundException e) {
							} catch (ExceptionInInitializerError e){
							} catch (NoClassDefFoundError e) {
							}

							/* Write the methods to a file */
							if (newClass != null) {
								/*writeLargerTextFile(OUTPUT_FILE_NAME,
										GetMethods(newClass));*/
								ArrayList<String> aLines = GetMethods(newClass);
								for (String line : aLines) {
									writer.write(line);
									writer.newLine();
								}

							}
						}
					}
				}

			} catch (IOException e) {
				throw new RuntimeException(pkgname + " (" + directory
						+ ") does not appear to be a valid package", e);
			}
		}
		return classes;
	}

	public void writeLargerTextFile(String aFileName, List<String> aLines)
			throws IOException {
		Path path = Paths.get(aFileName);
		try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)) {
			for (String line : aLines) {
				writer.write(line);
				writer.newLine();
			}
		}
	}
}
