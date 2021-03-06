package ch.cern.properties.source.types;

import ch.cern.components.RegisterComponent;
import ch.cern.properties.ConfigurationException;
import ch.cern.properties.Properties;
import ch.cern.properties.source.PropertiesSource;

@RegisterComponent("file")
public class FilePropertiesSource extends PropertiesSource {

	private static final long serialVersionUID = -2444021351363428469L;
	
	private String path;
	
	@Override
	public void config(Properties properties) throws ConfigurationException {
		path = properties.getProperty("path");
	}

	@Override
	public Properties load() throws Exception {
		return Properties.fromFile(path);
	}

}
