BUILD_DIR=${BUILD_NUMBER}
DATA_DIR=${BUILD_DIR}/data
REMOTE_BUILD=/users/gen/omswrk1/JEE/OMS/logs/OmsDomain/OmsServer/sanity_logs/${JOB_NAME}_${BUILD_NUMBER}

mkdir -p "${DATA_DIR}"

for ENV in SIT1 QA1 UAT1 HF1; do
  if [ "${ENV}" = "SIT1" ]; then
    HOST=mwhlvchca01
  elif [ "${ENV}" = "QA1" ]; then
    HOST=mwhlvchca02
  elif [ "${ENV}" = "UAT1" ]; then
    HOST=mwhlvchca03
  elif [ "${ENV}" = "HF1" ]; then
    HOST=mwhlvchca04
  fi
  
  scp omswrk1@${HOST}:${REMOTE_BUILD}/*.date \
    "${DATA_DIR}"
  scp omswrk1@${HOST}:${REMOTE_BUILD}/*.id \
    "${DATA_DIR}"
done

REPORT_DIR="${CD}/${BUILD_DIR}/junit_report

java -cp "java\local\target\classes;java\local\target\dependency\*" \
  com.amdocs.sanity.SanityRunner \
  "${REPORT_DIR}" \
  "${DATA_DIR}" \
  "${BUILD_DIR}"