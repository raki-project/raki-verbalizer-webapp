# A simple Web application for the RAKI project

This application serves as an interface for the RAKI project.

By default the port is set to 9081 and the max file sizes are set to 1GB in `src/main/resources/application.properties`.

## Possible requests

### Verbalize with a trained model
This endpoint will apply a trained model.

Send a POST request to `http://<host>:<port>/verbalize` with the following parameters:

1. `ontology`
  - an ontology file
2. `axioms`
  - an axioms file
3. `response`
  - a file name to store the response


For instance:
```bash
ontology="@exampleOntology.owl"
axioms="@exampleAxioms.owl"
response="exampleResponse.json"

curl \
	-F ontology=$ontology \
	-F axioms=$axioms \
	-H "charset=utf-8" \
	-o $response \
	http://localhost:9081/verbalize
```

### Verbalize with a rules-based model
This endpoint will apply a rule-based model.

Send a POST request to `http://<host>:<port>/rules` with the same parameters as above.

### Information
Gets some information about this application.

Send a GET request to `http://<host>:<port>/info`

For instance: `curl http://localhost:9081/info` will respond with some like
```
{"version":"0.0.1-SNAPSHOT"}   
```
More information will be added soon.
