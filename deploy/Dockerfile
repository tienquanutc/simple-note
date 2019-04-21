FROM adoptopenjdk/openjdk8-openj9:jdk8u192-b12_openj9-0.11.0-alpine-slim
RUN mkdir /app
WORKDIR /app/
ADD . /app/
EXPOSE 5000
CMD java -Dapp.name=notepad -Dconfig.env=prod -XX:+IdleTuningGcOnIdle -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Djdk.nativeCrypto=false -jar notepad-site.jar