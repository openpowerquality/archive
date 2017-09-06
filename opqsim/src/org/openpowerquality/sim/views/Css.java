/*
  This file is part of opq-sim.

  opq-sim is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  opq-sim is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with opq-sim. If not, see <http://www.gnu.org/licenses/>.

  Copyright 2014 Anthony Christe
*/


package org.openpowerquality.sim.views;

import javafx.scene.Node;

/**
 * Provides constants for referencing CSS classes.
 */
public class Css {
  /**
   * Adds a css style to the specified JavaFX node.
   * @param style The css style to apply to the node.
   * @param node The node to apply the css style to.
   */
  public static void addClass(Style style, Node node) {
    node.getStyleClass().add(style.val());
  }

  /**
   * Adds a css style to multiple JavaFX nodes.
   * @param style The css style to apply to the nodes.
   * @param nodes The nodes to apply the css style to.
   */
  public static void addClass(Style style, Node... nodes) {
    for (Node node : nodes) {
      addClass(style, node);
    }
  }

  /**
   * Removes a css style from a JavaFX node.
   * @param style The css style to remove from the node.
   * @param node The node to remove the css style from.
   */
  public static void removeClass(Style style, Node node) {
    node.getStyleClass().remove(style.val());
  }

  /**
   * Removes a css style from multiple JavaFX nodes.
   * @param style The css style to remove from the nodes.
   * @param nodes The nodes to remove the css style from.
   */
  public static void removeClass(Style style, Node... nodes) {
    for (Node node : nodes) {
      removeClass(style, node);
    }
  }

  /**
   * Constants for common CSS styles used in this simulator.
   */
  public static enum Style {
    INVALID_FIELD("invalidField"),
    NON_EDITABLE_FIELD("nonEditableField");

    private final String styleClass;

    Style(String styleClass) {
      this.styleClass = styleClass;
    }

    String val() {
      return this.styleClass;
    }
  }


}
