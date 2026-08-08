# P360ContingencyServices

This repository contains P360ContingencyServices, the web layer of the P360 middleware. Its responsibility is to expose HTTP servlet endpoints, receive requests from external consumers, resolve runtime configuration, and delegate business processing to P360ExternalProcedures.

## Overview

The project is implemented as an Eclipse Dynamic Web Project for Apache Tomcat. Endpoints are declared with servlet annotations and are organized under Java packages for P360 REST services and supporting integrations.

At runtime, the application acts as an entry point between consumers and the internal P360 integration layer. It handles request parsing, response encoding, and servlet-level concerns, while the reusable processing logic remains in P360ExternalProcedures.

## Project Structure

```text
src/main/java/       Servlet source code and HTTP entry points
src/main/webapp/     Web application metadata
src/resources/       Application configuration resources
build/classes/       Generated Eclipse build output
```

## Main Responsibilities

- Expose REST-style servlet endpoints for P360-related workflows.
- Parse query parameters, request bodies, and uploaded or posted content.
- Apply servlet-level response settings such as JSON content type and UTF-8 encoding.
- Read runtime properties through the shared configuration mechanism.
- Delegate core business logic, P360 API calls, file processing, and integrations to P360ExternalProcedures.

## Build and Runtime

This project does not use Maven or Gradle. It is built and deployed through Eclipse.

1. Import P360ExternalProcedures first.
2. Import P360ContingencyServices. In Eclipse, the local project name may appear as `Contingencia`.
3. Confirm that P360ContingencyServices has a project dependency on P360ExternalProcedures.
4. Configure Apache Tomcat v9 in Eclipse.
5. Deploy and run the web project from Eclipse.

The project targets JavaSE-17 in its Eclipse classpath and depends on local application libraries configured outside the repository.

## Configuration

Runtime behavior is controlled through externalized properties. Required values include the P360 base URL, authentication data, cache/work directories, feature flags, and integration settings.

Do not store production credentials, tokens, private keys, service account files, or environment-specific paths in source control. Each environment should provide its own configuration file and required filesystem locations.

## Development Notes

Keep servlet classes focused on HTTP concerns. New or changed business rules should normally be implemented in P360ExternalProcedures and invoked from this project.

Useful discovery commands:

```powershell
rg -n "@WebServlet" src\main\java
rg -n "PropertiesManager.get" src
```

Before deploying a change, validate the affected endpoint with representative non-production requests and review the JSON response, status code, logs, and any downstream P360 side effects.

## Environment Warnings

This project assumes a prepared runtime environment. A new developer or server may need local JAR dependencies, Tomcat configuration, external property files, writable cache directories, and access to P360 or related integration systems. Confirm these prerequisites before troubleshooting application behavior.

## Maintainers

- Juan Capiz | jcapizc@liverpool.com.mx
- Alfonso de la Rosa | jose.aguirre@isol.dev
