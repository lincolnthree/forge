package org.jboss.forge.container.modules;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;

import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link ResourceLoader} that can load classes from a {@link Module}
 * <p>
 * Thread Safety: This class is thread safe, and does not require a happens before even between construction and usage
 * 
 * @author Stuart Douglas
 * 
 */
public class ModuleResourceLoader implements ResourceLoader
{
   @Override
   public String toString()
   {
      return "ModuleResourceLoader [module=" + module + "]";
   }

   private final Module module;

   /**
    * Additional classes that have been added to the bean archive by the container or by a portable extension
    */
   private final Map<String, Class<?>> classes;
   private ModuleClassLoader classLoader;

   public ModuleResourceLoader(Module module)
   {
      this.module = module;
      this.classes = new ConcurrentHashMap<String, Class<?>>();
      this.classLoader = module.getClassLoader();
   }

   /**
    * If the class name is found in additionalClasses then return it.
    * 
    * Otherwise the class will be loaded from the module ClassLoader
    */
   @Override
   public Class<?> classForName(String name)
   {
      try
      {
         if (classes.containsKey(name))
         {
            return classes.get(name);
         }
         final Class<?> clazz = classLoader.loadClass(name);
         classes.put(name, clazz);
         return clazz;
      }
      catch (NoClassDefFoundError e)
      {
         throw new ResourceLoadingException(e);
      }
      catch (ClassNotFoundException e)
      {
         throw new ResourceLoadingException(e);
      }
      catch (LinkageError e)
      {
         throw new ResourceLoadingException(e);
      }
   }

   public void addAdditionalClass(Class<?> clazz)
   {
      this.classes.put(clazz.getName(), clazz);
   }

   /**
    * Loads a resource from the module class loader
    */
   @Override
   public URL getResource(String name)
   {
      try
      {
         return module.getClassLoader().getResource(name);
      }
      catch (Exception e)
      {
         throw new ResourceLoadingException(e);
      }
   }

   /**
    * Loads resources from the module class loader
    */
   @Override
   public Collection<URL> getResources(String name)
   {
      try
      {
         final HashSet<URL> resources = new HashSet<URL>();
         Enumeration<URL> urls = module.getClassLoader().getResources(name);
         while (urls.hasMoreElements())
         {
            resources.add(urls.nextElement());
         }
         return resources;
      }
      catch (Exception e)
      {
         throw new ResourceLoadingException(e);
      }

   }

   @Override
   public void cleanup()
   {
      // noop
   }

}