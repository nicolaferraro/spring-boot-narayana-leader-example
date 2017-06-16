# Execution steps
1. Deploy database
```
oc new-app \
    -p POSTGRESQL_USER=narayana \
    -p POSTGRESQL_PASSWORD=narayana \
    -p POSTGRESQL_DATABASE=narayana \
    -p DATABASE_SERVICE_NAME=narayana-database \
    --name=narayana-database \
    --template=postgresql-persistent
```
2. Build Spring Boot from [here](https://github.com/gytis/spring-boot/tree/1.5.x-narayana-connection-fixes) (1.5.5.BUILD-SNAPSHOT)
3. Build Narayana master (5.6.2.Final-SNAPSHOT)
4. Deploy to OpenShift

```
    mvn clean fabric8:deploy -Dfabric8.mode=kubernetes
```

# Undeploy application
```
kubectl delete statefulsets/spring-boot-narayana-stateful-set-example
```

# Check object store
1. Open PostgreSQL pod terminal
2. Connect (password: narayana)
```
psql -h narayana-database -U narayana
```
3. Select object store entries
```
select * from actionjbosststxtable;
```

# Clear object store
1. Open PostgreSQL pod terminal
2. Connect (password: narayana)
```
psql -h narayana-database -U narayana
```
3. Select object store entries
```
delete from actionjbosststxtable;
```

# Get entries
```
curl http://spring-boot-narayana-stateful-set-example-test.192.168.64.3.nip.io
```

# Create new entry
```
curl -X POST http://spring-boot-narayana-stateful-set-example-test.192.168.64.3.nip.io?entry=hello
```

# Crash when creating entry
```
curl -X POST http://spring-boot-narayana-stateful-set-example-test.192.168.64.3.nip.io?entry=kill
```
New entry 'kill' should appear after pod is restarted and recovery completes.