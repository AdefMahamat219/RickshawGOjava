🛺 RickshawGo

A JavaFX desktop app for calculating rickshaw fares and planning routes in Board Bazar, Gazipur, Bangladesh — fully offline, using real GPS-verified locations.

Features
Interactive map (OpenLayers + OpenStreetMap) with clickable locations
Shortest-path routing via Dijkstra's algorithm
Dynamic fares with night/peak-hour surcharges
Configurable pricing, saved locations, and ride history — all persisted to a local MySQL database
Tech Stack

Java · JavaFX · OpenLayers (via WebView) · MySQL

Project Structure
src/
├── application/   → Main.java (entry point)
├── algorithm/     → Graph, Dijkstra, RoadData (routing)
├── model/         → FareCalculator, Location, RideHistory
├── storage/       → DB connection + DAOs (settings, history, saved locations)
└── ui/            → Screens: Welcome, Map, Settings, History, Saved
Fare Formula
Base rate + per-km rate (both configurable)
Night multiplier (default ×1.5) — auto-detected using a configurable night-hour window
Peak-hour multiplier (default ×1.2) — fixed morning/evening windows
Database Setup

Requires a local MySQL database rickshawgo with tables: settings, ride_history, saved_locations. Update credentials in storage/DBConnection.java.

Running
Start MySQL, ensure the rickshawgo database exists.
Add JavaFX SDK + MySQL Connector/J to the classpath.
Run application.Main.
Notes
Locations are real, GPS-verified places in Board Bazar (not fictional coordinates).
Route lines follow approximate real road shapes for longer routes; short edges render as straight lines.
MapController.java / MapData.java / MapScreen.fxml are legacy/unused — the app runs on MapWebController + map.html.


video link: https://youtu.be/OXH-eNZHmOo
