package models

import org.anormcypher.{CypherRow, Cypher}

/**
 * Created by Administrator on 20/03/14.
 */
case class APourDomaine(article: Article, domaine: Domaine) {

}

object APourDomaine {

  def create(article: Article, domaine: Domaine): Boolean = {
    Cypher(
      """
         match (article: Article), (domaine: Domaine)
         where article.url = {url} and domaine.nom = {nom}
         create (article)-[r:aPourDomaine]->(domaine)
      """
    ).on("url" -> article.url,
        "nom" -> domaine.nom
      ).execute()
  }

  def create(aPourDomaine: APourDomaine): Boolean = {
     create(aPourDomaine.article, aPourDomaine.domaine)
  }


  def getDomainesLies(article: Article): Option[List[Domaine]] = {
    val result: List[Domaine] = Cypher(
      """
        match (article: Article {url : {urlArticle}})-[r:aPourDomaine]->(domaine: Domaine)
                return domaine.nom as nom;
      """).on("urlArticle" -> article.url)().collect {
      case CypherRow(nom: String) =>
        new Domaine(nom)
      case _ => throw new IllegalArgumentException("Mauvais format du domaine")
    }.toList

    result match {
      case Nil => None
      case _ => Some(result)
    }
  }
}
