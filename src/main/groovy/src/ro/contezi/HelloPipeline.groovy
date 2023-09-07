package ro.contezi

public class HelloPipeline implements Serializable {

  def context

  public HelloPipeline(context) {
    this.context = context;
  }

  void run() {
    pipeline {
      agent any
      stages {
        stage("Hi") {
          sayHello()
        }
      }
    }
  }

  void sayHello() {
    context.sh "echo 'Hello pipeline with param ${context.params.PARAM}'"
  }
}