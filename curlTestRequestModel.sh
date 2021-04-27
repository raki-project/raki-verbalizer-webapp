ontology="@koala.owl"
axioms="@koala.owl"
response=$0.json

curl \
	-F ontology=$ontology \
	-F axioms=$axioms \
	-H "charset=utf-8" \
	-o $response \
	http://localhost:4443/verbalize  > $0.log 2>&1 </dev/null &