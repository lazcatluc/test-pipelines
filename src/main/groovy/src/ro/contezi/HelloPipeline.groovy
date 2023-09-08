package ro.contezi

public class HelloPipeline implements Serializable {

  def context

  public HelloPipeline(context) {
    this.context = context;
  }

  void run() {
    def jdk = context.tool name: 'jdk8', type: 'JDK'
    context.properties ([
        context.parameters ([
            context.choice(choices: ['A', 'B'], name: 'PARAM')
        ]),
        context.buildDiscarder context.logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '30', numToKeepStr: '10'),


    ])
    context.node {
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