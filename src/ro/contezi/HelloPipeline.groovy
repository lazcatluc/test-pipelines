package ro.contezi

public class HelloPipeline implements Serializable {

  def context

  public HelloPipeline(context) {
    this.context = context;
  }

  void run() {
    context.node {
      context.sh "echo 'Hello pipeline'"
    }
  }
}