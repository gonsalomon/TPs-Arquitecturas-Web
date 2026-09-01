package integrador_1.factory;

import java.sql.Connection;

public interface ConnectionManager {
    Connection getConnection();
    void shutdown();
}
