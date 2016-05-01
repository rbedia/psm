The Profiling Security Manager (PSM) is a Java Security Manager that automatically
generates security policy permissions when a permission is denied. This greatly
speeds up the process of creating a policy for an application.

PSM is based on the work of Mark Petrovic. See 
http://onjava.com/pub/a/onjava/2007/01/03/discovering-java-security-requirements.html
for more information about how PSM works.

Build
```
mvn install
```

Run your application with PSM as the security manager. For example:
```
java -Djava.security.manager=psm.ProfilingSecurityManager -Dpsm.log.file=psm.policy -Djava.security.policy==security.policy -cp <path to psm.jar>:<your app classpath> your.main.Class
```

Process the output into a condensed policy file using the post-processor:
```
java -jar psm.jar < psm.policy > security.policy
```

The post-processor also replaces file paths with their corresponding system
properties which will help with making the policy system indendepent. Supported
properties:
* user.home
* java.home
* java.io.tmpdir
* jboss.home.dir
* user.home

jboss.home.dir is not a standard system property so you must explicitly set it
to an appropriate value when running the post-processor.

