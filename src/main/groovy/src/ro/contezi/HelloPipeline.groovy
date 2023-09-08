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
            context.agent context.any,
            context.parameters([
                    context.choice(choices: ['A', 'B'], name: 'PARAM')
            ])
    ])
  }

  private void stages() {
    context.node {
      def jdk = context.tool name: 'jdk8'
      context.stage("Build") {
        context.env.JAVA_HOME = "${jdk}"
        context.env.PATH = "${jdk}/bin:${context.env.PATH}"
        context.sh "echo ${context.env.PATH}"
        context.sh "java -version"
      }
      context.stage("Hi") {
        sayHello()
      }
    }
  }

  void sayHello() {
    context.sh "echo 'Hello pipeline'"
  }
}