BUILD_DIR=${BUILD_NUMBER}
ERROR_DIR=${BUILD_DIR}/error_logs
REMOTE_BUILD=/users/gen/omswrk1/JEE/OMS/logs/OmsDomain/OmsServer/sanity_logs/${JOB_NAME}_${BUILD_NUMBER}

mkdir -p "${ERROR_DIR}"

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
  
  scp omswrk1@${HOST}:${REMOTE_BUILD}/*.err \
    "${ERROR_DIR}"
done

java -cp "java\local\target\classes;java\local\target\dependency\*" ^
  com.amdocs.sanity.SanityRunner ^
  --config config\sanity.properties ^
  --buildDir %BUILD_DIR% ^
  --jobName "%JOB_NAME%_#%BUILD_NUMBER%" ^
  --type "%SANITY_TYPE%" ^
  --env %ENV% ^
  --tester "%TESTER%" ^
  --project OE ^
  --dmp x.x.x.x