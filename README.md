## Sample car rental system

## Assumptions

- Rental time granularity is day. It's standard car rental like Hertz. Not short time car sharing like Traficar
- You book class of car, not a specific car. Specific car assignment happens at pick-up
- Overbooking is not possible
- There are many branches, but you hve to return car at the same branch that you rented it from

## Architecture

Gradle modules:

- carrental-app - application module (implements data access, infra, app services, APIs, etc.)
- carrental-domain - pure domain model

## Domain model

Domain model consists of 5 modules:

- fleet - management of concrete cars
- reservation - management of reservation
- availability - handles blocking of slots for given car class
- assignment - assigns specific car at pick-up

Flow:

1. Cars of specific types is registered in the **fleet**
2. User checks availability for specified (branch, startDate, endDate) which makes query to **availability**
3. User crates a **reservation** for a given class of car, which is reflected in daily slots in **availability**
4. At pick up, **assignment** is created which connects **reservation** with car from **fleet**.
   Strategy: first available car of given class is picked  
    