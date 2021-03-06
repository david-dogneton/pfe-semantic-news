package controllers


import models._
import scala.{None, Some}
import org.joda.time.DateTime
import controllers.sparql.SparqlQueryExecuter
import play.api.data._
import play.api.data.Forms._
import jp.t2v.lab.play2.auth._
import play.api._
import play.api.mvc._
import play.api.libs.json.{JsValue, JsObject, Json}


object Application extends Controller with OptionalAuthElement with LoginLogout with AuthConfigImpl {
  val sparql: SparqlQueryExecuter = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql")

  def javascriptRoutes = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          controllers.routes.javascript.Application.getArt,
          controllers.routes.javascript.Application.displayLinkedArt,
          controllers.routes.javascript.Application.getDomaines,
          controllers.routes.javascript.Application.getTypes,
          controllers.routes.javascript.Application.getTop,
          controllers.routes.javascript.Application.getTopJour,
          controllers.routes.javascript.Application.getTopMois,
          controllers.routes.javascript.Application.getTopSemaine,
          controllers.routes.javascript.Application.getTop10,
          controllers.routes.javascript.Application.getArticlesByTag,
          controllers.routes.javascript.Application.changerCoeur,
          controllers.routes.javascript.Application.enregistrerLecture,
          controllers.routes.javascript.Application.enregistrerLikeEntite,
          controllers.routes.javascript.Application.getLikeEntite,
          controllers.routes.javascript.Application.getLikeArticle,
          controllers.routes.javascript.Application.getArticlesRecommandes,
          controllers.routes.javascript.Application.getEntityInfos
        )
      ).as("text/javascript")
  }

  def changerCoeur() = StackAction {
    implicit request =>
      val bodyJSonOpt = request.body.asJson
      bodyJSonOpt match {
        case Some(bodyJSon) => {
          val mailUser: String = (bodyJSon \ "mailUser").as[String]
          val urlArticle: String = (bodyJSon \ "urlArticle").as[String]
          val userOpt = Utilisateur.get(mailUser)
          userOpt match {
            case Some(user) => {
              val articleOpt = Article.getByUrl(urlArticle)
              articleOpt match {
                case Some(article) => {
                  val noteOpt = Note.get(user, article)
                  noteOpt match {
                    case Some(note) => {
                      Note.delete(user, article)
                    }
                    case None => {
                      Note.create(new Note(user, article, 0, true))
                    }
                  }
                }
                case None => throw new Exception("Article not found.")
              }
            }
            case None => throw new Exception("Utilisateur not found.")
          }
        }
        case None => {}
      }
      Ok("200")
  }


  def enregistrerLecture() = StackAction {
    implicit request =>
      val bodyJSonOpt = request.body.asJson
      bodyJSonOpt match {
        case Some(bodyJSon) => {
          val mailUser: String = (bodyJSon \ "mailUser").as[String]
          val urlArticle: String = (bodyJSon \ "urlArticle").as[String]
          val userOpt = Utilisateur.get(mailUser)
          userOpt match {
            case Some(user) => {
              val articleOpt = Article.getByUrl(urlArticle)
              articleOpt match {
                case Some(article) => {
                  val noteOpt = Consultation.get(user, article)
                  noteOpt match {
                    case Some(note) => {
                    }
                    case None => {
                      Consultation.create(new Consultation(user, article, DateTime.now()))
                    }
                  }
                }
                case None => throw new Exception("Article not found.")
              }
            }
            case None => throw new Exception("Utilisateur not found.")
          }
        }
        case None => {}
      }
      Ok("200")
  }

  def getLikeEntite() = StackAction {
    implicit request =>
      val bodyJSonOpt = request.body.asJson
      var aCoeur = 0
      var nbCoeurs = 0
      bodyJSonOpt match {
        case Some(bodyJSon) => {
          val urlEntite: String = (bodyJSon \ "urlEntite").as[String]
          val userOpt = loggedIn
          Logger.debug("URL recherché : "+urlEntite)
          val entiteOpt = Entite.getByUrl(urlEntite)
          entiteOpt match {
            case Some(entite) => {
              nbCoeurs = AppreciationEntite.getNbCoeurs(entite)
              Logger.debug("On a récupéré le nombre de coeurs, et il est de : "+nbCoeurs)
              userOpt match {
                case Some(user) => {
                  val appreciationEntiteOpt = AppreciationEntite.get(user, entite)
                  appreciationEntiteOpt match {
                    case Some(appreciation) => {
                      aCoeur = 1
                      nbCoeurs = AppreciationEntite.getNbCoeurs(entite)
                    }
                    case None => {
                    }
                  }
                }
                case None => {}
              }
            }
            case None => throw new Exception("Entité not found.")
          }
        }
        case None => {}
      }
      Ok(Json.obj("valeur" -> aCoeur, "nbCoeurs" -> nbCoeurs))
  }

  def getLikeArticle() = StackAction {
    implicit request =>
      val bodyJSonOpt = request.body.asJson
      var aCoeur = 0
      bodyJSonOpt match {
        case Some(bodyJSon) => {
          val urlArticle: String = (bodyJSon \ "urlArticle").as[String]
          val userOpt = loggedIn
          userOpt match {
            case Some(user) => {
              val articleOpt = Article.getByUrl(urlArticle)
              articleOpt match {
                case Some(article) => {
                  val noteOpt = Note.get(user, article)
                  noteOpt match {
                    case Some(note) => {
                      aCoeur = 1
                    }
                    case None => {
                    }
                  }
                }
                case None => throw new Exception("Article not found.")
              }
            }
            case None => throw new Exception("Utilisateur not found.")
          }
        }
        case None => {}
      }
      Ok(Json.obj("valeur" -> aCoeur))
  }

  def enregistrerLikeEntite() = StackAction {
    implicit request =>
      val bodyJSonOpt = request.body.asJson
      bodyJSonOpt match {
        case Some(bodyJSon) => {
          val mailUser: String = (bodyJSon \ "mailUser").as[String]
          val urlEntite: String = (bodyJSon \ "urlEntite").as[String]
          val userOpt = Utilisateur.get(mailUser)
          userOpt match {
            case Some(user) => {
              val entiteOpt = Entite.getByUrl(urlEntite)
              entiteOpt match {
                case Some(entite) => {
                  val appreciationEntiteOpt = AppreciationEntite.get(user, entite)
                  appreciationEntiteOpt match {
                    case Some(appreciationEntite) => {
                      AppreciationEntite.delete(user, entite)
                    }
                    case None => {
                      AppreciationEntite.create(new AppreciationEntite(user, entite, 0, 1, true))
                    }
                  }
                }
                case None => throw new Exception("Entité not found.")
              }
            }
            case None => throw new Exception("Utilisateur not found.")
          }
        }
        case None => {}
      }
      Ok("200")
  }


  def mapage = StackAction {
    implicit request =>
      val maybeUser: Option[User] = loggedIn
      maybeUser match {
        case Some(usr) => {
          Ok(views.html.mapage())
        }
        case None => {
          Ok(views.html.index())
        }
      }
  }

  val urlForm = Form(
    single(
      "url" -> nonEmptyText
    )
  )

  // Router.JavascriptReverseRoute
  def getArt = StackAction {
    implicit request =>
      val maybeUser: Option[User] = loggedIn
      val user: User = maybeUser.getOrElse(Utilisateur("default", "", ""))
      // Logger.debug("avant")
      val listeArt: List[Article] = Article.getLastArticle
      // Logger.debug("apres")
      val res: List[JsObject] = listeArt.map(art => {
        val dateF: String = art.date.year().get() + "-" + art.date.monthOfYear().get() + "-" + art.date.dayOfMonth().get() + " " + art.date.hourOfDay().get() + ":" + art.date.minuteOfHour().get()
        val tags: List[JsObject] = Tag.getTagsOfArticles(art).map(tag => (Json.obj("id" -> tag._1.id,
          "nom" -> tag._1.nom)))
        val note = Note.get(user, art)
        var coeurPresent = 0
        note match {
          case Some(no) => coeurPresent = 1
          case None => coeurPresent = 0
        }
        Json.obj(
          "id" -> art.id,
          "url" -> art.url,
          "titre" -> art.titre,
          "description" -> art.description,
          "site" -> art.site.nom,
          "image" -> art.image,
          "consultationsJour" -> art.consultationsJour,
          "coeurs" -> art.nbCoeurs,
          "domaine" -> art.site.typeSite,
          "tags" -> tags,
          "note" -> art.nbEtoiles,
          "tags" -> tags,
          "note" -> art.nbEtoiles,
          "date" -> dateF,
          "lies" -> EstLie.countLinkedArticles(art),
          "coeurPresent" -> coeurPresent
        )
      })
      // Logger.debug("RES " +res )
      Ok(Json.obj("liste" -> res))
  }

  def getArticlesRecommandes = StackAction {
    implicit request =>
      val maybeUser: Option[User] = loggedIn
      val user: User = maybeUser.getOrElse(Utilisateur("default", "", ""))
      // Logger.debug("avant")

      val listeArt: List[Article] = Recommandation.buildRecommandations(user)
      // Logger.debug("apres")
      val res: List[JsObject] = listeArt.map(art => {
        val dateF: String = art.date.year().get() + "-" + art.date.monthOfYear().get() + "-" + art.date.dayOfMonth().get() + " " + art.date.hourOfDay().get() + ":" + art.date.minuteOfHour().get()
        val tags: List[JsObject] = Tag.getTagsOfArticles(art).map(tag => (Json.obj("id" -> tag._1.id,
          "nom" -> tag._1.nom)))
        val note = Note.get(user, art)
        var coeurPresent = 0
        note match {
          case Some(no) => coeurPresent = 1
          case None => coeurPresent = 0
        }
        Json.obj(
          "id" -> art.id,
          "url" -> art.url,
          "titre" -> art.titre,
          "description" -> art.description,
          "site" -> art.site.nom,
          "image" -> art.image,
          "consultationsJour" -> art.consultationsJour,
          "coeurs" -> art.nbCoeurs,
          "domaine" -> art.site.typeSite,
          "tags" -> tags,
          "note" -> art.nbEtoiles,
          "tags" -> tags,
          "note" -> art.nbEtoiles,
          "date" -> dateF,
          "lies" -> EstLie.countLinkedArticles(art),
          "coeurPresent" -> coeurPresent
        )
      })
      // Logger.debug("RES " +res )
      Ok(Json.obj("liste" -> res))
  }

  def getEntityInfos = StackAction {
    implicit request =>
      val bodyJSonOpt = request.body.asJson
      var likes:Int=0
      var entityType=""
      bodyJSonOpt match {
        case Some(bodyJSon) => {
          val entityUrl: String = (bodyJSon \ "url").as[String]
          val tmp = Entite.getByUrl(entityUrl)
          tmp match {
            case Some(entite) => {
              likes=AppreciationEntite.getNbCoeurs(entite)
              /*val tmp =Type.getEntityType(entite)
              tmp match {
                case Some(aType) =>{
                  entityType=aType.denomination
                }
                case None =>{
                  //IdoNotCare
                }
              }*/
            }
            case None =>{
              //IdoNotCare
            }
          }
          Ok(Json.obj("url" -> sparql.getImage(entityUrl),"name"->sparql.getName(entityUrl),"type"->entityType,"likes"->likes))
        }
        case None => {
          Ok(Json.obj("url" -> "noPic","name"->"","type"->"","likes"->""))
        }
      }
  }

  val searchForm = Form(
    single(
      "search" -> nonEmptyText
    )
  )

  def displayArt(id: Int) = StackAction {
    implicit request =>

      Logger.debug("URL OKAY ")
      // Logger.debug("URL TEST " + url)
      val article: Option[Article] = Article.getById(id)
      if (article.isDefined) {
        val listeTag = Tag.getTagsOfArticles(article.get)
        val listeTagArticles: List[Entite] = listeTag.map(elt => elt._1)
        Ok(views.html.visualisationarticle(article.get, listeTagArticles))
      } else {
        Redirect(routes.Application.index).flashing("error" -> "L'article à visualiser n'existe plus ! :o")

      }

  }

  def displayLinkedArt(id: Int) = StackAction {
    implicit request =>

      Logger.debug("TEST DISPLAY LINKED ART" + id)
      //       val listeLinked = EstLie.getLinkedArticlesById(id)
      //       val listeArt: List[Article] =listeLinked.map(elt=>{
      //          Article.getByUrl(elt._2).get
      //       })
      val maybeUser: Option[User] = loggedIn
      val user: User = maybeUser.getOrElse(Utilisateur("default", "", ""))
      val listeArt = EstLie.getById(id)
      val res: List[JsObject] = listeArt.map(art => {
        val dateF: String = art.date.year().get() + "-" + art.date.monthOfYear().get() + "-" + art.date.dayOfMonth().get() + " " + art.date.hourOfDay().get() + ":" + art.date.minuteOfHour().get()
        val tags: List[JsObject] = Tag.getTagsOfArticles(art).map(tag => (Json.obj("id" -> tag._1.id,
          "nom" -> tag._1.nom)))
        val note = Note.get(user, art)
        var coeurPresent = 0
        note match {
          case Some(no) => coeurPresent = 1
          case None => coeurPresent = 0
        }
        Json.obj(
          "id" -> art.id,
          "url" -> art.url,
          "titre" -> art.titre,
          "description" -> art.description,
          "site" -> art.site.nom,
          "image" -> art.image,
          "consultationsJour" -> art.consultationsJour,
          "coeurs" -> art.nbCoeurs,
          "domaine" -> art.site.typeSite,
          "tags" -> tags,
          "note" -> art.nbEtoiles,
          "tags" -> tags,
          "note" -> art.nbEtoiles,
          "date" -> dateF,
          "lies" -> EstLie.countLinkedArticles(art),
          "coeurPresent" -> coeurPresent
        )
      })
      Ok(Json.obj("liste" -> res))


  }

  def getArticlesByTag = StackAction {
    implicit request =>
      val maybeUser: Option[User] = loggedIn
      val user: User = maybeUser.getOrElse(Utilisateur("default", "", ""))
      urlForm.bindFromRequest.fold(
        hasErrors = {
          form =>
            Ok(Json.obj())
        },
        success = {
          url =>
            val tmp = Entite.getByUrl(url)
            tmp match {
              case Some(entite) =>
                val listeTmp = Tag.getArticlesLies(entite, 20)
                listeTmp match {
                  case Some(liste) =>
                    val res: List[JsObject] = liste.map(art => {
                      val dateF: String = art._1.date.year().get() + "-" + art._1.date.monthOfYear().get() + "-" + art._1.date.dayOfMonth().get() + " " + art._1.date.hourOfDay().get() + ":" + art._1.date.minuteOfHour().get()
                      val tags: List[JsObject] = Tag.getTagsOfArticles(art._1).map(tag => (Json.obj("id" -> tag._1.id,
                        "nom" -> tag._1.nom)))
                      val note = Note.get(user, art._1)
                      var coeurPresent = 0
                      note match {
                        case Some(no) => coeurPresent = 1
                        case None => coeurPresent = 0
                      }
                      Json.obj(
                        "id" -> art._1.id,
                        "url" -> art._1.url,
                        "titre" -> art._1.titre,
                        "description" -> art._1.description,
                        "site" -> art._1.site.nom,
                        "image" -> art._1.image,
                        "consultationsJour" -> art._1.consultationsJour,
                        "coeurs" -> art._1.nbCoeurs,
                        "domaine" -> art._1.site.typeSite,
                        "tags" -> tags,
                        "note" -> art._1.nbEtoiles,
                        "date" -> dateF,
                        "lies" -> EstLie.countLinkedArticles(art._1),
                        "coeurPresent" -> coeurPresent
                      )
                    })
                    Ok(Json.obj("liste" -> res))
                  case None =>
                    Ok(Json.obj())
                }
              case None =>
                Ok(Json.obj())
            }
        })
  }

  /*  def getTop10 = StackAction {
      implicit request =>

        val top: List[Entite] = Entite.lesPlusTaggesDuJour()
        val sparql: SparqlQueryExecuter = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql")
        val res: List[JsObject] = top.map(entite => {
          Json.obj("nom" -> sparql.getName(entite.url),
            "nombre" -> entite.apparitionsJour,
            "id" -> entite.id
          )
        })
        Ok(Json.obj("liste" -> res))
    }*/

  def getTop10 = StackAction {
    implicit request =>

      val top: List[Entite] = Entite.lesPlusTaggesDuJour()
      val res: List[JsObject] = top.map(entite => {
        Json.obj("nom" -> sparql.getName(entite.url),
          "nombre" -> entite.apparitionsJour,
          "id" -> entite.id,
          "image" -> sparql.getImage(entite.url)
        )
      })
      Ok(Json.obj("liste" -> res))
  }

  def getEntitesTitle = StackAction {
    implicit request =>

      val top: List[Entite] =  Entite.topAnnotations(10000, -1)
      val res: List[JsObject] = top.map(entite => {
        Json.obj("title" -> sparql.getName(entite.url))
      })
      Ok(Json.obj("content" -> res))
  }

  def getTop(idType: Int) = StackAction {
    implicit request =>

      val top: List[Entite] = Entite.topAnnotations(10000, idType)
      val sparql: SparqlQueryExecuter = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql")
      top.length match {
        case 0 =>
          val res = {
            Json.obj("nom" -> "Pas de résultats pour ce filtrage")
          }
          Ok(Json.obj("liste" -> res))
        case _ =>
          val res: List[JsObject] = top.map(entite => {
            Json.obj("nom" -> sparql.getName(entite.url),
              "nombre" -> entite.apparitionsSemaine,
              "image" -> sparql.getImage(entite.url),
              "id" -> entite.id
            )
          })

          Ok(Json.obj("liste" -> res))
      }
  }

  def getTopJour(idType: Int) = StackAction {
    implicit request =>

      val top: List[Entite] = Entite.topAnnotationsJour(10000, idType)
      val sparql: SparqlQueryExecuter = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql")
      top.length match {
        case 0 =>
          val res = {
            Json.obj("nom" -> "Pas de résultats pour ce filtrage")
          }
          Ok(Json.obj("liste" -> res))
        case _ =>
          val res: List[JsObject] = top.map(entite => {
            Json.obj(//"nom" -> sparql.getName(entite.url),
              "nom" -> entite.nom,
              "nombre" -> entite.apparitionsJour,
              //"image" -> sparql.getImage(entite.url),
              "id" -> entite.id,
              "url" -> entite.url
            )
          })
          Ok(Json.obj("liste" -> res))
      }
  }

  def getTopSemaine(idType: Int) = StackAction {
    implicit request =>

      val top: List[Entite] = Entite.topAnnotationsSemaine(10000, idType)
      val sparql: SparqlQueryExecuter = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql")
      top.length match {
        case 0 =>
          val res = {
            Json.obj("nom" -> "Pas de résultats pour ce filtrage")
          }
          Ok(Json.obj("liste" -> res))
        case _ =>
          val res: List[JsObject] = top.map(entite => {
            Json.obj("nom" -> sparql.getName(entite.url),
              "nombre" -> entite.apparitionsSemaine,
              "image" -> sparql.getImage(entite.url),
              "id" -> entite.id
            )
          })
          Ok(Json.obj("liste" -> res))
      }
  }

  def getTopMois(idType: Int) = StackAction {
    implicit request =>

      val top: List[Entite] = Entite.topAnnotationsMois(10000, idType)
      val sparql: SparqlQueryExecuter = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql")
      top.length match {
        case 0 =>
          val res = {
            Json.obj("nom" -> "Pas de résultats pour ce filtrage")
          }
          Ok(Json.obj("liste" -> res))
        case _ =>
          val res: List[JsObject] = top.map(entite => {
            Json.obj("nom" -> sparql.getName(entite.url),
              "nombre" -> entite.apparitionsMois,
              "image" -> sparql.getImage(entite.url),
              "id" -> entite.id
            )
          })
          Ok(Json.obj("liste" -> res))
      }
  }

  def getDomaines = StackAction {
    implicit request =>

      val listeDomaines: List[Site] = Site.getTypes

      val res: List[JsObject] = listeDomaines.map(site => {
        Json.obj("nom" -> site.typeSite
        )
      })
      Ok(Json.obj("liste" -> res))
  }

  def getTypes = StackAction {
    implicit request =>

      val listeTypes: List[Type] = Type.getAll

      val res: List[JsObject] = listeTypes.map(typeEntite => {
        Json.obj("nom" -> typeEntite.denomination,
          "id" -> typeEntite.id
        )
      })
      Ok(Json.obj("liste" -> res))
  }


  def index = StackAction {
    implicit request =>
      val maybeUser: Option[User] = loggedIn
      val user: User = maybeUser.getOrElse(Utilisateur("default", "", ""))
      Ok(views.html.index())
  }


  def presentation = StackAction {
    implicit request => Ok(views.html.presentation())
  }

  def statistiques = StackAction {
    implicit request => Ok(views.html.stats())
  }

  def createEntite = StackAction {
    implicit request =>
      val result = Entite.insert(Entite("Robin Van Persie", "http://quartsDeFinale.com"))
      Logger.debug("result test create entité : " + result)
      Ok(views.html.index())
  }

  def createAppreciationEntite = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail2@test.com")
      val entiteOpt = Entite.getByUrl("http://quartsDeFinale.com")
      utilisateurOpt match {
        case Some(utilisateur) =>
          entiteOpt match {
            case Some(entite) =>
              val result = AppreciationEntite.create(AppreciationEntite(utilisateur, entite, 4, 3))
              Logger.debug("result test create appréciation entité : " + result)
            case None => println("entiteOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def getAppreciationEntite = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail1@test.com")
      val entiteOpt = Entite.getByUrl("http://quartsDeFinale.com")
      utilisateurOpt match {
        case Some(utilisateur) =>
          entiteOpt match {
            case Some(entite) =>
              val result = AppreciationEntite.get(utilisateur, entite)
              Logger.debug("result test create appréciation entité : " + result)
            case None => println("entiteOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def setQuantiteAppreciationEntite = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail1@test.com")
      val entiteOpt = Entite.getByUrl("http://quartsDeFinale.com")
      utilisateurOpt match {
        case Some(utilisateur) =>
          entiteOpt match {
            case Some(entite) =>
              val result = AppreciationEntite.setQuantite(utilisateur, entite, -3)
              Logger.debug("result test set quantité appréciation entité : " + result)
            case None => println("entiteOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def setNbCoeursAppreciationEntite = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail1@test.com")
      val entiteOpt = Entite.getByUrl("http://quartsDeFinale.com")
      utilisateurOpt match {
        case Some(utilisateur) =>
          entiteOpt match {
            case Some(entite) =>
              val result = AppreciationEntite.incrNbCoeurs(utilisateur, entite)
              Logger.debug("result test set quantité appréciation entité : " + result)
            case None => println("entiteOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def setFavoriAppreciationEntite = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail1@test.com")
      val entiteOpt = Entite.getByUrl("http://quartsDeFinale.com")
      utilisateurOpt match {
        case Some(utilisateur) =>
          entiteOpt match {
            case Some(entite) =>
              val result = AppreciationEntite.setFavori(utilisateur, entite)
              Logger.debug("result test set quantité appréciation entité : " + result)
            case None => println("entiteOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def estFavoriAppreciationEntite = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail2@test.com")
      val entiteOpt = Entite.getByUrl("http://quartsDeFinale.com")
      utilisateurOpt match {
        case Some(utilisateur) =>
          entiteOpt match {
            case Some(entite) =>
              val result = AppreciationEntite.estFavori(utilisateur, entite)
              Logger.debug("result test set quantité appréciation entité : " + result)
            case None => println("entiteOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def createSite = StackAction {
    implicit request =>
      val result = Site.create(Site("www.magness.fr", "Magness", "Informations diverses"))
      Logger.debug("result test create site : " + result)
      Ok(views.html.index())
  }

  def createNote = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail2@test.com")
      val articleOpt = Article.getByUrl("http://magness.fr/blablabla")
      utilisateurOpt match {
        case Some(utilisateur) =>
          articleOpt match {
            case Some(article) =>
              val result = Note.create(Note(utilisateur, article, 4))
              Logger.debug("result test créer note : " + result)
            case None => println("articleOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def createConsultation = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail2@test.com")
      val articleOpt = Article.getByUrl("http://magness.fr/blablabla")
      utilisateurOpt match {
        case Some(utilisateur) =>
          articleOpt match {
            case Some(article) =>
              val result = Consultation.create(Consultation(utilisateur, article, DateTime.now()))
              Logger.debug("result test créer consultation : " + result)
            case None => println("articleOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def createRecommandation = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail2@test.com")
      val articleOpt = Article.getByUrl("http://magness.fr/blablabla")
      utilisateurOpt match {
        case Some(utilisateur) =>
          articleOpt match {
            case Some(article) =>
              val result = Recommandation.create(Recommandation(utilisateur, article, 4.3))
              Logger.debug("result test créer recommandation : " + result)
            case None => println("articleOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def getNote = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail2@test.com")
      val articleOpt = Article.getByUrl("http://magness.fr/blablabla")
      utilisateurOpt match {
        case Some(utilisateur) =>
          articleOpt match {
            case Some(article) =>
              val result = Note.get(utilisateur, article)
              Logger.debug("result test get note : " + result)
            case None => println("articleOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def getConsultation = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail2@test.com")
      val articleOpt = Article.getByUrl("http://magness.fr/blablabla")
      utilisateurOpt match {
        case Some(utilisateur) =>
          articleOpt match {
            case Some(article) =>
              val result = Consultation.get(utilisateur, article)
              Logger.debug("result test get consultation : " + result)
            case None => println("articleOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def getRecommandation = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail2@test.com")
      val articleOpt = Article.getByUrl("http://magness.fr/blablabla")
      utilisateurOpt match {
        case Some(utilisateur) =>
          articleOpt match {
            case Some(article) =>
              val result = Recommandation.get(utilisateur, article)
              Logger.debug("result test get recommandation : " + result)
            case None => println("articleOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def createArticle = StackAction {
    implicit request =>
      val siteOpt = Site.getByUrl("www.magness.fr")
      siteOpt match {
        case Some(site) =>
          val result = Article.insert(Article("Monuments Men : jusqu'au bout de l'ennui.", "Thib", "Ceci est la description de la critique de Monuments Men.", new DateTime(), "http://magness.fr/blablabla", site))
          Logger.debug("result test create article : " + result)
        case None => println("www.magness.fr not found.")
      }

      Ok(views.html.index())
  }


  //début tests AppreciationDomaine
  def createAppreciationDomaine = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail2@test.com")
      val domaineOpt = Domaine.get("Sport")
      utilisateurOpt match {
        case Some(utilisateur) =>
          domaineOpt match {
            case Some(domaine) =>
              val result = AppreciationDomaine.create(AppreciationDomaine(utilisateur, domaine, 4))
              Logger.debug("result test create appréciation domaine : " + result)
            case None => println("domaineOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def getAppreciationDomaine = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail1@test.com")
      val domaineOpt = Domaine.get("Sport")
      utilisateurOpt match {
        case Some(utilisateur) =>
          domaineOpt match {
            case Some(domaine) =>
              val result = AppreciationDomaine.get(utilisateur, domaine)
              Logger.debug("result test create appréciation domaine : " + result)
            case None => println("domaineOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def setNbCoeursAppreciationDomaine = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail1@test.com")
      val domaineOpt = Domaine.get("Sport")
      utilisateurOpt match {
        case Some(utilisateur) =>
          domaineOpt match {
            case Some(domaine) =>
              val result = AppreciationDomaine.incrNbCoeurs(utilisateur, domaine)
              Logger.debug("result test set quantité appréciation domaine : " + result)
            case None => println("domaineOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def setFavoriAppreciationDomaine = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail1@test.com")
      val domaineOpt = Domaine.get("Sport")
      utilisateurOpt match {
        case Some(utilisateur) =>
          domaineOpt match {
            case Some(domaine) =>
              val result = AppreciationDomaine.setFavori(utilisateur, domaine)
              Logger.debug("result test set quantité appréciation domaine : " + result)
            case None => println("domaineOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def estFavoriAppreciationDomaine = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail2@test.com")
      val domaineOpt = Domaine.get("Sport")
      utilisateurOpt match {
        case Some(utilisateur) =>
          domaineOpt match {
            case Some(domaine) =>
              val result = AppreciationDomaine.estFavori(utilisateur, domaine)
              Logger.debug("result test set quantité appréciation domaine : " + result)
            case None => println("domaineOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  //fin tests AppreciationDomaine

  def createDomaine = StackAction {
    implicit request =>
      val result = Domaine.create(Domaine("Sport"))
      Logger.debug("result test create domaine : " + result)
      Ok(views.html.index())
  }

  def createUser = StackAction {
    implicit request =>
      val result = Utilisateur.create(Utilisateur("mail2@test.com", "mdpTest2", "pseudoTest2"))
      Logger.debug("result test create user : " + result)
      Ok(views.html.index())
  }

  def getUser = StackAction {
    implicit request =>
      val resultOpt = Utilisateur.get("mail3@test.com")
      resultOpt match {
        case Some(result) =>
          Logger.debug("result test get user : " + result)
        case None => println("Utilisateur not found.")
      }
      Ok(views.html.index())
  }


  def setMailUser = StackAction {
    implicit request =>
      val result = Utilisateur.setMail("mail1Change@test.com", "mail1Change@test.com")
      Logger.debug("result test set mail user : " + result)
      Ok(views.html.index())
  }

  def setPseudoUser = StackAction {
    implicit request =>
      val result = Utilisateur.setPseudo("mail1Change@test.com", "pseudoTest1Change")
      Logger.debug("result test set pseudo user : " + result)
      Ok(views.html.index())
  }

  def setMdpUser = StackAction {
    implicit request =>
      val result = Utilisateur.setMdp("mail1Change@test.com", "mdpTest1Change")
      Logger.debug("result test set mdp user : " + result)
      Ok(views.html.index())
  }

  def incremrNbCoeurs = StackAction {
    implicit request =>
      val result = Utilisateur.incrementerNbCoeurs("mail1Change@test.com")
      Logger.debug("result test incrémenter nb coeurs : " + result)
      Ok(views.html.index())
  }

  def decrementerNbCoeurs = StackAction {
    implicit request =>
      val result = Utilisateur.decrementerNbCoeurs("mail1Change@test.com")
      Logger.debug("result test décrémenter nb coeurs : " + result)
      Ok(views.html.index())
  }

  def deleteUser = StackAction {
    implicit request =>
      val result = Utilisateur.delete("mail1Change@test.com")
      Logger.debug("result test delete : " + result)
      Ok(views.html.index())
  }

  def delete = StackAction {
    implicit request =>
      Utils.delete()
      Ok(views.html.index())
  }

  def miseAJourFlux = StackAction {
    implicit request =>
      FluxRss.misAJourTousSites()
      Ok(views.html.index())
  }

  def miseAJourSites = StackAction {
    implicit request =>
      FluxRss.miseAJourBddSites
      Ok(views.html.index())
  }

  def entite(id: Int) = StackAction {
    implicit request =>
      val res = Entite.getById(id)
      val sparql: SparqlQueryExecuter = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql")
      res match {
        case Some(entite) =>
          val url: String = entite.url
          val fullName: String = sparql.getName(url);
          val name = if (fullName.length > 0) fullName else entite.nom
          Ok(views.html.entite(name, sparql.getImage(url), sparql.getImageDescription(url), sparql.getAbstract(url), sparql.getWikiLink(url), url, sparql.getImageFullSize(url)))
        case None => Ok(views.html.index())
      }
  }

  def search() = StackAction {
    implicit request =>

      searchForm.bindFromRequest.fold(
        hasErrors = {
          form =>
            Ok(views.html.index())
        },
        success = {
          input =>
            val entites: List[Entite] = Entite.rechercheDansNom(input)
            val articles: List[Article] = Article.rechercheDansTitre(input)
            Ok(views.html.results(entites, articles, input))
        })
  }


}