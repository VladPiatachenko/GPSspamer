package sumdu.edu.ua.GPSspamer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class GpSspamerApplication {

		public static void main(String[] args) {
			SpringApplication.run(GpSspamerApplication.class, args);
		}

		@Bean
		public GpsGenerator gpsGenerator() {
			return new GpsGenerator();
		}
}


