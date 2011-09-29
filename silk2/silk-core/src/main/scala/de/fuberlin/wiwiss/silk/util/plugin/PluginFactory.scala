package de.fuberlin.wiwiss.silk.util.plugin

import java.io.File
import java.net.{URL, URLClassLoader}
import org.clapper.classutil._

/**
 * An abstract Factory.
 */
class PluginFactory[T <: AnyPlugin : Manifest] extends ((String, Map[String, String]) => T) {
  /** Map of all plugins by their id */
  private var plugins = Map[String, PluginDescription[T]]()

  /**
   * Creates a new instance of a specific plugin.
   */
  override def apply(id: String, params: Map[String, String] = Map.empty): T = {
    val plugin = {
      plugins.get(id) match {
        case Some(s) => s(params)
        case None => throw new NoSuchElementException("No plugin called '" + id + "' found.")
      }
    }

    plugin
  }

  /**
   * Retrieves the parameters of a plugin instance e.g. to serialize it.
   */
  def unapply(t: T): Option[(String, Map[String, String])] = {
    Some(t.id, t.parameters)
  }

  /**
   * Retrieves a specific plugin.
   */
  def plugin(id: String) = plugins(id)

  /**
   * List of all registered plugins.
   */
  def availablePlugins: Traversable[PluginDescription[T]] = plugins.values

  /**
   * Registers a single plugin.
   */
  def register(implementationClass: Class[_ <: T]) {
    val pluginDesc = PluginDescription(implementationClass)

    plugins += ((pluginDesc.id, pluginDesc))
  }

  /**
   * Registers all plugins on the classpath.
   */
  def registerClasspath() {
    val classFinder = ClassFinder()
    val classes = classFinder.getClasses()

    val pluginClassNames = ClassFinder.concreteSubclasses(manifest[T].erasure.getName, classes).map(_.name)
    val pluginClasses = pluginClassNames.map(Class.forName)

    for(pluginClass <- pluginClasses)
      register(pluginClass.asInstanceOf[Class[T]])
  }

  /**
   * Registers all plugins which from a directory of jar files.
   */
  def registerJars(jarDir: File) {
    //Collect all jar file in the directory
    val jarFiles = Option(jarDir.listFiles())
                   .getOrElse(throw new Exception("Directory " + jarDir + " does not exist"))
                   .filter(_.getName.endsWith(".jar"))

    //Create a classinfo of the plugin interface
    val pluginClassInfo = new ClassInfo {
      def name: String = manifest[T].erasure.getName
      def signature: String = null
      def methods: Set[MethodInfo] = null
      def location: File = null
      def superClassName: String = null
      def modifiers: Set[Modifier.Modifier] = Set(Modifier.Interface)
      def fields: Set[FieldInfo] = null
      def interfaces: List[String] = null
    }

    ///Find the names of all classes which implement the plugin interface
    val classes = ClassFinder(jarFiles).getClasses ++ Iterator(pluginClassInfo)
    val pluginClassNames = ClassFinder.concreteSubclasses(manifest[T].erasure.getName, classes).map(_.name)

    //Load all found classes
    val classLoader = URLClassLoader.newInstance(jarFiles.map(file => new URL("jar:file:" + file.getName + "!/")))
    val pluginClasses = pluginClassNames.map(classLoader.loadClass)

    //Register all plugins
    for(pluginClass <- pluginClasses)
      register(pluginClass.asInstanceOf[Class[T]])
  }
}