/*
  This file is part of OPQHub.

  OPQHub is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  OPQHub is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with OPQHub.  If not, see <http://www.gnu.org/licenses/>.

  Copyright 2014 Anthony Christe
 */

package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Provides utilities for switching between different units of time.
 */
public class DateUtils {
  /**
   * Number of milliseconds in certain time frames
   */
  public enum TimeUnit {
    Minute  (60000L),
    Hour    (3600000L),
    Day     (86400000L),
    Week    (604800000L),
    Month   (2592000000L),
    Year    (31536000000L);

    final private Long milliseconds;

    TimeUnit(Long milliseconds) {
      this.milliseconds = milliseconds;
    }

    public Long getMilliseconds() {
      return this.milliseconds;
    }
  }

  public static Long getPastTime(Long timestamp, TimeUnit timeUnit) {
    return timestamp - timeUnit.getMilliseconds();
  }

  public static Long getPastTime(Long timestamp, TimeUnit timeUnit, double multiplier) {
    return (long) (timestamp - (timeUnit.getMilliseconds() * multiplier));
  }

  public static Long getMillis() {
    return new Date().getTime();
  }

  /**
   * Converts milliseconds since the epoch into a human readable format.
   * @param millisecondsSinceEpoch Milliseconds since the epoch.
   * @return Human formatted string of timestamp.
   */
  public static String toDateTime(Long millisecondsSinceEpoch) {
    Date dateStr = new Date(millisecondsSinceEpoch);

    return new SimpleDateFormat("HH:mm:ss [dd MMM YYYY]").format(dateStr);
  }

  public static String toShortDateTime(Long millisecondsSinceEpoch) {
    Date dateStr = new Date(millisecondsSinceEpoch);
    return new SimpleDateFormat("HH:mm:ss MM/dd/YY").format(dateStr);
  }
}
