package ro.contezi

public class HelloPipeline implements Serializable {

  def context

  public HelloPipeline(context) {
    this.context = context;
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
      if (context.params.PARAM == 'A') {
        context.stage("A") {
          context.echo "you picked A"
        }
      }
      context.stage('Parallelized') {
        context.parallel {
          context.stage('First parallel') {
            context.sh('First')
          }
          context.stage('Second parallel') {
            context.sh('Second')
          }
          context.stage('Third parallel') {
            context.sh('Third')
          }
        }
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
    context.sh "echo 'Hello pipeline'"
  }
}