# MAVEN_GOAL=deploy (set by CI when publishing) packages and uploads in one build
mvn clean ${MAVEN_GOAL:-package} -B --no-transfer-progress