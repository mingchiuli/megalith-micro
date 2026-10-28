package wiki.chiu.micro.search.application.port.in;

public interface BootstrapSearchIndexes {

    void bootstrap(int batchSize);
}
