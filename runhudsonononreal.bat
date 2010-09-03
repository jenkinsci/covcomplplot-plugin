call mvn clean package
set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n
set HUDSON_HOME=.\test_binary\hudson_home
rmdir /S /Q test_binary\hudson_home\plugins\covcomplplot
copy target\covcomplplot.hpi .\test_binary\hudson_home\plugins
java -jar test_binary\hudson.war