#!/usr/bin/env bash
bash -c "./notary.sh"
bash -c "sleep 5"
echo 'Corda notary started...'
bash -c "./partyA.sh"
bash -c "./partyB.sh"
bash -c "sleep 15"
echo 'Corda is up and running...'
#bash -c "./startSpring.sh"
bash -c "sleep 35"
echo 'SpringServer is up and running!'