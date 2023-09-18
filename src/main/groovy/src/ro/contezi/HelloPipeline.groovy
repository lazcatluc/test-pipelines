package ro.contezi

public class HelloPipeline implements Serializable {

  def context
  def a
  def b
  def c

  public HelloPipeline(context, options) {
    this.context = context
    this.a = options.a
    this.b = options.b
    this.c = options.c
  }

  void run() {
    preparePipeline()
    stages()
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
                    context.choice(choices: ['A', 'B'], name: 'PARAM')
            ])
    ])
  }

  private void stages() {
    context.node {
      def jdk = context.tool name: 'jdk8'
      build(jdk)
      context.stage("Hi") {
        sayHello()
      }
      context.stage(name: "A", when: context.params.PARAM == 'A') {
        context.echo "you picked A"
      }

      def parallels= ['First', 'Second', 'Third', 'Fourth']
      def mappedServers = [:]
      parallels.each {
        server ->
          mappedServers[server] = {
            context.stage(server) {
              context.echo "${server}"
            }
          }
      }
      context.stage('Parallelized') {
        context.parallel(mappedServers)
      }
    }
  }

  private void build(jdk) {
    context.stage("Build") {
      context.env.JAVA_HOME = "${jdk}"
      context.env.PATH = "${jdk}/bin:${context.env.PATH}"
      context.sh "echo ${context.env.PATH}"
      context.sh "java -version"
    }
  }

  void sayHello() {
    context.sh "echo 'Hello pipeline' context number ${context.BUILD_NUMBER} ${a} ${b} ${c}"
  }
}