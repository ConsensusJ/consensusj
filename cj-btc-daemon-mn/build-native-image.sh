set -x
VERSION=0.3.2-SNAPSHOT
../gradlew assemble
java -cp build/libs/cj-btc-daemon-mn-${VERSION}-all.jar io.micronaut.graal.reflect.GraalClassLoadingAnalyzer
native-image --no-server \
             --class-path build/libs/cj-btc-daemon-mn-${VERSION}-all.jar \
             -H:EnableURLProtocols=http \
             -H:ReflectionConfigurationFiles=build/reflect.json \
             -H:IncludeResources="logback.xml|application.yml|META-INF/services/*.*" \
             -H:Name=cj-btc-daemon-mn-graal \
             -H:Class=org.consensusj.daemon.micronaut.Application \
             -H:+ReportUnsupportedElementsAtRuntime \
             -H:+AllowVMInspection \
             -H:-ThrowUnsafeOffsetErrors \
             -H:-UseServiceLoaderFeature \
             --allow-incomplete-classpath \
             --rerun-class-initialization-at-runtime='sun.security.jca.JCAUtil$CachedSecureRandomHolder,javax.net.ssl.SSLContext' \
             --delay-class-initialization-to-runtime=io.netty.handler.codec.http.HttpObjectEncoder,io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder,io.netty.handler.ssl.util.ThreadLocalInsecureRandom,com.sun.jndi.dns.DnsClient,java.util.concurrent.CompletableFuture

