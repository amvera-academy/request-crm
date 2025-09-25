package avishgreen.amvera.crm.initializer;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Slf4j
@RequiredArgsConstructor
public class LiquibaseMigration {
    private final DataSource dataSource;
    private Liquibase liquibase;

    private void initLiquibaseObject() throws SQLException, LiquibaseException {
//        System.out.println("Инициализация liquibase");
        Connection connection = dataSource.getConnection();
        // Создаем объект Database с использованием JdbcConnection
        JdbcConnection jdbcConnection = new JdbcConnection(connection);
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
        liquibase = new Liquibase("db/changelog/db.changelog-master.yaml",
                new ClassLoaderResourceAccessor(),
                database);
    }

    public void makeMigrationRollbackToTag(String tag) throws LiquibaseException {
        if(liquibase == null) {
            throw new LiquibaseException("ERROR: Rollback - liquibase not initialized");
        }
        final String contexts = "dev,test";
        log.info("Liquibase: [Rollback] tag: "+tag);
        liquibase.rollback(tag, contexts);
    }

    public void makeMigrationUpdate() throws LiquibaseException {
        if(liquibase == null) {
            throw new LiquibaseException("ERROR: Update - liquibase not initialized");
        }
        log.info("Liquibase: [Update]");
        liquibase.update();
    }


    @SneakyThrows
    public void start(){
        initLiquibaseObject();
        makeMigrationUpdate();
//        makeMigrationRollbackToTag("empty-db");

    }

}
