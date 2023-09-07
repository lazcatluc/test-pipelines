package ro.contezi

public class HelloPipeline implements Serializable {

  def context

  public HelloPipeline(context) {
    this.context = context;
  }

  void run() {
    context.pipeline {
      context.parameters {
        context.choice(choices: ['A', 'B'], name: 'PARAM')
      }
      context.stages {
        context.stage {
          sayHello()
        }
      }
    }
  }

  void sayHello() {
    context.sh "echo 'Hello pipeline with param ${context.params.PARAM}'"
  }
}