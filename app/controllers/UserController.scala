package controllers

import play.api.mvc.Controller
import jp.t2v.lab.play2.auth.{AuthenticationElement, LoginLogout}

/**
 * Created by Administrator on 11/04/14.
 */
object UserController extends Controller with LoginLogout with AuthenticationElement with AuthConfigImpl {






}
