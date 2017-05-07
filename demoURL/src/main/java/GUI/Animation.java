package GUI;


/**
 * Created with ♥ by Max on 07/05/2017.
 **/
public class Animation extends Thread {

  private volatile boolean animate = false;

  public Animation() {
    this.setDaemon(true);
  }

  private void startAnimation() {
    while (true) {
      System.out.println(animate);
      while (animate) {
        AutoSuggestion.displayCount("Loading.");
        System.out.println("Loading.");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        AutoSuggestion.displayCount("Loading..");
        System.out.println("Loading...");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        AutoSuggestion.displayCount("Loading...");
        System.out.println("Loading..");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        AutoSuggestion.displayCount("Loading..");
        System.out.println("Loading.");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void setAnimate(boolean animate) {
    this.animate = animate;
  }
}
