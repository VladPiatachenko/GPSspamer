# GPS Spoofing Simulator

Java-based GPS coordinate simulator with spoofing attack injection.  
This project is part of a research framework for detecting GPS spoofing attacks in UAVs. It provides a backend service that simulates drone GPS telemetry and allows injection of spoofed data on selected axes (latitude, longitude, altitude).

## Features

- REST API that serves real-time simulated GPS data
- Optional spoofing of `lat`, `lon`, `alt` coordinates
- Configurable attack mode via `PUT /gps/attack`
- Can be extended for real-time simulation and analysis