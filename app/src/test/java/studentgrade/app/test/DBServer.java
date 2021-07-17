package studentgrade.app.test;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DBServer implements QuarkusTestResourceLifecycleManager {

    //private MariaDBContainer<?> DBSERVER;
    private GenericContainer DBSERVER;

    private String imageName = (System.getProperty("dbserver.imageName")==null)?"mariadb:10.6.3":System.getProperty("dbserver.imageName");
    private long timeout = (System.getProperty("dbserver.secondsTimeout")==null)?900:Long.parseLong(System.getProperty("dbserver.secondsTimeout"));
    private Duration startupTimeout = Duration.ofSeconds(timeout);
   
    private static final String MYSQL_READY_FOR_CONNECTIONS_MESSAGE = ".*ready for connections.*\\n";
    private static final int MYSQL_READY_FOR_CONNECTIONS_COUNT = 4;
    private static final String MYSQL_INIT_PROCESS_DONE_MESSAGE = ".*init process done. Ready for start up.*\\n";
    private static final int MYSQL_INIT_PROCESS_DONE_COUNT =1;
    
    private static final String MARIADB_READY_FOR_CONNECTIONS_MESSAGE = ".*ready for connections.*\\n";
    private static final int MARIADB_READY_FOR_CONNECTIONS_COUNT = 2;
    private static final String MARIADB_INIT_PROCESS_DONE_MESSAGE = ".*init process done. Ready for start up.*\\n";
    private static final int MARIADB_INIT_PROCESS_DONE_COUNT =1;
    
    @Override
    public Map<String, String> start() {
        if (imageName==null)    {
            throw new IllegalStateException("Cannot find system property keycloak.imageName.");
        }//if
       
        log.info("DBSERVER init.." + imageName);
 
        /*
        * MYSQL
        * docker run --rm --name=mysql1 -p 3306:3306 -e "MYSQL_ROOT_PASSWORD=quarkus_test" -e "MYSQL_DATABASE=quarkus_test" -e "MYSQL_USER=quarkus_test" -e "MYSQL_PASSWORD=quarkus_test" -it mysql:8.0.25
        *
        * MARIADB
        * docker run --rm --name=mysql1 -p 3306:3306 -e "MYSQL_ROOT_PASSWORD=quarkus_test" -e "MYSQL_DATABASE=quarkus_test" -e "MYSQL_USER=quarkus_test" -e "MYSQL_PASSWORD=quarkus_test" -it mariadb:10.6.3
        */
        DBSERVER =  new FixedHostPortGenericContainer(imageName)
                .withFixedExposedPort(3306, 3306)
                .withEnv("MYSQL_ROOT_PASSWORD", "quarkus_test")
                .withEnv("MYSQL_DATABASE", "quarkus_test")
                .withEnv("MYSQL_USER", "quarkus_test")
                .withEnv("MYSQL_PASSWORD", "quarkus_test")
                .withLogConsumer(new Slf4jLogConsumer(log))
                .waitingFor(Wait.forListeningPort())
                .waitingFor(Wait.forHealthcheck())
                .waitingFor(Wait.forLogMessage(MARIADB_INIT_PROCESS_DONE_MESSAGE, MARIADB_INIT_PROCESS_DONE_COUNT))
                .waitingFor(Wait.forLogMessage(MARIADB_READY_FOR_CONNECTIONS_MESSAGE, MARIADB_READY_FOR_CONNECTIONS_COUNT))
                .withStartupTimeout(startupTimeout); //15 minutes
 
        log.info("DBSERVER STARTING...");
        DBSERVER.start();
        
        /*
        log.info("jdbcUrl=".concat(DBSERVER.getJdbcUrl()));
        String reactiveUrl = DBSERVER.getJdbcUrl().substring(5); //cutoff jdbc:
        log.info("reactiveUrl=".concat(reactiveUrl));
        System.setProperty("quarkus.datasource.reactive.url", reactiveUrl);
        */
        log.info("DBSERVER READY!!");
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        if (DBSERVER!=null) {
            DBSERVER.stop();
            log.info("DBSERVER STOPPED!!");
        }//if
    }
}