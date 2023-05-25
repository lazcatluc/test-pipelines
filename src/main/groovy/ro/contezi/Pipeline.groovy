package ro.contezi

class Pipeline {

  def context

  public Pipeline(context) {
    this.context = context;
  }

  void run() {
    context.sh "echo 'Hello pipeline'"
  }
}