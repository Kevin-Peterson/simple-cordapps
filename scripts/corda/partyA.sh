#!/usr/bin/env bash
cd ../../build/nodes/PartyA/

nohup java "-Xmx512m" "-Dname=PartyA-corda.jar" "-Dcapsule.jvm.args=-javaagent:drivers/jolokia-jvm-1.3.7-agent.jar=port=7005,logHandlerClass=net.corda.node.JolokiaSlf4Adapter" "-jar" "corda.jar" >logPartyA.log & exit