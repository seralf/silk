package de.fuberlin.wiwiss.silk.preprocessing.extractor

import de.fuberlin.wiwiss.silk.preprocessing.transformer.Transformer
import de.fuberlin.wiwiss.silk.preprocessing.entity.{Property, Entity}
import scala.io.Source
import de.fuberlin.wiwiss.silk.preprocessing.dataset.Dataset

/**
 * Created with IntelliJ IDEA.
 * User: Petar
 * Date: 21/01/14
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
case class Dictionary(override  val id: String,
                      override val propertyToExtractFrom: String,
                      override val transformers:List[Transformer],
                      override val param:String) extends ManualExtractor{


  val values = Source.fromFile(param).getLines.mkString("\n")

  val propertyForTraining = solvePath(id)

  def solvePath(s: String) = {
    "(?<=[A-Z])(?=[A-Z][a-z])".r.findAllIn(s).subgroups(0)
  }

  def apply(dataset:Dataset):Traversable[Entity] = {

    val filteredEntities = dataset.filter(propertyToExtractFrom)


    for(entity <- filteredEntities) yield {
      val extractedProperties = for(property <- entity.properties) yield{

        new Property(propertyForTraining, values)
      }
      new Entity(entity.uri, extractedProperties)
    }
  }

}
