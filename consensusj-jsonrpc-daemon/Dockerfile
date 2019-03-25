FROM oracle/graalvm-ce:1.0.0-rc14 as graalvm
COPY . /home/app/jsonrpcdaemon
WORKDIR /home/app/jsonrpcdaemon
RUN native-image --no-server -cp build/libs/consensusj-jsonrpc-daemon-*-all.jar

FROM frolvlad/alpine-glibc
EXPOSE 8080
COPY --from=graalvm /home/app/jsonrpcdaemon .
ENTRYPOINT ["./jsonrpcdaemon"]
