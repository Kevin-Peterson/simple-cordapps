#!/usr/bin/env bash
cd ../../build/nodes/Notary/

nohup java "-Xmx512m" "-Dname=Notary-corda.jar" "-Dcapsule.jvm.args=-javaagent:drivers/jolokia-jvm-1.3.7-agent.jar=port=7005,logHandlerClass=net.corda.node.JolokiaSlf4Adapter" "-jar" "corda.jar" >logNotary.log & exit