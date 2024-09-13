# StarterProject

This project serves as a template for quickly setting up new Android projects. It includes a basic structure and initial configurations that allow developers to focus on implementing core features without starting from scratch.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [License](#license)

## Introduction

This starter project provides a foundational setup for Android applications, including essential components and configurations. It is designed to help developers jumpstart their projects with a standardized structure and commonly used libraries.

## Features

- **MVVM Architecture**:
    - Organized code with Model-View-ViewModel architecture to separate concerns.

- **Dependency Injection**:
    - Integrated with Hilt for managing dependencies and promoting modular design.

- **Search Functionality**:
    - Dynamic search capability to find movies or TV shows.
    - Results are updated as the user types.

- **Initial Load State**:
    - Default search term for initial data load when no local cache is available.
    - Shimmer Animation for a smooth loading experience.

- **Error Handling**:
    - Displays an error state with retry options if data fetching fails.

- **Data Caching**:
    - Local storage of fetched data for each search term.
    - Cache is used to provide data quickly if available.

- **Data Refresh**:
    - Refresh data from the remote API if internet connectivity is restored after initial cache load.

- **Connectivity Handling**:
    - Handles internet connectivity issues and shows appropriate error messages if there is no connection and no cached data is available.

- **Pagination** (Good to Have):
    - Implemented pagination to navigate through results efficiently using the Paging library.

- **Modern Android Development Practices** (Good to Have):
    - Utilizes Material 3 design components for a modern UI.
    - Asynchronous operations handled with Kotlin Coroutines & Flow.

## License

Created by Wisnu Andrian - Android Developer

---

Feel free to adjust or expand on these features based on the specifics of your project!