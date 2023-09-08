package src.ro.contezi

public class DeployBackend {
    def context
    def deploymentScript
    def target
    def servers
    def testServers

    public DeployBackend(context, deploymentScript, target, servers, testServers) {
        this.context = context
        this.deploymentScript = deploymentScript
        this.target = target
        this.servers = servers
        this.testServers = testServers
    }

    public DeployBackend(context, deploymentScript, target, servers) {
        this(context, deploymentScript, target, servers, ['RQUI'])
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
            vulnerabilityCheck()
            vulnerabilityPublish()
            archiveArtifacts()
            if (context.params.MODALITA == 'TEST') {
                installInTest()
            }
            deployToRepository()
            if (context.params.MODALITA == 'PROD') {
                deployToProduction()
            }
        }
    }

    protected void compile() {
        context.stage('Compilazione') {
            context.echo "MODALITA = ${context.params.MODALITA}"
            context.echo "PATH = ${context.env.PATH}"
            context.echo "M2_HOME = ${context.env.M2_HOME}"
            context.configFileProvider([
                    context.configFile(fileId: 'global-maven-settings', targetLocation: 'testfile.xml')
            ]) {
                context.echo "dentro config"
                context.sh "mvn package -s testfile.xml"
            }
        }
    }

    protected void vulnerabilityCheck() {
        context.stage('Controllo vulnerabilità') {
            context.dependencyCheck additionalArguments: """
                --disableOssIndex
                --disableCentral
                --disableYarnAudit
                --suppression file:////var/lib/jenkins/workspace/gvPom/src/main/resources/suppressions.xml
                --failOnCVSS 8
            """, odcInstallation: '7.1.1'
        }
    }

    protected void vulnerabilityPublish() {
        context.dependencyCheckPublisher pattern: "**/dependency-check-report.xml"
    }

    protected void archiveArtifacts() {
        context.stage('Archiviazione') {
            context.archiveArtifacts artifacts: "target/*.${target}", followSymlinks: false
        }
    }

    protected void installInTest() {
        context.stage('Installo in TEST') {
            def server = context.params.TEST_SERVER.toLowerCase()
            context.echo "Installo su ${server}..."
            context.sh "${deploymentScript} ${server}"
        }
    }

    protected void deployToRepository() {
        context.configFileProvider([
                context.configFile(fileId: 'global-maven-settings', targetLocation: 'testfile.xml')
        ]) {
            context.sh "mvn deploy -s testfile.xml"
        }
    }

    protected void deployToProduction() {
        def mappedServers = [:]
        servers.each {
            server ->
                mappedServers[server] = {
                    context.stage(server) {
                        context.echo "Installo su ${server}..."
                        context.sh "${deploymentScript} ${server}"
                    }
                }
        }
        context.stage('Installo in produzione') {
            context.parallel(mappedServers)
            context.mail bcc: '', body: "Autore: ${context.BUILD_USER_ID}(${context.BUILD_USER}), Build Number: ${context.BUILD_NUMBER}", cc: '', from: 'Jenkins', replyTo: '', subject: "Deploy in Produzione ${context.JOB_NAME}", to: 'areappt@gruppoveronesi.com'
        }
    }


    public void run() {
        preparePipeline()
        stages()
    }
}
