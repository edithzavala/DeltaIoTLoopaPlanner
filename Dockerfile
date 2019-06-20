FROM java:8
VOLUME /tmp
EXPOSE 8093
ADD /build/libs/DeltaIoTLoopaPlanner.jar DeltaIoTLoopaPlanner.jar
ENTRYPOINT ["java","-jar","DeltaIoTLoopaPlanner.jar"]
