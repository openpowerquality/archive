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

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains Ebean utility methods for creates queries.
 */
public class DbUtils {
  /**
   * Creates a query that matches one field over multiple values.
   * @param clazz The type of query to return.
   * @param field The value value to match on.
   * @param values The values to match against field.
   * @return A query based on these matching parameters.
   */
  public static <T> Query<T> getAnyLike(Class<T> clazz, String field, Set<String> values) {
    return getAny(clazz, field, values, "%s like ?");
  }

  private static <T> Query<T> getAny(Class<T> clazz, String field, Set<String> values, String queryStr) {
    List<String> sqlList = new ArrayList<>();
    List<Object> paramsList = new ArrayList<>();
    Query<T> query = Ebean.createQuery(clazz);
    String sql = String.format(queryStr, field);

    for (String value : values) {
      sqlList.add(sql);
      paramsList.add(String.format("%s%%", value));
    }

    query.where(StringUtils.join(sqlList, " OR "));

    int i = 1;
    for (Object param : paramsList) {
      query.setParameter(i++, param);
    }

    return query;
  }

  public static String parens(String val) {
    return String.format("(%s)", val);
  }

  public static String or(List<String> ors) {
    return parens(StringUtils.join(ors, " OR "));
  }

  public static String and(List<String> ands) {
    return parens(StringUtils.join(ands, " AND "));
  }

  public static String eq(String field, Long val) {
    return String.format("(%s = %d)", field, val);
  }

  public static String eq(String field, String val) {
    return String.format("(%s = \"%s\")", field, val);
  }

  public static String beginsWith(String field, String value) {
    return String.format("(%s LIKE %s%%)", field, value);
  }

}
