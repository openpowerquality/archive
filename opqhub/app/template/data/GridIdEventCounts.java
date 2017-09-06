package template.data;

import utils.PqUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by anthony on 3/18/15.
 */
public class GridIdEventCounts {
  public AtomicInteger okEvents;
  public AtomicInteger moderateEvents;
  public AtomicInteger severeEvents;

  public GridIdEventCounts() {
    this.okEvents = new AtomicInteger(0);
    this.moderateEvents = new AtomicInteger(0);
    this.severeEvents = new AtomicInteger(0);
  }

  public void update(PqUtils.IticRegion iticRegion) {
    switch(iticRegion) {
      case NO_DAMAGE:
        this.moderateEvents.incrementAndGet();
        break;
      case NO_INTERRUPTION:
        this.okEvents.incrementAndGet();
        break;
      case PROHIBITED:
        this.severeEvents.incrementAndGet();
        break;
    }
  }

  @Override
  public String toString() {
    return String.format("%d %d %d", this.okEvents.get(), this.moderateEvents.get(), this.severeEvents.get());
  }
}
