BUILD_DIR=${BUILD_NUMBER}

OMS_BASE=/users/gen/omswrk1/JEE/OMS/logs/OmsDomain/OmsServer
OMS_WORKSPACE=${OMS_BASE}/sanity_logs
OMS_BUILD=${OMS_WORKSPACE}/${JOB_NAME}_${BUILD_NUMBER}

for ENV in SIT1 QA1 UAT1 HF1; do
  PROJECT="${CD}/xml/Charter -Phase3 Mec Model Version50-project-Sanity-readyapi-project-${ENV}.xml"
  REPORT_DIR="${CD}/${BUILD_DIR}/${ENV}/junit_report"

  if [ "${ENV}" = "SIT1" ]; then
    HOST=mwhlvchca01
  elif [ "${ENV}" = "QA1" ]; then
    HOST=mwhlvchca02
  elif [ "${ENV}" = "UAT1" ]; then
    HOST=mwhlvchca03
  elif [ "${ENV}" = "HF1" ]; then
    HOST=mwhlvchca04
  fi

  ssh omswrk1@${HOST} \
    "ps -eo pid,etimes,cmd | awk '$2 >= 21600 && $0 ~ /tail -fn 0 \/users\/gen\/omswrk1\/JEE\/OMS\/logs\/OmsDomain\/OmsServer\/weblogic/ {print $1}' | xargs -r kill -9"

  ssh omswrk1@${HOST} \
    "mkdir -p ${OMS_BUILD}"
    
  scp /java/remote/LogSearch.java \
    omswrk1@${HOST}:${OMS_WORKSPACE}
  ssh omswrk1@${HOST} \
    "javac ${OMS_WORKSPACE}/LogSearch.java"

  ssh omswrk1@${HOST} \
    "tail -fn 0 \$(ls -t ${OMS_BASE}/weblogic.*.log | head -1) \
      > ${OMS_BUILD}/${ENV}.log \
      2>&1 & echo \$! > ${OMS_BUILD}/${ENV}.pid"

  /opt/SoapUI-5.5.0/bin/testrunner.sh \
    -s "Sanity - Guided Flow - New Connect" \
    -c "TP (DP Offer 19867641 TV Select Signature + Internet Gig + 1Y Voice Bundle Offer 295671251) SPP 4.0 NC - Add Premium Channel MGM"
    -j -f "${REPORT_DIR}" \
    -r "${PROJECT}" \
  || true

  ssh omswrk1@${HOST} \
    "kill \$(cat ${OMS_BUILD}/${ENV}.pid)"

  ssh omswrk1@${HOST} \
    "java -cp ${OMS_WORKSPACE} LogSearch \
      ${OMS_BUILD}/${ENV}.log"
done