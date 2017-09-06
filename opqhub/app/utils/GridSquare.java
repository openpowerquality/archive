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

import java.util.LinkedList;
import java.util.List;

/**
 * Utility class used for mapping a gridId -> set a metrics about that grid square.
 */
public class GridSquare {
  public class IticPoint {
    public final double voltageValue;
    public final long duration;

    public IticPoint(final long duration, final double voltageValue) {
      this.voltageValue = voltageValue;
      this.duration = duration;
    }

    @Override
    public String toString() {
      return "[" + this.duration + "," + this.voltageValue + "]";
    }
  }

  public String gridId;
  public int numDevices = 0;
  public int numAffectedDevices = 0;
  public int numFrequencyEvents = 0;
  public int numVoltageEvents = 0;
  public final List<IticPoint> iticPoints = new LinkedList<>();

  public void addIticPoint(long duration, double voltage) {
    iticPoints.add(new IticPoint(duration, voltage));
  }
}
