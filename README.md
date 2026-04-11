# pricetracker-mobileapp
**Supermarket Price Tracker**

A comprehensive mobile solution designed to provide transparent, real-time pricing and availability data for major supermarket chains across Serbia. By combining Official Open Data with Crowdsourced User Reports, the app empowers users to find the most cost-effective and convenient locations for their daily shopping.

**Overview**

In an era of fluctuating prices, this application serves as a central hub for grocery information. It leverages the official "Decree on Mandatory Records and Submission of Prices" datasets provided by the Office for IT and eGovernment of Serbia, supplemented by real-time community verification.

**Key Features**

Smart Search & Comparison: Search by name, brand, or barcode. View a side-by-side price comparison of a single product across all major Serbian retail chains.

Price History & Analytics: Interactive graphical displays showing price trends over time, including the duration and frequency of promotional (action) prices.

Crowdsourced Real-time Status: Users can report the "on-shelf" reality of a store:

Report missing or sold-out items.

Verify if the app's price matches the shelf price.

Submit price discrepancy reports with photo verification and GPS location tagging.

Store Integration: Google Maps integration to locate the nearest retail objects and view store-specific availability based on other users' recent reports.

Smart Shopping Lists: Create multiple lists. The app calculates the cheapest store for your entire list or the closest store containing all items.

**Technical Stack**

Backend
Framework: Ktor (Kotlin)

Database: MongoDB

Data Processing: Automated daily synchronization with data.gov.rs (Open Data Portal) and auxiliary web resources for non-regulated items.

Android Client
UI: Jetpack Compose

Dependency Injection: Hilt

Networking: Retrofit with OkHttp

Local Persistence: Room Database (for offline caching and shopping lists)

Architecture: Adheres to Modern Android App Architecture (Layered Architecture: UI, Domain, and Data layers) utilizing the MVVM (Model-View-ViewModel) pattern.

**Data Sources**

The application's reliability is built on a multi-tier data strategy:

Primary: Official Serbian Price Registry (.csv) - updated weekly.

Secondary: Alternative sources such as retail websites.

Tertiary: Verified user-submitted photos and reports.

**Architecture Overview**

The project follows a clean, decoupled architecture to ensure scalability and testability:

Presentation Layer: Built with Jetpack Compose, handling UI state via ViewModels.

Domain Layer: Contains business logic, UseCases, and entity models.

Data Layer: Implements the Repository pattern, managing data rotation between the Ktor API (Remote) and Room (Local Cache).

Implementation Note

This repository contains both the Ktor backend service and the Android application source code. The first commit of the actual application source code will happen in the near future.
