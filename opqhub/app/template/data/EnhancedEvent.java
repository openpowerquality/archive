package template.data;

import models.Event;
import utils.PqUtils;

public class EnhancedEvent {
  public Event event;
  public PqUtils.IticRegion iticRegion;

  public EnhancedEvent(Event event, PqUtils.IticRegion iticRegion) {
    this.event = event;
    this.iticRegion = iticRegion;
  }
}
