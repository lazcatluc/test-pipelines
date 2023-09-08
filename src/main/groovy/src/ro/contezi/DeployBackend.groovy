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

    private void stages() {
        context.node {
            def jdk = context.tool name: 'jdk-8u202'
            context.env.JAVA_HOME = "${jdk}"
            def maven = context.tool name: 'maven-3.8.5'
            context.env.M2_HOME = "${maven}"
            context.env.MAVEN_HOME = "${maven}"
            context.env.PATH = "${jdk}/bin:${maven}/bin:${context.env.PATH}"
            compile()
        }
    }

    protected void compile() {
        context.stage('Compilazione') {
            context.steps {

            }
        }
    }

    void run() {
        preparePipeline()
        stages()
    }
}
