package org.minerift.ether.database.sql;

import com.google.common.net.HostAndPort;

public class DatabaseConnectionSettings {

    public static Builder builder() {
        return new Builder();
    }

    private final SQLDialect dialect;
    private final HostAndPort address;
    private final String username;
    private final String password;
    private final String dbName;


    private DatabaseConnectionSettings(Builder builder) {
        this.dialect = builder.dialect;
        this.address = builder.address;
        this.username = builder.username;
        this.password = builder.password;
        this.dbName = builder.dbName;
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public HostAndPort getAddress() {
        return address;
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
        private HostAndPort address;
        private String username;
        private String password;
        private String dbName;

        private Builder() {}

        public Builder setDialect(SQLDialect dialect) {
            this.dialect = dialect;
            return this;
        }

        public Builder setAddress(HostAndPort address) {
            this.address = address;
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
