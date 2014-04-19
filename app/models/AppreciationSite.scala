package models

import org.anormcypher.Cypher
import org.joda.time.DateTime
import play.api.Logger

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 20/03/14
 * Time: 09:57
 * To change this template use File | Settings | File Templates.
 */
case class AppreciationSite(utilisateur: Utilisateur, site: Site, totalEtoiles: Int = 0, nbEtoiles: Int = 0, nbCoeurs: Int = 0, estFavori: Boolean = false)

object AppreciationSite {
  def create(appreciationSite: AppreciationSite): Boolean = {
    Cypher(
      """
         match (user: Utilisateur), (site: Site)
         where user.mail = {mailUser} and site.url = {urlSite}
         create (user)-[r:appreciationSite {totalEtoiles : {totalEtoiles}, nbEtoiles : {nbEtoiles}, nbCoeurs : {nbCoeurs}, estFavori : {estFavori}}]->(site)
      """
    ).on("mailUser" -> appreciationSite.utilisateur.mail,
      "urlSite" -> appreciationSite.site.url,
      "totalEtoiles" -> appreciationSite.totalEtoiles,
      "nbEtoiles" -> appreciationSite.nbEtoiles,
      "nbCoeurs" -> appreciationSite.nbCoeurs,
      "estFavori" -> appreciationSite.estFavori
    ).execute()
  }

  def get(user: Utilisateur, site: Site): Option[AppreciationSite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site {url : {urlSite}})
         return r.totalEtoiles as totalEtoiles, r.nbEtoiles as nbEtoiles, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url)().toList

    result match {
      case Nil => None
      case head :: tail => {
        Some(AppreciationSite(user, site, head[BigDecimal]("totalEtoiles").toInt, head[BigDecimal]("nbEtoiles").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
      }
    }
  }


  def setTotalEtoiles(user: Utilisateur, site: Site, changementEtoiles: Int): Option[AppreciationSite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         set r.totalEtoiles = r.totalEtoiles + {changementEtoiles}
         return r.totalEtoiles as totalEtoiles, r.nbEtoiles as nbEtoiles, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url,
      "changementEtoiles" -> changementEtoiles)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationSite(user, site, head[BigDecimal]("totalEtoiles").toInt, head[BigDecimal]("nbEtoiles").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def incrNbCoeurs(user: Utilisateur, site: Site): Option[AppreciationSite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         set r.nbCoeurs = r.nbCoeurs + 1
         return r.totalEtoiles as totalEtoiles, r.nbEtoiles as nbEtoiles, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationSite(user, site, head[BigDecimal]("totalEtoiles").toInt, head[BigDecimal]("nbEtoiles").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def decrNbCoeurs(user: Utilisateur, site: Site): Option[AppreciationSite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         set r.nbCoeurs = r.nbCoeurs - 1
         return r.totalEtoiles as totalEtoiles, r.nbEtoiles as nbEtoiles, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationSite(user, site, head[BigDecimal]("totalEtoiles").toInt, head[BigDecimal]("nbEtoiles").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }


  def incrNbEtoiles(user: Utilisateur, site: Site): Option[AppreciationSite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         set r.nbEtoiles = r.nbEtoiles + 1
         return r.totalEtoiles as totalEtoiles, r.nbEtoiles as nbEtoiles, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationSite(user, site, head[BigDecimal]("totalEtoiles").toInt, head[BigDecimal]("nbEtoiles").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def decrNbEtoiles(user: Utilisateur, site: Site): Option[AppreciationSite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         set r.nbEtoiles = r.nbEtoiles - 1
         return r.totalEtoiles as totalEtoiles, r.nbEtoiles as nbEtoiles, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationSite(user, site, head[BigDecimal]("totalEtoiles").toInt, head[BigDecimal]("nbEtoiles").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def estFavori(user: Utilisateur, site: Site): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         return r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url)().toList

    result match {
      case Nil => throw new Exception("AppreciationSite node doesn't exist.")
      case head :: tail => head[Boolean]("estFavori")
    }
  }

  def setFavori(user: Utilisateur, site: Site): Option[AppreciationSite] = {

    val estFavoriList = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         return r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url)().toList

    estFavoriList match {
      case Nil => None
      case head :: tail => {
        val estFavori = !head[Boolean]("estFavori")
        val result = Cypher(
          """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         set r.estFavori = {nouvFavori}
         return r.totalEtoiles as totalEtoiles, r.nbEtoiles as nbEtoiles, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
          """
        ).on("mailUser" -> user.mail,
          "urlSite" -> site.url,
          "nouvFavori" -> estFavori)().toList

        result match {
          case Nil => None
          case head :: tail => Some(AppreciationSite(user, site, head[BigDecimal]("totalEtoiles").toInt, head[BigDecimal]("nbEtoiles").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
        }
      }
    }
  }

  def delete(user: Utilisateur, site: Site): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         delete r
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url).execute()
    result
  }

  def majAvecCreate(note: Note) = {
    var site = note.article.site
    var appreciationSiteOpt = AppreciationSite.get(note.utilisateur, site)
    appreciationSiteOpt match {
      case Some(appreciationSite) => {
        if (note.nbEtoiles != 0) {
          AppreciationSite.setTotalEtoiles(note.utilisateur, site, note.nbEtoiles)
          AppreciationSite.incrNbEtoiles(note.utilisateur, site)
        }
        if (note.aCoeur) AppreciationSite.incrNbCoeurs(note.utilisateur, site)
      }
      case None => {
        var nbCoeurs = 0
        var nbEtoiles = 0
        if (note.aCoeur) nbCoeurs = 1
        if (note.nbEtoiles > 0) nbEtoiles = 1
        AppreciationSite.create(new AppreciationSite(note.utilisateur, site, note.nbEtoiles, nbEtoiles, nbCoeurs))
      }
    }


  }

  def majSansCreate(note: Note, suppressionNote: Boolean = false, changementNbEtoiles: Int = 0, setCoeur: Boolean = false, aCoeur: Boolean = false) = {
    var site = note.article.site
    var appreciationSiteOpt = AppreciationSite.get(note.utilisateur, site)
    appreciationSiteOpt match {
      case Some(appreciationSite) => {
        if(suppressionNote){
          AppreciationSite.decrNbEtoiles(note.utilisateur, site)
        }
        AppreciationSite.setTotalEtoiles(note.utilisateur, site, changementNbEtoiles)
        if (setCoeur) {
          if (aCoeur) AppreciationSite.incrNbCoeurs(note.utilisateur, site)
          else AppreciationSite.decrNbCoeurs(note.utilisateur, site)
        }
      }
      case None => throw new Exception("AppreciationSite non trouvée")
    }
    false
  }

}
