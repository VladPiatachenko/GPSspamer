# GPSspamer

GPSspamer is a Java/Spring Boot application for live aircraft position tracking and controlled GPS spoofing simulation.
The system retrieves real flight telemetry by flight identifier and applies configurable attack models to navigation data,
allowing comparison of true and manipulated coordinates in real time.

The project is designed as a research and experimentation platform for studying GPS spoofing attacks
and for developing and validating detection and mitigation methods.

---

## Overview

Modern UAVs and aircraft navigation systems are vulnerable to GPS spoofing attacks that may cause
position deviation, route manipulation, or loss of situational awareness.
GPSspamer provides a controlled environment in which such attacks can be simulated
on top of real flight data without interfering with actual navigation systems.

The application continuously polls a flight tracking data source, applies a selected attack model,
and exposes both original and spoofed coordinates via a REST API and live data stream.
All observations can be logged for further offline analysis.

---

## Core Capabilities

- Live tracking of aircraft position by flight identifier
- Real-time simulation of GPS spoofing attacks
- Support for multiple attack models:
  - constant bias (position offset)
  - time-dependent drift
  - stochastic noise
- Simultaneous availability of:
  - true coordinates
  - attacked coordinates
- In-memory track history
- CSV logging of full flight traces for offline processing
- Streaming interface for real-time consumers

---

## Attack Model

GPS spoofing behavior is controlled via an attack configuration that defines:

- whether the attack is enabled
- attack type
- numerical parameters of the attack model

Supported attack types:
- **NONE** — no modification
- **BIAS** — constant spatial offset
- **DRIFT** — gradual deviation over time
- **NOISE** — random perturbation with configurable variance

This design allows reproducible experiments and systematic evaluation of detection algorithms.

---

## Architecture

The application follows a modular Spring Boot architecture:

- integration layer for external flight data sources
- attack engine responsible for coordinate manipulation
- session state for managing selected flight and attack configuration
- storage layer for track history
- logging subsystem for persistent experiment records
- REST and streaming interfaces for data access

The system is intentionally kept stateless with respect to long-term persistence,
making it suitable for experimentation, containerized deployment, and extension.

---

## Running the Application

### Requirements

- Java 17 or compatible
- Maven (or Maven Wrapper)

### Local execution

```bash
./mvnw spring-boot:run
