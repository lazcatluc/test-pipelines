package src.ro.contezi

public class DeployBackend {
    def context
    def servers
    def testServers

    public DeployBackend(context, servers, testServers) {
        this.context = context
        this.servers = servers
        this.testServers = testServers
    }

    public DeployBackend(context, servers) {
        this(context, servers, ['RQUI'])
    }

    private void preparePipeline() {
        def buildDiscarderSettings = [
                buildLogRetention: [
                        daysToKeepStr: '30',
                        numToKeepStr : '10'
                ],
                artifactRetention: [
                        daysToKeepStr: '',
                        numToKeepStr : ''
                ]
        ]
        context.buildDiscarder buildDiscarderSettings
        context.properties([
            context.parameters([
                context.choice(
                        choices: ['TEST', 'PROD'], name: 'MODALITA', description: 'Modalità di installazione, in PROD esegue il deploy su RNOG, RSMB, RVAZ e RVIL, in TEST scegliere il server su cui deployare sotto...'),
                context.choice(
                        choices: testServers, name: 'TEST_SERVER', description: 'Selezionare il server di TEST (solo per modalità TEST)')
            ])
        ])
    }

    void run() {
        preparePipeline()
        stages()
    }
}
