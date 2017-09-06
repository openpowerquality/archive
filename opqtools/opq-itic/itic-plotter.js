var iticPlotter = (function() {
  // Private API

  /**
   * Plot object
   */
  var plot;

  /**
   * Polygon that represents the "prohibted region".
   */
  var upperPoly;

  /**
   * Polygon that represents the "no damage region".
   */
  var lowerPoly;

  var MS_PER_C = Object.freeze(new Object(16.67));

  /**
   * Enumeration for all regions with the ITIC curve
   */
  var Region = Object.freeze({
    NO_INTERRUPTION: "No Interruption",
    NO_DAMAGE: "No Damage",
    PROHIBITED: "Prohibited"
  });

  // PQ Event data series
  /**
   * Events that are in the "no interruption" region of the ITIC curve
   * @type {{color: string, points: {show: boolean}, lines: {show: boolean}, data: Array}}
   */
  var noInterruptionEvents = {
    label: Region.NO_INTERRUPTION,
    color: "#0000FF",
    points: {
      show: true
    },
    lines: {
      show: false
    },
    data: []
  };

  /**
   * Events that are in the "prohibited" region of the ITIC curve
   * @type {{color: string, points: {show: boolean}, lines: {show: boolean}, data: Array}}
   */
  var prohibitedEvents = {
    label: Region.PROHIBITED,
    color: "#FF0000",
    points: {
      show: true
    },
    lines: {
      show: false
    },
    data: []
  };

  /**
   * Events that are in the "no damage" region of the ITIC curve
   * @type {{color: string, points: {show: boolean}, lines: {show: boolean}, data: Array}}
   */
  var noDamageEvents = {
    label: Region.NO_DAMAGE,
    color: "#00FF00",
    points: {
      show: true
    },
    lines: {
      show: false
    },
    data: []
  };

  /**
   * Top data series of ITIC curve.
   */
  var topCurve = {
    color: "#AAAAAA",
    data: [[0.1667, 500],
      [1, 200],
      [3, 140],
      [3, 120],
      [500, 120],
      [500, 110],
      [100001, 110]]
  };

  // Construct the upper polygon
  upperPoly = topCurve.data.slice(0);
  upperPoly.push([100001, 500]);

  /**
   * Bottom data series of ITIC curve.
   */
  var bottomCurve = {
    color: "#AAAAAA",
    data: [[20, 0],
      [20, 70],
      [500, 70],
      [500, 80],
      [10000, 80],
      [10000, 90],
      [100000, 90],
      [100001, 90]
    ]};

  // Construct the lower polygon
  lowerPoly = bottomCurve.data.slice(0);
  lowerPoly.push([100001, 0]);

  /**
   * Options for overall rendering of the plot.
   * @type {{xaxis: {min: number, transform: transform, inverseTransform: inverseTransform}, yaxis: {max: number}, xaxes: {axisLabel: string}[], yaxes: {axisLabel: string}[]}}
   */
  var plotOptions = {
    xaxis: {
      min: 0.001,
      ticks: [[0.01667, "0.01667<br />(0.001c)"], [0.1667, "0.1667<br />(0.01c)"], [1, "1"], [3, "3"], [8.335, "8.335<br />(0.5c)"], [16.67, "<br>(1c)"], [20, "20"], [166.7, "166.7<br />(10c)"], [500, "500<br />(0.5s)"], [1667, "1667<br />(100c)"], [10000, "10000<br />(10s)"]],
      transform: function(v){return Math.log(v + 0.0001);},
      inverseTransform: function(v){return Math.exp(v);}
    },
    yaxis: {
      max: 500
    },
    xaxes: [{
      axisLabel: "Duration (ms)"
    }],
    yaxes: [{
      axisLabel: "% Nominal Voltage"
    }],
    grid: {
      hoverable: true
    }
  };



  /**
   * Uses ray casting to determine if a point is within a polygon.
   * @param poly The polygon to test inclusion on.
   *             this is represented as an array of points (i.e. [[x1, y1], [x2, y2], ..., [xn, yn]]).
   * @param point An object which represents a point (i.e. {x: x1, y: y1}).
   * @returns {boolean} True if the point lies within the polygon, false otherwise.
   */
  function isPointInPoly(poly, point) {
    var c = false;
    for(var i = -1, l = poly.length, j = l - 1; ++i < l; j = i) {
      ((poly[i][1] <= point.y && point.y < poly[j][1]) || (poly[j][1] <= point.y && point.y < poly[i][1]))
      && (point.x < (poly[j][0] - poly[i][0]) * (point.y - poly[i][0]) / (poly[j][0] - poly[i][1]) + poly[i][0])
      && (c = !c);
    }
    return c;
  }

  /**
   * Tests if a point lies within the "prohibited region" of the ITIC curve.
   * @param duration Duration of the event.
   * @param percentNominalVoltage % nominal voltage of the event.
   * @returns {boolean} Returns true if the point lies within this region, false otherwise.
   */
  function isInProhibitedRegion(duration, percentNominalVoltage) {
    return isPointInPoly(upperPoly, {x: duration, y: percentNominalVoltage});
  }

  /**
   * Tests if a point lies within the "no damage region" of the ITIC curve.
   * @param duration Duration of the event.
   * @param percentNominalVoltage % nominal voltage of the event.
   * @returns {boolean} Returns true if the point lies within this region, false otherwise.
   */
  function isInNoDamageRegion(duration, percentNominalVoltage) {
    return isPointInPoly(lowerPoly, {x: duration, y: percentNominalVoltage});
  }

  // Public API
  /**
   * Creates an empty ITIC plot using the passed in div.
   * @param div Div to create an empty ITIC plot out of.
   */
  function init(div) {
    plot = $.plot($(div), [topCurve, bottomCurve, noInterruptionEvents, prohibitedEvents, noDamageEvents], plotOptions);
  }

  /**
   * Adds an event to the ITIC plot.
   * @param duration The duration of the event.
   * @param percentNominalVoltage The % nominal voltage of the event.
   */
  function addPoint(duration, percentNominalVoltage) {
    var events;
    switch(getRegionOfPoint(duration, percentNominalVoltage)) {
      case Region.NO_INTERRUPTION: events = noInterruptionEvents.data; break;
      case Region.PROHIBITED: events = prohibitedEvents.data; break;
      case Region.NO_DAMAGE: events = noDamageEvents.data; break;
    }

    events.push([duration, percentNominalVoltage]);
  }

  /**
   * Adds a list of events to the ITIC plot.
   * @param points An array of points where each point is an array consisting of a duration and % nominal voltage.
   */
  function addPoints(points) {
    for(var i = 0; i < points.length; i++) {
      addPoint(points[i][0], points[i][1]);
    }
  }

  /**
   * Removes event points from the ITIC plot.
   */
  function removePoints() {
    while(noInterruptionEvents.data.length > 0) {
      noInterruptionEvents.data.pop();
    }
    while(noDamageEvents.data.length > 0) {
      noDamageEvents.data.pop();
    }
    while(prohibitedEvents.data.length > 0) {
      prohibitedEvents.data.pop();
    }
  }

  /**
   * Redraw the ITIC plot with the current set of event points.
   */
  function update() {
    plot.setData([topCurve, bottomCurve, noInterruptionEvents, prohibitedEvents, noDamageEvents]);
    plot.draw();
  }

  /**
   * Determines which ITIC curve region a point lies within.
   * @param duration The duration of the event.
   * @param percentNominalVoltage The % nominal voltage of the event.
   * @returns {string} String enumeration of the ITIC curve region that this point resides in.
   */
  function getRegionOfPoint(duration, percentNominalVoltage) {
    if(isInProhibitedRegion(duration, percentNominalVoltage)) return Region.PROHIBITED;
    if(isInNoDamageRegion(duration, percentNominalVoltage)) return Region.NO_DAMAGE;
    return Region.NO_INTERRUPTION;
  }

  function formatTooltip(data) {
    var table = "<table>";
    for(var i = 0; i < data.length; i++) {
      table +=
        "<tr>" +
        "<td>" + data[i][0] + "</td>" +
        "<td>" + data[i][1] + "</td>" +
        "</tr>";
    }
    table += "</table>";
    return table;
  }

  function msToC(ms) {
    return ms / MS_PER_C;
  }

  function cToMs(c) {
    return c * MS_PER_C;
  }

  function voltageToPercentNominalVoltage(v) {
    return Math.abs(v / 120.0 * 100);
  }

  function percentNominalVoltageToVoltage(p) {
    return (p * 120.0) / 100;
  }

  // Exports the public API
  return {
    init: init,
    addPoint: addPoint,
    addPoints: addPoints,
    removePoints: removePoints,
    update: update,
    getRegionOfPoint: getRegionOfPoint,
    formatTooltip: formatTooltip,
    msToC: msToC,
    cToMs: cToMs,
    percentNominalVoltageToVoltage: percentNominalVoltageToVoltage,
    voltageToPercentNominalVoltage: voltageToPercentNominalVoltage
    //tooltipCallback: tooltipCallback
  };
})();
