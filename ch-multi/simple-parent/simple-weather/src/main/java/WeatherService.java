package org.sonatype.mavenbook.weather;
 
import org.apache.log4j.PropertyConfigurator;

import java.io.InputStream;


public class WeatherService {

	public WeatherService() {

	}


	public String retrieveForecast(String zip) throws Exception {

		// Configure Log4J
		PropertyConfigurator.configure(WeatherService.class.getClassLoader().getResource("log4j.properties"));
		// Retrieve Data
		InputStream dataIn = new YahooRetriever().retrieve( zip );

		// Parse Data
		Weather weather = new YahooParser().parse( dataIn );

		// Format (Print) Data
		return new WeatherFormatter().format( weather );
	}

}
