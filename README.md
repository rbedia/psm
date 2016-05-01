The Profiling Security Manager (PSM) is a Java Security Manager that automatically
generates security policy permissions when a permission is denied.

PSM is based on the work of Mark Petrovic. See 
http://onjava.com/pub/a/onjava/2007/01/03/discovering-java-security-requirements.html
for more information about how PSM works.

Build
```
mvn install
```

Then, run your application under it using something like
```
java -Djava.security.manager=psm.ProfilingSecurityManager -Dpsm.log.file=psm.policy -Djava.security.policy==security.policy -cp <path to psm.jar>:<your app classpath> your.main.Class
```

Process the output into a condensed policy file:
```
java -jar psm.jar < psm.policy > security.policy
```
