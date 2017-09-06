/*
 This file is part of opq-tools.

 opa-tools is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 opa-tools is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with opq-tools. If not, see <http://www.gnu.org/licenses/>.

 Copyright 2014 Anthony Christe
 */

var grid = (function () {
  "use strict";
  /* ------------------------- Private API -------------------------*/
  /**
   * The div containing the map.
   */
  var map;

  /**
   * The layer which contains the set of polygon grids.
   */
  var gridLayer;

//  var numEventsMarkers = [];
//  var numEventsLayer;

//  var numDevicesMarkers = [];
//  var numDevicesLayer;

  /**
   * List of grid-squares that are colored before a pan so that they can be recolored after a pan.
   * @type {Array}
   */
  var coloredLayers = [];

  var popupContent = [];

  var visibleIds = [];

  var idsToCenter = [];

  /**
   * Converts degrees to radians.
   * @param degs - Degrees (as decimal)
   * @returns {number} radians.
   */
  function rads(degs) {
    return degs * (Math.PI / 180);
  }

  /**
   * Converts radians to decimal degrees.
   * @param rads - Radians to convert.
   * @returns {number} decimal degrees.
   */
  function degs(rads) {
    return rads * (180 / Math.PI);
  }

  /**
   * Convenience bearings for getNextLatLng method. These values can be passed into the method
   * as the bearing parameter.
   * @type {{NORTH: number, NORTH_EAST: number, EAST: number, SOUTH_EAST: number, SOUTH: number, SOUTH_WEST: number,
   * WEST: number, NORTH_WEST: number}}
   */
  var bearing = {
    NORTH: 0,
    NORTH_EAST: rads(45),
    EAST: rads(90),
    SOUTH_EAST: rads(135),
    SOUTH: rads(180),
    SOUTH_WEST: rads(225),
    WEST: rads(270),
    NORTH_WEST: rads(315)
  };

  /**
   * Radius of earth in km.
   * @type {number}
   */
  var RADIUS_OF_EARTH = 6371.0;


  /**
   * Converts distance to angular distance.
   * @param distance - Non-angular distance.
   * @returns {number} Angular distance.
   */
  function getAngularDistance(distance) {
    return distance / RADIUS_OF_EARTH;
  }

  /**
   * Returns a new latitude and longitude given a starting latitude, longitude, bearing, and distance.
   * This algorithm was adapted from http://www.movable-type.co.uk/scripts/latlong.html
   * The new latitude and longitude is calculated using the shortest path between two points over a great arc.
   * @param latDegrees The latitude in decimal degrees.
   * @param lngDegrees The longitude in decimal degrees.
   * @param bear Direction from the starting point to get to the new point in radians.
   * @param distance The distance from the starting point to the new point in km.
   * @returns {LatLng} A new LatLng object representing the new point in decimal degrees.
   */
  function getNextLatLng(latDegrees, lngDegrees, bear, distance) {
    var lat = rads(latDegrees);
    var lng = rads(lngDegrees);
    var ad = getAngularDistance(distance);
    var newLat = Math.asin((Math.sin(lat) * Math.cos(ad)) + (Math.cos(lat) * Math.sin(ad) * Math.cos(bear)));
    var newLng = lng + Math.atan2(Math.sin(bear) * Math.sin(ad) * Math.cos(lat),
        Math.cos(ad) - Math.sin(lat) * Math.sin(newLat));
    return L.latLng(degs(newLat), degs(newLng));

  }

  /**
   * Given a bounding box, return a new padded bounding box.
   * @param bounds - The bounding box to pad.
   * @param padding - The amount of padding (in km) that will be added in each direction.
   * @returns {*} - The padded ounding box.
   */
  function getPaddedBounds(bounds, padding) {
    var boundsSW = getNextLatLng(bounds.getSouth(), bounds.getWest(), bearing.SOUTH_WEST, padding);
    var boundsNE = getNextLatLng(bounds.getNorth(), bounds.getEast(), bearing.NORTH_EAST, padding);
    return L.latLngBounds(boundsSW, boundsNE);
  }

  /**
   * Returns an object that contains a point, plus the row and col of the point.
   * @param r - Row of the point.
   * @param c - Col of point.
   * @param point
   * @returns {{r: Number, c: Number, point: *}}
   */
  function getAnnotatedPoint(r, c, point) {
    return {r: r, c: c, point: point};
  }

  /**
   * Given a bounding box and distance scale, get the closest NW point that is within the bounding box.
   * @param bounds - The bounding box to find the point inside.
   * @param distance - The scale of the grid squares in km.
   * @returns {*} - The first NW point inside the bounding box, plus its row and col.
   */
  function getNWPoint(bounds, distance) {
    var point = config.startPoint;
    var r = 0;
    var c = 0;

    while (point.lng < bounds.getWest() && point.lng < config.endPoint.lng) {
      point = getNextLatLng(point.lat, point.lng, bearing.EAST, distance);
      c++;
    }

    while (point.lat > bounds.getNorth() && point.lat > config.endPoint.lat) {
      point = getNextLatLng(point.lat, point.lng, bearing.SOUTH, distance);
      r++;
    }

    return getAnnotatedPoint(r, c, point);
  }

  /**
   * Returns a matrix of points representing the corners of each square in our grid.
   * Points are only saved if they are just outside of the bounding box determined by
   * the current viewable area of the map.
   * @param distance - The distance east and south and each point in the grid.
   * @returns {Array} - A 2d array where each row is a row of points in the grid.
   */
  function getGridPoints(distance) {
    // Pad the bounding box so that grid stops just off of screen
    var paddedBounds = getPaddedBounds(map.getBounds(), distance * 4);

    // Find the closest point just NW of our BB
    var nwPoint = getNWPoint(paddedBounds, distance);
    var pointRow = nwPoint.point;
    var pointCol;

    var r = nwPoint.r;
    var c = nwPoint.c;

    var matrix = [];
    var row = [];

    // For each row of our grid
    while (pointRow.lat > paddedBounds.getSouth() && pointRow.lat > config.endPoint.lat) {
      row = [];
      pointCol = pointRow;

      // Reset the column index
      c = nwPoint.c;

      // For each col in that row
      while (pointCol.lng < paddedBounds.getEast() && pointCol.lng < config.endPoint.lng) {
        // If this point is visible on current map, save it
        if (paddedBounds.contains(pointCol)) {
          row.push(getAnnotatedPoint(r, c, pointCol));
        }
        pointCol = getNextLatLng(pointCol.lat, pointCol.lng, bearing.EAST, distance);
        c++;
      }
      pointRow = getNextLatLng(pointRow.lat, pointRow.lng, bearing.SOUTH, distance);
      r++;

      // Don't add empty rows to the matrix
      if (row.length > 0) {
        matrix.push(row);
      }
    }

    return matrix;
  }


  /**
   * Calculates the hierarchical id of a grid square.
   * @param r The row of the grid square.
   * @param c The col of the grid square.
   * @param scale The length of each grid square.
   * @param id The field is used in the recursive call to build the id string.
   * @returns {*} The formatted id for this grid square.
   */
  function getGridSquareId(r, c, scale, id) {
    id = id || "";

    if (scale === 128) {
      return r + "," + c + ":" + id;
    }

    var left = (c % 2) === 0;
    var top = (r % 2) === 0;
    var idPart;

    if (left && top) {
      idPart = "0";
    }
    if (!left && top) {
      idPart = "1";
    }
    if (!left && !top) {
      idPart = "2";
    }
    if (left && !top) {
      idPart = "3";
    }

    return getGridSquareId(Math.floor(r / 2), Math.floor(c / 2), scale * 2, idPart + id);
  }


  /**
   * Creates a polygon given a set of points, a starting point, and the distance between points.
   * @param gridPoints - The matrix of points.
   * @param r - The upper left row of the polygon.
   * @param c = The upper left column of the polygon.
   * @param distance - The distance between points.
   * @returns {{type: string, properties: {row: (*|Number), col: (*|Number), scale: *, popupContent: string},
   * geometry: {type: string, coordinates: *[]}}}
   */
  function getPoly(gridPoints, r, c, distance) {
    /**
     * Swaps the latitude and longitude from a latLng object.
     * @param latLng The latLng to swap.
     * @returns {number[]} Swapped latLng as an array.
     */
    function swapLatLng(latLng) {
      return [latLng.lng, latLng.lat];
    }

    var row = gridPoints[r][c].r;
    var col = gridPoints[r][c].c;
    var id = getGridSquareId(row, col, distance, "");

    // Store center of polygon for displaying text
    var nwPoint = gridPoints[r][c].point;
    var sePoint = gridPoints[r + 1][c + 1].point;
    // Fudge the center a little to get text more centered instead of corner
    var centerLng = (nwPoint.lng + sePoint.lng) / 2;
    var centerLat = (nwPoint.lat + sePoint.lat) / 2;

    idsToCenter.push({
      id: id,
      center: [centerLat, centerLng],
      nw: [nwPoint.lat, nwPoint.lng],
      sw: [sePoint.lat, nwPoint.lng]
    });

    // Setup meta-data
    var feature = {
      type: "Feature",
      properties: {
        row: row,
        col: col,
        scale: distance,
        id: id,
        boundingBox: L.latLngBounds(gridPoints[r + 1][c].point, gridPoints[r][c + 1].point),
        popupContent: "row: " + row +
        "<br />col: " + col +
        "<br />scale: " + distance +
        "<br />id: " + id
      },
      geometry: {
        type: "Polygon",
        coordinates: [
          [
            swapLatLng(gridPoints[r][c].point),
            swapLatLng(gridPoints[r + 1][c].point),
            swapLatLng(gridPoints[r + 1][c + 1].point),
            swapLatLng(gridPoints[r][c + 1].point)
          ]
        ]
      }
    };

    return feature;
  }

  /**
   * Points are stored for each polygon in the following order: NW, SW, SE, NE.
   * It should also be noted that the latitude and longitude are switched positionally
   * when creating a multi-polygon compared to the rest of the leaflet api.
   * @param gridPoints
   */
  function getPolys(gridPoints, distance) {
    var polys = [];

    // For each point in the grid
    for (var r = 0; r < gridPoints.length; r++) {
      for (var c = 0; c < gridPoints[r].length; c++) {

        // If a square polygon can be made from the current point, make it
        if (r + 1 < gridPoints.length - 1 && c + 1 < gridPoints[r].length - 1) {
          polys.push(getPoly(gridPoints, r, c, distance));
        }
      }
    }
    return polys;
  }

  function clearMap() {
    if (map.hasLayer(gridLayer)) {
      map.removeLayer(gridLayer);
    }
//      if (map.hasLayer(numDevicesLayer)) {
//          map.removeLayer(numDevicesLayer);
//      }
//      if (map.hasLayer(numEventsLayer)) {
//          map.removeLayer(numEventsLayer);
//      }
    while (visibleIds.length > 0) {
      visibleIds.pop();
    }
    while (idsToCenter.length > 0) {
      idsToCenter.pop();
    }
  }

  function redrawMap() {
    var distance = getDistanceByZoom(map.getZoom());

    clearMap();

//    numDevicesLayer = L.layerGroup([]);
//    numEventsLayer = L.layerGroup([]);

    function onEachFeature(feature, layer) {
      if (feature.properties && feature.properties.popupContent) {
        for (var i = 0; i < popupContent.length; i++) {
          if (popupContent[i].gridId === feature.properties.id) {
            layer.bindPopup(popupContent[i].content);
          }
        }
      }

      if (callbacks.onGridClick) {
        layer.on({
          click: function () {
            callbacks.onGridClick(feature, layer);
          }
        });
      }

      visibleIds.push(feature.properties.id);

      for (var j = 0; j < coloredLayers.length; j++) {
        if (feature.properties.id === coloredLayers[j].id) {
          layer.setStyle({fillColor: coloredLayers[j].color});
        }
      }
    }

    var points = getGridPoints(distance);
    var polys = getPolys(points, distance);

    gridLayer = L.geoJson(polys, {
      onEachFeature: onEachFeature,
      weight: 2
    }).addTo(map);
  }

  /**
   * Redraw the grid.
   * @param distance - The length of the side of each square in the grid.
   */
  function onMapChange() {
    redrawMap();

    if (callbacks.onMapChange) {
      callbacks.onMapChange();
    }
  }


  /**
   * Return the distance between points in the grid based on the zoom level of the map.
   * @param zoom - Zoom of the map.
   * @returns {number} distance in km.
   */
  function getDistanceByZoom(zoom) {
    switch (zoom) {
      case 5:
      case 6:
      case 7:
        return 128;
      case 8:
      case 9:
        return 64;
      case 10:
        return 32;
      case 11:
        return 16;
      case 12:
        return 8;
      case 13:
        return 4;
      case 14:
        return 2;
      case 15:
        return 1;
      case 16:
        return 0.5;
      case 17:
        return 0.25;
      case 18:
        return 0.125;
    }
  }

  /* ------------------------- Public API -------------------------*/

  function getZoomByDistance(distance) {
    for (var i = 5; i <= 18; i++) {
      if (getDistanceByZoom(i) === distance) {
        return i;
      }
    }
  }

  /**
   * Defines starting and stopping points (lat, lng) that bound the grid creation algorithms.
   * Currently, the bounding box created by the two points contain the entire Hawaiian island chain.
   * The points would need to be customized to work over other regions of interest. The startPoint should be the
   * NW point of the bounding box, and the endpoint should be the SE point of the bounding box.
   *
   * Our default values are provides below, but any of these can be overridden since they're presented in the public
   * API.
   *
   * @type {{startPoint: latLng, endPoint: latLng}}
   */
  var config = {
    startPoint: L.latLng(22.534353, -161.004639),
    endPoint: L.latLng(16.719592, -151.853027),
    maxZoom: 18,
    minZoom: 5,
    singleSelectionMode: false
  };

  /**
   * List of public callbacks that users can implement.
   */
  var callbacks = {
    onGridClick: null,
    onMapChange: null
  };


  /**
   * Create a map with a grid layer.
   * @param div The div to create the map on.
   * @param center The center of the map view.
   * @param zoom The zoom level of the map.
   */
  function initMap(div, center, zoom) {
    var osmUrl = "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
    var osmAttrib = "Map data © OpenStreetMap contributors";
    var osm = new L.TileLayer(osmUrl, {attribution: osmAttrib});
    map = L.map(div, {maxZoom: config.maxZoom, minZoom: config.minZoom});
    map.addLayer(osm);

    map.setView(center, zoom);
    onMapChange();

    map.on("zoomend", onMapChange);
    map.on("dragend", onMapChange);
  }

  /**
   * Color a grid square by its given id.
   * @param id Id of the grid-square to color.
   * @param color The color to color the grid-square (in english (red) or hex (#FF0000)).
   */
  function colorSquareById(id, color) {
    if (config.singleSelectionMode) {
      coloredLayers = [];
    }
    coloredLayers.push({id: id, color: color});
    redrawMap();
  }

  function addTextToSquare(id, text, sqLocation, cssClass) {
    var icon = L.divIcon({
      className: "leaflet-num-icon" + " " + cssClass,
      iconSize: null,
      html: text
    });
    for (var i = 0; i < idsToCenter.length; i++) {
      if (idsToCenter[i].id === id) {
        L.marker(idsToCenter[i][sqLocation], {icon: icon}).addTo(gridLayer);
      }
    }
  }

  function addEventNumbers(gridId, high, medium, low) {
      function span(clazz, body) {
          return "<span class='" + clazz + "'>" + body + "</span>";
      }

      var text = span("text-bad", high) + "/" +
                 span("text-ok", medium) + "/" +
                 span("text-good", low);
      addTextToSquare(gridId, text, "nw", "leaflet-center-text");
  }

  function addNumberOfDevices(gridId, n) {
//    addTextToSquare(gridId, n, "nw", "leaflet-num-devices", gridLayer);
    //addTextToSquare(gridId, n, "nw", "leaflet-num-devices");
  }

  function addNumberOfEvents(gridId, n) {
      console.log(gridId);
    if (gridId && n) {
      //addTextToSquare(gridId, n, "sw", "leaflet-num-events");
    }
  }

  function setView(center, zoom) {
    map.setView(center, zoom);
  }

  /**
   * Clear the styling of all layers.
   */
  function clearColoredLayers() {
    coloredLayers = [];
    redrawMap();
  }

  function addPopupContent(gridId, content) {
    popupContent.push({gridId: gridId, content: content});
  }

  /**
   *  Returns a list of the visible ids currently in the bounding box of the browser.
   */
  function getVisibleIds() {
    return visibleIds;
  }

  function getCenterLat() {
    return map.getCenter().lat;
  }

  function getCenterLng() {
    return map.getCenter().lng;
  }

  function getZoom() {
    return map.getZoom();
  }

  /**
   * Draws a small circle on the map which can be used in debugging.
   * @param latLng - The latitude and longitude to place the point.
   * @param color - The color of the point.
   */
  function addDebugPoint(latLng, color) {
    color = color || "red";
    var debugPoint = L.circle(latLng, 500, {color: color}).addTo(map);
  }

  /**
   * If the map is drawn and the page it is drawn on is dynamically updated, the map may be displayed incorrectly.
   * This method invalidates the map and forces a redraw.
   */
  function invalidateSize() {
    if (map) {
      map.invalidateSize();
    }
  }

  /**
   * Convienience object allowing us to center the map over any of the following Hawaiian islands.
   */
  var island = {
    BIG_ISLAND: {
      latLng: L.latLng(19.609926, -155.484009),
      defaultZoom: 9
    },
    KAUAI: {
      latLng: L.latLng(22.057244, -159.506378),
      defaultZoom: 11
    },
    LANAI: {
      latLng: L.latLng(20.829093, -156.919785),
      defaultZoom: 12
    },
    MAUI: {
      latLng: L.latLng(20.786128, -156.305237),
      defaultZoom: 10
    },
    MOLOKAI: {
      latLng: L.latLng(21.121454, -156.996689),
      defaultZoom: 11
    },
    OAHU: {
      latLng: L.latLng(21.466700, -157.983300),
      defaultZoom: 8
    }
  };

  return {
    config: config,
    callbacks: callbacks,
    initMap: initMap,
    colorSquare: colorSquareById,
    addTextToSquare: addTextToSquare,
    addEventNumbers: addEventNumbers,
    addNumberOfDevices: addNumberOfDevices,
    addNumberOfEvents: addNumberOfEvents,
    setView: setView,
    addPopupContent: addPopupContent,
    clearColoredLayers: clearColoredLayers,
    getVisibleIds: getVisibleIds,
    redrawMap: redrawMap,
    getZoomByDistance: getZoomByDistance,
    addDebugPoint: addDebugPoint,
    invalidateSize: invalidateSize,
    island: island,
    getCenterLat: getCenterLat,
    getCenterLng: getCenterLng,
    getZoom: getZoom
  };
})();





