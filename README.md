# RSO: mikrostoritev-pot

## Prerequisites

Clean and package the project
```
mvn clean package
```

To run locally, create a new network. Run consul and postgres in the background. Then run the app container
```
docker run -p 8081:8081 --network rso -e KUMULUZEE_DATASOURCES0_CONNECTIONURL=jdbc:postgresql://potdb:5432/pot -e KUMULUZEE_CONFIG_CONSUL_AGENT=http://consul:8500 --name pot pot

```
