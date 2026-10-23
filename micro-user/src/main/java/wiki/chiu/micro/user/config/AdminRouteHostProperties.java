package wiki.chiu.micro.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Resolves seeded administration routes to the current deployment's service hosts.
 */
@ConfigurationProperties("megalith.admin-routes")
public class AdminRouteHostProperties {

    private final Hosts hosts = new Hosts();

    public Hosts getHosts() {
        return hosts;
    }

    public static class Hosts {

        private String auth = "micro-auth";
        private String user = "micro-user";
        private String blog = "micro-blog";
        private String sync = "micro-sync-rs";
        private String exhibit = "micro-exhibit";
        private String search = "micro-search";

        public String getAuth() {
            return auth;
        }

        public void setAuth(String auth) {
            this.auth = auth;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getBlog() {
            return blog;
        }

        public void setBlog(String blog) {
            this.blog = blog;
        }

        public String getSync() {
            return sync;
        }

        public void setSync(String sync) {
            this.sync = sync;
        }

        public String getExhibit() {
            return exhibit;
        }

        public void setExhibit(String exhibit) {
            this.exhibit = exhibit;
        }

        public String getSearch() {
            return search;
        }

        public void setSearch(String search) {
            this.search = search;
        }
    }
}
