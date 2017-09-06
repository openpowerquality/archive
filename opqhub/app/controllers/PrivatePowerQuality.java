package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SecuredAndMatched;
import controllers.routes;
import models.*;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PrivatePowerQuality extends Controller {
  @Security.Authenticated(SecuredAndMatched.class)
  public static Result display(String email) {
    return TODO;
  }


    @Security.Authenticated(SecuredAndMatched.class)
    public static Result getTrends(String email) {
        return ok(views.html.privatetrends.render());
    }
}
