package ro.contezi

public class Pipeline implements Serializable {

  def context

  public Pipeline(context) {
    this.context = context;
  }

  void run() {
    context.node {
      context.sh "echo 'Hello pipeline'"
    }
  }
}