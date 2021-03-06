package models

import org.anormcypher.{CypherRow, Cypher}
import org.joda.time.DateTime


case class EstLie(urlA: String, urlB: String, ponderation: Double) {

}

object EstLie {

  def create(urlA: String, urlB: String, ponderation: Double): Boolean = {

    Cypher(
      """
         match (articleA: Article), (articleB: Article)
         where articleA.url = {urlA} and articleB.url = {urlB}
         create unique (articleA)-[r:estLie {ponderation : {ponderation}}]->(articleB),
                (articleA)<-[r2:estLie {ponderation : {ponderation}}]-(articleB)
      """
    ).on("urlA" -> urlA,
        "urlB" -> urlB,
        "ponderation" -> ponderation
      ).execute()
  }
  def create(articleA: Article, articleB: Article, ponderation: Double): Boolean = {
      create(articleA.url, articleB.url, ponderation)
  }


  def create(estLie: EstLie): Boolean = {
    create(estLie.urlA, estLie.urlB, estLie.ponderation)
  }

//  def getPonderation(articleA: Article, articleB: Article): Int = {
//
//    val result = Cypher(
//      """
//         match (articleA: Article {url : {urlA}})-[r:estLie]-(articleB: Article {url : {urlB}})
//         return r.ponderation as ponderation
//      """
//    ).on("urlA" -> articleA.url,
//        "urlB" -> articleB.url)().toList
//
//    result match {
//      case Nil=> -1
//      case head::tail => head[BigDecimal]("ponderation").toInt
//    }
//  }

  def getById(id : Int): List[Article] = {

    val result: List[Option[Article]] = Article.getArticles("param" -> id, "<-[r:estLie]-(articleToLinked: Article) where ID(articleToLinked) = {param}", "Order By r.ponderation DESC;").toList

    result.map {
      case Some(article) => article
      case None => throw new NoSuchElementException("Pas d'article")
    }
  }

  def getByIdWithPonderation(id : Int): Option[List[(Article, Double)]] = {
    val result: List[(Article, Double)] = Cypher(
      """
        match (article1: Article)-[r:estLie]-(article2: Article)
                 where ID(article1) = {idArticle}
                 return  article.titre,
                        article.auteur,
                        article.description,
                        article.date,
                        article.url,
                        site.url,
                        site.nom,
                        site.type,
                        ID(site),
                        article.image,
                        article.consultationsJour,
                        article.consultationsSemaine,
                        article.consultationsSemaineDerniere,
                        article.consultationsMois,
                        article.consultations,
                        article.totalEtoiles,
                        article.nbEtoiles,
                        article.nbCoeurs,
                        ID(article),
                        r.ponderation
                 order by r.nbCoeurs DESC;
      """).on("idArticle" -> id)().collect {
      case CypherRow(titre: String,
      auteur: String,
      description: String,
      date: String,
      url: String,
      urlSite: String,
      nomSite: String,
      typeSite: String,
      idSite: BigDecimal,
      image: String,
      consultationsJour: BigDecimal,
      consultationsSemaine: BigDecimal,
      consultationsSemaineDerniere: BigDecimal,
      consultationsMois: BigDecimal,
      consultations: BigDecimal,
      totalEtoiles: BigDecimal,
      nbEtoiles: BigDecimal,
      nbCoeurs: BigDecimal,
      id: BigDecimal,
      ponderation: BigDecimal) =>
        (new Article(
          titre,
          auteur,
          description,
          new DateTime(date),
          url,
          new Site(urlSite, nomSite, typeSite, idSite.toInt),
          image,
          consultationsJour.toInt,
          consultationsSemaine.toInt,
          consultationsSemaineDerniere.toInt,
          consultationsMois.toInt,
          consultations.toInt,
          totalEtoiles.toInt,
          nbEtoiles.toInt,
          nbCoeurs.toInt,
          id.toInt), ponderation.toDouble)
      case _ => throw new IllegalArgumentException("Mauvais format de l'entite")
    }.toList

    result match {
      case Nil => None
      case _ => Some(result)
    }
  }

  def getLinkedArticles(article : Article): List[(String, String, String, Double)] = {
    getLinkedArticlesById(article.id)
  }

  def countLinkedArticles(article : Article) : Int = {
    Cypher(
      """
        MATCH (a:Article)-[r:`estLie`]->(b:Article) where ID(a) = {idArticle}
        return count(r);
      """).on("idArticle" -> article.id)().collect {
      case CypherRow(count : BigDecimal) => count.toInt
      case _ => -1
    }.head
  }

  def getLinkedArticlesById(id : Int): List[(String, String, String, Double)] = {

    val result: List[(String, String, String, Double)] = Cypher(
      """
        MATCH (a:Article)<-[r:`tag`]-(b:Entite)-[r2:`tag`]->(c:Article) where ID(a) = {id}
        return distinct a.url, c.url, b.url, r.quantite, r2.quantite
      """).on("id" ->id)().collect {
      case CypherRow(urlA : String, urlB : String, urlEntite: String, quantiteA : BigDecimal, quantiteB : BigDecimal) =>
        (urlA, urlB, urlEntite, if( quantiteA > quantiteB) (quantiteB / quantiteA).toDouble else (quantiteA / quantiteB).toDouble)
    }.toList

    val mapByUrlB: Map[String, List[(String, String, String, Double)]] = result.filter(el => el._1 != el._2).groupBy(_._2)

    mapByUrlB.map(el => {

      val nouvelleNote : Double = el._2.map(_._4).foldLeft(0.0)(_ + _) / el._2.size
      (el._2.head._1, el._1, el._2.head._3, nouvelleNote)
    }).toList
  }
}

