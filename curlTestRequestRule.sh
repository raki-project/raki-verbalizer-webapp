#ontology="@koalalabels.owl"
ontology="@koalalabels.owl"
axioms="@koala.owl"
response=$0.json

curl \
	-F ontology=$ontology \
	-F axioms=$axioms \
	-H "charset=utf-8" \
	-o $response \
	http://localhost:9081/rules  > $0.log 2>&1 </dev/null &