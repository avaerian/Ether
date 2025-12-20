package org.minerift.ether.database;

import com.google.common.net.HostAndPort;
import org.minerift.ether.database.sql.SQLDialect;
import org.minerift.ether.debug.Experimental;

@Experimental // experiment with this class being a record
public record DatabaseConnectionSettings(SQLDialect dialect, String url, String username, String password,
                                         String dbName) {

    public static Builder builder() {
        return new Builder();
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
            return new DatabaseConnectionSettings(dialect, url, username, password, dbName);
        }
    }

}
