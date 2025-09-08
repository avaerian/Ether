package org.minerift.ether.database;

import com.google.common.net.HostAndPort;
import org.minerift.ether.database.sql.SQLDialect;

public class DatabaseConnectionSettings {

    public static Builder builder() {
        return new Builder();
    }

    private final SQLDialect dialect;
    private final String url;
    private final String username;
    private final String password;
    private final String dbName;


    private DatabaseConnectionSettings(Builder builder) {
        this.dialect    = builder.dialect;
        this.url        = builder.url;
        this.username   = builder.username;
        this.password   = builder.password;
        this.dbName     = builder.dbName;
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDbName() {
        return dbName;
    }

    public static class Builder {

        private SQLDialect dialect;
        private String url;
        private String username;
        private String password;
        private String dbName;

        private Builder() {
            this.dialect = null;
            this.url = "";
            this.username = "";
            this.password = "";
            this.dbName = "";
        }

        public Builder setDialect(SQLDialect dialect) {
            this.dialect = dialect;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        @Deprecated
        public Builder setUrl(HostAndPort addr) {
            this.url = addr.toString();
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setDbName(String dbName) {
            this.dbName = dbName;
            return this;
        }

        public DatabaseConnectionSettings build() {
            return new DatabaseConnectionSettings(this);
        }
    }

}
